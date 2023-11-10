package com.example.maps.model

import java.io.InputStream

data class CachedWebResourceResponse(
    val data: InputStream? = null,
    val mimeType: String = "image/png",
    val encoding: String = "UTF-8",
    val headers: HashMap<String,String>? = null
)
