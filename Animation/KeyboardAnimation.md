# 키보드 애니메이션

## 배경
### Demo
![1_9wELaIXfyzEQrHlsU2jkug](https://user-images.githubusercontent.com/36095102/154826071-246cef97-c2b3-452a-a655-68ef5a4b8327.gif)
- Android 10 이하에서는 키보드 영역이 먼저 계산되서 올라가고, 그 이후에 키보드가 올라가는 애니메이션 동작
- 어떻게 하면 키보드와 함께 자연스럽게 올라가도록 할 수 있을까?

## 구현해보기
### 1. 키패드 영역을 애니메이션 가능하도록 설정하기
#### 1. 최상단 레이아웃을 전체 화면으로 설정하기
```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
```
```WindowCompat.java
static void setDecorFitsSystemWindows(@NonNull Window window,
        final boolean decorFitsSystemWindows) {
    final int decorFitsFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    final View decorView = window.getDecorView();
    final int sysUiVis = decorView.getSystemUiVisibility();
    decorView.setSystemUiVisibility(decorFitsSystemWindows
            ? sysUiVis & ~decorFitsFlags
            : sysUiVis | decorFitsFlags);
}
```
#### 2. 키패드 영역만큼 패딩값 적용하기 (OnApplyWindowInsetsListener)
```kotlin
private var view: View? = null
private var lastWindowInsets: WindowInsetsCompat? = null

private var deferredInsets = false

override fun onApplyWindowInsets(
    v: View,
    windowInsets: WindowInsetsCompat
): WindowInsetsCompat {
    // 뷰와 insets 정보 저장
    view = v
    lastWindowInsets = windowInsets
    
    // 키패드 영역만큼 패딩 영역 설정
    val typeInsets = windowInsets.getInsets(types)
    v.setPadding(typeInsets.left, typeInsets.top, typeInsets.right, typeInsets.bottom)
    
    // insets이 뷰 계층구조로 더이상 dispatch되지 않도록 함
    return WindowInsetsCompat.CONSUMED
}
```
```kotlin
ViewCompat.setOnApplyWindowInsetsListener(binding.root, deferringInsetsListener)
```
#### 3. 키패드 영역 애니메이션 이벤트 수신하기 (WindowInsetsAnimationCompat.Callback)
- DISPATCH_MODE_STOP (뷰 계층구조로 이벤트 전달하지 않음)
- DISPATCH_MODE_CONTINUE_ON_SUBTREE (뷰 계층구조로 이벤트 전달)
```kotlin
override fun onPrepare(animation: WindowInsetsAnimationCompat) {
    if (animation.typeMask and deferredInsetTypes != 0) {
        // 키패드가 보이지 않는 경우에는 ime insets을 지연시킴
        // 즉, 키패드가 보이지 않는 경우에는 SystemBar inset만 적용하여, 스크롤뷰 크기를 크게 유지함
        deferredInsets = true
    }
}

override fun onEnd(animation: WindowInsetsAnimationCompat) {
    if (deferredInsets && (animation.typeMask and deferredInsetTypes) != 0) {
        deferredInsets = false
        
        // 애니메이션이 동작했던 상황이라면, 해당 리소스 해제
        if (lastWindowInsets != null && view != null) {
            ViewCompat.dispatchApplyWindowInsets(view!!, lastWindowInsets!!)
        }
    }
}
```
```kotlin
ViewCompat.setWindowInsetsAnimationCallback(binding.root, deferringInsetsListener)
```
### 2. 키패드에 애니메이션 적용하기
#### 키패드 영역 Translate 애니메이션 적용하기
```kotlin
override fun onProgress(
    insets: WindowInsetsCompat,
    runningAnimations: List<WindowInsetsAnimationCompat>
): WindowInsetsCompat {
    
    val typesInset = insets.getInsets(deferredInsetTypes) // 키패드
    val otherInset = insets.getInsets(persistentInsetTypes) // 시스템바

    val diff = Insets.subtract(typesInset, otherInset).let {
        Insets.max(it, Insets.NONE)
    }

    view.translationX = (diff.left - diff.right).toFloat()
    view.translationY = (diff.top - diff.bottom).toFloat()

    return insets
}
```
```kotlin
override fun onEnd(animation: WindowInsetsAnimationCompat) {
    // 애니메이션이 끝나면 translation 값 초기화
    view.translationX = 0f
    view.translationY = 0f
}
```
```kotlin
ViewCompat.setWindowInsetsAnimationCallback(
    binding.messageHolder,
    TranslateDeferringInsetsAnimationCallback(
        view = binding.messageHolder,
        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
        // 자식뷰가 있는 경우에만 추가
        dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
    )
)
```

## 참고 URL
- https://medium.com/androiddevelopers/animating-your-keyboard-reacting-to-inset-animations-839be3d4c31b
- https://github.com/android/user-interface-samples/tree/main/WindowInsetsAnimation