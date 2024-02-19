package com.example.project

import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandler
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

        //Create a new CefAppBuilder instance
        val builder = CefAppBuilder()

        //Configure the builder instance
        builder.setInstallDir(File("jcef-bundle")); //Default
        builder.setProgressHandler(ConsoleProgressHandler()); //Default
        //builder.addJcefArgs("--disable-gpu"); //Just an example
        builder.cefSettings.windowless_rendering_enabled = true; //Default - select OSR mode
        builder.cefSettings

        //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
        //builder.setAppHandler(new MavenCefAppHandlerAdapter(){...});

        //Build a CefApp instance using the configuration above
        val app = builder.build();
        val client = app.createClient()
        client.addContextMenuHandler(object : CefContextMenuHandler {
            override fun onBeforeContextMenu(browser: CefBrowser?, frame: CefFrame?, params: CefContextMenuParams?, model: CefMenuModel?) {
                model?.clear()
            }

            override fun onContextMenuCommand(browser: CefBrowser?, frame: CefFrame?, params: CefContextMenuParams?, commandId: Int, eventFlags: Int): Boolean {
                return false
            }

            override fun onContextMenuDismissed(browser: CefBrowser?, frame: CefFrame?) {
            }
        })

        println("browser init http://localhost:${port.port}")
        val browser = client.createBrowser("http://localhost:${port.port}", true, false)
        val component = browser.uiComponent
        val jFrame = JFrame()
        jFrame.title = "Hello World"
        jFrame.add(component)
        jFrame.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent?) {
            }

            override fun windowClosing(e: WindowEvent?) {
            }

            override fun windowClosed(e: WindowEvent?) {
                client.dispose()
                app.dispose()
                engine.stop()
            }

            override fun windowIconified(e: WindowEvent?) {
            }

            override fun windowDeiconified(e: WindowEvent?) {
            }

            override fun windowActivated(e: WindowEvent?) {
            }

            override fun windowDeactivated(e: WindowEvent?) {
            }

        })
        jFrame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        jFrame.minimumSize = Dimension(1000, 618)
        jFrame.isVisible = true
        //jFrame.extendedState = MAXIMIZED_BOTH
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