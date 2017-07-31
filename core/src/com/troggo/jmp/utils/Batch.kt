package com.troggo.jmp.utils

import com.badlogic.gdx.graphics.g2d.Batch
import com.troggo.jmp.entities.Entity

fun Batch.draw(entity: Entity, flipX: Boolean = false, flipY: Boolean = false) = with (entity) {
    // render texture at body's position, scaled to its size
    val (x, y) = with (body.position) { Pair(x, y) }
    val (w, h) = dimensions
    draw(texture, x - w / 2, y - h / 2, w, h, 0, 0, texture.width, texture.height, flipX, flipY)
}
