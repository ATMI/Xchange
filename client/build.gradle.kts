plugins {
	kotlin("jvm") version "1.9.23"
}

group = "xchng.mya.su"

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":api"))

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(17)
}