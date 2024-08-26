import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.testImplementation

group = "io.github.benkeil"

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  alias(libs.plugins.jvm)
  alias(libs.plugins.pluginPublish)
}

gradlePlugin {
  website = "https://github.com/benkeil/kotlin-dsl-gradle-plugin"
  vcsUrl = "https://github.com/benkeil/kotlin-dsl-gradle-plugin.git"
  plugins {
    create("kotlinDsl") {
      id = "io.github.benkeil.kotlin-dsl-gradle-plugin"
      displayName = "Kotlin DSL Gradle Plugin"
      description =
          "Generates special kotlin classes from different API schemas, that can be used to build a custom DSL"
      tags = listOf("json", "schema", "openapi", "dsl", "kotlin", "generate")
      implementationClass = "pub.keil.kotlin.dsl.plugin.KotlinDslGradlePlugin"
    }
  }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(gradleApi())
  api(gradleKotlinDsl())
  if (System.getenv()["CI"]?.toBoolean() == true && rootProject.version.toString()[0].isDigit()) {
    println("Using GitHubPackages")
    println(System.getenv("CI"))
    println("rootProject.version: ${rootProject.version}")
    implementation("io.github.benkeil.kotlin-dsl-gradle-plugin:${rootProject.version}")
  } else {
    api(project(":lib"))
  }
  implementation(gradleKotlinDsl())
  implementation(libs.bundles.implementation)
  testImplementation(libs.bundles.testImplementation)
  testImplementation(gradleTestKit())
}

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/benkeil/*")
    credentials {
      username = "not_required"
      password = System.getenv("GITHUB_PACKAGES_READ_TOKEN")
    }
  }
}
