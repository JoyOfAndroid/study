# Alpha track에 자동 배포하기
## 들어가기 전에
- Github Action을 기준으로 진행
## 설정하기
### step1. Repository의 Actions 탭에서 workflows 검색
<img src="https://user-images.githubusercontent.com/48344355/154060286-72b1a6fe-e4ad-4cd2-9d4f-9ef10957ecf9.png">

### step2. `Configure` 클릭
<img src="https://user-images.githubusercontent.com/48344355/154060516-12377829-a7dc-476e-a71c-597409a71004.png" width=30%>

### step3. android.yml 생성
```yml
name: Android CI

on:
   push:
      branches: [ main ]
   pull_request:
      branches: [ main ]

jobs:
   build:

      runs-on: ubuntu-latest

      steps:
         - uses: actions/checkout@v2
         - name: set up JDK 11
           uses: actions/setup-java@v2
           with:
              java-version: '11'
              distribution: 'temurin'
              cache: gradle

         - name: Grant execute permission for gradlew
           run: chmod +x gradlew
         #- name: Build with Gradle // 이 부분은 제거해주세요
         #  run: ./gradlew build // 이 부분은 제거해주세요
```
### step4. 빌드 시 필요한 속성 등록
Repository의 Settings -> Secrets -> Actions 진입
<img src="https://user-images.githubusercontent.com/48344355/154062077-ecc946ec-99a5-49cc-b422-2dc0ce325fc8.png">

- ALIAS : release 빌드 시 필요한 alias 정보
- GOOGLE_SERVICES_JSON : 파이어베이스 사용 시 필요한 정보
- KEY_PASSWORD : release 빌드 시 필요한 key password
- KEY_STORE_PASSWORD : release 빌드 시 필요한 keystore password
- SERVICES_ACCOUNT_JSON : PlayStore 서비스 계정 정보 (step4-1에서 추가 설명)
- SIGNING_KEY : keystore를 base64로 인코딩한 정보
    - 터미널에 `base64 KeyStore파일명` 입력 시 출력되는 값

#### step4-1. PlayStore Service 계정 정보 만들기
1. 구글 플레이 콘솔 로그인
2. 설정 -> API 액세스 진입

   <img src="https://user-images.githubusercontent.com/48344355/154064097-cdb5ccee-3973-4353-ae8b-fd56321c221e.png" width=30%>
3. `새 서비스 계정 만들기` 클릭
   <img src="https://user-images.githubusercontent.com/48344355/154064610-16471b66-4b32-4a88-a9eb-bd1a0e9d93f9.png">
4. 아래와 같은 절차 진행

   <img src="https://user-images.githubusercontent.com/48344355/154064997-eac34d96-727d-4865-bef1-c9e6c500b190.png" width=30%">
5. Google Cloud Platform(GCP)에서 서비스 계정 만들기
6. 생성된 계정을 클릭하여 `새 키 만들기` 클릭 (json으로 만들기)

   <img src="https://user-images.githubusercontent.com/48344355/154065587-9aa692e9-7b6c-4ea4-bea0-04474a675cf9.png" width=30%><img src="https://user-images.githubusercontent.com/48344355/154065645-a0abb734-5faa-4ecd-921f-77ce314adbae.png" width=30%>
7. 다운로드된 json 파일을 step4의 SERVICES_ACCOUNT_JSON으로 등록

### step5. google-services.json 생성 (생략 가능)
파이어베이스를 사용하는 경우, 프로젝트를 빌드하기 위함
```yml
      - name: Create google-services.json
        run: echo '${{ secrets.GOOGLE_SERVICES_JSON }}' > ./app/google-services.json
```

### step6. aab로 빌드
```yml
      - name: Build Release AAB
        run: ./gradlew bundleRelease
```

### step7. 생성된 aab를 signing
```yml
      - name: Sign AAB
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: app/build/outputs/bundle/release # 릴리즈 빌드 시 aab가 생성되는 폴더 경로
          signingKeyBase64: ${{ secrets.SIGNING_KEY }}
          alias: ${{ secrets.ALIAS }}
          keyStorePassword: ${{ secrets.KEY_STORE_PASSWORD }}
          keyPassword: ${{ secrets.KEY_PASSWORD }}
```

### step8. artifacts에 결과물 저장 (생략 가능)
Github Action이 성공된 경우, CI건 단위로 결과물이 업로드됨
```yml
      - name: Upload AAB
        uses: actions/upload-artifact@v1
        with:
          name: app
          path: app/build/outputs/bundle/release/app-release.aab
```

### step9. service_account.json 생성
구글 플레이 콘솔 API를 사용하기 위함
```yml
      - name: Create service_account.json
        run: echo '${{ secrets.SERVICE_ACCOUNT_JSON }}' > service_account.json
```

### step10. 구글 플레이 콘솔 비공개 테스트 등록
```yml
      - name: Deploy to Play Store
        uses: r0adkll/upload-google-play@v1.0.15
        with:
          serviceAccountJson: service_account.json
          packageName: com.beok.runewords
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: alpha # beta, production 옵션도 있음
          whatsNewDirectory: whatsnew/ # 출시노트 경로
```
ref. https://github.com/r0adkll/upload-google-play

#### step10-1. whatsnew 폴더에 출시 노트 작성
언어별로 작성이 가능하며, 작성해두면 비공개 테스트에 등록될 때 작성된 내용으로 출시 노트에 추가됨

<img src="https://user-images.githubusercontent.com/48344355/154069039-44009029-3ab1-4e11-9669-8697359fc301.png" width="30%">


## 참고
- [Automate Android App Publishing on Play Store using GitHub Actions](https://medium.com/@niraj_prajapati/automate-android-app-publishing-on-play-store-using-github-actions-554de7801c36)
- [적용된 Repository](https://github.com/BeokBeok/RuneWords)
- [적용된 Repository의 yml파일](https://github.com/BeokBeok/RuneWords/blob/main/.github/workflows/ci.yml)
