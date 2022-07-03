package com.dragos.challenge.data.source.api

import com.google.gson.annotations.SerializedName

data class ImageSearchResult(
    @SerializedName("photos")
    val result: SearchResult
)

data class SearchResult(
    @SerializedName("photo")
    val images: List<Image>
)