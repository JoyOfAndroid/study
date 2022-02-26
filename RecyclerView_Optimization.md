# RecyclerView Optimisations

## ViewHolder 라이프싸이클

- **onCreateViewHolder :**
    1. 특정 타입의 View에 대한 새 ViewHolder가 필요할 때 RecyclerView가 제공하는 곳. ****
    2. onClickListener 설정과 같은 초기화 관련 작업은 여기에서 수행
    
- **onBindViewHolder()** :
    1. 여기서 데이터가 뷰에 attach
    2. ViewHolder는 재활용 되므로 동일한 ViewHolder가 다른 데이터와 함께 사용됨.
    3. 이 함수가 호출될 때마다 데이터를 업데이트 할 수 있음.

- **onViewAttachedToWindow()** :
    1. ViewHolder가 화면에 나올때마다 이 콜백이 실행됨.
    2. View가 화면에 나올 때 Video 또는 Audio 재생과 같은 사용자 지향 이벤트는 여기서 수행.

- **onViewDetachedFromWindow() :**
    1. ViewHolder가 화면을 벗어나면 호출.
    2. Video 및 Audio 또는 기타 메모리 이벤트를 일시 중지할 때 사용.

- **onViewRecycled() :**
    1. ViewHolder가 성공적으로 Recycled 되면 onViewRecycled가 호출
    2. ViewHolder가 보유한 리소스를 해제 할 때 사용

## 중첩된 RecyclerView에 대한 공통 RecyclerViewPool 사용.

### 문제점

- VerticalRecyclerView 내부에 Horizontal RecyclerView를 중첩하는 경우
- Google Play Store 가 이런 형태
- 이러한 하위 RecyclerVIew는 여러번 공통 ViewType을 공유
- 각 개별 RecyclerView가 다른 RecyclerView의 RecycledViewPool 내의 메모리에 RecycledView가 이미 있는지 알 수 있는 방법이 없음
- 이러한 공통 ViewType에 대한 추가 RecycledView를 자체 RecycledViewPool에 만듬.
- 하위 RecyclerView 각각에 공통 RecycledViewPool을 사용하여 적용하는 방법이 있음.

### 해결 방법

- RecyclerView 내부에 static 클래스인 RecycledViewPool 클래스의 인스턴스를 생성
- code

```kotlin
RecyclerView.setRecycledViewPool(mRecycledViewPool)
```

- 위의 코드를 호출하여 자식 RecyclerView가 각각 공통의 RecycledViewPool을 설정

## StableIds 사용

### 문제점

- RecyclerView에서 몇몇개의 데이터 셋을 변경하고서
- Adapter에서 notifyDataSetChanged()를 호출 하는 경우가 있음.
- 이렇게 하면 RecyclerView 전체가 깜빡임.
- ViewHolders의 내부에 이미지가 있는 경우 더 눈에 띔.
- Adapter 전체 데이터 셋와 RecyclerViewPool에 있는 것을 전부 무효화하고
- 모두 다시 생성

### 해결 방법

- StableIds를 사용하여 피할 수 있음.
- 이들은 어댑터 내부의 특정 데이터 항목을 나타내는 고유 Id
- 기본적으로 RecyclerViewAdapters에서는 StableId가 비활성화 되어 있음.
- 그래서 notifyDataSetChanged() 를 호출하면 Adapter가 각 뷰를 다시 생성함.
- StableIds를 활성화 하면 어댑터는 무효화 할 항목과 유지할 항목을 인식하여 깜빡임을 방지.
- adapter에서 `setHasStableIds(true)` 를 호출
- `getItemId(position: Int)`를 override하여 각 데이터 항목을 나타내는 고유한 ID값을 반환.
- 아니면 HashCode를 반환

## onBindViewHolder의 다른 것(이웃사촌)

- RecyclerVIew는 부분 및 전체 바인딩의 개념을 따름
- override 함수를 보면 onBindViewHolder에 PayLoad라는 객체 인수가 있는 onBindViewHolder를 발견할 수 있음
- code

```kotlin
onBindViewHolder(viewHolder: ViewHolder, position: Int, payloads: List<Any>)
```

- RecyclerViewAdapter는 항목 위치와 함께 페이로드 객체를 보낼 수 있는 notifyItemChanged(object: Any)  메서드를 제공.
- 아래의 코드를 호출 하기 전에 Adapter는 위의 payload가 있는 메서드를 호출

```kotlin
onBindViewHolder(viewHolder: ViewHolder, position: Int)
```
- list가 비었는 지 여부를 확인 할 수 있음
- payload 객체가 전송되었는지 여부를 확인 할 수 있음
- 변경된 ViewHolder만 업데이트 할 수 있음
- 변경되지 않으면 super의 다른 onBindViewHolder를 호출

### RecycledView

- RecyclerView가 ViewPool에서 Recycle 된 View를 선택한다고 생각하지만...
- RecyclerView가 ViewHolder를 찾기 위해 ViewPool을 참조하기 전에는  Scrap, HiddenView, ViewCache, ViewCacheExtension 과 같은 여러 가지의 캐시에 접근함.
- 여기서 주목할 것은 ViewCache~!!!!!

### ViewCache

- 기본 크기가 2
- ViewHolder가 재활용 되면 기본 크기가 2 인 ViewCache로 푸쉬되어 내부에 최대 2개의 뷰를 보유 할 수 있음.
- 그 후, ViewHolder를 ViewHolder 스택처럼 작동하는 ViewPool에 푸쉬

### ViewCache와 ViewPool의 차이점

#### ViewCache

- ViewCache의 ViewHolders는 연결된 위치를 포함하여 일부 상태를 유지 하기 때문에
- 다시 사용할 때 inflate하여 그대로 사용.

#### ViewPool

- 일부 상태만 유지
- 재사용할 때는 무효화를 하고 Re bind함.

### 사용법

- setItemViewCacheSize() 메서드를 사용하여 RecyclerView에 대한 사용자 지정 ViewCache크기를 설정할 수 있음
- 위 아래로 스크롤이 자주 발생하는 경우 ViewCache의 ViewHolder가 상태를 유지하고 그대로 infllate할 수 있음.
- ViewCache크기를 크게 설정하면 Re binding을 하지 않기 때문에 부드럽게 스크롤 할 수 있음.
