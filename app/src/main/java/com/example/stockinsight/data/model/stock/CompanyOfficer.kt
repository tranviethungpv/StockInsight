package com.example.stockinsight.data.model.stock

import com.google.gson.annotations.SerializedName

data class CompanyOfficer(
    @SerializedName("age") val age: Int?,
    @SerializedName("exercisedValue") val exercisedValue: Int,
    @SerializedName("fiscalYear") val fiscalYear: Int,
    @SerializedName("maxAge") val maxAge: Int,
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String,
    @SerializedName("totalPay") val totalPay: Long,
    @SerializedName("unexercisedValue") val unexercisedValue: Long,
    @SerializedName("yearBorn") val yearBorn: Int?
)