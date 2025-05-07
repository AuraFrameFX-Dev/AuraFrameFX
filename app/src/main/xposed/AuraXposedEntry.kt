package dev.aurakai.auraframefx.xposed

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.view.View
import com.crossbowffs.remotepreferences.RemotePreferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.aurakai.auraframefx.util.PrefsConstants
import java.io.InputStream

/**
 * Xposed module entry point for AuraFrameFX.
 * Handles system UI modifications including status bar and notification shade decorations.
 */
class AuraXposedEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        private const val TAG = "AuraXposedEntry"
        const val MY_PACKAGE_NAME = "dev.aurakai.auraframefx"

        // Primary Targets
        private const val TARGET_STATUS_BAR_VIEW =
            "com.android.systemui.statusbar.phone.StatusBarWindowView"
        private const val TARGET_SHADE_VIEW =
            "com.android.systemui.statusbar.phone.NotificationShadeWindowView"
    }

    private var xhancementPrefs: RemotePreferences? = null
    private var decalBitmap: Bitmap? = null
    private var lastDecalUri: String? = null
    private val decalPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    private val bgPaint = Paint()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            MY_PACKAGE_NAME -> handleAppLoad(lpparam)
            "com.android.systemui" -> handleSystemUILoad(lpparam)
        }
    }

    private fun handleAppLoad(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.Application",
                lpparam.classLoader,
                "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.thisObject as? Context
                        context?.let { setupModuleStatus(it) }
                            ?: Log.e(TAG, "Context was null in app onCreate hook")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Hook app onCreate failed: ${e.message}", e)
        }
    }

    private fun handleSystemUILoad(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.SystemUIService",
                lpparam.classLoader,
                "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.thisObject as? Context
                        if (context != null) {
                            Log.i(TAG, "SystemUIService onCreate. Initializing AuraFrameFX hooks.")
                            initializeRemotePrefs(context)
                            hookViewDrawing(lpparam.classLoader, TARGET_STATUS_BAR_VIEW)
                            hookViewDrawing(lpparam.classLoader, TARGET_SHADE_VIEW)
                        } else {
                            Log.e(TAG, "Context null in SystemUIService")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Hook SystemUI failed: ${e.message}", e)
        }
    }

    private fun initializeRemotePrefs(context: Context) {
        try {
            xhancementPrefs = RemotePreferences(
                context,
                PrefsConstants.AUTHORITY,
                PrefsConstants.XHANCEMENT_PREFS_NAME,
                true // Enable file observer
            )
            Log.i(TAG, "RemotePreferences initialized for AuraFrameFX.")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize RemotePreferences: ${e.message}", e)
            xhancementPrefs = null
        }
    }

    private fun hookViewDrawing(classLoader: ClassLoader, targetClassName: String) {
        try {
            XposedHelpers.findAndHookMethod(
                targetClassName,
                classLoader,
                "dispatchDraw",
                Canvas::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        val canvas = param.args[0] as? Canvas ?: return
                        val context = view.context
                        val prefs = xhancementPrefs ?: return

                        // Reload preferences to get the latest values
                        prefs.reload()

                        val isStatusBarTarget = targetClassName == TARGET_STATUS_BAR_VIEW
                        val isShadeTarget = targetClassName == TARGET_SHADE_VIEW

                        try {
                            // Handle background color for status bar
                            if (isStatusBarTarget) {
                                val bgColorEnabled = prefs.getBoolean(
                                    PrefsConstants.KEY_STATUS_BAR_BG_COLOR_ENABLED, 
                                    false
                                )
                                
                                if (bgColorEnabled) {
                                    val bgColor = prefs.getInt(
                                        PrefsConstants.KEY_STATUS_BAR_TARGET_COLOR,
                                        PrefsConstants.Defaults.STATUS_BAR_TARGET_COLOR
                                    )
                                    val isDecalEnabled = prefs.getBoolean(
                                        PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED,
                                        false
                                    )

                                    // Only draw background if decal is disabled or background is fully opaque
                                    if (!isDecalEnabled || Color.alpha(bgColor) == 255) {
                                        bgPaint.color = bgColor
                                        canvas.drawRect(
                                            0f, 0f, 
                                            view.width.toFloat(), 
                                            view.height.toFloat(), 
                                            bgPaint
                                        )
                                    }
                                }
                            }


                            // Handle decal drawing
                            val shouldDrawDecal = when {
                                isStatusBarTarget -> prefs.getBoolean(
                                    PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED,
                                    false
                                )
                                isShadeTarget -> prefs.getBoolean(
                                    PrefsConstants.KEY_QS_SHADE_DECAL_ENABLED,
                                    false
                                )
                                else -> false
                            }

                            if (shouldDrawDecal) {
                                val decalUri = prefs.getString(PrefsConstants.KEY_DECAL_URI, null)
                                if (!decalUri.isNullOrEmpty()) {
                                    loadDecalBitmapIfNeeded(context, decalUri)
                                    decalBitmap?.let { bmp ->
                                        if (!bmp.isRecycled) {
                                            val alpha = prefs.getFloat(
                                                PrefsConstants.KEY_DECAL_ALPHA,
                                                PrefsConstants.Defaults.DECAL_ALPHA
                                            )
                                            val scale = prefs.getFloat(
                                                PrefsConstants.KEY_DECAL_SCALE,
                                                PrefsConstants.Defaults.DECAL_SCALE
                                            )
                                            val offsetX = prefs.getInt(
                                                PrefsConstants.KEY_DECAL_OFFSET_X,
                                                PrefsConstants.Defaults.DECAL_OFFSET_X
                                            )
                                            val offsetY = prefs.getInt(
                                                PrefsConstants.KEY_DECAL_OFFSET_Y,
                                                PrefsConstants.Defaults.DECAL_OFFSET_Y
                                            )

                                            decalPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                                            val viewWidth = view.width
                                            val viewHeight = view.height
                                            if (viewWidth <= 0 || viewHeight <= 0) return

                                            val bmpWidth = bmp.width.toFloat()
                                            val bmpHeight = bmp.height.toFloat()
                                            if (bmpWidth <= 0 || bmpHeight <= 0) return

                                            val scaledWidth = bmpWidth * scale
                                            val scaledHeight = bmpHeight * scale

                                            // Calculate position with offset
                                            val left = (viewWidth - scaledWidth) / 2f + offsetX
                                            val top = (viewHeight - scaledHeight) / 2f + offsetY

                                            val srcRect = Rect(0, 0, bmp.width, bmp.height)
                                            val destRect = Rect(
                                                left.toInt(),
                                                top.toInt(),
                                                (left + scaledWidth).toInt(),
                                                (top + scaledHeight).toInt()
                                            )

                                            canvas.drawBitmap(bmp, srcRect, destRect, decalPaint)
                                        } else {
                                            Log.w(TAG, "Decal bitmap was recycled")
                                            decalBitmap = null
                                        }
                                    } ?: Log.w(TAG, "Decal bitmap was null")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in draw hook: ${e.message}", e)
                        }
                    }
                }
            )
            Log.i(TAG, "Successfully hooked $targetClassName.dispatchDraw")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to hook $targetClassName.dispatchDraw: ${e.message}", e)
            if (e is XposedHelpers.ClassNotFoundError) {
                Log.e(TAG, "Class $targetClassName not found. Check ROM compatibility.")
            }
            if (e is NoSuchMethodError) {
                Log.e(TAG, "Method dispatchDraw not found in $targetClassName")
            }
        }
    }

    /**
     * Loads a bitmap from the specified URI if it hasn't been loaded already.
     * Handles resource cleanup and error cases appropriately.
     */
    private fun loadDecalBitmapIfNeeded(context: Context, uriString: String) {
        // If we already have the correct bitmap loaded, no need to reload
        if (uriString == lastDecalUri && decalBitmap != null) {
            return
        }

        // Clear previous bitmap if URI has changed
        if (uriString != lastDecalUri) {
            decalBitmap?.recycle()
            decalBitmap = null
            lastDecalUri = null
        }

        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Failed to open input stream for URI: $uriString")

            try {
                // Configure bitmap options for high quality loading
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inJustDecodeBounds = false
                    inSampleSize = 1
                    inDensity = 1
                    inTargetDensity = 1
                    inScaled = false
                }

                // Try to load the bitmap
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                if (bitmap != null) {
                    decalBitmap = bitmap
                    lastDecalUri = uriString
                    Log.i(TAG, "Successfully loaded decal from: $uriString (${bitmap.width}x${bitmap.height})")
                } else {
                    throw IllegalStateException("Failed to decode bitmap from URI: $uriString")
                }
            } catch (e: Exception) {
                // Clean up if there was an error during decoding
                decalBitmap?.recycle()
                decalBitmap = null
                lastDecalUri = null
                throw e
            } finally {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error closing input stream", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading decal from '$uriString': ${e.message}", e)
            decalBitmap?.recycle()
            decalBitmap = null
            lastDecalUri = null
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            Log.i(TAG, "AuraFrameFX Xposed module initializing in Zygote")
            
            // Add any Zygote-level initializations here if needed
            // For example, you might want to load native libraries or set up global configurations
            
            Log.i(TAG, "Zygote initialization completed")
        } catch (e: Throwable) {
            Log.e(TAG, "Error during Zygote initialization: ${e.message}", e)
        }
    }

    /**
     * Updates the module status in shared preferences to indicate that the module is active.
     * This is used by the main app to detect if the Xposed module is properly installed and active.
     */
    private fun setupModuleStatus(context: Context) {
        try {
            @Suppress("DEPRECATION") // MODE_WORLD_READABLE is deprecated but needed for Xposed
            val prefs = context.getSharedPreferences(
                "xposed_module_status_${context.packageName}",
                Context.MODE_WORLD_READABLE
            )
            
            // Get the package info to retrieve version information
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "unknown"
            val versionCode = packageInfo.longVersionCode
            
            // Update the module status
            prefs.edit().apply {
                putBoolean("is_active", true)
                putString("version_name", versionName)
                putLong("version_code", versionCode)
                putLong("last_updated", System.currentTimeMillis())
                apply()
            }
            
            Log.i(TAG, "Module status updated: v$versionName (build $versionCode)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update module status: ${e.message}", e)
            
            // Try a fallback with minimal information if the first attempt fails
            try {
                context.getSharedPreferences(
                    "xposed_module_status_${context.packageName}",
                    Context.MODE_WORLD_READABLE
                ).edit()
                    .putBoolean("is_active", true)
                    .putLong("last_updated", System.currentTimeMillis())
                    .apply()
                Log.w(TAG, "Used fallback method to update module status")
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback module status update also failed: ${e2.message}", e2)
            }
        }
    }
