package com.troggo.jmp.entities

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.*

public  const val GUY_HEIGHT = 2f   // m
private const val GUY_WEIGHT = 55f  // kg
private const val GUY_DAMPING = 0f

private const val GUY_MAX_SPEED = 12f           // m/s
private const val GUY_MOVE_FORCE = 6000f        // N
private const val GUY_JUMP_IMPULSE_UP = 720f    // N*s
private const val GUY_JUMP_IMPULSE_SIDE = 1000f // N*s
private const val GUY_JUMP_COUNT = 2            // jumps
private const val GUY_WALK_ANIM_DIST = 2f       // m
private const val GUY_TRAIL_LENGTH = 3
private const val GUY_TRAIL_ALPHA = 0.3f
private const val GUY_TRAIL_INTERVAL = 0.1f     // s

private const val WALL_FRICTION_FORCE = 700f    // N

private const val SPRITESHEET_ROWS = 2
private const val SPRITESHEET_COLS = 7

private enum class GUY_SPRITE(private val frame: Int? = null) {
    STAND,
    HANG,
    JUMP,
    AIR_ACCEL,
    FALL,
    DEAD,
    DEAD2,
    WALK_START,
    WALK_END(GUY_SPRITE.WALK_START() + 3);
    operator fun invoke() = frame ?: ordinal
}

enum class Direction {
    LEFT, RIGHT, STOPPED
}

// only one man can save us now...
//         ...his name...?
//                         ...Guy.
class Guy(game: Jmp, y: Float) : Body(
    game,
    sprites = SpriteSheet("guy.png", SPRITESHEET_ROWS, SPRITESHEET_COLS),
    height = GUY_HEIGHT,
    weight = GUY_WEIGHT,
    damping = GUY_DAMPING,
    x = game.camera.viewportWidth / 2,
    y = y
) {
    val controller = Controller()
    var isDead = false
        set(dead) { field = dead || field } // no rising from the dead

    private val foot = Sensor(y = -GUY_HEIGHT / 2, width = dimensions.width / 1.2f)
    private val head = Sensor(y = GUY_HEIGHT / 2, width = dimensions.width / 1.2f)
    private val left = Sensor(x = -dimensions.width / 2, height = GUY_HEIGHT / 1.2f)
    private val right = Sensor(x = dimensions.width / 2, height = GUY_HEIGHT / 1.2f)

    private var jumpCount = 0

    private var direction = Direction.RIGHT
    private val walk = sprites.Animation(GUY_SPRITE.WALK_START()..GUY_SPRITE.WALK_END(), GUY_WALK_ANIM_DIST)
    private val trail = CappedArrayList<Triple<Int, Vector2, Direction>>(GUY_TRAIL_LENGTH)
    private var trailDelta = GUY_TRAIL_INTERVAL

    override fun render(delta: Float) {
        body.linearVelocity.let { v ->
            direction = when {
                right.isInContact -> Direction.RIGHT
                left.isInContact -> Direction.LEFT
                v.x > 0 -> Direction.RIGHT
                v.x < 0 -> Direction.LEFT
                else -> direction
            }
            val frame = when {
                // dead
                isDead -> GUY_SPRITE.DEAD()
                // sides touching a box
                right.isInContact || left.isInContact -> GUY_SPRITE.HANG()
                // in the air
                !foot.isInContact -> when {
                    v.y < 0 -> GUY_SPRITE.FALL()
                    v.x.abs < GUY_MAX_SPEED && controller.direction != Direction.STOPPED -> GUY_SPRITE.AIR_ACCEL()
                    else -> GUY_SPRITE.JUMP()
                }
                // on the ground
                v.x.abs < 0.1f -> { walk.reset(); GUY_SPRITE.STAND() }
                else -> walk.apply { step(delta * v.x.abs) }.frame
            }

            // draw trail
            trail.reversed().forEachIndexed { i, (frame, position, direction) ->
                game.batch.withAlpha(GUY_TRAIL_ALPHA - i * GUY_TRAIL_ALPHA / GUY_TRAIL_LENGTH) {
                    draw(sprites[frame], position, dimensions, flipX = (direction != Direction.RIGHT))
                }
            }
            trailDelta += delta
            if (trailDelta >= GUY_TRAIL_INTERVAL)  {
                trail.add(Triple(frame, position.copy(), direction))
                trailDelta -= GUY_TRAIL_INTERVAL
            }

            game.batch.draw(sprites[frame], position, dimensions, flipX = (direction != Direction.RIGHT))
        }
    }

    override fun step() {
        // move Guy
        when (controller.direction) {
            Direction.LEFT -> body.applyForceToCenter(x = -GUY_MOVE_FORCE)
            Direction.RIGHT -> body.applyForceToCenter(x = GUY_MOVE_FORCE)
            // simulate friction
            else -> body.applyLinearImpulse(x = -1f * body.linearVelocity.x)
        }

        // clamp Guy's horizontal velocity.
        val x = body.linearVelocity.x
        body.setLinearVelocity(x = Math.signum(x) * Math.min(x.abs, GUY_MAX_SPEED))

        // give Guy friction against walls
        if (left.isInContact || right.isInContact) {
            body.applyForceToCenter(y = -WALL_FRICTION_FORCE * Math.signum(body.linearVelocity.y))
        }

        // kill Guy if he leaves the screen
        isDead = position.y < game.camera.bottom
    }

    private fun sensorBeginContact(sensor: Sensor, entity: Entity) {
        val landOn = { e: Entity ->
            jumpCount = 0
            (e as? Box)?.touch()
        }
        when (sensor) {
            foot -> {
                isDead = head.isInContact
                landOn(entity)
            }
            head -> isDead = foot.isInContact
            left, right -> if (entity is Box) landOn(entity)
        }
    }

    private fun sensorEndContact(sensor: Sensor, entity: Entity) = Unit

    private fun jump() {
        if (jumpCount < GUY_JUMP_COUNT) {
            jumpCount++
            // push Guy off the wall
            if (left.isInContact) body.applyLinearImpulse(x = GUY_JUMP_IMPULSE_SIDE)
            if (right.isInContact) body.applyLinearImpulse(x = -GUY_JUMP_IMPULSE_SIDE)
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
        val isInContact get() = contacts > 0
        private fun filter(entity: Entity, fn: () -> Unit) {
            // we only handle contacts with certain entities
            if (entity is Box || entity is Ground) fn()
        }
        override fun beginContact(entity: Entity) = filter(entity) {
            contacts++
            sensorBeginContact(this, entity)
        }
        override fun endContact(entity: Entity) = filter(entity) {
            contacts--
            sensorEndContact(this, entity)
        }
    }

    inner class Controller : InputAdapter() {
        var direction = Direction.STOPPED
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
