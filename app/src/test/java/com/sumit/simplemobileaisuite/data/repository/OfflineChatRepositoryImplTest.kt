package com.sumit.simplemobileaisuite.data.repository

import com.sumit.simplemobileaisuite.data.datasource.local.OfflineLLMDataSource
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OfflineChatRepositoryImplTest {

    private lateinit var localDataSource: OfflineLLMDataSource
    private lateinit var repository: OfflineChatRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk()
        every { localDataSource.partialResults } returns MutableSharedFlow()
        every { localDataSource.offlineLLMStatus } returns MutableStateFlow(OfflineLLMStatus.Idle)
        repository = OfflineChatRepositoryImpl(localDataSource)
    }

    @Test
    fun `initializationState should reflect data source status`() {
        val status = OfflineLLMStatus.Ready
        every { localDataSource.offlineLLMStatus } returns MutableStateFlow(status)

        // Re-instantiate to catch the new mocked flow
        repository = OfflineChatRepositoryImpl(localDataSource)

        assertEquals(status, repository.offlineLLMStatus.value)
    }

    @Test
    fun `initialize should delegate to data source`() {
        every { localDataSource.initialize() } returns Unit
        repository.initialize()
        verify { localDataSource.initialize() }
    }

    @Test
    fun `generateResponse should delegate to data source`() {
        val prompt = "Hello"
        every { localDataSource.generateResponse(prompt) } returns Unit
        repository.generateResponse(prompt)
        verify { localDataSource.generateResponse(prompt) }
    }

    @Test
    fun `close should delegate to data source`() {
        every { localDataSource.close() } returns Unit
        repository.close()
        verify { localDataSource.close() }
    }
}
