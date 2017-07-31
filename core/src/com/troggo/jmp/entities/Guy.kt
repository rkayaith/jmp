package com.troggo.jmp.entities

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.troggo.jmp.Jmp

private const val GUY_HEIGHT = 2f   // m
private const val GUY_WEIGHT = 55f  // kg
private const val GUY_DAMPING = 0f

private const val GUY_MAX_SPEED = 12f       // m/s
private const val GUY_MOVE_FORCE = 10000f   // N
private const val GUY_JUMP_IMPULSE = 720f   // N*s

private const val WALL_GRAVITY_SCALE = 0.4f

private enum class Direction {
    LEFT, RIGHT, STOPPED
}

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(game: Jmp) : Entity(
    game,
    texture = Texture("guy.png"),
    height = GUY_HEIGHT,
    weight = GUY_WEIGHT,
    damping = GUY_DAMPING,
    x = 3f,
    y = game.camera.viewportHeight / 2
) {
    val controller = Controller()
    private var direction = Direction.STOPPED
    private var wallContacts = 0

    override fun render() = with (game.batch) {
        // TODO: maintain previous direction once stopped
        when (direction) {
            Direction.LEFT -> draw(this@Guy)
            else -> draw(this@Guy, flipX = true)
        }
    }

    override fun step() {
        // move Guy
        when (direction) {
            Direction.LEFT -> body.applyForceToCenter(-GUY_MOVE_FORCE, 0f, true)
            Direction.RIGHT -> body.applyForceToCenter(GUY_MOVE_FORCE, 0f, true)
            // simulate friction
            else -> body.applyLinearImpulse(-1f * body.linearVelocity.x, 0f, 0f, 0f, true)
        }

        // clamp Guy's horizontal velocity.
        body.linearVelocity = with (body.linearVelocity) {
            Vector2(Math.signum(x) * Math.min(Math.abs(x), GUY_MAX_SPEED), y)
        }

        // TODO: kill Guy if he leaves the screen
    }

    private fun updateGravity() {
        wallContacts = Math.max(wallContacts, 0)
        // slow down Guy's fall if he's holding on to a wall
        body.gravityScale = if (wallContacts > 0) WALL_GRAVITY_SCALE else 1f
    }

    override fun beginContact(entity: Entity) {
        if (entity is Wall) {
            wallContacts++
            updateGravity()
            body.linearVelocity = Vector2(body.linearVelocity.x, 0f)
        }
    }

    override fun endContact(entity: Entity) {
        if (entity is Wall) {
            wallContacts--
            updateGravity()
        }
    }

    private fun jump() {
        // TODO: limit number of jumps without touching ground

        // "move" Guy off the wall
        wallContacts = 0
        updateGravity()
        // reset vertical velocity for consistent jump heights
        body.linearVelocity = Vector2(body.linearVelocity.x, 0f)
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
