package com.troggo.jmp.utils

class Timer(private val initTime: Float = 0f, private val underflow: Boolean = true) {
    private var t = initTime
    val isDone get() = t <= 0
    fun reset() = apply { t = initTime }
    fun add(time: Float) = apply { t += time }
    fun set(time: Float) = apply { t = time }
    fun step(time: Float) = apply {
        t -= time
        // use underflow = true to account for "over stepping" of the timer
        if (!underflow && t < 0) t = 0f
    }
}
