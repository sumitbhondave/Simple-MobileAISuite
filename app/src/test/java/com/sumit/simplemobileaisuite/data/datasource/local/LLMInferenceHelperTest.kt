package com.sumit.simplemobileaisuite.data.datasource.local

import android.content.Context
import android.util.Log
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.ui.screens.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class LLMInferenceHelperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var helper: LLMInferenceHelper

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        context = mockk(relaxed = true)
        helper = LLMInferenceHelper(context)
    }

    @Test
    fun `initialize should emit Error when model file does not exist`() = runTest {
        val tempDir = File(System.getProperty("java.io.tmpdir")!!)
        every { context.filesDir } returns tempDir

        helper.initialize()

        helper.offlineLLMStatus.test {
            val status = expectMostRecentItem()
            assertTrue(status is OfflineLLMStatus.Error)
        }
    }
}
