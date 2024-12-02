package com.example.pennywise.api

import retrofit2.http.GET

// Define the endpoint and expected response
interface ApiService {
    @GET("latest/USD")
    suspend fun getExchangeRates(): ApiResponse
}

// Model for API response
data class ApiResponse(val rates: Map<String, Double>)
