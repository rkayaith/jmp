package com.troggo.jmp.actors

import com.troggo.jmp.Jmp

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.FixtureDef

private const val GUY_HEIGHT = 2f   // m
private const val GUY_WEIGHT = 55f  // kg
private const val GUY_DAMPING = 10f

private const val GUY_MAX_SPEED = 12f       // m/s
private const val GUY_MOVE_FORCE = 10000f   // N
private const val GUY_JUMP_IMPULSE = 2300f    // Ns

private enum class Direction {
    LEFT, RIGHT, STOPPED
}

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(private val game: Jmp) {
    val batch: Batch = game.batch
    val controller = Controller()
    private val texture = Texture("guy.png")
    private var direction = Direction.STOPPED
    private val body = game.world.createBody(
        height = GUY_HEIGHT, width = GUY_HEIGHT * texture.width / texture.height,
        weight = GUY_WEIGHT, damping = GUY_DAMPING,
        x = 3f, y = game.camera.viewportHeight / 2
    )

    fun dispose() {
        texture.dispose()
    }

    fun render() {
        // draw Guy
        batch.render {
            // TODO: maintain previous direction once stopped
            when (direction) {
                Direction.LEFT -> draw(texture, body)
                else -> draw(texture, body, flipX = true)
            }
        }
    }

    fun step() {
        // move Guy
        when (direction) {
            Direction.LEFT -> body.applyForceToCenter(-GUY_MOVE_FORCE, 0f, true)
            Direction.RIGHT -> body.applyForceToCenter(GUY_MOVE_FORCE, 0f, true)
            else -> {}
        }

        // clamp Guy's horizontal velocity
        body.linearVelocity = with(body.linearVelocity) {
            Vector2(if (x > 0) Math.min(GUY_MAX_SPEED, x) else Math.max(-GUY_MAX_SPEED, x), y)
        }

        // TODO: keep Guy on the screen
    }

    private fun jump() {
        // TODO: limit number of jumps without touching ground
        body.applyLinearImpulse(0f, GUY_JUMP_IMPULSE, 0f, 0f, true)
    }

    inner class Controller : InputAdapter() {
        override fun touchDown(x: Int, y: Int, pointer: Int, button: Int) = handler(pointer) {
            jump()
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
            val cameraWidth = game.camera.viewportWidth
            val touch = game.camera.unproject(Vector3(x.toFloat(), 0f, 0f))
            direction = if (touch.x < cameraWidth / 2) Direction.LEFT else Direction.RIGHT
        }
    }
}

// extensions
private data class Dimensions(val width: Float, val height: Float)

private fun World.createBody(width: Float, height: Float, weight: Float = 0f,
                             friction: Float = 0f, damping: Float = 0f,
                             x: Float = 0f, y: Float = 0f): Body {
    val bodyDef = BodyDef()
    with (bodyDef) {
        type = BodyDef.BodyType.DynamicBody
        position.set(x, y)
        linearDamping = damping
        fixedRotation = true
    }

    val box = PolygonShape()
    box.setAsBox(width / 2, height / 2)

    val fixture = FixtureDef()
    with (fixture) {
        shape = box
        density = weight / (height * width)
        this.friction = friction
    }

    val body = createBody(bodyDef)
    with (body) {
        createFixture(fixture)
        userData = Dimensions(width, height)
    }

    box.dispose()
    return body
}

private fun Batch.render(fn: Batch.() -> Unit) { begin(); fn(); end() }

private fun Batch.draw(texture: Texture, body: Body, flipX: Boolean = false, flipY: Boolean = false) {
    // render texture at body's position, scaled to its size
    val (x, y) = with (body.position) { Pair(x, y) }
    val (w, h) = body.userData as? Dimensions ?: throw IllegalArgumentException("Body userData must be Dimensions")
    draw(texture, x - w / 2, y - h / 2, w, h, 0, 0, texture.width, texture.height, flipX, flipY)
}

