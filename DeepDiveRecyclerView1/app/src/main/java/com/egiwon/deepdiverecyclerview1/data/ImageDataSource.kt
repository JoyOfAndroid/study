package com.egiwon.deepdiverecyclerview1.data

import com.egiwon.deepdiverecyclerview1.data.model.RandomImageResponse
import io.reactivex.Single

interface ImageDataSource {
    fun fetchRandomImages(count: Int): Single<List<RandomImageResponse>>
}
