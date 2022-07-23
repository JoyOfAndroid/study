# AccountManager란
- 유저의 온라인 계정의 레지스트리 접근 제공
- 유저는 계정당 한 번 인증하여 "one click" 승인하며, 앱에 온라인 리소스 접근 권한 부여
- 계정이 아닌 앱에 대한 인증 토큰을 생성할 수 있으므로, 앱이 비밀번호를 직접 처리할 필요가 없음

# AccountManager를 활용하여 계정 정보 저장하기
## 1. Authenticator 생성
https://github.com/BeokBeok/AccountManagerSample/blob/66b0585db265ba8346d69a82e612dcb099ac6978/app/src/main/java/com/beok/accountmanagersample/auth/SampleAuthenticator.kt#L12-L54
