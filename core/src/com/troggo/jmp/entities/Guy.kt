package com.troggo.jmp.entities

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.applyForceToCenter
import com.troggo.jmp.utils.applyLinearImpulse
import com.troggo.jmp.utils.draw
import com.troggo.jmp.utils.setLinearVelocity

private const val GUY_HEIGHT = 2f   // m
private const val GUY_WEIGHT = 55f  // kg
private const val GUY_DAMPING = 0f

private const val GUY_MAX_SPEED = 12f           // m/s
private const val GUY_MOVE_FORCE = 6000f        // N
private const val GUY_JUMP_IMPULSE_UP = 720f    // N*s
private const val GUY_JUMP_IMPULSE_SIDE = 1000f // N*s
private const val GUY_JUMP_COUNT = 2

private const val WALL_FRICTION_FORCE = 700f    // N

private enum class Direction {
    LEFT, RIGHT, STOPPED
}

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(game: Jmp) : Body(
    game,
    texture = Texture("guy.png"),
    height = GUY_HEIGHT,
    weight = GUY_WEIGHT,
    damping = GUY_DAMPING,
    x = 3f,
    y = game.camera.viewportHeight / 2
) {
    val controller = Controller()
    private val foot = Sensor(y = -GUY_HEIGHT / 2, width = dimensions.width / 2)
    private val left = Sensor(x = -dimensions.width / 2, height = GUY_HEIGHT / 1.2f)
    private val right = Sensor(x = dimensions.width / 2, height = GUY_HEIGHT / 1.2f)

    private var direction = Direction.STOPPED
    private var wallContacts = 0
    private var jumpCount = 0

    override fun render() {
        // TODO: maintain previous direction once stopped
        game.batch.draw(this, flipX = (direction == Direction.RIGHT))
    }

    override fun step() {
        // move Guy
        when (direction) {
            Direction.LEFT -> body.applyForceToCenter(x = -GUY_MOVE_FORCE)
            Direction.RIGHT -> body.applyForceToCenter(x = GUY_MOVE_FORCE)
            // simulate friction
            else -> body.applyLinearImpulse(x = -1f * body.linearVelocity.x)
        }

        // clamp Guy's horizontal velocity.
        val x = body.linearVelocity.x
        body.setLinearVelocity(x = Math.signum(x) * Math.min(Math.abs(x), GUY_MAX_SPEED))

        // give Guy friction against walls
        if (wallContacts > 0) {
            body.applyForceToCenter(y = -WALL_FRICTION_FORCE * Math.signum(body.linearVelocity.y))
        }

        // TODO: kill Guy if he leaves the screen
    }

    private fun sensorBeginContact(sensor: Sensor, entity: Entity) {
        when (sensor) {
            foot -> {
                jumpCount = 0
            }
            left, right -> {
                if (entity is Box) {
                    wallContacts++
                    jumpCount = 0
                }
            }
        }
    }

    private fun sensorEndContact(sensor: Sensor, entity: Entity) {
        when (sensor) {
            left, right -> {
                if (entity is Box) {
                    wallContacts--
                }
            }
        }
    }

    private fun jump() {
        if (jumpCount < GUY_JUMP_COUNT) {
            jumpCount++
            // push Guy off the wall
            if (left.inContact()) body.applyLinearImpulse(x = GUY_JUMP_IMPULSE_SIDE)
            if (right.inContact()) body.applyLinearImpulse(x = -GUY_JUMP_IMPULSE_SIDE)
            // reset vertical velocity for consistent jump heights
            body.setLinearVelocity(y = 0f)
            body.applyLinearImpulse(y = GUY_JUMP_IMPULSE_UP)
        }
    }

    inner class Sensor(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 0.2f,
        height: Float = 0.2f
    ) : Fixture(
        isSensor = true,
        width = width,
        height = height,
        offsetX = x,
        offsetY = y
    ) {
        var contacts = 0
        fun inContact() = contacts > 0
        override fun beginContact(entity: Entity) {
            contacts++
            sensorBeginContact(this, entity)
        }
        override fun endContact(entity: Entity) {
            contacts--
            sensorEndContact(this, entity)
        }
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
