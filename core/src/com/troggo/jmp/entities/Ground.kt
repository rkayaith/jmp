package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.SpriteSheet

class Ground(game: Jmp, width: Float) : Body (
    game,
    width = width,
    height = game.camera.viewportHeight,
    x = width / 2,
    sprites = SpriteSheet("ground.png"),
    type = BodyDef.BodyType.StaticBody
)
