import org.gradle.api.tasks.bundling.Jar

val javafxVersion = "21.0.8"

val osName = System.getProperty("os.name").lowercase()
val arch = System.getProperty("os.arch").lowercase()

val javafxClassifier = when {
	osName.contains("win") && (arch.contains("aarch64") || arch.contains("arm64")) -> "win-aarch64"
	osName.contains("win") -> "win"
	osName.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) -> "mac-aarch64"
	osName.contains("mac") -> "mac"
	osName.contains("linux") && (arch.contains("aarch64") || arch.contains("arm64")) -> "linux-aarch64"
	osName.contains("linux") -> "linux"
	else -> throw GradleException("Unsupported OS/arch for JavaFX: $osName / $arch")
}

fun javafxDep(module: String) = "org.openjfx:javafx-$module:$javafxVersion:$javafxClassifier"

plugins {
	java
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.javafx)
}

version = (libs.versions.version.get())
group = project.findProperty("maven_group") as String

base {
	archivesName.set(project.findProperty("archives_base_name") as String)
}

repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	mavenLocal()
}

dependencies {
	minecraft(libs.minecraft)
	mappings(libs.yarn)
	modCompileOnly(libs.fabric.api)
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.kotlin)
	modImplementation(libs.fabric.api)
	modImplementation(libs.modmenu)
	modRuntimeOnly(libs.devauth)

	implementation(javafxDep("base"))
	implementation(javafxDep("graphics"))
	implementation(javafxDep("controls"))
	implementation(javafxDep("fxml"))
	implementation(javafxDep("web"))
	implementation(javafxDep("media"))

	include(javafxDep("base"))
	include(javafxDep("graphics"))
	include(javafxDep("controls"))
	include(javafxDep("fxml"))
	include(javafxDep("web"))
	include(javafxDep("media"))
}

tasks.named<ProcessResources>("processResources") {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}

tasks.withType<JavaCompile> {
	options.release.set(21)
}

tasks.named<Jar>("jar") {
	from("LICENSE") {
		rename { "${it}_${project.findProperty("archives_base_name")}" }
	}

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.findProperty("archives_base_name") as String
			from(components["java"])
		}
	}
	repositories {

	}
}