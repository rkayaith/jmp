package com.troggo.jmp.utils

class CappedArrayList<T>(private val maxSize: Int) : ArrayList<T>() {
    override fun add(element: T): Boolean {
        while (size >= maxSize) removeAt(0)
        super.add(element)
        return true
    }
}
