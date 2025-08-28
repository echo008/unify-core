package com.unify.sample.di

import com.unify.sample.data.UserRepository
import com.unify.sample.presentation.UserViewModel
import org.koin.dsl.module

val sampleModule = module {
    // Repository
    single<UserRepository> {
        UserRepository(
            database = get(),
            networkService = get()
        )
    }
    
    // ViewModels
    factory<UserViewModel> {
        UserViewModel(
            userRepository = get()
        )
    }
}
