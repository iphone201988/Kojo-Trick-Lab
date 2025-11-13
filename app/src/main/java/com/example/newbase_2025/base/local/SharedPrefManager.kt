package com.example.newbase_2025.base.local

import android.content.SharedPreferences
import com.example.newbase_2025.data.model.UserLoginData
import com.google.gson.Gson
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val IS_FIRST = "is_first"
        const val COMBO_LIST = "combo_list"
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

    internal inline fun <reified T> saveList(key: String, list: List<T>) {
        val gson = Gson()
        val json = gson.toJson(list)
        sharedPreferences.edit().putString(key, json).apply()
    }

    internal inline fun <reified T> getList(key: String): List<T> {
        val gson = Gson()
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearList(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}