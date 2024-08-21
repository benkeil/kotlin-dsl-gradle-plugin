plugins { alias(libs.plugins.jvm) }

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.implementation)
  testImplementation(libs.bundles.testImplementation)
}

sourceSets {
  // main { kotlin { srcDirs("${layout.buildDirectory.get()}/generated/sources/kotlin-dsl") } }
}
