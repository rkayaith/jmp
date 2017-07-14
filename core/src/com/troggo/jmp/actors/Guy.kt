package com.troggo.jmp.actors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

private const val GUY_WIDTH = 20f
private const val GUY_HEIGHT = 50f
private const val GUY_SPEED = 200

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(private val camera: Camera, private val batch: Batch) {
    private val texture = Texture(Gdx.files.internal("guy.png"))
    private val body = Rectangle(0f, 0f, GUY_WIDTH, GUY_HEIGHT)

    fun render(delta: Float) {
        batch.render {
            draw(texture, body.x, body.y)
        }

        val cameraWidth = camera.viewportWidth
        if (Gdx.input.isTouched) {
            val touch = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f).projectOn(camera)
            if (touch.x < cameraWidth / 2) {
                body.x -= GUY_SPEED * delta
            } else {
                body.x += GUY_SPEED * delta
            }
        }

        if (body.x < 0) body.x = 0f
        if (body.x > cameraWidth - GUY_WIDTH) body.x = cameraWidth - GUY_WIDTH
    }

    fun dispose() {
        texture.dispose()
    }
}

// extensions
private fun Batch.render(fn: Batch.() -> Unit) {
    this.begin()
    this.fn()
    this.end()
}

private fun Vector3.projectOn(camera: Camera): Vector3 {
    camera.unproject(this)
    return this
}