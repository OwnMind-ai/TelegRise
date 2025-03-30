package org.telegrise.kotlin

import org.telegrise.telegrise.SessionMemory
import org.telegrise.telegrise.core.elements.Tree

/**
 * Retrieves a value from the session memory based on the specified key.
 *
 * @param key The key to look up the value.
 * @return The value associated with the key, or null if not found.
 */
inline fun <reified T> SessionMemory.value(key: String): T? {
    return get(key, T::class.java)
}

/**
 * Retrieves a value from the session memory based on the specified key and tree.
 *
 * @param key The key to look up the value.
 * @param tree The tree structure to use for the lookup.
 * @return The value associated with the key and tree, or null if not found.
 */
inline fun <reified T> SessionMemory.value(key: String, tree: Tree): T? {
    return get(key, tree, T::class.java)
}

/**
 * Retrieves a local value from the session memory based on the specified key.
 *
 * @param key The key to look up the local value.
 * @return The local value associated with the key, or null if not found.
 */
inline fun <reified T> SessionMemory.valueLocal(key: String): T? {
    return getLocal(key, T::class.java)
}

/**
 * Retrieves a component from the session memory.
 *
 * @return The component of the specified type, or null if not found.
 */
inline fun <reified T> SessionMemory.getComponent(): T? {
    return getComponent(T::class.java)
}

/**
 * Removes a component from the session memory.
 *
 * @return The removed component of the specified type, or null if not found.
 */
inline fun <reified T> SessionMemory.removeComponent(): T? {
    return removeComponent(T::class.java)
}

/**
 * Checks if a component of the specified type is present in the session memory.
 *
 * @return true if the component is present, false otherwise.
 */
inline fun <reified T> SessionMemory.containsComponent(): Boolean {
    return containsComponent(T::class.java)
}