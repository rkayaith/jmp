package com.troggo.jmp.utils

import com.badlogic.gdx.graphics.Texture

// sprite properties describe its dimensions and position in the texture
data class Sprite(val texture: Texture, val width: Int, val height: Int, val x: Int, val y: Int)

class SpriteSheet(textureStr: String, private val rows: Int = 1, private val cols: Int = 1) {
    private val texture = Texture(textureStr)
    val width = texture.width / cols
    val height = texture.height / rows
    operator fun get(frame: Int) = if (frame in 0..(rows * cols - 1)) Sprite(
        texture, width, height,
        width * (frame % cols), height * (frame / cols)
    ) else throw IllegalArgumentException("Frame out of range: " + frame)

    fun dispose() = texture.dispose()

    inner class Animation(private val range: ClosedRange<Int>, private val interval: Float) {
        private val max = interval * (range.endInclusive - range.start + 1)
        private var delta = 0f

        val frame get() = range.start + (delta / interval).toInt()
        val sprite get() = get(frame)
        fun step(d: Float): Sprite {
            delta = (delta + d) % max
            return sprite
        }
        fun reset() {
            delta = 0f
        }
    }
}
