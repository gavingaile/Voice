plugins {
  id("voice.library")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  explicitApi()
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.serialization.json)
}
