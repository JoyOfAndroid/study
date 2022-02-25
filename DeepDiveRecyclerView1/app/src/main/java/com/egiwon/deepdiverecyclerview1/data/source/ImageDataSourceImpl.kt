package com.egiwon.deepdiverecyclerview1.data.source

import com.egiwon.deepdiverecyclerview1.data.ImageDataSource
import com.egiwon.deepdiverecyclerview1.data.model.RandomImageResponse
import com.egiwon.deepdiverecyclerview1.data.service.RandomImageService
import io.reactivex.Single
import javax.inject.Inject

class ImageDataSourceImpl @Inject constructor(
    private val randomImageService: RandomImageService
) : ImageDataSource {
    override fun fetchRandomImages(count: Int): Single<List<RandomImageResponse>> {
        return randomImageService.fetchRandomImage(count)
    }
}
