package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp

class Ground(game: Jmp, width: Float) : Body (
    game,
    width = width,
    height = 2f,
    x = width / 2,
    type = BodyDef.BodyType.StaticBody
)
