package com.troggo.jmp.utils

import com.badlogic.gdx.math.Vector2

operator fun Vector2.component1() = x
operator fun Vector2.component2() = y
fun Vector2.set(x: Float = this.x, y: Float = this.y) = set(x, y)
fun Vector2.copy() = Vector2(x, y)
