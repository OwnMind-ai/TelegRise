package org.telegrise.kotlin

import org.telegrise.telegrise.SessionMemory
import org.telegrise.telegrise.core.elements.Tree

inline fun <reified T> SessionMemory.value(key: String): T? {
    return get(key, T::class.java)
}

inline fun <reified T> SessionMemory.value(key: String, tree: Tree): T? {
    return get(key, tree, T::class.java)
}

inline fun <reified T> SessionMemory.valueLocal(key: String): T? {
    return getLocal(key, T::class.java)
}

inline fun <reified T> SessionMemory.getComponent(): T? {
    return getComponent(T::class.java)
}

inline fun <reified T> SessionMemory.removeComponent(): T? {
    return removeComponent(T::class.java)
}

inline fun <reified T> SessionMemory.containsComponent(): Boolean {
    return containsComponent(T::class.java)
}