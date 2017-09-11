package com.troggo.jmp.utils

import com.badlogic.gdx.InputAdapter

class TouchInput(private val touchDown: () -> Boolean) : InputAdapter() {
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = touchDown()
}
