package com.troggo.jmp.utils

import com.badlogic.gdx.graphics.Camera

val Camera.top
    get() = position.y + viewportHeight / 2

val Camera.bottom
    get() = position.y - viewportHeight / 2
