package com.tech.kojo.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tech.kojo.data.model.AuthModel

class AuthSharedPreferenceHelper(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("MyPrefsAuth", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    // Save HashSet of AuthModel to SharedPreferences
    fun saveAuthList(key: String, list: HashSet<AuthModel>) {
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    // Retrieve HashSet of AuthModel from SharedPreferences
    fun getAuthList(key: String): HashSet<AuthModel> {
        val gson = Gson()
        val json = sharedPreferences.getString(key, null)
        val type = object : TypeToken<HashSet<AuthModel>>() {}.type
        return if (json != null) gson.fromJson(json, type) else HashSet()
    }
}