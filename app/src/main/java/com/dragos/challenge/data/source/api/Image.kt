package com.dragos.challenge.data.source.api

import com.google.gson.annotations.SerializedName

data class Image(
        val id: String,
        val owner: String,
        val secret: String,
        val server: String,
        @SerializedName("url_w")
        val url: String? = null
    )