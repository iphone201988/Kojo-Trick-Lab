package com.tech.kojo.data.api

import com.tech.kojo.base.local.SharedPrefManager
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService, private val sharedPrefManager: SharedPrefManager) :
    ApiHelper {

    override suspend fun apiForRawBody(request: HashMap<String, Any>,url:String): Response<JsonObject> {
        return apiService.apiForRawBody(request,url)
    }

    override suspend fun apiPostForRawBody(
        url: String,
        request: HashMap<String, Any>,
    ): Response<JsonObject> {
        return apiService.apiPostForRawBody(getTokenFromSPref(), url, request)
    }

    override suspend fun apiPostForRawQuery(
        url: String,
        request: HashMap<String, Any>,
    ): Response<JsonObject> {
        return apiService.apiPostForRawQuery(getTokenFromSPref(), url, request)
    }

    override suspend fun apiPostForToken(
        url: String,
    ): Response<JsonObject> {
        return apiService.apiPostForToken(getTokenFromSPref(), url)
    }

    override suspend fun apiForFormData(data: HashMap<String, Any>, url: String): Response<JsonObject> {
        return apiService.apiForFormData(data,url)
    }

    override suspend fun apiForFormDataPut(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForFormDataPut(data,url, getTokenFromSPref())
    }

    override suspend fun apiGetOutWithQuery(url:String): Response<JsonObject> {
        return apiService.apiGetOutWithQuery(url)
    }

    override suspend fun apiGetOnlyAuthToken(url: String): Response<JsonObject> {
        return apiService.apiGetOnlyAuthToken(url,getTokenFromSPref())
    }

    override suspend fun apiGetWithQuery(data: HashMap<String, Any>, url: String): Response<JsonObject> {
        return apiService.apiGetWithQuery(getTokenFromSPref(),url,data)
    }


    override suspend fun apiForPostMultipart(
        url: String,
        part: MultipartBody.Part?,
    ): Response<JsonObject> {
        return apiService.apiForPostMultipart(url,getTokenFromSPref(), part)
    }

    override suspend fun apiForMultipartPost(
        url: String,
        map: HashMap<String, RequestBody>?,
        part: MultipartBody.Part?
    ): Response<JsonObject> {
        return apiService.apiForMultipartPost(url,getTokenFromSPref(), map, part)
    }

    override suspend fun apiPutForRawBody(
        url: String,
        map: HashMap<String, Any>,
    ): Response<JsonObject> {
        return apiService.apiPutForRawBody(url,getTokenFromSPref(), map)
    }

    private fun getTokenFromSPref(): String {
        return "Bearer ${
            sharedPrefManager.getToken()
        }"
    }
//    private fun getTokenFromSPref(): String {
//        return "Bearer ${"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiZTIwMGQzMDNiNmMyYmI3MThiMTQzMzY5MzYyMWJlM2I5NDA0NmI1ODMyNGJhYTIwMmE0OGVlZmEyNDk4ZjIwMGZkOGI3YjY3Nzg2MzVjOTYiLCJpYXQiOjE3NjI5MzIzOTkuMzIyNTU1MDY1MTU1MDI5Mjk2ODc1LCJuYmYiOjE3NjI5MzIzOTkuMzIyNTU2OTcyNTAzNjYyMTA5Mzc1LCJleHAiOjE3OTQ0NjgzOTkuMzIxMDUyMDc0NDMyMzczMDQ2ODc1LCJzdWIiOiIxMTgiLCJzY29wZXMiOltdfQ.faDCrUL4skbEsEAVWyi_YqDEL0YcoZWqXNEBS61SN15rqRXhL_KcBs2jndP5FcqnxHkEqVHDObnZVXGmNfTXmIMFU4gVkzE66b_NdzNynDgihh3iK2h1rIFGP2oeZSpzni_NiTwrw9pZXCFJ1HDTch2CfgVaZLK58xnO0sFuka5NJwzt59ByX-PFnPpz0igZJ8BcAMd1fULzs2qS3kl4aFwF5PbSEyhH-4DDYPZ1yJj_ukwhA9q0dST_BWj8gR2p7O_RpjDzIBDbkKhJI3HEx5StzXtI2icJpdM1ZcFuNOuEYyrJmowOvUl_0ZixYUD6yNVIUAf0416OQ1atO8uO1orGy1WyQXe-icUbUklLefyK3XVEkzAK9VPLUhLmXXSlr9HB01iuR4OlkdzIoG5WLXaOGF-MjUitZa_bvdFZ3weqJAuRXHZw7nUPR3sJfliRazT46q_CKkf4D-wCMHoNCOnmn3YUaFysq1tG-X6HhTN3r79VKTYdHSYhs87NUkp9cBA6r1HQN8zZ-wFa0DjwkUDIa_CuOdVY60zbc6-kc_KeZSDFwBHEZ0bOLjLMGkwkKo61n86k3NSpLlIxXtB8iGKnUaZe2GGlfq17nGoixW9apE2YVfZEaXAGZK_StxsPIq8GR1f0n1GQPM8dpk8-4yJnYeQ_ACvbnxIFzw2jqx4"}"
//    }

}