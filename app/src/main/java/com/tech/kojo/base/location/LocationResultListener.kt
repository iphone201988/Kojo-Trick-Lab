package com.tech.kojo.base.location

import android.location.Location

interface LocationResultListener {
    fun getLocation(location: Location)
}