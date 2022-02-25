package com.egiwon.deepdiverecyclerview1.data.service

import com.egiwon.deepdiverecyclerview1.data.model.RandomImageResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomImageService {
    @GET("/photos/random")
    fun fetchRandomImage(
        @Query("count") count: Int
    ): Single<List<RandomImageResponse>>
}
