package com.kangyee.locationselector.locationselector.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationSelectorResultModel(
    @SerializedName("address")
    val address: String?,
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?
) : Serializable