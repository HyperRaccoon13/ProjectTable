package com.github.erdragh.projecttable.utils

class GenericTypeChecker<T : Any> (private val klass: Class<T>) {
    companion object {
        inline operator fun <reified T : Any> invoke() = GenericTypeChecker(T::class.java)
    }

    fun checkType(t: Any): Boolean {
        return when {
            klass.isAssignableFrom(t.javaClass) -> true
            else -> false
        }
    }
}
