plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  alias(libs.plugins.jvm)
  id("com.gradle.plugin-publish") version "1.2.1"
  id("me.qoomon.git-versioning") version "6.4.4"
}

group = "io.github.benkeil"

apply { plugin("me.qoomon.git-versioning") }

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
      implementationClass = "pub.keil.kotlin.dsl.KotlinDslGradlePlugin"
    }
  }
}

gitVersioning.apply {
  refs {
    considerTagsOnBranches = System.getenv()["CI"]?.toBoolean() ?: true
    tag("v(?<version>.+)") { version = "\${ref.version}" }
    branch(".+") { version = "\${ref}" }
  }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(gradleApi())
  api(gradleKotlinDsl())
  implementation(project(":lib"))
  implementation(gradleKotlinDsl())
  implementation(libs.bundles.implementation)
  testImplementation(libs.bundles.testImplementation)
  testImplementation(gradleTestKit())
}

repositories { mavenCentral() }
