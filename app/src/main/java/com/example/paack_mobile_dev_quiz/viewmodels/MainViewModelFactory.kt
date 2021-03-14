package com.example.paack_mobile_dev_quiz.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.paack_mobile_dev_quiz.networking.ApiService
import java.lang.IllegalArgumentException

/**
 *@Created by Yerimah on 3/14/2021.
 */

class MainViewModelFactory(private val apiService: ApiService): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return  MainViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown Class Name")
    }

}