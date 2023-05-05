package com.kangyee.locationselector.locationselector.model


import com.google.gson.annotations.SerializedName

data class LocationSelectorResultModel(
    @SerializedName("address")
    val address: String?,
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?
)