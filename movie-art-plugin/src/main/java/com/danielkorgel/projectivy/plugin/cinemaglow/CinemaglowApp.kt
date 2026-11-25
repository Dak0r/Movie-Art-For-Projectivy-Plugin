package com.danielkorgel.projectivy.plugin.cinemaglow

import android.app.Application

class CinemaglowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }
}
