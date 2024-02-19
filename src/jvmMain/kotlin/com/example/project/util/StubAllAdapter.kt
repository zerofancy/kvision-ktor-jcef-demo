package com.example.project.util

import java.lang.reflect.Proxy

/**
 * 你糊弄我我糊弄你
 */
inline fun <reified T> stubAll(): T {
    return Proxy.newProxyInstance(Unit.javaClass.classLoader, arrayOf(T::class.java)
    ) { proxy, method, args -> null } as T
}