package com.example.data.model

data class AppUpdateInfo(
    val versionCode: Int = 1,
    val versionName: String = "1.0",
    val releaseNotes: String = "",
    val downloadUrl: String = "https://ais-dev-wrt3x77rkmwpod2cweoth6-79122091620.asia-east1.run.app/JISAN_MOBILE.apk",
    val forceUpdate: Boolean = false,
    val releaseDate: String = "",
    val minSupportedVersion: Int = 1
)
