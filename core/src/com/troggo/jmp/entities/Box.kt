package com.troggo.jmp.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.Pool
import com.troggo.jmp.utils.Poolable
import com.troggo.jmp.utils.bottom
import com.troggo.jmp.utils.setLinearVelocity

const val BOX_HEIGHT = 4f               // m/s
const val BOX_FALL_SPEED = 5f           // m/s

class Box(
    game: Jmp,
    private val pool: Pool<Box>,
    x: Float,
    y: Float,
    width: Float
) : Poolable, Body(
    game,
    x = x,
    y = y,
    width = width,
    height = BOX_HEIGHT,
    gravityScale = 0f,
    shapeType = Shape.Type.Chain,
    type = BodyDef.BodyType.KinematicBody
) {
    private var dead = false
    private val collision = Collision(x, y, width)
    override var position
        get() = super.position
        set(vec) {
            super.position = vec
            collision.position = vec
        }
    init { reset() }

    override fun reset() {
        dead = false
        body.setLinearVelocity(y = -BOX_FALL_SPEED)
    }

    override fun dispose() {
        collision.dispose()
        super.dispose()
    }

    override fun step() {
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
        if (entity is Box || entity is Ground) {
            body.setLinearVelocity(y = 0f)
        }
    }

    // we need a separate body to detect collisions for the box since kinematic bodies can't collide with other bodies
    inner class Collision(x: Float, y: Float, width: Float) : Body (
        game,
        x = x,
        y = y,
        width = width - 0.1f,   // allow for rounding errors
        height = BOX_HEIGHT,
        isSensor = true
    ) {
        init {
            // weld collision 'hitbox' to Box
            game.world.createJoint(WeldJointDef().apply {
                initialize(this@Box.body, body, Vector2())
            })
        }
        override fun render() = Unit
        override fun beginContact(entity: Entity) = this@Box.beginContact(entity)
    }
}
