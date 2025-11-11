package com.example.newbase_2025.base.local

import android.content.SharedPreferences
import com.example.newbase_2025.data.model.UserLoginData
import com.google.gson.Gson
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val IS_FIRST = "is_first"
    }

    fun setLoginData(isFirst: UserLoginData) {
        val gson = Gson()
        val json = gson.toJson(isFirst)
        val editor = sharedPreferences.edit()
        editor.putString(KEY.IS_FIRST, json)
        editor.apply()
    }

    fun getLoginData(): UserLoginData {
        val gson = Gson()
        val json: String? = sharedPreferences.getString(KEY.IS_FIRST, "")
        val obj: UserLoginData = gson.fromJson(json, UserLoginData::class.java)
        return obj
    }

    fun setToken(isFirst: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY.IS_FIRST, isFirst)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY.IS_FIRST, "")
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}