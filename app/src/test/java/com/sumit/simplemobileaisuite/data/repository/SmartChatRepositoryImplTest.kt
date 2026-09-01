package com.sumit.simplemobileaisuite.data.repository

import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import com.sumit.simplemobileaisuite.domain.repository.NetworkMonitor
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SmartChatRepositoryImplTest {

    private lateinit var offlineChatRepository: OfflineChatRepository
    private lateinit var geminiRepository: GeminiRepository
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var repository: SmartChatRepositoryImpl

    @Before
    fun setup() {
        offlineChatRepository = mockk()
        geminiRepository = mockk()
        networkMonitor = mockk()
        repository =
            SmartChatRepositoryImpl(offlineChatRepository, geminiRepository, networkMonitor)
    }

    @Test
    fun `when local LLM is ready, it should emit from local repository`() = runTest {
        val prompt = "Hello"
        val localFlow = MutableSharedFlow<Pair<String, Boolean>>()

        every { offlineChatRepository.offlineLLMStatus } returns MutableStateFlow(OfflineLLMStatus.Ready)
        every { offlineChatRepository.generateResponse(prompt) } returns Unit
        every { offlineChatRepository.partialResults } returns localFlow

        repository.getSmartResponse(prompt).test {
            localFlow.emit("Local " to false)
            assertEquals(SmartResponse.TextChunk("Local "), awaitItem())
            localFlow.emit("Response" to true)
            assertEquals(SmartResponse.TextChunk("Response"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when local LLM is not ready, it should fallback to cloud if online`() = runTest {
        val prompt = "Hello"

        every { offlineChatRepository.offlineLLMStatus } returns MutableStateFlow(OfflineLLMStatus.Loading)
        every { networkMonitor.isOnline() } returns true
        every { geminiRepository.generateContentStream(prompt) } returns flowOf("Cloud Response")

        repository.getSmartResponse(prompt).test {
            assertEquals(SmartResponse.SystemEvent.LocalAiNotInitialized, awaitItem())
            assertEquals(SmartResponse.SystemEvent.CloudFallbackStarted, awaitItem())
            assertEquals(SmartResponse.TextChunk("Cloud Response"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when local LLM is not ready and offline, it should emit error event`() = runTest {
        val prompt = "Hello"

        every { offlineChatRepository.offlineLLMStatus } returns MutableStateFlow(OfflineLLMStatus.Idle)
        every { networkMonitor.isOnline() } returns false

        repository.getSmartResponse(prompt).test {
            assertEquals(SmartResponse.SystemEvent.LocalAiNotInitialized, awaitItem())
            assertEquals(SmartResponse.SystemEvent.CloudFallbackStarted, awaitItem())
            assertEquals(SmartResponse.SystemEvent.OfflineNoLocalFallback, awaitItem())
            awaitComplete()
        }
    }
}
