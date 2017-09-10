package com.troggo.jmp.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.Pool
import com.troggo.jmp.utils.Poolable
import com.troggo.jmp.utils.SpriteSheet
import com.troggo.jmp.utils.bottom
import com.troggo.jmp.utils.draw
import com.troggo.jmp.utils.setLinearVelocity

const val BOX_HEIGHT = 4f               // m/s
const val BOX_FALL_SPEED = 5f           // m/s

private enum class BOX_SPRITE(private val frame: Int? = null) {
    S_TOUCHED,
    S_UNTOUCHED,
    R_TOUCHED(4),
    R_UNTOUCHED(5);
    operator fun invoke() = frame ?: ordinal
}

class Box(
    game: Jmp,
    private val pool: Pool<Box>,
    private val square: Boolean,
    x: Float,
    y: Float
) : Poolable, Body(
    game,
    x = x,
    y = y,
    sprites = SpriteSheet("box.png", cols = if (square) 3 else 6),
    height = BOX_HEIGHT,
    gravityScale = 0f,
    shapeType = Shape.Type.Chain,
    type = BodyDef.BodyType.KinematicBody
) {
    private var touched = false
    private var dead = false
    private val collision = Collision(x, y, dimensions.width)
    override var position
        get() = super.position
        set(vec) {
            super.position = vec
            collision.position = vec
        }
    init { reset() }

    override fun reset() {
        touched = false
        dead = false
        body.setLinearVelocity(y = -BOX_FALL_SPEED)
    }

    override fun dispose() {
        collision.dispose()
        super.dispose()
    }

    override fun render(delta: Float) {
        val frame = when {
            touched && square -> BOX_SPRITE.S_TOUCHED()
            touched && !square -> BOX_SPRITE.R_TOUCHED()
            !touched && square -> BOX_SPRITE.S_UNTOUCHED()
            else -> BOX_SPRITE.R_UNTOUCHED()
        }
        game.batch.draw(sprites[frame], position, dimensions)
    }

    override fun step(delta: Float) {
        if (!dead) {
            // stop boxes from falling near the edge of the screen
            if (position.y <= game.camera.bottom + BOX_HEIGHT / 2) body.setLinearVelocity(y = 0f)
            // clean up off screen boxes
            if (position.y < game.camera.bottom - BOX_HEIGHT) {
                dead = true
                pool.free(this)
            }
        }
    }

    override fun beginContact(entity: Entity) {
        when (entity) {
            is Box, is Ground -> body.setLinearVelocity(y = 0f)
        }
    }

    // used by Guy
    fun touch() {
        touched = true
    }

    // we need a separate body to detect collisions for the box since kinematic bodies can't collide with other bodies
    inner class Collision(x: Float, y: Float, width: Float) : Body (
        game,
        x = x,
        y = y,
        width = width - 0.1f,                       // allow for rounding errors
        height = BOX_HEIGHT - game.pixelWidth * 2,  // allow for rounding errors
        isSensor = true
    ) {
        init {
            // weld collision 'hitbox' to Box
            game.world.createJoint(WeldJointDef().apply {
                initialize(this@Box.body, body, Vector2())
            })
        }
        override fun render(delta: Float) = Unit
        override fun beginContact(entity: Entity) = this@Box.beginContact(entity)
    }
}
