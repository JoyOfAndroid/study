object Version {
    const val KOTLIN_VER = "1.5.31"
    const val HILT = "2.38.1"
}

object ProjectConfig {
    const val GRADLE = "com.android.tools.build:gradle:7.0.3"
    const val KOTLIN_GRADLE_PLUGIN =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.KOTLIN_VER}"
    const val HILT_ANDROID_GRADLE_PLUGIN =
        "com.google.dagger:hilt-android-gradle-plugin:${Version.HILT}"
}

object AndroidConfig {
    const val COMPILE_SDK = 30
    const val APP_ID = "com.egiwon.deepdiverecyclerview1"
    const val MIN_SDK = 23
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}

object Dependencies {
    private const val RXJAVA_VER = "2.2.21"

    const val HILT_ANDROID = "com.google.dagger:hilt-android:${Version.HILT}"
    const val HILT_ANDROID_COMPILER = "com.google.dagger:hilt-android-compiler:${Version.HILT}"

    const val HILT_COMPILER = "com.google.dagger:hilt-compiler:${Version.HILT}"
    const val HILT_CORE = "com.google.dagger:hilt-core:${Version.HILT}"

    const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib:${Version.KOTLIN_VER}"
    const val CORE_KTX = "androidx.core:core-ktx:1.6.0"
    const val MATERIAL = "com.google.android.material:material:1.4.0"
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:2.1.0"

    const val RXJAVA = "io.reactivex.rxjava2:rxjava:$RXJAVA_VER"
    const val RX_ANDROID = "io.reactivex.rxjava2:rxandroid:2.1.1"
    const val RX_KOTLIN = "io.reactivex.rxjava2:rxkotlin:2.4.0"

    const val ACTIVITY_EXT = "androidx.activity:activity-ktx:1.3.1"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:1.3.6"
}

object GlideDependencies {
    private const val GLIDE_VER = "4.12.0"

    const val GLIDE = "com.github.bumptech.glide:glide:$GLIDE_VER"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:$GLIDE_VER"
}

object NetworkConfig {
    private const val RETROFIT_VER = "2.9.0"

    const val RETROFIT = "com.squareup.retrofit2:retrofit:$RETROFIT_VER"
    const val LOGGING_INTERCEPTER = "com.squareup.okhttp3:logging-interceptor:4.9.0"
    const val GSON = "com.squareup.retrofit2:converter-gson:$RETROFIT_VER"
    const val RXJAVA_ADAPTER = "com.squareup.retrofit2:adapter-rxjava2:$RETROFIT_VER"
}

object TestConfig {
    private const val JUNIT_VERSION = "5.7.0"

    const val JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:$JUNIT_VERSION"
    const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:$JUNIT_VERSION"
    const val JUNIT_JUPITER_PARAMS = "org.junit.jupiter:junit-jupiter-params:$JUNIT_VERSION"

    const val ASSERTJ_CORE = "org.assertj:assertj-core:3.18.0"
}
