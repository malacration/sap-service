import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jaxws by configurations.creating

plugins {
	id("java")
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	id("jacoco")
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

java.sourceCompatibility = JavaVersion.VERSION_17

java.sourceSets["main"].java {
	srcDir("${buildDir}/generated/sources/jaxws")
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
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web-services") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
	}

	implementation("com.sun.xml.ws:jaxws-tools:4.0.1")
	implementation("jakarta.xml.ws:jakarta.xml.ws-api:4.0.1")
	implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
	implementation("jakarta.activation:jakarta.activation-api:2.1.2")
	implementation("com.sun.xml.ws:jaxws-rt:4.0.1")
	implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")


	jaxws("com.sun.xml.ws:jaxws-tools:4.0.1",)
	jaxws("jakarta.xml.ws:jakarta.xml.ws-api:4.0.1")
	jaxws("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
	jaxws("jakarta.activation:jakarta.activation-api:2.1.2")
	jaxws("com.sun.xml.ws:jaxws-rt:4.0.1")


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

task("wsimport-softExpert") {
	group = BasePlugin.BUILD_GROUP
	val dir = "${buildDir}/generated/sources/jaxws"
	val destDir = file(dir)
	destDir.mkdirs()
	val sourceDestDir = file(dir)
	doLast {
		ant.withGroovyBuilder {
			"taskdef"(
				"name" to "wsimport",
				"classname" to "com.sun.tools.ws.ant.WsImport",
				"classpath" to jaxws.asPath
			)

			"wsimport"(
				"keep" to true,
				"sourcedestdir" to sourceDestDir,
				"verbose" to true,
				"extension" to true,
				"xnocompile" to true,
				//"destDir" to destDir, alreaddy compiled java classes, not needed
				"package" to "softexpert",
				"wsdl" to "https://se.gruporovema.com.br/se/ws/wf_ws.php?wsdl",
			)
		}
	}
}
