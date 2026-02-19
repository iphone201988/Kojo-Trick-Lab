package com.tech.kojo.base.local

import android.content.SharedPreferences
import com.tech.kojo.data.model.LoginUser
import com.tech.kojo.data.model.ProfileUser
import com.google.gson.Gson
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val LOGIN_DATA = "loginData"
        const val PROFILE_DATA = "profileData"
        const val COMBO_LIST = "combo_list"
        const val TOKEN = "token"
        const val ON_BOARDING = "onboarding"
    }

    fun setLoginData(isFirst: LoginUser?) {
        val gson = Gson()
        val json = gson.toJson(isFirst)
        val editor = sharedPreferences.edit()
        editor.putString(KEY.LOGIN_DATA, json)
        editor.apply()
    }

    fun getLoginData(): LoginUser? {
        val json = sharedPreferences.getString(KEY.LOGIN_DATA, null)

        if (json.isNullOrEmpty()) return null

        return try {
            Gson().fromJson(json, LoginUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun setToken(isFirst: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY.TOKEN, isFirst)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY.TOKEN, "")
    }

    fun setOnBoarding(onBoarding: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY.ON_BOARDING, onBoarding)
        editor.apply()
    }

    fun getOnBoarding(): String? {
        return sharedPreferences.getString(KEY.ON_BOARDING, "")
    }


    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}