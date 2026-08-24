package com.birthdayreminder.testutil

import android.net.TestUri
import android.net.Uri

/**
 * Test utilities for Android framework types that cannot be instantiated or mocked in plain JVM
 * unit tests.
 */
object TestUris {
    /**
     * Creates a content URI stand-in for testing.
     *
     * [android.net.Uri] cannot be constructed on the JVM (its static initializers throw
     * RuntimeException("Stub!")) nor mocked (ByteBuddy cannot instrument Android framework
     * classes), so a [TestUri] instance is allocated directly via sun.misc.Unsafe reflection
     * without invoking any constructor or static initializer.
     */
    fun fakeUri(): Uri {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateInstance =
            unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, TestUri::class.java) as Uri
    }
}
