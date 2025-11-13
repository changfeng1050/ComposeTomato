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
            // androidx.compose
            implementation(libs.androidx.compose.components.resources)
            implementation(libs.androidx.compose.foundation)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.compose.material.icons.extended)
            implementation(libs.androidx.compose.runtime)
            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.tooling.preview)

            // androidx.lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // androidx.navigation3
            implementation(libs.androidx.navigation3.ui)

            // kotlinx.serialization
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
