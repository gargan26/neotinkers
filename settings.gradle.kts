pluginManagement {
    repositories {
        gradlePluginPortal()
        // NeoForged: ModDevGradle plugin + NeoForge artifacts
        maven("https://maven.neoforged.net/releases")
        // Parchment mappings
        maven("https://maven.parchmentmc.org")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Build Mantle from the sibling vancevoj/Mantle 1.21 port checkout. The
// `slimeknights.mantle:Mantle` dependency declared in build.gradle is
// substituted with this local build, so the two ports compile together.
// Clone it next to this repo:  git clone https://github.com/vancevoj/Mantle ../Mantle (branch 1.21)
if (file("../neomantle/settings.gradle").exists() || file("../neomantle/settings.gradle.kts").exists()) {
    includeBuild("../neomantle")
} else {
    logger.warn("WARNING: ../neomantle not found. Clone the Mantle 1.21 port next to this repo; TConstruct will not compile without it.")
}

rootProject.name = "TConstruct"
