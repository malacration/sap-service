import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	id("jacoco")
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "br.andrew.sap"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-quartz")
	//implementation("org.springframework.boot:spring-boot-starter-security")


	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

	implementation("org.apache.tika:tika-core:2.8.0")

	implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.mockito:mockito-core:5.3.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("jacocoTestReport", JacocoReport::class) {
	dependsOn(tasks.withType<Test>())

	group = "Reporting"
	reports {
		html.required.set(true)
		xml.required.set(true)
		csv.required.set(false)
		html.outputLocation.set(file("$buildDir/jacocoHtml"))
		xml.outputLocation.set(file("$buildDir/jacoco.xml"))
	}

	classDirectories.from(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
					"**/*Test*.*",
					"**/*Controller*.*",
				)
			}
		})
	)
	executionData.from(files("${project.buildDir}/jacoco/test.exec"))
}
