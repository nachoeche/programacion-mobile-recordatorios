package com.ignacio.recordatorios.data.remote

import retrofit2.http.*

// JSONPlaceholder: servicio publico de DEMO. Las operaciones de escritura son simuladas.
data class PostDto(val id: Int? = null, val userId: Int = 1, val title: String, val body: String)

interface RemoteService {
    @GET("posts") suspend fun list(): List<PostDto>
    @POST("posts") suspend fun create(@Header("X-Idempotency-Key") key: String,
                                      @Body post: PostDto): PostDto
    @PUT("posts/{id}") suspend fun update(@Path("id") id: Int,
                        @Header("X-Idempotency-Key") key: String, @Body post: PostDto): PostDto
    @DELETE("posts/{id}") suspend fun delete(@Path("id") id: Int,
                         @Header("X-Idempotency-Key") key: String): retrofit2.Response<Unit>
}
