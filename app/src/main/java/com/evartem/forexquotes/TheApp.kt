package com.evartem.forexquotes

import android.app.Application
import com.evartem.forexquotes.di.networkModule
import com.evartem.forexquotes.di.useCasesModule
import com.evartem.forexquotes.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class TheApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@TheApp)
            modules(
                viewModelModule,
                useCasesModule,
                networkModule
            )
        }
    }
}