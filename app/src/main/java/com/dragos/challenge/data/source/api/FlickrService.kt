package com.dragos.challenge.data.source.api

import com.dragos.challenge.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrService {
    @GET("services/rest/")
    suspend fun search(
        @Query("method") method: String = "flickr.photos.search",
        @Query("format") format: String = "json",
        @Query("privacy_filter") privacyFilter: String = "1",
        @Query("nojsoncallback") noJsonCallback: String = "1",
        @Query("api_key") apiKey: String = BuildConfig.FLICKR_API,
        @Query("safe_search") safeSearch: String = "1",
        @Query("lat") lat: Double,
        @Query("lon") lng: Double,
        @Query("radius") radius: Double = 1.0,
        @Query("per_page") perPage: Int = 1,
        @Query("extras") extras: String = "url_w"
    ): ImageSearchResult

    @GET
    @Headers(
        "Host: live.staticflickr.com",
        "User-Agent: curl/7.79.1",
        "Accept: */*"
    )
    suspend fun downloadImage(@Url url: String): Response<ResponseBody>

}