# BadgeDrawable
<img src="https://user-images.githubusercontent.com/48344355/172030297-9389e139-ed27-4c07-93d2-acf660500272.png" width=30% />

보통 이 부분을 구현하기 위해서는 두 가지의 방법을 사용한다.
- BottomNavigationView인 경우, onDraw를 오버라이딩해서 특정 아이템에 배지를 보여줄 위치를 계산해서 그린다.
- 일반 View인 경우, Constraint Layout을 활용하여 ImageView와 TextView의 조합으로 그린다.

하지만 BadgeDrawable을 사용하면, 간단하게 배지를 그릴 수 있다.
# 일반 뷰에 적용해보기
## 1. FrameLayout 추가
```BadgeDrawable.java
  @Nullable private WeakReference<View> anchorViewRef;
  @Nullable private WeakReference<FrameLayout> customBadgeParentRef;
```
- 적용할 뷰를 FrameLayout으로 감싸야한다.
- anchorViewRef는 배지가 적용될 뷰를 의미하며, customBadgeParentRef는 배지가 적용될 뷰의 FrameLayout을 의미한다.

### 로직 살펴보기
```BadgeDrawable.java
  @IntDef({
    TOP_END,
    TOP_START,
    BOTTOM_END,
    BOTTOM_START,
  })
```
```BadgeDrawable.java
  public void setBadgeGravity(@BadgeGravity int gravity) {
    if (state.getBadgeGravity() != gravity) {
      state.setBadgeGravity(gravity);
      onBadgeGravityUpdated();
    }
  }
```
- setBadgeGravity 함수를 통해서 배지를 어디에 위치시킬지 설정한다.

---

```BadgeDrawable.java
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable FrameLayout customBadgeParent) {
    this.anchorViewRef = new WeakReference<>(anchorView);

    if (BadgeUtils.USE_COMPAT_PARENT && customBadgeParent == null) {
      tryWrapAnchorInCompatParent(anchorView);
    } else {
      this.customBadgeParentRef = new WeakReference<>(customBadgeParent);
    }
    if (!BadgeUtils.USE_COMPAT_PARENT) {
      updateAnchorParentToNotClip(anchorView);
    }
    updateCenterAndBounds();
    invalidateSelf();
  }
```

```BadgeDrawable.java
  private void tryWrapAnchorInCompatParent(final View anchorView) {
    ViewGroup anchorViewParent = (ViewGroup) anchorView.getParent();
    if ((anchorViewParent != null && anchorViewParent.getId() == R.id.mtrl_anchor_parent)
        || (customBadgeParentRef != null && customBadgeParentRef.get() == anchorViewParent)) {
      return;
    }
    // Must call this before wrapping the anchor in a FrameLayout.
    updateAnchorParentToNotClip(anchorView);

    // Create FrameLayout and configure it to wrap the anchor.
    final FrameLayout frameLayout = new FrameLayout(anchorView.getContext());
    frameLayout.setId(R.id.mtrl_anchor_parent);
    frameLayout.setClipChildren(false);
    frameLayout.setClipToPadding(false);
    frameLayout.setLayoutParams(anchorView.getLayoutParams());
    frameLayout.setMinimumWidth(anchorView.getWidth());
    frameLayout.setMinimumHeight(anchorView.getHeight());

    int anchorIndex = anchorViewParent.indexOfChild(anchorView);
    anchorViewParent.removeViewAt(anchorIndex);
    anchorView.setLayoutParams(
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    frameLayout.addView(anchorView);
    anchorViewParent.addView(frameLayout, anchorIndex);
    customBadgeParentRef = new WeakReference<>(frameLayout);

    // Update the badge's coordinates after the FrameLayout has been added to the view hierarchy and
    // has a size.
    frameLayout.post(
        new Runnable() {
          @Override
          public void run() {
            updateBadgeCoordinates(anchorView, frameLayout);
          }
        });
  }
```
- tryWrapAnchorInCompatParent 함수를 통해서, View를 기준으로 FrameLayout을 설정한다

---

```BadgeDrawable.java
  private void updateCenterAndBounds() {
    /* 중략 */

    calculateCenterAndBounds(context, anchorRect, anchorView);

    updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, halfBadgeHeight);
    
    /* 중략 */
  }
```
- BadgeGravity를 기준으로 배지를 어디에 그릴지(calculateCenterAndBounds)를 계산한다.

---

```BadgeDrawable.java
  private void calculateCenterAndBounds(
      @NonNull Context context, @NonNull Rect anchorRect, @NonNull View anchorView) {
    int totalVerticalOffset = getTotalVerticalOffsetForState();
    switch (state.getBadgeGravity()) {
      case BOTTOM_END:
      case BOTTOM_START:
        badgeCenterY = anchorRect.bottom - totalVerticalOffset;
        break;
      case TOP_END:
      case TOP_START:
      default:
        badgeCenterY = anchorRect.top + totalVerticalOffset;
        break;
    }

    /* 중략 */
    
    switch (state.getBadgeGravity()) {
      case BOTTOM_START:
      case TOP_START:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.left - halfBadgeWidth + inset + totalHorizontalOffset
                : anchorRect.right + halfBadgeWidth - inset - totalHorizontalOffset;
        break;
      case BOTTOM_END:
      case TOP_END:
      default:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.right + halfBadgeWidth - inset - totalHorizontalOffset
                : anchorRect.left - halfBadgeWidth + inset + totalHorizontalOffset;
        break;
    }
  }
```
- BadgeGravity를 기준으로 badgeCenterX와 badgeCenterY를 계산한다.

---

```BadgeUtils.java
  public static void updateBadgeBounds(
      @NonNull Rect rect, float centerX, float centerY, float halfWidth, float halfHeight) {
    rect.set(
        (int) (centerX - halfWidth),
        (int) (centerY - halfHeight),
        (int) (centerX + halfWidth),
        (int) (centerY + halfHeight));
  }
```
- badgeCenterX와 badgeCenterY값을 기준으로 rect를 그린다.

---

## 2. 배지 생성
```kotlin
val badge = BadgeDrawable.create(this)
```
## 3. 배지 속성 초기화
```kotlin
with(badge) {
    number = 5
    backgroundColor = getColor(R.color.black)
    badgeTextColor = getColor(R.color.white)
    badgeGravity = BadgeDrawable.TOP_END
}
```
- number : 배지에 표시할 숫자
- backgroundColor : 배지 배경색
- badgeTextColor : 배지 텍스트 색상
- badgeGravity : 배지 위치
## 4. 배지 적용
```kotlin
binding.flMain.foreground = badge
```
- BadgeDrawable을 FrameLayout의 foreground로 설정한다.
```kotlin
binding.flMain.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ -> 
    BadgeUtils.attachBadgeDrawable(badge, view)
}
```
- onLayoutChangeListener에서 해당 뷰에 배지를 attach한다.
## 결과
<img src="https://user-images.githubusercontent.com/48344355/172031271-341a5941-0e84-4007-87a4-f154921e26aa.png" width=30% />

# BottomNavigationView에 적용해보기
## 1. 배지 생성
```kotlin
val badge = binding.bnvMain.getOrCreateBadge(R.id.item_bottom_navigation_three)
```
- BottomNavigationView 자체에서 BadgeDrawable을 지원한다. (getOrCreateBadge)
- 별도의 FrameLayout을 추가할 필요가 없다.
## 2. 배지 속성 초기화 및 적용
- 일반 뷰에서 했던 것과 동일하다.
## 결과
<img src="https://user-images.githubusercontent.com/48344355/172031677-3e0df9da-3f2b-4868-ae3f-d369e4c49c1a.png" width=30% />
