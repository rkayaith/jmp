package com.troggo.jmp.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2

fun Batch.draw(sprite: Sprite, position: Vector2, dimensions: Dimensions, flipX: Boolean = false, flipY: Boolean = false) {
    val (texture, sw, sh, sx, sy) = sprite
    val (x, y) = position
    val (w, h) = dimensions
    draw(texture, x - w / 2, y - h / 2, w, h, sx, sy, sw, sh, flipX, flipY)
}

fun Batch.withAlpha(alpha: Float, fn: Batch.() -> Unit) {
    val c = color
    color = Color(c).apply { a = alpha}
    fn()
    color = c
}

fun Batch.run(queue: BatchQueue) = queue.run {
    sortBy { (z, _) -> z }
    forEach { (_, fn) -> fn() }
    clear()
}

class BatchQueue : ArrayList<Pair<Int, Batch.() -> Unit>>() {
    fun add(fn: Batch.() -> Unit) = add(0, fn)
    fun add(z: Int, fn: Batch.() -> Unit) { add(Pair(z, fn)) }
}
