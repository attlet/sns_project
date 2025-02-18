import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.8.20"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
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
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	//test
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.mockito:mockito-core:5.2.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
	runtimeOnly("com.h2database:h2")

	implementation("org.springframework.boot:spring-boot-starter-logging")

	//aop
	implementation("org.springframework.boot:spring-boot-starter-aop")

	//db 연동
	implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")

	//querydsl 설정
	implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
	kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
	kapt("jakarta.annotation:jakarta.annotation-api")
	kapt("jakarta.persistence:jakarta.persistence-api")

	//mapStruct 의존성
	implementation("org.mapstruct:mapstruct:1.4.2.Final")
	kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")

	//swagger 설정
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.1.0")

	//spring security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	//logging
	implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")
	implementation("ch.qos.logback:logback-classic:1.4.11")

	//aws s3
	implementation(platform("software.amazon.awssdk:bom:2.20.50"))
	implementation("software.amazon.awssdk:s3")

	//redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	//kafka
	implementation("org.springframework.kafka:spring-kafka")

	//rabbitMQ
	implementation("org.springframework.boot:spring-boot-starter-amqp")

	//actuator
	implementation ("org.springframework.boot:spring-boot-starter-actuator")

	//prometheus
	implementation ("io.micrometer:micrometer-registry-prometheus")


}


// Querydsl 설정부 추가 - start
val generated = file("src/main/generated")

// querydsl QClass 파일 생성 위치를 지정
tasks.withType<JavaCompile> {
	options.generatedSourceOutputDirectory.set(generated)
}

// kotlin source set 에 querydsl QClass 위치 추가
sourceSets {
	main {
		kotlin.srcDirs += generated
	}
	test {
		kotlin.srcDirs("src/test/kotlin")
	}
}
// gradle clean 시에 QClass 디렉토리 삭제
tasks.named("clean") {
	doLast {
		generated.deleteRecursively()
	}
}


kapt {
	generateStubs = true
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		freeCompilerArgs += listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}
