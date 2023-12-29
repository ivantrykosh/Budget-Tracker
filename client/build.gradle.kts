// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.sonarqube") version "4.4.1.3373"
}
sonar {
  properties {
    property("sonar.projectKey", "ivantrykosh_Budget-Tracker")
    property("sonar.organization", "ivantrykosh")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}
