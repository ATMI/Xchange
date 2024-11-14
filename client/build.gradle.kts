group = "xchng.mya.su"

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":api"))

	// ktor
	implementation("io.ktor:ktor-client-core-jvm")
	implementation("io.ktor:ktor-client-cio-jvm")
	implementation("io.ktor:ktor-client-content-negotiation-jvm")

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(17)
}