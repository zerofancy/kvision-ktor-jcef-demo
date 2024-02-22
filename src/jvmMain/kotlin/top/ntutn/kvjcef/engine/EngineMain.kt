package top.ntutn.kvjcef.engine

import dev.webview.webview_java.Webview
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
        val wv = Webview(true) // Can optionally be created with an AWT component to be painted on.


        // Calling `await echo(1,2,3)` will return `[1,2,3]`
        //wv.bind("echo") { arguments -> arguments }

        wv.setTitle("My Webview App")

        wv.setSize(800, 600);

        // load a URL
        wv.loadURL("http://localhost:$port")

        /*

        Or, load raw html from a file with:
        wv.setHTML("<h1>This is a test!<h1>");

        String htmlContent = loadContentFromFile("index.html");
        wv.setHTML(htmlContent);

         */
        wv.run() // Run the webview event loop, the webview is fully disposed when this returns.
        wv.close() // Free any resources allocated.
        //EngineFrame(engine)
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