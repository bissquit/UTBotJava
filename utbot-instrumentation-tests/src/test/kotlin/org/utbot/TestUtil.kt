package org.utbot

import java.io.Serializable


class UtPair<K, V>(key: K, value: V) : Serializable {

    val key: K?
    val value: V?

    init {
        this.key = key
        this.value = value
    }

    override fun toString(): String {
        return key.toString() + "=" + value
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 + (value?.hashCode() ?: 0)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is UtPair<*, *>) {
            if (if (key != null) key != other.key else other.key != null) return false
            return !if (value != null) value != other.value else other.value != null
        }
        return false
    }
}