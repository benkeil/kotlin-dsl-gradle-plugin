plugins { alias(libs.plugins.jvm) }

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.implementation)
  testImplementation(libs.bundles.testImplementation)
}
