group = "io.github.benkeil"

plugins {
  alias(libs.plugins.jvm)
  `maven-publish`
}

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.implementation)
  testImplementation(libs.bundles.testImplementation)
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/benkeil/${rootProject.name}")
      credentials {
        username = "not_required"
        password = System.getenv("GITHUB_PACKAGES_WRITE_TOKEN")
      }
    }
  }

  publications { register<MavenPublication>("gpr") { from(components["java"]) } }
}

// sourceSets {
  // test { kotlin { srcDirs("${layout.buildDirectory.get()}/generated/sources/kotlin-dsl") } }
// }
