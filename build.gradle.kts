val logback_version: String by project
val bouncycastle_version: String by project

plugins {
	id("groovy")
	id("io.ktor.plugin") version "3.0.1"

	kotlin("jvm") version "2.0.21"
	kotlin("plugin.serialization") version "2.0.21"
}

group = "xchng.mya.su"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "kotlinx-serialization")
	apply(plugin = "io.ktor.plugin")

	dependencies {
		implementation("org.apache.groovy:groovy:4.0.14")

		// ktor
		implementation("io.ktor:ktor-serialization-kotlinx-protobuf")
		implementation("ch.qos.logback:logback-classic:$logback_version")

		// cryptography
		implementation("org.bouncycastle:bcpkix-jdk18on:$bouncycastle_version")

		testImplementation(platform("org.junit:junit-bom:5.10.0"))
		testImplementation("org.junit.jupiter:junit-jupiter")
	}

	tasks.test {
		useJUnitPlatform()
	}
}
