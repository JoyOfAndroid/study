- Android 13 은 POST_NOTIFICATIONS 앱에서 [non-exempt(Foreground Service 포함)](https://www.notion.so/Notification-runtime-permission-064c8181eb214538aeae0c7d06a7afc9) 알림을 보내기 위해 런타임 권한을 지원

- Android 13(sdk 33) 에서 대응됨
- [앱 기능의 컨텍스트 내에서 권한을 요청](https://www.notion.so/Notification-runtime-permission-064c8181eb214538aeae0c7d06a7afc9)

<aside>
💡 Foreground Service를 시작할 때 알림을 포함해야 됨.

</aside>

# Permission 정의

- 새 알림 권한을 요청하려면 13으로 업데이트하고 런타임 권한을 요청하는 것과 유사하게 처리가 필요

```kotlin
<manifest ...>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <application ...>
        ...
    </application>
</manifest>
```

# 앱 기능은 Permission Dialog 의 선택에 따름

- 허용
- 허용하지 않음
- 아무 버튼을 누르지 않고 스와이프

## 허용

- 모든 알림채널이 허용됨
- 알림을 보냄

## 허용하지 않음

- 앱이 면제 자격(exemtion)이 없는 한 알림을 보낼 수 없음
- 모든 알림 채널이 차단됨
- 시스템 설정에서 알림을 수동으로 끄는 것과 같은 동작

```
💡 Caution : 앱이 12L 이하이고 허용안함을 누르면 아래와 같은 상황이 발생되기 전까지 Dialog가 다시 표시 되지 않음. 

- 사용자가 재설치하기 전
- Android 13 대상으로 앱을 업데이트
```


## 아무 버튼을 누르지 않고 스와이프

- 알림 권한 상태가 변경되지 않음

# 새로 설치된 앱에 미치는 영향

- Android 13 기기에서 앱을 설치시 알림권한이 꺼져있음
- 앱이 새 권한을 요청하고 사용자가 앱에 해당 권한을 부여하기 전까지 알림을 보내지 않음

- 앱이 13을 대상으로 하면 Permission Dialog가 표시되는 시기를 제어 할 수 있음
- 이 것을 이용하여 권한 시점과 권한이 필요한 이유에 대해서 설명 할 수 있음

- Android 12 이하를 대상으로 하는 경우 시스템은 알림 채널을 생성한 후
- 앱이 처음으로 Activity를 시작할 때 또는 Activity 가 시작하고 첫번째 알림 채널을 생성할 때
- Permission Dialog를 표시
- 일반적으로 앱 시작 시

# 기존 앱 업데이트 미치는 영향

- 알림 권한과 관련된 중단을 최소화 하기 위해
- Android 13으로 업데이트 시 모든 적격 앱에 권한을 자동으로 부여함
- 이런 앱은 계속해서 사용자에게 알림을 보낼 수 있으며
- 런타임 Permission Dialog는 표시 되지 않음

```
💡 사용자가 12L 에서 앱을 설치했고 알림을 허용한 경우 

시스템은 13으로 업데이트 했을 때 앱의 알림 권한을 자동으로 부여함
```


# 사전 허가에 대한 자격

- 앱이 자동 사전 부여 대상이 되려면 기존 알림 채널이 있어야 되고
- 12L에서 앱이 알림을 비활성화 하지 않아야 함

- 12에서 비활성화 했을경우 13에서도 지속됨

# Exemptions

- Android 13에서 알림 권한을 거부 하면 Foreground Service, 작업 관리자의 Foreground Service와 관련된 알림이 표시되지만 알림 창에는 표시 되지 않음

## Media Session

- 미디어 관련 알림은 제외됨

## 전화 관리 앱

- Notification.CallStyle 알림 스타일을 사용하는 알림은 POST_NOTIFICATION 권한이 필요 없음

시스템은 다음의 경우 전화 통화를 관리한다고 간주

1. MANAGE_OWN_CALLS 권한
2. ConnectionService 인터페이스 구현
3. `registerPhoneAccount()` 를 호출하여 장치의 통신 공급자 에 등록

# 앱을 테스트 해보자

- ADB 를 사용하여 테스트 할 수 있음

## Android 13 이상을 실행하는 기기에 앱이 새로 설치된 경우 :

```kotlin
$ adb shell pm revoke PACKAGE_NAME android.permission.POST_NOTIFICATIONS
$ adb shell pm clear-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-set
$ adb shell pm clear-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-fixed
```

## 앱이 12L 이하에서 설치되었고 알림을 활성화 했고 13으로 업그레이드 :

```kotlin
$ adb shell pm grant PACKAGE_NAME android.permission.POST_NOTIFICATIONS
$ adb shell pm set-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-set
$ adb shell pm clear-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-fixed
```

## 앱이 12L 이하에서 설치되었고 알림 비활성화 했고 13으로 업그레이드 :

```kotlin
$ adb shell pm revoke PACKAGE_NAME android.permission.POST_NOTIFICATIONS
$ adb shell pm set-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-set
$ adb shell pm clear-permission-flags PACKAGE_NAME \
  android.permission.POST_NOTIFICATIONS user-fixed
```

# Best Practices

알림을 효과적으로 사용하는 방법

## Target Sdk 버전 올리기

- 권한의 유연성을 제공하려면 13으로 업데이트

## Notification Permission Dialog

사용자가 알림 권한 부여를 확인하기 위해 권한 요청 전에 자세히 설명해주세요

- 사용자는 앱 시작 시에 앱을 탐색하길 원함
- 권한 프롬프트를 노출하여 미리 권한에 대해서 이해할 수 있게 할 수 있음

아래는 프롬프트 표시 예시

- 사용자가 “경고 벨"을 탭
- 사용자가 누군가의 소셜미디어를 팔로우
- 사용자가 음식 배달 주문

`shouldShowRequestPermissionRationale()` 이 true 를 반환하지 않는 한 앱은 제목 텍스트가 있는 “알림 받기!”를 중간 화면에 표시할 필요가 없음

- 또는 사용자가 앱에 익숙해질 때까지 기다렸다가 표시할 수 있음
- 사용자가 앱을 4~5번째 실행할 때 표시하기

중간화면은 shouldShowRequestPermissionRationale() 이 true를 반환하는 경우에만 필요

## 컨텍스트내에서 권한 요청

- 사용자가 인지할 수 있는 컨텍스트 내에서 알림을 요청할 경우
- 이메일 앱에서 모든 새로운 알림의 옵션을 정할 때

## 앱에서 알림을 보낼 수 있는 지 확인

- 앱에서 알림을 보낼 수 있는 지 알림 활성화를 체크
- `areNotificationsEnabled()` 를 호출

## 권한을 책임감있게 사용

[](https://developer.android.com/develop/ui/views/notifications/notification-permission)
