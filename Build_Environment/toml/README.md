# 배경
https://github.com/android/nowinandroid

2022 Google I/O를 보면서 NowInAndroid 프로젝트를 알게 되었다.
코드를 분석하다가 디펜던시 관리를 특이(?)하게 하고 있다는 것을 발견하게 되었다.
```kts
dependencies {    
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewModelCompose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```
이렇게 되어 있는 경우에는, 대부분 특정 gradle에서 라이브러리와 버전을 관리하고 이를 참조해서 사용을 한다.
```groovy
ext.versions = [
   'hilt' = "2.41"
]

ext.libs = [
   'hilt' : [
      'android' : "com.google.dagger:dagger-android:${versions.hilt}"
      'compiler' : "com.google.dagger:dagger-compiler:${versions.hilt}"
   ]
]
```
하지만 아무리 찾아봐도 이런 코드들은 없었고, 한참을 찾아보니 libs.version.toml 이라는 파일에서 관리를 하고 있었다.
평소 buildSrc + kts 조합으로 디펜던시를 관리하고 있고 익숙해진 상태이므로 그냥 넘길까 하다가, 처음 보는 디펜던시 관리 방식이라 한번 알아보게 되었다.
# 적용해보기
https://github.com/BeokBeok/RuneWords/pull/73
## 1. toml 만들기
### 버전 정의
```toml
[versions]
jUnit5 = "5.8.2"
compose = "1.1.1"
hilt = "2.41"
```
### 라이브러리 정의
```toml
[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.7.0" }
junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "jUnit5" }
compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
```
- `group` + `name` 조합을 사용해도 되고, `module` 조합을 사용해도 된다.
    - 개인적으로는 module이 마이그레이션 하기 편하다.
- 정의한 버전 정보를 사용하려면 `version.ref`를 쓰고, 아니라면 `version` 을 쓰면 된다.
- 하이픈(-) 단위로 참조가 가능하며, 반드시 모든 항목을 다 담아야 한다.
    - [junit.jupiter ❌ , junit.jupiter.api ✅ ]
    - 누락해서 빌드 에러가 발생해서 조금 애먹었다.
    - 에러 메시지가 불친절해서 어떤 부분이 누락되었는지 찾기가 어려웠다.
- 허용하지 않는 문법
    - 언더바 [core_ktx ❌ , core-ktx ✅ ]
    - plugin 문구 [kotlin-gradle-plugin ❌ , kotlin-gradle ✅ ]

### 번들 정의 (Optional)
```toml
[bundles]
compose = [
    "compose-ui",
    "compose-material",
    "compose-ui-tooling",
    "compose-runtime-livedata",
    "activity-compose",
    "constraintlayout-compose"
]
```
```kts
// 사용 예 - kts 기준
implementation(libs.bundles.compose)
```
컴포즈와 같이 여러 디펜던시를 묶어서 써야하는 경우에는, 배열로 정의해서 묶을 수 있다.
다만, implementation과 kapt를 섞어서 쓰고 싶은 경우는 묶어서 사용할 수 없다.
## 2. toml 연동하기
setting.gradle.kts에 적용한다.
```kts
enableFeaturePreview("VERSION_CATALOGS")
```
Gradle 7.4 미만에는 위 옵션을 추가해줘야 한다.

```kts
versionCatalogs {
    create("libs") {
        from(files("./gradle/dependency.toml"))
    }
}
```
- `create`에 문구를 넣게 되면, 해당 문구로 접근할 수 있다. (예, libs.compose.ui)
- `from` 에는 toml의 경로를 넣어준다.
## 3. toml 적용하기
implementation(Compose.UI) ⇒ implementation(libs.compose.ui) 와 같이 변경해주면 끝!
# 정리
## 장점
- 다른 프로젝트에서 빠르게 재활용할 수 있다. (toml 복사 → 연동)
## 단점
- 라이브러리 최신버전이 릴리즈되더라도 알 수 없다.
    - 최근 안드로이드 스튜디오에서 buildSrc + kts를 지원하게 되면서, 최신 버전을 알 수 있음 (일부만)
        ![image](https://user-images.githubusercontent.com/48344355/170158150-af7c96c7-b460-41f7-ac7d-3fd4138144a9.png)
- 커맨드+마우스 클릭(Mac 기준)시 해당 디펜던시로 이동이 안된다.
    - 개인적으로 이 부분이 가장 불편했다.
# 결론
- 프로젝트를 여러 개를 관리하는 경우에는 toml 도입을 고려해볼만 하다.
- 아직까지는 toml보다는 buildSrc + kts 조합이 갖는 장점이 많다.
- toml만 단독으로 쓰기보다는, buildSrc + kts와 같이 사용하면, 개발 환경에 따라 좋을 것 같다.
