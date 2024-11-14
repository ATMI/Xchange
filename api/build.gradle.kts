plugins {
	kotlin("jvm") version "1.9.23"
}

group = "xchng.mya.su"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(17)
}