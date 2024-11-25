package com.test.weatherapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    companion object {
        private var instance: WeatherApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}