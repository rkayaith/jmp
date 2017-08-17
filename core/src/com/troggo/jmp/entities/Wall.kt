package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp

class Wall(game: Jmp, width: Float, height: Float, x: Float) : Body(
    game,
    x = x,
    y = height / 2,
    width = width,
    height = height,
    type = BodyDef.BodyType.StaticBody
)
