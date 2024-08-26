plugins { alias(libs.plugins.jvm) }

repositories { mavenCentral() }

dependencies {
  implementation(libs.bundles.pipeline.implementation)
  testImplementation(libs.bundles.testImplementation)
}
