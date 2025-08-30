import org.gradle.api.tasks.bundling.Jar

val javafxVersion = "21.0.8"

// http://insecure.repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.8/
val javafxClassifiers = listOf(
	"win",
	"mac",
	"mac-aarch64",
	"linux"
)

fun javafxDep(module: String, classifier: String) =
	"org.openjfx:javafx-$module:$javafxVersion:$classifier"

plugins {
	java
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.javafx)
}

version = libs.versions.version.get()
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

	implementation(libs.ikonli)
	include(libs.ikonli)

	for (classifier in javafxClassifiers) {
		implementation(javafxDep("base", classifier))
		implementation(javafxDep("graphics", classifier))
		implementation(javafxDep("controls", classifier))
		implementation(javafxDep("web", classifier))
		implementation(javafxDep("media", classifier))

		include(javafxDep("base", classifier))
		include(javafxDep("graphics", classifier))
		include(javafxDep("controls", classifier))
		include(javafxDep("web", classifier))
		include(javafxDep("media", classifier))
	}
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
	exclude("module-info.class")
	exclude("**/module-info.class")
	exclude("META-INF/MANIFEST.MF")
	exclude("META-INF/*.SF")
	exclude("META-INF/*.DSA")
	exclude("META-INF/*.RSA")

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