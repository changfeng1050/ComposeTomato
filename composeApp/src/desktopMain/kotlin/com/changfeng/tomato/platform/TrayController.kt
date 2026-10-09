package com.changfeng.tomato.platform

import java.awt.EventQueue
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * 系统托盘：
 * - 左键单击 = 显示/隐藏主窗（关闭窗口即隐藏到托盘，计时继续）
 * - 右键菜单 = 显示/隐藏主窗 / 便笺窗口 / 退出
 * - 承担系统通知（托盘气泡）
 */
class TrayController(
    private val image: Image,
    private val onToggleMainWindow: () -> Unit,
    private val onShowNotes: () -> Unit,
    private val onExit: () -> Unit,
) {

    data class Labels(
        val tooltip: String,
        val showHide: String,
        val notes: String,
        val exit: String,
    )

    private var trayIcon: TrayIcon? = null

    val isInstalled: Boolean get() = trayIcon != null

    fun install(labels: Labels) {
        if (!SystemTray.isSupported()) return
        val popup = PopupMenu().apply {
            add(MenuItem(labels.showHide).apply { addActionListener { EventQueue.invokeLater(onToggleMainWindow) } })
            add(MenuItem(labels.notes).apply { addActionListener { EventQueue.invokeLater(onShowNotes) } })
            add(MenuItem(labels.exit).apply { addActionListener { EventQueue.invokeLater(onExit) } })
        }
        val icon = TrayIcon(image, labels.tooltip, popup).apply {
            isImageAutoSize = true
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) {
                        EventQueue.invokeLater(onToggleMainWindow)
                    }
                }
            })
        }
        try {
            SystemTray.getSystemTray().add(icon)
            trayIcon = icon
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 托盘气泡通知（Windows 下呈现为系统通知）。 */
    fun notify(title: String, message: String) {
        trayIcon?.displayMessage(title, message, TrayIcon.MessageType.INFO)
    }

    fun remove() {
        trayIcon?.let { SystemTray.getSystemTray().remove(it) }
        trayIcon = null
    }
}
