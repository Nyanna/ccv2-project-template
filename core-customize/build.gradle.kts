plugins {
   id("sap.commerce.build") version("4.0.0")
   id("sap.commerce.build.ccv2") version("4.0.0")
}

import mpern.sap.commerce.build.tasks.HybrisAntTask
import org.apache.tools.ant.taskdefs.condition.Os


val DEPENDENCY_FOLDER = "dependencies"
repositories {
    flatDir { dirs(DEPENDENCY_FOLDER) }
    mavenCentral()
}

tasks.register<Copy>("configureLocalProperties") {
    from("config/common/config/local.properties")
    into("hybris/config/")
}

tasks.register("setupLocalDevelopment") {
    group = "SAP Commerce"
    description = "Setup local development environment"
    dependsOn("bootstrapPlatform", "configureLocalProperties", "installManifestAddons", "yclean", "yall")
}
