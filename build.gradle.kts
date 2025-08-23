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

	implementation(libs.javafxcontrols)
	implementation(libs.javafxfxml)
	implementation(libs.javafxbase)
	implementation(libs.javafxweb)
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

// Guide on adding JavaFX: https://github.com/Col-E/Useful-Things/tree/master/tutorials/javafx/gradle-setup
tasks.named<Jar>("jar") {
	from("LICENSE") {
		rename { "${it}_${project.findProperty("archives_base_name")}" }
	}

	// Manually include JavaFX classes in the jar
	from({
		configurations.runtimeClasspath.get()
			.filter { it.name.endsWith("jar") }
			.filter { dep ->
				dep.name.contains("javafx") ||
						dep.absolutePath.contains("javafx")
			}
			.map { zipTree(it) }
	}) {
		// Exclude module-info and META-INF files that cause issues
		exclude("module-info.class")
		exclude("**/module-info.class")
		exclude("META-INF/MANIFEST.MF")
		exclude("META-INF/*.SF")
		exclude("META-INF/*.DSA")
		exclude("META-INF/*.RSA")
		exclude("META-INF/versions/**")
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