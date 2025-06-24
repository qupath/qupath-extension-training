plugins {
	id("qupath-conventions")
	`maven-publish`
}

qupathExtension {
	name = "qupath-extension-training"
	version = "0.1.0-rc2"
	group = "io.github.qupath"
	description = "A QuPath extension to help learn how to use the software"
	automaticModule = "qupath.extension.training"
}

dependencies {

	implementation(libs.bundles.qupath)
	implementation(libs.bundles.logging)
	implementation(libs.qupath.fxtras)
	implementation(libs.bundles.markdown)

	// For testing
	testImplementation(libs.junit)

}

publishing {
	repositories {
		maven {
			name = "SciJava"
			val releasesRepoUrl = uri("https://maven.scijava.org/content/repositories/releases")
			val snapshotsRepoUrl = uri("https://maven.scijava.org/content/repositories/snapshots")
			// Use gradle -Prelease publish
			url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
			credentials {
				username = System.getenv("MAVEN_USER")
				password = System.getenv("MAVEN_PASS")
			}
		}
	}

	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				licenses {
					license {
						name = "Apache License v2.0"
						url = "https://www.apache.org/licenses/LICENSE-2.0"
					}
				}
			}
		}
	}
}
