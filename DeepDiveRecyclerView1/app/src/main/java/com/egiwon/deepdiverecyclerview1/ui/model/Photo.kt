package com.egiwon.deepdiverecyclerview1.ui.model

data class Photo(
    val id: String = "",
    val likes: Int = 0,
    val fullImageUrl: String = "",
    val regularImageUrl: String = "",
    val smallImageUrl: String = "",
    val thumb: String = "",
    val views: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val selected: Boolean = false
)
