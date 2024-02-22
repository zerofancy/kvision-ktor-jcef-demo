package top.ntutn.kvjcef.engine

import com.jogamp.common.os.Platform
import io.ktor.server.netty.*
import jogamp.common.os.PlatformPropsImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandler
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandler
import java.awt.Dimension
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import javax.swing.JFrame
import kotlin.system.exitProcess

class EngineFrame(engine: NettyApplicationEngine): JFrame() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var browserLoading = true
    private val loadingQueue = ArrayDeque<String>()

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(1000, 618)
        setLocationRelativeTo(null)
        addWindowListener(object : WindowListener by DummyWindowListener {
            override fun windowClosed(e: WindowEvent?) {
                coroutineScope.cancel()
                engine.stop()
            }
        })
        val browser = plantBrowser()
        add(browser.uiComponent)
        coroutineScope.launch {
            val port = engine.resolvedConnectors().first().port
            isVisible = true
            loadUrl(browser, "http://localhost:$port")
        }
    }

    /**
     * wait browser ready
     */
    private fun loadUrl(browser: CefBrowser, url: String) {
        if (browserLoading) {
            loadingQueue.addLast(url)
        } else {
            browser.loadURL(url)
        }
    }

    private fun plantBrowser(): CefBrowser {
        //Create a new CefAppBuilder instance fixme 从github下载镜像，导致初次启动很慢
        val builder = CefAppBuilder()

        //Configure the builder instance
        builder.setInstallDir(File("jcef-bundle")); //Default
        builder.setProgressHandler(ConsoleProgressHandler()); //Default

        builder.cefSettings.windowless_rendering_enabled = false

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
        client.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onTitleChange(browser: CefBrowser?, title: String?) {
                super.onTitleChange(browser, title)
                this@EngineFrame.title = title
            }
        })

        client.addLoadHandler(object : CefLoadHandler by DummyCefLoadHandler {
            override fun onLoadingStateChange(
                browser: CefBrowser?,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean
            ) {
                browserLoading = isLoading
                coroutineScope.launch {
                    if (!isLoading && loadingQueue.isNotEmpty()) {
                        loadingQueue.removeFirst().let {
                            browser?.loadURL(it)
                        }
                    }
                }
            }
        })

        return client.createBrowser("about:blank", false, false)
    }
}