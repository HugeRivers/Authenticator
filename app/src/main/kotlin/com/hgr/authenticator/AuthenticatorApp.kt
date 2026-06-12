package com.hgr.authenticator

import android.app.Application
import com.hgr.authenticator.utils.TimeSource
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuthenticatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TimeSource.initialize()
    }
}
