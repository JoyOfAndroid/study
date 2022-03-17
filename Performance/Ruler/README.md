# Ruler 적용하기
## Ruler란
- 안드로이드 앱 사이즈를 측정해주는 도구 
- (구글 플레이 콘솔에도 앱 크기 정보를 알려주는데 Ruler는 뭐가 좋은걸까? :thinking:)
## Ruler 설정하기
1. 플러그인 추가
   - [spotify/ruler](https://github.com/spotify/ruler) Adding the plugin 참고
2. app모듈의 gradle에 configuration 설정
    ```groovy
    ruler { // 이 4개의 속성중에 하나라도 빠지면 에러 발생
        abi.set("armeabi-v7a")
        locale.set("ko")
        screenDensity.set(640)
        sdkVersion.set(31)
    }    
    ```
   - [abi](https://developer.android.com/ndk/guides/abis) : armeabi-v7a, arm64-v8a, x86, x86-64
   - locale : en, ko, jp 등
   - [screenDensity](https://developer.android.com/training/multiscreen/screendensities) : 320, 480, 640 등
   - sdkVersion : sdk 버전
3. Ownership 설정 (선택)
   - ownership.yaml 생성
       ```yaml
       # 모듈 단위 owner 설정
       - identifier: :app
         owner: app-team
    
       # 라이브러리 단위로 owner 설정할 수 있으나, 의미가 있을까? 싶다
       #- identifier: androidx.core:core
       #  owner: core-team
    
       # 와일드카드 사용 가능
       - identifier: :feature:*
         owner: feature-team
       ```
   - 2번의 ruler 블록에 추가  
       ```groovy
       ruler {
           ownershipFile.set(project.file("/path/to/ownership.yaml"))
           defaultOwner.set("default-team") // unknown by default
       }
       ```
4. `./gradlew analyze<VariantName>Bundle` 실행
   - 기본값 (analyzeReleaseBundle, analyzeDebugBundle)
    ```text
   // 실행 결과
    > Task :app:analyzeReleaseBundle
    Wrote JSON report to file:///Users/.../app/build/reports/ruler/release/report.json
    Wrote HTML report to file:///Users/.../app/build/reports/ruler/release/report.html
    ```
## 생성된 파일 알아보기
![스크린샷 2022-03-07 오후 11 03 14](https://user-images.githubusercontent.com/48344355/157048852-994e0d11-08bb-46a9-87bd-b3ea798ba2cc.png)
- Breakdown 탭
  ![스크린샷 2022-03-07 오후 11 03 38](https://user-images.githubusercontent.com/48344355/157048907-982a27cd-4fbf-4c05-b3db-f71d66ad6afc.png)
  - test 관련 컴포넌트를 제외한 모든 컴포넌트 리스트 및 용량 정보
- Insights 탭
  ![스크린샷 2022-03-07 오후 11 04 15](https://user-images.githubusercontent.com/48344355/157049007-2f9727ce-3bec-440b-9aa0-4e86aeb0f2c4.png)
- Ownership 탭
  ![스크린샷 2022-03-07 오후 11 05 11](https://user-images.githubusercontent.com/48344355/157049176-e2789f2c-ecb0-4a6a-84f6-9b3c57391abe.png)
  - Components grouped by owner를 선택하여 각 모듈별 컴포넌트 리스트 및 용량 정보를 볼 수 있음
## 결론
- 큰 규모의 다이나믹 피쳐 기반 멀티 모듈 프로젝트에서는 의미가 있어보임