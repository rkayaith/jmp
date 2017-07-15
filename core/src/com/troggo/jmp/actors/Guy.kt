package com.troggo.jmp.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

private const val GUY_WIDTH = 20f
private const val GUY_HEIGHT = 50f
private const val GUY_SPEED = 400

private enum class Direction {
    LEFT, RIGHT, STOPPED
}

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(private val camera: Camera, private val batch: Batch) {
    val controller = Controller()
    private val texture = Texture(Gdx.files.internal("guy.png"))
    private val body = Rectangle(0f, 0f, GUY_WIDTH, GUY_HEIGHT)
    private var direction = Direction.STOPPED

    fun render(delta: Float) {
        // draw Guy
        batch.render {
            // TODO: maintain previous direction once stopped
            when (direction) {
                Direction.LEFT -> draw(texture, body.x, body.y)
                else -> draw(texture, body.x, body.y, flipX = true)
            }
        }

        // move Guy
        when (direction) {
            Direction.LEFT -> body.x -= GUY_SPEED * delta
            Direction.RIGHT -> body.x += GUY_SPEED * delta
            else -> {}
        }

        // keep Guy on the screen
        val cameraWidth = camera.viewportWidth
        if (body.x < 0) body.x = 0f
        if (body.x > cameraWidth - GUY_WIDTH) body.x = cameraWidth - GUY_WIDTH
    }

    fun dispose() {
        texture.dispose()
    }

    inner class Controller : InputAdapter() {
        override fun touchDown(x: Int, y: Int, pointer: Int, button: Int) = handler(pointer) {
            // TODO: jump
            updateDirection(x)
        }
        override fun touchDragged(x: Int, y: Int, pointer: Int) = handler(pointer) {
            updateDirection(x)
        }
        override fun touchUp(x: Int, y: Int, pointer: Int, button: Int) = handler(pointer) {
            direction = Direction.STOPPED
        }

        private fun handler(pointer: Int, f: () -> Unit) = when (pointer) {
            // only handle first pointer (touch)
            // TODO: support multiple pointers
            0 -> { f(); true }
            else -> false
        }

        private fun updateDirection(x: Int) {
            val cameraWidth = camera.viewportWidth
            val touch = camera.unproject(Vector3(x.toFloat(), 0f, 0f))
            direction = if (touch.x < cameraWidth / 2) Direction.LEFT else Direction.RIGHT
        }
    }
}

// extensions
private fun Batch.render(fn: Batch.() -> Unit) {
    this.begin()
    this.fn()
    this.end()
}

private fun Batch.draw(texture: Texture, x: Float, y: Float, flipX: Boolean = false, flipY: Boolean = false) {
    val w = texture.width
    val h = texture.height
    draw(texture, x, y, w.toFloat(), h.toFloat(), 0, 0, w, h, flipX, flipY)
}
