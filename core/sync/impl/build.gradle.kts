plugins {
  id("voice.library")
}

dependencies {
  implementation(projects.core.data.api)
  implementation(projects.core.sync.api)
  implementation(libs.okhttp)
  implementation(libs.serialization.json)

  testImplementation(libs.bundles.testing.jvm)
}
