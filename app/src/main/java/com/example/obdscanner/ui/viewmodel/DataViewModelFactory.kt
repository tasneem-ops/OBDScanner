package com.example.obdscanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.obdscanner.model.db.LocalDataSource

class DataViewModelFactory(private val localDataSource : LocalDataSource) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(DataViewModel::class.java)){
            DataViewModel(localDataSource) as T
        }else{
            throw IllegalArgumentException("ViewModel Class Not Found")
        }
    }
}