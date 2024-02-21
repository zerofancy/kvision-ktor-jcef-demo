package top.ntutn.kvjcef.engine

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandler

object DummyContextMenuHandler: CefContextMenuHandler {
    override fun onBeforeContextMenu(
        browser: CefBrowser?,
        frame: CefFrame?,
        params: CefContextMenuParams?,
        model: CefMenuModel?
    ) {
    }

    override fun onContextMenuCommand(
        browser: CefBrowser?,
        frame: CefFrame?,
        params: CefContextMenuParams?,
        commandId: Int,
        eventFlags: Int
    ): Boolean {
        return false
    }

    override fun onContextMenuDismissed(browser: CefBrowser?, frame: CefFrame?) {
    }
}