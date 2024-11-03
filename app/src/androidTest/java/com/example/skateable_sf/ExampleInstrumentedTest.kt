package com.example.skateable_sf

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.testng.annotations.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.skateable_sf", appContext.packageName)
    }
}