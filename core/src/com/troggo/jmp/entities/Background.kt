package com.troggo.jmp.entities

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.troggo.jmp.Jmp
import com.troggo.jmp.utils.Dimensions
import com.troggo.jmp.utils.SpriteSheet
import com.troggo.jmp.utils.bottom
import com.troggo.jmp.utils.copy
import com.troggo.jmp.utils.draw
import com.troggo.jmp.utils.set
import com.troggo.jmp.utils.top

private const val BG_BASE_X_OFFSET = 0          // px
private const val BG_BASE_Y_OFFSET = 13         // px
private const val BG_PARALLAX_FACTOR = 0.003f
private const val CLOUD_PARALLAX_FACTOR = 0.1f

private enum class BG_SPRITES {
    BUILDING6,
    BUILDING2,
    BUILDING3
}

class Background(game: Jmp) : Body(
    game,
    sprites = SpriteSheet("background.png", cols = 3),
    x = game.camera.viewportWidth / 2f,
    width = Jmp.WORLD_WIDTH,
    isSensor = true,
    type = BodyDef.BodyType.StaticBody
) {
    private val resolution = sprites[0].width / Jmp.WORLD_WIDTH
    private val initCamY = game.camera.position.y
    private val clouds = Clouds()

    override fun dispose() = clouds.dispose()
    fun reset() = clouds.reset()
    override fun render(delta: Float) {
        // background
        game.batchQueue.add(-1) {
            clouds.render(this)
            draw(sprites[BG_SPRITES.BUILDING2.ordinal], offset(y = 66, x = -3), dimensions)
            draw(sprites[BG_SPRITES.BUILDING3.ordinal], offset(y = 36, x = 30), dimensions)
            draw(sprites[BG_SPRITES.BUILDING2.ordinal], offset(y = -41, x = -60), dimensions)
            draw(sprites[BG_SPRITES.BUILDING6.ordinal], offset(), dimensions)
        }
        // foreground
        game.batchQueue.add(2) {
            val z = 21
            draw(sprites[BG_SPRITES.BUILDING2.ordinal], offset(y = -5, x = 34, z = z), dimensions)
            draw(sprites[BG_SPRITES.BUILDING2.ordinal], offset(y = -50, x = -29, z = z), dimensions)
            draw(sprites[BG_SPRITES.BUILDING2.ordinal], offset(y = -81, x = 60, z = z), dimensions)
        }
    }

    private fun offset(x: Int = 0, y: Int = 0, z: Int = 0) = position.copy().apply {
        // convert offset from pixels to meters and add parallax effect
        val deltaCamY = game.camera.position.y - initCamY
        this.x += (x + BG_BASE_X_OFFSET) / resolution
        this.y += (y + BG_BASE_Y_OFFSET) / resolution + (y + z) * deltaCamY * BG_PARALLAX_FACTOR
    }

    inner class Clouds {
        private val sprites = SpriteSheet("clouds.png")
        private val dimensions = Dimensions(sprites.width / resolution, sprites.height / resolution)
        private var clouds = listOf<Vector2>()
        private var cloudsY = 0f
        fun dispose() = sprites.dispose()
        fun reset() {
            clouds = listOf()
            cloudsY = 0f
        }
        fun render(batch: Batch) {
            clouds = clouds.filter { it.y > game.camera.bottom - dimensions.height }
            while (cloudsY < game.camera.top) {
                cloudsY += 6 + Math.random().toFloat() * 6f
                clouds += Vector2(Math.random().toFloat() * Jmp.WORLD_WIDTH, cloudsY)
            }
            clouds
                .map { it.copy().set(y = it.y - (game.camera.position.y - it.y) * CLOUD_PARALLAX_FACTOR) }
                .forEach { position -> batch.draw(sprites[0], position, dimensions) }
        }
    }
}
