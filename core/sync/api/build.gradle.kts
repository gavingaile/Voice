plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "voice.core.sync.api"
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.serialization.json)
}
