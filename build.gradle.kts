plugins {
	java
}


allprojects {
	repositories {
		mavenCentral()
	}
	tasks.withType<JavaCompile>().configureEach {
		options.release.set(21)
	}
}
