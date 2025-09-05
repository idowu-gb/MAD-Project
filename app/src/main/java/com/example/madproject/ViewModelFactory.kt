package com.example.madproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val userDao: UserDao, private val tripDao: TripDao, private val contactDao: ContactDao, private val panicalertDao: PanicAlertDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userDao, tripDao, contactDao, panicalertDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}