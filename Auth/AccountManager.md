# AccountManager란
- 유저의 온라인 계정의 레지스트리 접근 제공
- 유저는 계정당 한 번 인증하여 "one click" 승인하며, 앱에 온라인 리소스 접근 권한 부여
- 계정이 아닌 앱에 대한 인증 토큰을 생성할 수 있으므로, 앱이 비밀번호를 직접 처리할 필요가 없음
- 계정 정보는 일반 텍스트로 저장하며, 이를 확인해보려면 루트 권한이 있어야 함

# AccountManager 연동
## 1. Authenticator 구현
```kotlin
class Authenticator(context: Context) : AbstractAccountAuthenticator(context)
```
- AbstractAccountAuthenticator 상속하여 오버라이딩 메서드 구현
- 오버라이딩 메서드 중 `addAccount` 함수에는 주로 회원가입/로그인 화면으로 이동하는 로직을 넣음
## 2. Authenticator를 Framework에 바인딩
```kotlin
class Service : Service() {

    lateinit var authenticator: Authenticator

    override fun onCreate() {
        authenticator = Authenticator(this)
    }
    
    override fun onBind(intent: Intent): IBinder =
        authenticator.iBinder
}
```
## 3. 메니페스트에 서비스 등록
### 1. 커스텀 퍼미션 생성
```xml
<permission
    android:name="com.beok.accountmanagersample.auth.permission.XXX"
    android:description="@string/app_name"
    android:icon="@mipmap/ic_launcher_round"
    android:label="@string/app_name"
    android:protectionLevel="signature" />
```
- name : 커스텀으로 만들 퍼미션 이름
- protectionLevel : 퍼미션을 부여하는 정책
  - normal : 앱 설치 시 자동 퍼미션 부여
  - dangerous : 사용자에게 퍼미션을 요청하고, 허가할 경우에만 퍼미션 부여
  - signature : 퍼미션이 정의된 앱과 동일 key로 서명한 앱에만 퍼미션 부여 (예, 쿠팡 로그인 계정을 쿠팡이츠에서 땡겨쓸 수 있음)
### 2. 메타데이터 생성
```xml
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
    android:accountType="com.beok.accountmanagersample.auth"
    android:icon="@mipmap/ic_launcher_round"
    android:label="@string/app_name"
    android:smallIcon="@mipmap/ic_launcher_round"/>
```
- accountType : 내부에서 사용할 도메인 ID
#### 주의사항
label 속성에 문구를 하드코딩하면 "설정 > 계정 및 백업 > 계정 관리" 화면에서 계정을 삭제할 수 없음
### 3. 커스텀 퍼미션 기반 서비스 등록
```xml
<service
    android:name=".auth.SampleAuthService"
    android:exported="true"
    android:permission="com.beok.accountmanagersample.auth.permission.XXX">
    <intent-filter>
        <action android:name="android.accounts.AccountAuthenticator" />
    </intent-filter>
    <meta-data
        android:name="android.accounts.AccountAuthenticator"
        android:resource="@xml/authenticator" />
</service>
```
- permission : 커스텀 퍼미션 이름과 동일하게 작성
- intent-filter
  - android.accounts.AccountAuthenticator로 등록 
  - 시스템에서 AuthenticatorService 시작
- meta-data
  - name : 메타데이터를 authenticator 프레임워크에 연결
  - resource : authenticator 메타데이터 파일

# 계정 정보 등록
## 1. AccountManager 생성
```kotlin
AccountManager.get(context)
```
## 2. 계정 생성
```java
// 선언부
public boolean addAccountExplicitly(Account account, String password, Bundle userdata)
```
```kotlin
// 사용 예
accountManager.addAccountExplicitly(
    Account(email, ACCOUNT_TYPE), // android.accounts.Account
    null,
    bundleOf(KEY_TOKEN to token)
)
```
- Account(email, ACCOUNT_TYPE) : 유저 계정 정보, ACCOUNT_TYPE은 메타데이터 생성 시 accountType 속성과 동일
- null : 패스워드 정보
- bundleOf(KEY_TOKEN to token) : 기타 데이터 (인증 토큰 등)
# 특정 데이터 읽기
```java
// 선언부
public String getUserData(Account account, String key)
```
```kotlin
// 사용 예
accountManager.getUserData(account, KEY_TOKEN)
```
# 특정 데이터 업데이트
```java
// 선언부
public void setUserData(Account account, String key, String value)
```
```kotlin
// 사용 예
accountManager.setUserData(account, KEY_TOKEN, accessToken)
```
# 계정 삭제
```java
// 선언부
public boolean removeAccountExplicitly(Account account)
```
```kotlin
// 사용 예
accountManager.removeAccountExplicitly(account)
```
# 참고
- https://developer.android.com/reference/android/accounts/AccountManager
- https://developer.android.com/training/id-auth/identify
- https://developer.android.com/training/sync-adapters/creating-authenticator
- https://codechacha.com/ko/android-define-custom-permission/
## 적용 코드
- https://github.com/BeokBeok/AccountManagerSample
