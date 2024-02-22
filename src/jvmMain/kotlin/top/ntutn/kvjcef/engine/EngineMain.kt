package top.ntutn.kvjcef.engine

import com.github.winterreisender.webviewko.WebviewKo
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

private val scope = CoroutineScope(Dispatchers.Main)

fun main(vararg args: String) {

    println("Hello World!")
    val applicationEnvironment = if (System.getenv("KV_DEBUG") == "true") {
        commandLineEnvironment(arrayOf("-port=8080"))
    } else {
        commandLineEnvironment(arrayOf())
    }
    val engine = NettyApplicationEngine(applicationEnvironment) { loadConfiguration(applicationEnvironment.config) }

    if (System.getenv("KV_DEBUG") != "true") {
        launchBrowserFrame(engine)
    }

    engine.start(true)
    println("Bye")
    exitProcess(0)
}

private fun launchBrowserFrame(engine: NettyApplicationEngine) {
    scope.launch(Dispatchers.IO) {
        val port = engine.resolvedConnectors().first().port
        WebviewKo().run {
            size(1000, 618)
            title("Title")
            url("http://localhost:$port")
            show()
        }
    }
}

internal fun NettyApplicationEngine.Configuration.loadConfiguration(config: ApplicationConfig) {
    val deploymentConfig = config.config("ktor.deployment")
    loadCommonConfiguration(deploymentConfig)
    deploymentConfig.propertyOrNull("requestQueueLimit")?.getString()?.toInt()?.let {
        requestQueueLimit = it
    }
    deploymentConfig.propertyOrNull("runningLimit")?.getString()?.toInt()?.let {
        runningLimit = it
    }
    deploymentConfig.propertyOrNull("shareWorkGroup")?.getString()?.toBoolean()?.let {
        shareWorkGroup = it
    }
    deploymentConfig.propertyOrNull("responseWriteTimeoutSeconds")?.getString()?.toInt()?.let {
        responseWriteTimeoutSeconds = it
    }
    deploymentConfig.propertyOrNull("requestReadTimeoutSeconds")?.getString()?.toInt()?.let {
        requestReadTimeoutSeconds = it
    }
    deploymentConfig.propertyOrNull("tcpKeepAlive")?.getString()?.toBoolean()?.let {
        tcpKeepAlive = it
    }
    deploymentConfig.propertyOrNull("maxInitialLineLength")?.getString()?.toInt()?.let {
        maxInitialLineLength = it
    }
    deploymentConfig.propertyOrNull("maxHeaderSize")?.getString()?.toInt()?.let {
        maxHeaderSize = it
    }
    deploymentConfig.propertyOrNull("maxChunkSize")?.getString()?.toInt()?.let {
        maxChunkSize = it
    }
}