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

import ch.protonmail.android.core.Constants
import ch.protonmail.android.domain.entity.Id
import ch.protonmail.android.mailbox.domain.model.GetConversationsParameters
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId

interface ConversationsRepository {

    /**
     * @param params a model representing the params needed to define which conversations to get
     *
     * @return a List<Conversation> when the repository could successfully get conversations from some data source.
     * @return an empty optional when the repository encounters a handled failure getting conversations
     * @throws exception when the repository fails getting conversations for any unhandled reasons
     */
    fun getConversations(params: GetConversationsParameters): Flow<DataResult<List<Conversation>>>

    /**
     * @param conversationId the encrypted id of the conversation to get
     *
     * @return a Conversation object containing list of messages when the repository could successfully get it from some data source.
     * @return an empty optional when the repository encounters a handled failure getting the given conversation
     * @throws exception when the repository fails getting this conversation for any unhandled reasons
     */
    fun getConversation(conversationId: String, userId: Id): Flow<DataResult<Conversation>>

    /**
     * Deletes all the conversations from the [TABLE_CONVERSATIONS] inside the local storage
     */
    fun clearConversations()

    fun loadMore(params: GetConversationsParameters)

    suspend fun markRead(conversationIds: List<String>)

    suspend fun markUnread(conversationIds: List<String>, userId: UserId, location: Constants.MessageLocationType)
}