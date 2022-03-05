# 1. Cycling List

- 자동으로 스크롤 되는 recycler view를 만들어보자
- 마지막에 도달 시 다시 처음으로 스크롤 된다.

아래 이미지와 같은 위젯을 만들어보는 것

![https://user-images.githubusercontent.com/10140528/147478366-e3d951d5-27c3-43da-8912-53c16d99eb83.gif](https://user-images.githubusercontent.com/10140528/147478366-e3d951d5-27c3-43da-8912-53c16d99eb83.gif)

```kotlin
override fun onTouchEvent(e: MotionEvent?): Boolean {
    if (e?.action == MotionEvent.ACTION_DOWN) {
        stopAutoScroll()
    } else if (e?.action == MotionEvent.ACTION_UP || e?.action == MotionEvent.ACTION_CANCEL) {
        adapter?.let { autoScroll(it.itemCount) }
    }
    return super.onTouchEvent(e)
}

fun autoScroll(itemSize: Int) {
    dispose?.let {
        if (!it.isDisposed) return
    }

    dispose = Observable.interval(2000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            smoothScrollToPosition(getNextPosition(itemSize))
        }
}

private fun getNextPosition(itemSize: Int): Int {
    val position = 
        linearLayoutManager.findFirstCompletelyVisibleItemPosition()

    return (position + 1) % itemSize
}

private fun stopAutoScroll() {
    dispose?.let(Disposable::dispose)
}
```

- autoScroll 은 Interval로 지정한 시간이 지날때 마다 smoothScrollToPosition을 호출
- findFirstCompletelyVisibleItemPosition 은 화면에 완벽하게 보이는 최상단 항목의 인덱스를 가져옴 (Horizontal 일 경우 제일 왼쪽 기준)
- 최상단으로 완벽히 보이는 position은 최초시 0으로 나오고 next 인덱스로 전달해 scroll을 진행한다.
- touch시에는 잠시 scroll 을 멈추게 하기 위해 stopAutoScroll을 호출
- stopAutoScroll은 Disposable을 dispose시키므로 동작을 멈춘다.

# 2. Sequencially Loop

이번에는 끝에 도달했을때 다시 처음으로 돌아가지 않고

계속해서 다음 아이템이 채워지는 방식으로 진행한다.

![SequenciallyLoopList](https://user-images.githubusercontent.com/10140528/156889643-39877f93-d097-4e71-9a37-127093497047.png)

## RecyclerView + ListAdapter

- 위의 이미지 처럼 계속 반복되는 스크롤 처럼 보이도록 해야됨
- ListAdapter는 DiffUtil로 변경된 항목만 처리하도록 하는 알고리즘을 포함.

```kotlin
class AutoScrollAdapter2: ListAdapter<PhotoVO, ImageViewHolder>(diffCallBack) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(R.layout.item_scroll_image, parent)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBindData(getItem(position))
    }

    companion object {
        val diffCallBack = object: DiffUtil.ItemCallback<PhotoVO>() {
            override fun areItemsTheSame(oldItem: PhotoVO, newItem: PhotoVO): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PhotoVO, newItem: PhotoVO): Boolean {
                return oldItem == newItem
            }

        }
    }
}
```

```kotlin
adapter.submitList(it)
lifecycleScope.launch { autoScrollImageList() }
```

- viewModel에서 전달해준 list를 adapter에 업데이트 해준다.
- 코루틴이 다음으로 시작되며 auto scroll이 동작한다.
- 이번에는 재귀함수를 통해 무한스크롤을 만든다.

> *RecyclerView는 하나의 아이템씩 이동하며 끝에 도달하기전에 화면에 중앙에 보이는 아이템을 제외하고 양끝에 보이는 아이템과 화면 밖에 있는 아이템갯수 만큼 threshold를 지정합니다.

threshold + 화면에 보이는 아이템의 position이 list 맨끝에 도달할 경우 스크롤 되었던 부분을 list 뒤에 붙입니다.*
> 

```kotlin
private tailrec suspend fun autoScrollImageList() {
    val adapter = (binding.rvImages2.adapter as AutoScrollAdapter2)
    val size: Int = adapter.currentList.size
    val layoutManager: LinearLayoutManager = (binding.rvImages2.layoutManager as LinearLayoutManager)
    val firstPosition = layoutManager.findFirstVisibleItemPosition()

    if (firstPosition + THRESHOLD < size) {
        binding.rvImages2.smoothScrollToPosition(getNextPosition(size, layoutManager))
    } else {

        if (firstPosition != RecyclerView.NO_POSITION) {
            val currentList = adapter.currentList
            val secondPart = currentList.subList(0, firstPosition)
            val firstPart = currentList.subList(firstPosition, currentList.size)
            adapter.submitList(firstPart + secondPart)
        }
    }
    delay(DELAY_BETWEEN_SCROLL_MS)
    autoScrollImageList()
}

const val DELAY_BETWEEN_SCROLL_MS = 2000L
const val THRESHOLD = 3
```

- 첫번째 파트는 list의 화면에 보이는 아이템 부터 list의 끝까지이므로 스크롤하여 앞으로 보여줄 아이템들 입니다.
- 두번째 파트는 이미 스크롤 된 아이템들 입니다. (list 맨끝에 붙을 예정)
- new list를 submitList 를 통해 adapter로 전달

<aside>
💡 여기서 diffUtil을 사용하는 ListAdapter를 사용한 이유가 됩니다.
새 List를 submit해도 list가 화면에 보이는 부분이 동일하기 때문에 화면에서 view의 업데이트가 없으며 화면에 보이지 않는 앞으로 스크롤 될 아이템들도 추가됨.

</aside>

![https://user-images.githubusercontent.com/10140528/156888639-1f652dad-ffac-41b6-b3e9-7bfb4fea8dba.gif](https://user-images.githubusercontent.com/10140528/156888639-1f652dad-ffac-41b6-b3e9-7bfb4fea8dba.gif)
