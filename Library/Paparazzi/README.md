# Paparazzi란
실 기기 혹은 애뮬레이터 없이, 앱 화면 랜더링을 도와주는 라이브러리

# 환경 설정
## 1. project단 gradle 설정
```gradle
classpath 'app.cash.paparazzi:paparazzi-gradle-plugin:1.0.0'
```
classpath를 추가해준다. (8월 4일 기준 최신)
## 2. 모듈단 gradle 설정
```gradle
plugins {
  id 'com.android.library'
  id 'app.cash.paparazzi'
}
```
### 주의사항
`com.android.library`에만 동작
- `com.android.application`에 적용 시 빌드 에러
- 즉, app 모듈은 사용 불가

# 스냅샷 저장
스냅샷을 저장하기 위한 테스트 코드 작성 (androidTest X, test O)
## 1. rule 설정
```kotlin
@get:Rule
val paparazzi = Paparazzi()
```
```kotlin
class Paparazzi @JvmOverloads constructor(
  private val environment: Environment = detectEnvironment(),
  private val deviceConfig: DeviceConfig = DeviceConfig.NEXUS_5,
  private val theme: String = "android:Theme.Material.NoActionBar.Fullscreen",
  private val renderingMode: RenderingMode = RenderingMode.NORMAL,
  private val appCompatEnabled: Boolean = true,
  private val maxPercentDifference: Double = 0.1,
  private val snapshotHandler: SnapshotHandler = determineHandler(maxPercentDifference),
  private val renderExtensions: Set<RenderExtension> = setOf()
) : TestRule
```
## 2. 스냅샷 코드 작성
```kotlin
@Test
fun pixel5() {
    val launch = paparazzi.inflate<LinearLayout>(R.layout.activity_paparazzi)
    paparazzi.snapshot(launch)
}
```
```kotlin
    @Test
    fun pixel5() {
        paparazzi.snapshot {
            Greeting(name = "Compose")
        }
    }
```
### 2-1. 테마를 바꾸고 싶다면
```kotiln
paparazzi.unsafeUpdateConfig(theme = "android:Theme.Material.Light")
var launch = paparazzi.inflate<LinearLayout>(R.layout.activity_paparazzi)
paparazzi.snapshot(view = launch, name = "light")
```
### 2-2. 화면 방향을 바꾸고 싶다면
```kotiln
paparazzi.unsafeUpdateConfig(deviceConfig = NEXUS_5_LAND)
launch = paparazzi.inflate(R.layout.activity_paparazzi)
paparazzi.snapshot(view = launch, name = "landscape")
```

# 테스트
## 테스트 코드 실행
```
$ ./gradlew sample:testDebug
```
모든 snapshot 테스트 코드를 실행하고, HTML 형식의 report 파일 생성
- `sample/build/reports/paparazzi` 폴더에 저장됨

<details>
  <summary>결과물</summary>
  
  ![스크린샷 2022-08-04 오전 7 57 43](https://user-images.githubusercontent.com/48344355/182726574-d3b27e72-8eb5-4715-a01e-7c9b2ae1dec2.png)

</details>

## 스냅샷 저장
```
$ ./gradlew sample:recordPaparazziDebug
```
스냅샷을 golden value로 저장
- 스냅샷 변경 여부를 쉽게 파악하기 위한 값
- `src/test/snapshots` 폴더에 저장됨 

<details>
  <summary>결과물</summary>
  
  ![스크린샷 2022-08-04 오전 8 04 10](https://user-images.githubusercontent.com/48344355/182727261-1bc9f899-7822-4592-8fb1-a3598c290b88.png)
  
</details>

## 스냅샷 검증
```
$ ./gradlew sample:verifyPaparazziDebug
```
테스트를 실행하고 이전에 생성된 golden value와 새로 생성된 golden value와 비교
- 실패 시 `sample/out/failures` 폴더에 저장됨
- 실패한 화면의 스냅샷과, 변경전 스냅샷과 변경후 스냅샷이 무엇인지 보여줌

<details>
  <summary>결과물</summary>
  
  ![com beok paparazzi_PaparazziComposeTest_pixel5_differentThemes_light](https://user-images.githubusercontent.com/48344355/182728541-2d1f54a0-ad85-47cb-a570-d91ce6e45f27.png)

  ![delta-com beok paparazzi_PaparazziComposeTest_pixel5_differentThemes_light](https://user-images.githubusercontent.com/48344355/182728550-7d0e1267-e42d-4924-81ef-78bc1432bdaa.png)
  
</details>

# 참고
- https://github.com/cashapp/paparazzi
- https://proandroiddev.com/no-emulator-needed-for-screenshot-tests-of-compose-previews-on-ci-d292fa0eb26e
