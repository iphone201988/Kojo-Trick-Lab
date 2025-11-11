package com.example.newbase_2025.base.location

import android.location.Location

interface LocationResultListener {
    fun getLocation(location: Location)
}