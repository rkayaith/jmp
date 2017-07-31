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

private const val GUY_MAX_SPEED = 12f       // m/s
private const val GUY_MOVE_FORCE = 10000f   // N
private const val GUY_JUMP_IMPULSE = 720f   // N*s
private const val GUY_JUMP_COUNT = 2

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
            body.setLinearVelocity(y = 0f)
        }
        if (entity is Wall || entity is Ground) {
            jumpCount = 0
        }
    }

    override fun endContact(entity: Entity) {
        if (entity is Wall) {
            wallContacts--
            updateGravity()
        }
    }

    private fun jump() {
        if (jumpCount < GUY_JUMP_COUNT) {
            jumpCount++
            // "move" Guy off the wall
            wallContacts = 0
            updateGravity()
            // reset vertical velocity for consistent jump heights
            body.setLinearVelocity(y = 0f)
            body.applyLinearImpulse(y = GUY_JUMP_IMPULSE)
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
