package com.egiwon.deepdiverecyclerview1.data

import com.egiwon.deepdiverecyclerview1.data.model.RandomImageResponse
import com.egiwon.deepdiverecyclerview1.ui.model.Photo
import io.reactivex.Single
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageDataSource: ImageDataSource
) : ImageRepository {
    override fun fetchRandomImage(count: Int): Single<List<Photo>> {
        return imageDataSource.fetchRandomImages(count).map { list ->
            list.map(RandomImageResponse::toViewObject)
        }
    }
}
