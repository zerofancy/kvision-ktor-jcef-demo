package com.example.project

import com.example.project.util.stubAll
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.handler.CefContextMenuHandler
import org.cef.handler.CefDisplayHandlerAdapter
import java.awt.Dimension
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.system.exitProcess

fun main(vararg args: String) {
    println("Hello World!")
    val applicationEnvironment = if (System.getenv("KV_DEBUG") == "true") {
        commandLineEnvironment(arrayOf("-port=8080"))
    } else {
        commandLineEnvironment(arrayOf())
    }
    val engine = NettyApplicationEngine(applicationEnvironment) { loadConfiguration(applicationEnvironment.config) }

    // GDK_SCALE=2 java --add-exports java.desktop/sun.awt=ALL-UNNAMED -jar project-1.0.0-SNAPSHOT.jar
    if (System.getenv("KV_DEBUG") != "true") {
        launchBrowserFrame(engine)
    }

    engine.start(true)
    println("Bye")
    exitProcess(0)
}

private fun launchBrowserFrame(engine: NettyApplicationEngine) {
    GlobalScope.launch {
        val port = engine.resolvedConnectors().first()

        //Create a new CefAppBuilder instance fixme 从github下载镜像，导致初次启动很慢
        val builder = CefAppBuilder()

        //Configure the builder instance
        builder.setInstallDir(File("jcef-bundle")); //Default
        builder.setProgressHandler(ConsoleProgressHandler()); //Default
        //builder.addJcefArgs("--disable-gpu"); //Just an example
        builder.cefSettings.windowless_rendering_enabled = true; //Default - select OSR mode

        //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {
            override fun stateHasChanged(state: CefApp.CefAppState?) {
                super.stateHasChanged(state)
                if (state == CefApp.CefAppState.TERMINATED) {
                    exitProcess(1)
                }
            }
        })

        //Build a CefApp instance using the configuration above
        val app = builder.build();
        val client = app.createClient()
        client.addContextMenuHandler(object : CefContextMenuHandler by stubAll() {})

        println("browser init http://localhost:${port.port}")
        val browser = client.createBrowser("http://localhost:${port.port}", true, false)
        val component = browser.uiComponent
        val jFrame = JFrame()
        jFrame.title = "Hello World"
        jFrame.add(component)
        jFrame.addWindowListener(object : WindowListener by stubAll() {
            override fun windowClosed(e: WindowEvent?) {
                client.dispose()
                app.dispose()
                engine.stop()
            }
        })
        jFrame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        jFrame.minimumSize = Dimension(1000, 618)
        jFrame.isVisible = true
        client.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onTitleChange(browser: CefBrowser?, title: String?) {
                super.onTitleChange(browser, title)
                jFrame.title = title
            }
        })
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