package com.troggo.jmp.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.setLinearVelocity

const val BOX_HEIGHT = 4f               // m/s
private const val BOX_FALL_SPEED = 5f   // m/s

class Box(
    game: Jmp,
    private val width: Float,
    private val x: Float,
    private val y: Float
) : Body (
    game,
    x = x,
    y = y,
    width = width,
    height = BOX_HEIGHT,
    gravityScale = 0f,
    shapeType = Shape.Type.Chain,
    type = BodyDef.BodyType.KinematicBody
) {
    private val collision = Collision().also {
        // weld collision 'hitbox' to self
        game.world.createJoint(WeldJointDef().apply {
            initialize(body, it.body, Vector2())
        })
    }

    init {
        body.setLinearVelocity(y = -BOX_FALL_SPEED)
    }

    override fun dispose() {
        collision.dispose()
        super.dispose()
    }

    override fun beginContact(entity: Entity) {
        if (entity is Box || entity is Ground) {
            body.setLinearVelocity(y = 0f)
        }
    }

    // we need a separate body to detect collisions for the box since kinematic bodies can't collide with other bodies
    inner class Collision : Body (
        game,
        x = x,
        y = y,
        width = width - 0.1f,   // allow for rounding errors
        height = BOX_HEIGHT,
        isSensor = true
    ) {
        override fun render() = Unit
        override fun beginContact(entity: Entity) = this@Box.beginContact(entity)
    }
}



