package com.troggo.jmp.entities

import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.SpriteSheet
import com.troggo.jmp.utils.copy
import com.troggo.jmp.utils.draw

private const val BG_BASE_X_OFFSET = 0
private const val BG_BASE_Y_OFFSET = 6
private const val PARALLAX_SCALE_FACTOR = 0.003f

private enum class GROUND_SPRITES {
    BUILDING6,
    BUILDING2,
    BUILDING3
}

class Ground(game: Jmp, width: Float) : Body (
    game,
    width = width,
    x = width / 2,
    sprites = SpriteSheet("background.png", cols = 3),
    type = BodyDef.BodyType.StaticBody
) {
    private val initCamY = game.camera.position.y
    private val resolution = sprites[0].width / Jmp.WORLD_WIDTH
    override fun render(delta: Float) {
        // we render back/foreground here since this entity is persistent between scenes
        // background
        game.batchQueue.add(-1) {
            draw(sprites[GROUND_SPRITES.BUILDING2.ordinal], offset(y = 66, x = -3), dimensions)
            draw(sprites[GROUND_SPRITES.BUILDING3.ordinal], offset(y = 36, x = 30), dimensions)
            draw(sprites[GROUND_SPRITES.BUILDING2.ordinal], offset(y = -41, x = -60), dimensions)
            draw(sprites[GROUND_SPRITES.BUILDING6.ordinal], offset(), dimensions)
        }
        // foreground
        game.batchQueue.add(2) {
            val z = 21
            draw(sprites[GROUND_SPRITES.BUILDING2.ordinal], offset(y = -4, x = 34, z = z), dimensions)
            draw(sprites[GROUND_SPRITES.BUILDING2.ordinal], offset(y = -50, x = -29, z = z), dimensions)
            draw(sprites[GROUND_SPRITES.BUILDING2.ordinal], offset(y = -81, x = 60, z = z), dimensions)
        }
    }

    private fun offset(x: Int = 0, y: Int = 0, z: Int = 0) = position.copy().apply {
        // convert offset from pixels to meters and add parallax effect
        val deltaCamY = game.camera.position.y - initCamY
        this.x += (x + BG_BASE_X_OFFSET) / resolution
        this.y += (y + BG_BASE_Y_OFFSET) / resolution + (y + z) * deltaCamY * PARALLAX_SCALE_FACTOR
    }
}
