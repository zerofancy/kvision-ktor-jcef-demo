package com.example.project

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*
import io.kvision.remote.applyRoutes
import io.kvision.remote.getAllServiceManagers
import io.kvision.remote.kvisionInit
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun Application.main() {
    install(Compression)
    routing {
        getAllServiceManagers().forEach { applyRoutes(it) }
        staticResources("/", "productionExecutable", "index.html")
    }
    val module = module {
        factoryOf(::PingService)
    }
    val SimplePlugin = createApplicationPlugin(name = "SimplePlugin") {
        this@createApplicationPlugin.applicationConfig

        println("SimplePlugin is installed!")
    }
    install(SimplePlugin)
    kvisionInit(false, module)

}
