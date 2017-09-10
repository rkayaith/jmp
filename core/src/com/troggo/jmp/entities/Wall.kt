package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.bottom
import com.troggo.jmp.utils.set
import com.troggo.jmp.utils.top

class Wall(game: Jmp, x: Float) : Body(
    game,
    x = x,
    y = game.camera.position.y,
    width = 0.01f,
    height = game.camera.viewportHeight * 3,
    type = BodyDef.BodyType.StaticBody
) {
    override fun render(delta: Float) = Unit
    override fun step(delta: Float) {
        val cam = game.camera
        val pos = position
        if (pos.y > cam.top || pos.y < cam.bottom) {
            position = pos.set(y = cam.position.y)
        }
    }
}
