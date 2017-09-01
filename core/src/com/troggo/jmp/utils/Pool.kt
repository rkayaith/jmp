package com.troggo.jmp.utils

interface Poolable {
    fun reset()
}

open class Pool<T: Poolable>(private val _create: ((Pool<T>) -> T)? = null) {
    private val free = ArrayList<T>()
    private val obtained = ArrayList<T>()
    val pool get() = free + obtained

    open protected fun create(pool: Pool<T>): T {
        return _create?.invoke(pool) ?: throw Exception(
            "Provide create function to constructor or override 'fun create'"
        )
    }
    fun obtain(): T {
        return (if (free.isNotEmpty()) free.removeAt(0).apply { reset() } else create(this)).also {
            obtained.add(it)
        }
    }
    fun free(obj: T) {
        if (obtained.remove(obj)) free.add(obj) else throw IllegalArgumentException("Object is not in pool")
    }
    fun empty() = pool.also {
        free.clear()
        obtained.clear()
    }
}
