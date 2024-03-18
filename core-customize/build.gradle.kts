import groovy.json.JsonSlurper
import mpern.sap.commerce.ccv2.model.Manifest
import custom.GenerateLocalextensions
import org.gradle.internal.os.OperatingSystem


plugins {
   id("sap.commerce.build") version("4.0.0")
   id("sap.commerce.build.ccv2") version("4.0.0")
}

val DEPENDENCY_FOLDER = "dependencies"
repositories {
    flatDir { dirs(DEPENDENCY_FOLDER) }
    mavenCentral()
}

hybris{
    sparseBootstrap{
        enabled.set(true)
        alwaysIncluded.set(listOf("azurecloud"))
    }
}

apply(from = "build-resetDemoLicense.gradle")

tasks.register("setupLocalDevelopment") {
    group = "SAP Commerce"
    description = "Setup local development environment"
    dependsOn("clean", "updateProperties", "updateConfiguration", "bootstrapPlatform", "clean", "installManifestAddons", "yclean", "yall")
}

tasks.register<Exec>("run") {
    workingDir("hybris/bin/platform")
    args("debug")
    if (OperatingSystem.current().isWindows()) {
        executable("./hybrisserver.bat")
    } else {
        executable("./hybrisserver.sh")
    }
    dependsOn("updateProperties", "updateConfiguration")
}

tasks.register<Exec>("updateData") {
    var updateConfig = "../../config/updateSystem_data.json"
    workingDir("hybris/bin/platform")
    if (OperatingSystem.current().isWindows()) {
        commandLine("cmd", "/c", "setantenv.bat")
        commandLine("cmd", "/c", "ant updatesystem -DconfigFile=$updateConfig")
    } else {
        commandLine("sh", "-c", "setantenv.sh")
        commandLine("sh", "-c", "ant updatesystem -DconfigFile=$updateConfig")
    }
    dependsOn("updateProperties", "updateConfiguration")
}

tasks.register<Exec>("updateSystem") {
    var updateConfig = "../../config/updateSystem_all.json"
    workingDir("hybris/bin/platform")
    if (OperatingSystem.current().isWindows()) {
        commandLine("cmd", "/c", "setantenv.bat")
        commandLine("cmd", "/c", "ant updatesystem -DconfigFile=$updateConfig")
    } else {
        commandLine("sh", "-c", "setantenv.sh")
        commandLine("sh", "-c", "ant updatesystem -DconfigFile=$updateConfig")
    }
    dependsOn("updateProperties", "updateConfiguration")
}

tasks.register("updateProperties") {
    group = "SAP Commerce"
    description = "Generates local.properties out of the defined property profiles"
    doLast{
        val configDir: String = "hybris/config"
        val targetFile: File = file("$configDir/local.properties")
        if(targetFile.exists()){
            targetFile.delete()
        }
        for ( profile in listOf("common", "local-dev")) {
            targetFile.appendText("\n\n### $profile.properties\n")
            targetFile.appendText(file("$configDir/environments/$profile.properties").readText())
        }
    }
}

tasks.register<GenerateLocalextensions>("updateConfiguration") {
    group = "SAP Commerce"
    description = "Updates a localextensions.xml file based on the extensions list in the manifest. Generates *.properties files per aspect and persona as defined in manifest.json."
    target.set(file("hybris/config/localextensions.xml"))
    cloudExtensions.set(Manifest.fromMap(JsonSlurper().parse(file("manifest.json")) as MutableMap<String, Any>?).extensions)
}

tasks.register<Delete>("clean") {
    delete(buildDir)
}
