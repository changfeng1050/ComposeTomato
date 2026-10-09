package com.changfeng.tomato.platform

import java.awt.Color
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent
import javax.swing.SwingUtilities

/** 提示音播放：加载打包在 resources 里的 ding.wav，可重叠播放。 */
object SoundPlayer {

    @Volatile
    private var wavBytes: ByteArray? = null

    fun load(bytes: ByteArray) {
        wavBytes = bytes
    }

    fun play() {
        val bytes = wavBytes ?: return
        try {
            val clip = AudioSystem.getClip()
            clip.open(AudioSystem.getAudioInputStream(bytes.inputStream()))
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    SwingUtilities.invokeLater { clip.close() }
                }
            }
            clip.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/** 程序化绘制的番茄托盘图标（红色圆果 + 绿色果蒂）。 */
fun tomatoTrayImage(size: Int = 32): Image {
    val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val body = Color(0xE85648)
    val bodyDark = Color(0xC93B2F)
    val leaf = Color(0x4CAF50)
    // 果蒂
    g.color = leaf
    g.fillRect(size * 15 / 32, size / 8, size / 16, size / 6)
    g.fillArc(size / 5, size * 3 / 16, size * 3 / 5, size / 5, 0, 180)
    // 果身
    g.color = body
    g.fillOval(size / 8, size * 5 / 16, size * 3 / 4, size * 11 / 16)
    // 高光
    g.color = bodyDark
    g.drawArc(size / 8, size * 5 / 16, size * 3 / 4, size * 11 / 16, 0, 360)
    g.dispose()
    return img
}
