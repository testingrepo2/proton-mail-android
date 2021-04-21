/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package ch.protonmail.android.mailbox.domain

import ch.protonmail.android.core.Constants.MessageLocationType
import ch.protonmail.android.domain.entity.Id
import ch.protonmail.android.mailbox.domain.model.GetConversationsParameters
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.test.kotlin.CoroutinesTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetConversationsTest : CoroutinesTest {


    private var userId = Id("id")

    @RelaxedMockK
    private lateinit var conversationRepository: ConversationsRepository

    private lateinit var getConversations: GetConversations

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        getConversations = GetConversations(
            conversationRepository
        )
    }

    @Test
    fun getConversationsCallsRepositoryWithReceivedLocation() = runBlockingTest {
        val location = MessageLocationType.ARCHIVE
        coEvery { conversationRepository.getConversations(any()) } returns flowOf()

        getConversations.invoke(userId, location)

        val params = GetConversationsParameters(
            0,
            labelId = location.messageLocationTypeValue.toString(),
            userId = userId
        )
        coVerify { conversationRepository.getConversations(params) }
    }

    @Test
    fun getConversationsReturnsConversationsFlowWhenRepositoryRequestSucceeds() = runBlockingTest {
        val conversations = listOf(buildRandomConversation())
        val dataResult = DataResult.Success(ResponseSource.Remote, conversations)
        coEvery { conversationRepository.getConversations(any()) } returns flowOf(dataResult)

        val actual = getConversations.invoke(userId, MessageLocationType.INBOX)

        val expected = GetConversationsResult.Success(conversations)
        assertEquals(expected, actual.first())
    }

    @Test
    fun getConversationsReturnsErrorWhenRepositoryFailsGettingConversations() = runBlockingTest {
        coEvery { conversationRepository.getConversations(any()) } returns flowOf(DataResult.Error.Local(null))

        val actual = getConversations.invoke(userId, MessageLocationType.INBOX)

        val error = GetConversationsResult.Error
        assertEquals(error, actual.first())
    }

    private fun buildRandomConversation() = Conversation(
        UUID.randomUUID().toString(),
        "Conversation subject",
        listOf(),
        listOf(),
        5,
        2,
        1,
        0,
        listOf(),
        listOf()
    )
}
