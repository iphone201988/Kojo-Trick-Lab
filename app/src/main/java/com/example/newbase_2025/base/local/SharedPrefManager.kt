package com.example.newbase_2025.base.local

import android.content.SharedPreferences
import com.example.newbase_2025.data.model.LoginUser
import com.example.newbase_2025.data.model.ProfileUser
import com.google.gson.Gson
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val LOGIN_DATA = "loginData"
        const val PROFILE_DATA = "profileData"
        const val COMBO_LIST = "combo_list"
        const val TOKEN = "token"
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




    fun setProfileData(isFirst: ProfileUser?) {
        val gson = Gson()
        val json = gson.toJson(isFirst)
        val editor = sharedPreferences.edit()
        editor.putString(KEY.PROFILE_DATA, json)
        editor.apply()
    }



    fun getProfileData(): ProfileUser? {
        val gson = Gson()
        val json: String? = sharedPreferences.getString(KEY.PROFILE_DATA, null)
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, ProfileUser::class.java)
        } else {
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


    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}