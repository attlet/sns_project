plugins {
	val kotlinVersion = "1.9.25"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	id("org.springframework.boot") version "3.2.9"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version kotlinVersion
	kotlin("kapt") version kotlinVersion
}

group = "com.kotlin"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

val queryDslVersion = "5.1.0" //querydsl 버전

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	//querydsl 설정
	implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
	kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

	implementation("org.mapstruct:mapstruct:1.4.2.Final")
	kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

val querydslDir = "${layout.buildDirectory}/generated/querydsl"

tasks.withType<Test> {
	useJUnitPlatform()
}
