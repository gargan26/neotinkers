/*
 * Tinkers Construct - NeoForge 1.21.1 port build file
 * Original ForgeGradle build by boni, Sunstrike, ProgWML6 (Slime Knights).
 * Converted to ModDevGradle for NeoForge 1.21.1.
 */

val modVersion = providers.gradleProperty("mod_version")
val forkVersion = providers.gradleProperty("fork_version")

val mcVersion = providers.gradleProperty("minecraft_version")
val mcRange = providers.gradleProperty("minecraft_range")

val loaderRange = providers.gradleProperty("loader_range")
val neoForgeVersion = providers.gradleProperty("neoforge_version")
val neoForgeRange = providers.gradleProperty("neoforge_range")

val parchmentMinecraft = providers.gradleProperty("parchment_minecraft")
val parchmentVersion = providers.gradleProperty("parchment_version")

val mantleVersion = providers.gradleProperty("mantle_version")
val mantleRange = providers.gradleProperty("mantle_range")

val jeiVersion = providers.gradleProperty("jei_version")
val jeiRange = providers.gradleProperty("jei_range")
//json_things_version
val jsonThingsRange = providers.gradleProperty("json_things_range")
//ie_version
val emiVersion = providers.gradleProperty("emi_version")
//emi_range

plugins {
    idea
    eclipse
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.147"
    id("io.freefair.lombok") version "8.10"
}

group = "slimeknights.tconstruct"
// version = <mc>-<upstream Tinkers version>-v<fork version>, e.g. 1.21.1-3.11.2-v1.0
version = "${mcVersion.get()}-${modVersion.get()}-v${forkVersion.get()}"
base {
    archivesName = "NeoTinkers"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}, Version: ${version}")

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") {
        name = "BlameJared (JEI, Immersive Engineering, CraftTweaker)"
    }
    maven("https://www.dogforce-games.com/maven/") {
        name = "Dogforce Games (JSON Things)"
    }
    maven("https://maven.terraformersmc.com/releases") {
        name = "TerraformersMC (EMI)"
    }
    maven("https://www.cursemaven.com") {
        name = "CurseMaven"
        content {
            includeGroup("curse.maven")
        }
    }
}

neoForge {
    version = neoForgeVersion.get()

    parchment {
        minecraftVersion = parchmentMinecraft.get()
        mappingsVersion = parchmentVersion.get()
    }

    accessTransformers.from("src/main/resources/META-INF/accesstransformer.cfg")

    runs {
        configureEach {
            gameDirectory = file("run")
            logLevel = org.slf4j.event.Level.DEBUG
        }
        create("client") {
            client()
        }
        create("server") {
            server()
            programArgument("--nogui")
        }
        create("data") {
            data()
            programArguments.addAll(
                "--mod", "tconstruct",
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
                "--existing-mod", "mantle"
            )
        }
    }

    mods {
        create("tconstruct") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources")
        exclude(
            ".cache",
            // debug resources help develop the mod, they are not shipped
            "assets/tconstruct/debug"
        )
    }
}

// TEMPORARY: integrations whose 3rd-party APIs are not yet available for 1.21.1
// (their compileOnly deps below are commented out). Excluded so the core mod builds;
// re-enable each source tree when its dependency is restored in `dependencies`.
//  - jei: recipe viewer (mezz.jei)
//  - jsonthings: dev.gigaherz.jsonthings
//  - ImmersiveEngineeringPlugin: blusunrize.immersiveengineering
//  - craftingtweaks: net.blay09.mods.craftingtweaks
//  - DietPlugin: com.illusivesoulworks.diet
//  - DummmmmmyPlugin: net.mehvahdjukaar.dummmmmmy
sourceSets.main {
    java {
        // jei: ported to the 1.21.1 JEI 19.x API and re-enabled (deps below).
        exclude(
            "slimeknights/tconstruct/plugin/jsonthings/**",
            "slimeknights/tconstruct/plugin/craftingtweaks/**",
            "slimeknights/tconstruct/plugin/ImmersiveEngineeringPlugin.java",
            "slimeknights/tconstruct/plugin/DietPlugin.java",
            "slimeknights/tconstruct/plugin/DummmmmmyPlugin.java"
        )
    }
}

dependencies {
    // Mantle: substituted by the ../Mantle composite build (see settings.gradle).
    implementation("slimeknights.mantle:Mantle:${mcVersion.get()}-${mantleVersion.get()}")

    // --- Optional integrations. Re-enable each as its integration package is ported. ---
    // JEI (recipe viewer) - ported to JEI 19.x for 1.21.1
    compileOnly("mezz.jei:jei-${mcVersion.get()}-neoforge-api:${jeiVersion.get()}")
    runtimeOnly("mezz.jei:jei-${mcVersion.get()}-neoforge:${jeiVersion.get()}")
    // EMI (recipe viewer) - native plugin in slimeknights.tconstruct.plugin.emi
    compileOnly("dev.emi:emi-neoforge:${emiVersion.get()}:api")
    runtimeOnly("dev.emi:emi-neoforge:${emiVersion.get()}")
    // JSON Things
    // compileOnly "dev.gigaherz.jsonthings:JsonThings-${minecraft_version.get()}:${json_things_version}"
    // Immersive Engineering
    // compileOnly "blusunrize.immersiveengineering:ImmersiveEngineering:${minecraft_version.get()}-${ie_version}"

    // Unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
}


tasks.test {
    useJUnitPlatform()
}

// The src/test sources are a deferred part of the 1.21.1/NeoForge port: they still use 1.20.1/Forge APIs
// (net.minecraftforge.*) and the ModDevGradle test source set is not yet wired to the Minecraft/NeoForge
// classpath. Disable test compilation/execution so the main mod still builds its jar. Re-enable when porting tests.
tasks.compileTestJava { enabled = false }
tasks.test { enabled = false }

tasks.processResources {
    var replaceProperties = mapOf(
        "version"           to version,
        "mod_version"       to modVersion.get(),
        "fork_version"      to forkVersion.get(),
        "loader_range"      to loaderRange.get(),
        "minecraft_range"   to mcRange.get(),
        "neoforge_range"    to neoForgeRange.get(),
        "mantle_range"      to mantleRange.get(),
        "jei_range"         to jeiRange.get(),
        "json_things_range" to jsonThingsRange.get()
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Specification-Title"     to "Tinkers' Construct",
                "Specification-Vendor"    to "Slime Knights",
                "Specification-Version"   to "1",
                "Implementation-Title"    to name,
                "Implementation-Version"  to "${version}",
                "Implementation-Vendor"   to "Slime Knights"
        ))
    }
}

tasks.named<Jar>("sourcesJar") {
    exclude (
        "assets/**",
        "data/**",
        "pack.png",
        "pack.mcmeta",
        "META-INF/neoforge.mods.toml"
    )
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        if (hasProperty("DEPLOY_DIR")) {
            maven(uri(property("DEPLOY_DIR")!!))
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
