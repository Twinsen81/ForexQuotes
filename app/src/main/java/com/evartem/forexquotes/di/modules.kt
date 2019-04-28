package com.evartem.forexquotes.di

import com.evartem.forexquotes.BuildConfig
import com.evartem.forexquotes.quotes.QuotesViewModel
import com.evartem.forexquotes.remote.api.AuthInterceptor
import com.evartem.forexquotes.remote.api.createForexServiceNetworkClient
import com.evartem.forexquotes.usecase.GetQuotesUseCase
import com.evartem.forexquotes.usecase.GetSymbolsUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { QuotesViewModel(getSymbolsUseCase = get(), getQuotesUseCase = get()) }
}

val useCasesModule: Module = module {
    factory { GetSymbolsUseCase(service = get()) }
    factory { GetQuotesUseCase(service = get()) }
}

val networkModule: Module = module {
    single {
        createForexServiceNetworkClient(
            baseUrl = "https://forex.1forge.com/",
            debug = BuildConfig.DEBUG,
            authInterceptor = get()
        )
    }
    single { AuthInterceptor(apiKeyParamName = "api_key", apiKey = BuildConfig.API_KEY) }
}