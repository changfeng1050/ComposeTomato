import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.resources {
    packageOfResClass = "com.changfeng.tomato.resources"
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "com.changfeng.tomato.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "tomato" // 应用名称，tomato.exe
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("tomato.ico"))
            }
        }

        buildTypes.release {
            proguard {
                version.set("7.4.2") // 需要新版本才支持Java 21
                isEnabled.set(false) // 设置为true可能编译不过
                optimize.set(false)
                obfuscate.set(false)
                configurationFiles.from(project.file("proguard-rules.pro"))
            }
        }
    }
}
