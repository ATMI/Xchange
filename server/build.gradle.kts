val postgres_version: String by project
val exposed_version: String by project


group = "xchng.mya.su"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":api"))

	// ktor
	implementation("io.ktor:ktor-server-core-jvm")
	implementation("io.ktor:ktor-server-netty-jvm")
	implementation("io.ktor:ktor-server-content-negotiation-jvm")

	// exposed
	implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
	implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
	implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
	implementation("org.postgresql:postgresql:$postgres_version")

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(17)
}