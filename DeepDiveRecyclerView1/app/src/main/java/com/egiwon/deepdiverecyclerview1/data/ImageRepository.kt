package com.egiwon.deepdiverecyclerview1.data

import com.egiwon.deepdiverecyclerview1.ui.model.Photo
import io.reactivex.Single

interface ImageRepository {
    fun fetchRandomImage(count: Int): Single<List<Photo>>
}
