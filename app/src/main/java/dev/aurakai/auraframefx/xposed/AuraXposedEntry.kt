package dev.aurakai.auraframefx.xposed

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.view.View
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.aurakai.auraframefx.provider.PrefsConstants
import dev.aurakai.auraframefx.util.RemotePreferences

class AuraXposedEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        const val MY_PACKAGE_NAME = "dev.aurakai.auraframefx"
        const val TARGET_STATUS_BAR_VIEW =
            "com.android.systemui.statusbar.phone.StatusBarWindowView"
        const val TARGET_SHADE_VIEW =
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
                        setupModuleStatus(param.thisObject as Context)
                    }
                })
        } catch (e: Throwable) {
            Log.e("AuraXposedEntry", "Hook app fail: ${e.message}", e)
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
                            Log.i("AuraXposedEntry", "SystemUIService onCreate.")
                            initializeRemotePrefs(context)
                            hookViewDrawing(lpparam.classLoader, TARGET_STATUS_BAR_VIEW)
                            hookViewDrawing(lpparam.classLoader, TARGET_SHADE_VIEW)
                        } else {
                            Log.e("AuraXposedEntry", "Context null in SystemUIService")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e("AuraXposedEntry", "Hook SystemUI fail: ${e.message}", e)
        }
    }

    private fun initializeRemotePrefs(context: Context) {
        try {
            xhancementPrefs = RemotePreferences(
                context,
                PrefsConstants.AUTHORITY,
                PrefsConstants.XHANCEMENT_PREFS_NAME
            )
            xhancementPrefs?.setMode(RemotePreferences.MODE_NO_FILE_OBSERVER)
            Log.i("AuraXposedEntry", "RemotePrefs initialized")
        } catch (e: Throwable) {
            Log.e("AuraXposedEntry", "Failed to initialize RemotePrefs: ${e.message}", e)
            xhancementPrefs = null
        }
    }

    private fun setupModuleStatus(context: Context) {
        // Implementation for module status setup
    }

    private fun hookViewDrawing(classLoader: ClassLoader, targetClassName: String) {
        val targetMethodName = "dispatchDraw"

        try {
            XposedHelpers.findAndHookMethod(
                targetClassName,
                classLoader,
                targetMethodName,
                Canvas::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        val canvas = param.args[0] as? Canvas ?: return
                        val context = view.context
                        val prefs = xhancementPrefs ?: return

                        prefs.reload()

                        val isStatusBarTarget = targetClassName == TARGET_STATUS_BAR_VIEW
                        val isShadeTarget = targetClassName == TARGET_SHADE_VIEW

                        try {
                            // Handle background color for status bar
                            val applyBgColor = isStatusBarTarget &&
                                    prefs.getBoolean(
                                        PrefsConstants.KEY_STATUS_BAR_BG_COLOR_ENABLED,
                                        false
                                    )

                            if (applyBgColor) {
                                val bgColor = prefs.getInt(
                                    PrefsConstants.KEY_STATUS_BAR_TARGET_COLOR,
                                    Color.TRANSPARENT
                                )
                                val isDecalEnabledToo = prefs.getBoolean(
                                    PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED,
                                    false
                                )

                                if (!isDecalEnabledToo || Color.alpha(bgColor) == 255) {
                                    bgPaint.color = bgColor
                                    canvas.drawRect(
                                        0f, 0f,
                                        view.width.toFloat(),
                                        view.height.toFloat(),
                                        bgPaint
                                    )
                                }
                            }

                            // Handle decal drawing
                            if ((isStatusBarTarget || isShadeTarget) &&
                                prefs.getBoolean(
                                    if (isStatusBarTarget) PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED
                                    else PrefsConstants.KEY_SHADE_DECAL_ENABLED,
                                    false
                                )
                            ) {
                                val decalUri = prefs.getString(
                                    if (isStatusBarTarget) PrefsConstants.KEY_STATUS_BAR_DECAL_URI
                                    else PrefsConstants.KEY_SHADE_DECAL_URI,
                                    ""
                                )

                                if (decalUri.isNotEmpty()) {
                                    loadDecalBitmapIfNeeded(context, decalUri)
                                    decalBitmap?.let { bitmap ->
                                        val scale = prefs.getFloat(
                                            if (isStatusBarTarget) PrefsConstants.KEY_STATUS_BAR_DECAL_SCALE
                                            else PrefsConstants.KEY_SHADE_DECAL_SCALE,
                                            1.0f
                                        )
                                        val alpha = (prefs.getInt(
                                            if (isStatusBarTarget) PrefsConstants.KEY_STATUS_BAR_DECAL_OPACITY
                                            else PrefsConstants.KEY_SHADE_DECAL_OPACITY,
                                            100
                                        ) / 100f * 255).toInt()

                                        decalPaint.alpha = alpha.coerceIn(0, 255)

                                        val scaledWidth = (bitmap.width * scale).toInt()
                                        val scaledHeight = (bitmap.height * scale).toInt()
                                        val x = (view.width - scaledWidth) / 2f
                                        val y = (view.height - scaledHeight) / 2f

                                        canvas.save()
                                        canvas.translate(x, y)
                                        canvas.scale(scale, scale)
                                        canvas.drawBitmap(bitmap, 0f, 0f, decalPaint)
                                        canvas.restore()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AuraXposedEntry", "Error in draw hook: ${e.message}", e)
                        }
                    }
                })
            Log.i("AuraXposedEntry", "Hooked $targetClassName.$targetMethodName for drawing.")
        } catch (e: Throwable) {
            Log.e("AuraXposedEntry", "Failed hook $targetClassName draw: ${e.message}", e)
            if (e is XposedHelpers.ClassNotFoundError) {
                Log.e("AuraXposedEntry", "Class $targetClassName NOT FOUND!")
            }
            if (e is NoSuchMethodError) {
                Log.e("AuraXposedEntry", "Method $targetMethodName NOT FOUND in $targetClassName!")
            }
        }
    }

    private fun loadDecalBitmapIfNeeded(context: Context, uriString: String) {
        if (uriString == lastDecalUri && decalBitmap != null) {
            return
        }

        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                decalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                lastDecalUri = uriString
            }
        } catch (e: Exception) {
            Log.e("AuraXposedEntry", "Error loading decal bitmap: ${e.message}", e)
            decalBitmap = null
            lastDecalUri = null
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        // Zygote initialization if needed
    }
}
