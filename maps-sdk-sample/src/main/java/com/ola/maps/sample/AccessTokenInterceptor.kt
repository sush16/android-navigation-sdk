package com.ola.maps.sample

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AccessTokenInterceptor : Interceptor {

    private val TAG = "AccessTokenInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response

        // you will get Access token from the API call using Project ID and Secrets Id
        var accessToken = "<Access-Token>"
        response = chain.proceed(newRequestWithAccessToken(accessToken, request))

        return response
    }

    private fun newRequestWithAccessToken(accessToken: String?, request: Request) =
        request.newBuilder().header("Authorization", "Bearer $accessToken").build()
}