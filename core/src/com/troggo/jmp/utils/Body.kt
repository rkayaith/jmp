package com.troggo.jmp.utils

import com.badlogic.gdx.physics.box2d.Body

fun Body.setLinearVelocity(x: Float = linearVelocity.x, y: Float = linearVelocity.y) = setLinearVelocity(x, y)

fun Body.applyForceToCenter(x: Float = 0f, y: Float = 0f, wake: Boolean = true) = applyForceToCenter(x, y, wake)

fun Body.applyLinearImpulse(
    x: Float = 0f, y: Float = 0f, pointX: Float = 0f, pointY: Float = 0f, wake: Boolean = true
) = applyLinearImpulse(x, y, pointX, pointY, wake)
