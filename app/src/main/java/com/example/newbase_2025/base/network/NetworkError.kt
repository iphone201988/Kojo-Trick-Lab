
package com.example.newbase_2025.base.network

class NetworkError(val errorCode: Int, override val message: String?) : Throwable(message)
