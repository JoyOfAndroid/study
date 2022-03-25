안드로이드 UI 개발하다보면 ImageView 모양을 변경해서 노출해야하는 경우가 있다.

예를 들면 Circle Shape, Round 한 사각형등...

> ***왜냐하면 ImageView 기본 각진 사각형 모양은 디자인 적으로 지루하며 다른 앱과 차별성이 없으며 세련되지 않기 때문이다.***
> 

<img src="https://user-images.githubusercontent.com/10140528/159168580-ff3bf165-c61a-4123-9e15-eb68107eac57.png" width=30% />

위 이미지에서 확인시 circle type, round rectangle type이 보인다. 

이런 모양으로 이미지를 보여줘야 할 때 마다 항상 커스텀 뷰로 생성하거나 CardView로 감싸거나

이러한 구현이 미리 되어 있는 라이브러리를 만들어서 로드시키거나 해야한다.

[https://github.com/hdodenhof/CircleImageView](https://github.com/hdodenhof/CircleImageView)

이러한 작업은 성가시기 때문에 개발 퍼포먼스에 도움이 되지 않는다. 

구글 Android 팀에서는 우리의 고민을 알아챘는지 Material Design Component에 ShapeableImageView를 만들어 냈다.

# ShapeableImageView

```kotlin
implementation 'com.google.android.material:material:1.2.0'
```

1.2.0 버전에서 지원이 되며 AppCompatImageView 를 확장하여 만들어 졌다.

## Attribute

- strokeColor
- strokeWidth
- CornerFamily
    - Cut
    - Rounded
    
- CornerFamilyTopLeft
- CornerFamilyTopRight
- CornerFamilyBottomLeft
- CornerFamilyBottomRight
    - 개별적으로 한쪽 모서리의 모양을 바꿀 수 있다.
    - CornerFamily 처럼 enum값으로 설정한다 (Cut, Rounded)
    
- CornerSize
    - dp 값 또는 %값으로 모서리 모양의 크기를 처리할 값을 넣어주면 된다.
    - example :
        
        ```kotlin
        <item name="cornerSize">50%</item>
        <item name="cornerSize">8dp</item>
        ```
        
- CornerSizeTopLeft
- CornerSizeTopRight
- CornerSizeBottomLeft
- CornerSizeBottomRight
    - 개별적으로 한쪽 모서리 모양의 크기를 변경 할 수 있다

## 대표적으로 많이 사용될 모양

### Circle Image

```kotlin
// styles/circle.xml
<style name="Circle">
    <item name="cornerSize">50%</item>
</style>
```

circle은 cornerSize를 50% 정도 넣어준다.

```kotlin
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/iv_circle"
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:layout_margin="16dp"
    android:padding="5dp"
    android:scaleType="centerCrop"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:shapeAppearanceOverlay="@style/circle" />
```

<img src="https://user-images.githubusercontent.com/10140528/159168716-7f38c387-b3a3-44ae-a53b-7ace7aa9cb28.png" width=20% />


### Circle ImageView With Border

```kotlin
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/iv_circle_with_border"
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:layout_marginStart="16dp"
    android:padding="5dp"
    android:scaleType="centerCrop"
    app:layout_constraintStart_toEndOf="@id/iv_circle"
    app:layout_constraintTop_toTopOf="@id/iv_circle"
    app:shapeAppearanceOverlay="@style/circle"
    app:strokeColor="#4d4d4d"
    app:strokeWidth="5dp" />
```

<img src="https://user-images.githubusercontent.com/10140528/159168724-69050db0-9058-4fae-8818-c38fd096c726.png" width=20% />


### Rounded Image

```kotlin
// styles/rounded_corner.xml
<style name="rounded_corner">
    <item name="cornerSize">10dp</item>
</style>
```

```kotlin
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/iv_rounded"
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:layout_margin="16dp"
    android:padding="5dp"
    android:scaleType="centerCrop"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/iv_circle_with_border"
    app:shapeAppearanceOverlay="@style/rounded_corner" />
```

<img src="https://user-images.githubusercontent.com/10140528/159168742-3b231727-d70a-446f-b481-d9be14ef7858.png" width=20% />

### Rounded Image With Border

```kotlin
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/iv_rounded_with_border"
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:layout_marginStart="16dp"
    android:padding="5dp"
    android:scaleType="centerCrop"
    app:layout_constraintStart_toEndOf="@id/iv_rounded"
    app:layout_constraintTop_toTopOf="@id/iv_rounded"
    app:shapeAppearanceOverlay="@style/rounded_corner"
    app:strokeColor="#4d4d4d"
    app:strokeWidth="5dp" />
```

<img src="https://user-images.githubusercontent.com/10140528/159168751-68936f53-8c84-451f-93e5-457032bc62ff.png" width=20% />


## 코드로 적용

```kotlin
binding.ivTopRounded.shapeAppearanceModel = binding.ivTopRounded.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, dpToPx(10f))
            .setTopLeftCorner(CornerFamily.ROUNDED, dpToPx(10f))
            .build()
```

<img src="https://user-images.githubusercontent.com/10140528/159168775-69324255-a7f8-43f0-98a4-a945edac9882.png" width=20% />


## 어떻게 라운딩 처리를 할까

### Circle

0 → 90 → 180 → 270도로 arc(호)를 그린다. 

그릴때 matrix rotation을 지정해주고 corner 의 좌표를 지정해 지정된 좌표를 저장했다가 path를 그릴때 

정보들을 가져와서 그리게 된다.

저장되는 순서는 다음과 같다.

- 0 → top right
- 1 → bottom right
- 2 → bottom left
- 3 → top left

- Style에 dp 값이 아닌 % 값으로 적용했을경우 RelativeCornerSize에서 값을 가져온다.
- circle shape를 그릴때는 dp값이 아닌 % 값으로 지정해야된다. (50%)

```java
@Override
public float getCornerSize(@NonNull RectF bounds) {
  return percent * bounds.height();
}
```

- CornerTreatment는 RoundedCornerTreatment로 적용된다.

```java
@NonNull
static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily) {
  switch (cornerFamily) {
    case CornerFamily.ROUNDED:
      return new RoundedCornerTreatment();
    case CornerFamily.CUT:
      return new CutCornerTreatment();
    default:
      return createDefaultCornerTreatment();
  }
}

@NonNull
static CornerTreatment createDefaultCornerTreatment() {
  return new RoundedCornerTreatment();
}
```

ShapeAppearancePathProvider에서는 corner를 rounding 처리할 각 정보들을 셋팅하는 작업을 한다.

여기에는 ShapePath에 각종 필요한 값들을 셋팅하는데 top right, bottom right, bottom left, top left의 PathOperation 클래스가 있다. 

```java
private void setCornerPathAndTransform(@NonNull ShapeAppearancePathSpec spec, int index) {
  CornerSize size = getCornerSizeForIndex(index, spec.shapeAppearanceModel);
  getCornerTreatmentForIndex(index, spec.shapeAppearanceModel)
      .getCornerPath(cornerPaths[index], 90, spec.interpolation, spec.bounds, size);

  float edgeAngle = angleOfEdge(index);
  cornerTransforms[index].reset();
  getCoordinatesOfCorner(index, spec.bounds, pointF);
  cornerTransforms[index].setTranslate(pointF.x, pointF.y);
  cornerTransforms[index].preRotate(edgeAngle);
}
```

여기서 PathOperation 클래스는 PathArcOperation으로 지정된다. 

```java
public void addArc(
  float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
PathArcOperation operation = new PathArcOperation(left, top, right, bottom);
operation.setStartAngle(startAngle);
operation.setSweepAngle(sweepAngle);
operations.add(operation);

ArcShadowOperation arcShadowOperation = new ArcShadowOperation(operation);
float endAngle = startAngle + sweepAngle;
// Flip the startAngle and endAngle when drawing the shadow inside the bounds. They represent
// the angles from the center of the circle to the start or end of the arc, respectively. When
// the shadow is drawn inside the arc, it is going the opposite direction.
boolean drawShadowInsideBounds = sweepAngle < 0;
addShadowCompatOperation(
    arcShadowOperation,
    drawShadowInsideBounds ? (180 + startAngle) % 360 : startAngle,
    drawShadowInsideBounds ? (180 + endAngle) % 360 : endAngle);

setEndX(
    (left + right) * 0.5f
        + (right - left) / 2 * (float) Math.cos(Math.toRadians(startAngle + sweepAngle)));
setEndY(
    (top + bottom) * 0.5f
        + (bottom - top) / 2 * (float) Math.sin(Math.toRadians(startAngle + sweepAngle)));
}
```

PathArcOperation은 startAngle과 SweepAngle을 지정하는데

startAngle은 시작 각도이고

sweepAngle은 스윕(호의 각도)이다.

![example2](https://user-images.githubusercontent.com/10140528/159168789-55767096-abdd-4e57-b6b4-09ae27eaef9d.png)


Circle을 그릴때는 시작 좌표와 끝 좌표를 지정하고 startAngle과 sweepAngle은 항상 180, 90으로 지정된다.

그리는 각도는 동일하고 좌표의 조정으로 호를 그리게 된다.

그 이후로는 호의 좌표를 matrix이 지정하고 matrix에 angle값을 저장한다.

아래 코드는 PathArcOperation에서 path에 Arc를 적용해줍니다.

```java
@Override
public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
  Matrix inverse = matrix;
  transform.invert(inverse);
  path.transform(inverse);
  rectF.set(getLeft(), getTop(), getRight(), getBottom());
  path.arcTo(rectF, getStartAngle(), getSweepAngle(), false);
  path.transform(transform);
}
```

여기서 역행렬을 주는 이유는 부채꼴 모양의 각도가 항상 좌표에 의해 바뀌기 때문이다.

원을 4등분 했을때 시작 위치에서 끝위치로 호를 그리고

역행렬을 주어서 끝위치를 시작위치처럼 바꾸고 그 위치에서 지정된 end 좌표까지 호를 그리기 때문이다.

<aside>
💡 설명이 어려운데 한마디로 피자를 4등분해서 1조각 피자를 빼고 피자판을 돌리고 1조각 피자를 빼고 또 피자판을 돌리고 피자를 빼는 형식이다

</aside>

### Round

- Rounded Corner인 경우 CornerSize는 AbsoluteCornerSize로 셋팅된다.

```java
/** Sets the top left corner size for the current corner. */
@NonNull
public Builder setTopLeftCornerSize(@Dimension float cornerSize) {
  topLeftCornerSize = new AbsoluteCornerSize(cornerSize);
  return this;
}
```

- CornerTreatment는 RoundedCornerTreatment로 적용된다.

```java
@NonNull
static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily) {
  switch (cornerFamily) {
    case CornerFamily.ROUNDED:
      return new RoundedCornerTreatment();
    case CornerFamily.CUT:
      return new CutCornerTreatment();
    default:
      return createDefaultCornerTreatment();
  }
}

@NonNull
static CornerTreatment createDefaultCornerTreatment() {
  return new RoundedCornerTreatment();
}
```

나머지 작업은 동일하고 arc 의 위치 좌표에서 각도에 따라 호를 그려주는 정도가 차이가 있게 된다.

![example3](https://user-images.githubusercontent.com/10140528/159168802-82f7b346-1cc0-4e79-81bd-ad79edc27946.png)


circle을 그려주는 방식과 동일하지만 위의 그림처럼 호를 그려주며 사각형 버튼을 만들게 된다.
