pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        // Add Maven repository for KSP
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
    }
    
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Xposed repository
        maven {
            url = uri("https://api.xposed.info/")
            content {
                includeGroup("de.robv.android.xposed")
            }
        }
    }
}

rootProject.name = "AuraFrameFX"

// Include the main app module
include(":app")
