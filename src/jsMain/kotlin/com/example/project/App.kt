package com.example.project

import io.kvision.*
import io.kvision.core.onClick
import io.kvision.html.Span
import io.kvision.html.div
import io.kvision.panel.root
import io.kvision.toast.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

class App : Application() {

    override fun start(state: Map<String, Any>) {
        val root = root("kvapp") {
            div ("Hello").onClick {
                Toast.info("test")
            }
        }
        AppScope.launch {
            val pingResult = Model.ping("Hello world from client!")
            root.add(Span(pingResult))
        }
    }
}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        ToastifyModule,
        CoreModule
    )
}
