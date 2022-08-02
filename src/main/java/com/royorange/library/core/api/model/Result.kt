package com.royorange.library.core.api.model

/**
 *  Created by Roy on 2022/7/13
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}