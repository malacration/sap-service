import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
	id("jacoco")
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"
}

group = "br.andrew.sap"
version = if (project.hasProperty("version")) project.property("version") as String else "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}


repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web"){
		exclude("commons-logging")
	}
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

	implementation("org.apache.tika:tika-core:2.8.0")

	implementation("io.jsonwebtoken:jjwt-api:0.12.4")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.4")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.4")


	implementation("com.squareup.okhttp3:okhttp:4.12.0")



	//** Observability
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	//implementation("org.springframework.boot:spring-boot-starter-log4j2")
	implementation("biz.paluch.logging:logstash-gelf:1.15.1")

	implementation("io.micrometer:micrometer-tracing-bridge-brave")
	implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

	implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.mockito:mockito-core:5.3.0")

	//** - SOAP
	implementation("org.apache.axis:axis:1.4")
	implementation("org.apache.axis:axis-jaxrpc:1.4")
	implementation("org.apache.axis:axis-saaj:1.4")
	implementation("axis:axis-wsdl4j:1.5.1")
	implementation("javax.xml:jaxrpc-api:1.1")
	implementation("commons-discovery:commons-discovery:0.5"){
		exclude("commons-logging")
	}

	implementation("org.apache.commons:commons-lang3:3.9")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.0")
	implementation("com.itextpdf:itext-core:8.0.2")
	implementation("com.itextpdf:html2pdf:5.0.2")

}


val wsdlDir = "$projectDir/src/generated/java"

tasks.register("import-ws") {
	val packgePrefix = "br.andrew.sap"
	fileTree("src/main/resources/wsdl").forEach{ file ->
		doLast {
			javaexec {
				mainClass.set("org.apache.axis.wsdl.WSDL2Java")
//				configurations.implementation.get().isCanBeResolved = true
				classpath = files(
					configurations.runtimeClasspath.get().files,
//					configurations.implementation.get().files,
					configurations.annotationProcessor.get().files
				)
				args = listOf("-o","$wsdlDir", "-p","$packgePrefix.${file.name}", file.absolutePath,)
			}
		}
	}
}

tasks.withType<Test> {
	dependsOn(tasks.named("import-ws"))
	useJUnitPlatform()
}

tasks.named("jacocoTestReport", JacocoReport::class) {
	dependsOn(tasks.named("import-ws"),tasks.withType<Test>())
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


tasks.named("assemble") {
	dependsOn(tasks.named("import-ws"))
}

tasks.named("compileKotlin") {
	dependsOn(tasks.named("import-ws"))
}

tasks.buildDependents {
	dependsOn(tasks.named("import-ws"))
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}


configurations.all {
	resolutionStrategy {
		eachDependency {
			if (requested.group == "org.apache.axis") {
				useVersion("1.4")
			}
		}
	}
}

sourceSets {
	main {
		java {
			srcDirs(wsdlDir)
		}
	}
}