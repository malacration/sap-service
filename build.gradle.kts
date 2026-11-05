import javax.inject.Inject

plugins {
	id("jacoco")
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.4.21"
	kotlin("plugin.spring") version "2.4.21"
}

group = "br.andrew.sap"
version = if (project.hasProperty("version")) project.property("version") as String else "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}


repositories {
	mavenCentral()
}

dependencies {
	implementation("com.aventrix.jnanoid:jnanoid:2.0.0")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web"){
		exclude("commons-logging")
	}
    implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	// springdoc 2.8.x e a linha compativel com Boot 3.5 (3.x exige Boot 4).
	// springdoc-openapi-ui 1.x (Boot 2) removido: nao era usado e duplicava swagger-ui.
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")

	implementation("io.jsonwebtoken:jjwt-api:0.12.7")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.7")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.7")

	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	//** Observability
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	//implementation("org.springframework.boot:spring-boot-starter-log4j2")
	implementation("biz.paluch.logging:logstash-gelf:1.15.1")

	implementation("io.micrometer:micrometer-tracing-bridge-brave")
	implementation("com.github.ben-manes.caffeine:caffeine")


	// Versoes do Jackson 2 gerenciadas pelo BOM do Boot (antes fixadas em 2.16/2.18 misturadas).
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.jetbrains.kotlin:kotlin-stdlib")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	//** - SOAP
	implementation("org.apache.axis:axis:1.7.9")
	implementation("org.apache.axis:axis-jaxrpc:1.4")
	implementation("org.apache.axis:axis-saaj:1.4")
	implementation("axis:axis-wsdl4j:1.5.1")
	implementation("javax.xml:jaxrpc-api:1.1")
	implementation("commons-discovery:commons-discovery:0.5"){
		exclude("commons-logging")
	}

	implementation("org.apache.commons:commons-lang3:3.20.0")
	implementation("org.apache.commons:commons-text:1.15.0")
	implementation("org.apache.tika:tika-core:3.3.3")
	implementation("org.apache.httpcomponents.client5:httpclient5")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.12.0")
	implementation("com.itextpdf:itext-core:8.0.5")
	implementation("com.itextpdf:html2pdf:5.0.5")



	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("org.mockito:mockito-core")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")


}


val wsdlDir = "$projectDir/src/generated/java"

// Gradle 9 removeu Project.javaexec; ExecOperations e o substituto oficial.
interface InjectedExecOps {
	@get:Inject
	val execOps: ExecOperations
}

tasks.register("import-ws") {
	val packgePrefix = "br.andrew.sap"
	val injected = project.objects.newInstance<InjectedExecOps>()
	fileTree("src/main/resources/wsdl").forEach{ file ->
		doLast {
			injected.execOps.javaexec {
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
		html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
		xml.outputLocation.set(layout.buildDirectory.file("jacoco.xml"))
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
	executionData.from(layout.buildDirectory.file("jacoco/test.exec"))
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
