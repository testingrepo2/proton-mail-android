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

package ch.protonmail.android.ui.actionsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.core.Constants
import ch.protonmail.android.labels.domain.usecase.MoveMessagesToFolder
import ch.protonmail.android.labels.presentation.ui.LabelsActionSheet
import ch.protonmail.android.mailbox.domain.ChangeConversationsReadStatus
import ch.protonmail.android.mailbox.presentation.ConversationModeEnabled
import ch.protonmail.android.repository.MessageRepository
import ch.protonmail.android.usecase.delete.DeleteMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.EMPTY_STRING
import javax.inject.Inject

@HiltViewModel
class MessageActionSheetViewModel @Inject constructor(
    private val deleteMessage: DeleteMessage,
    private val moveMessagesToFolder: MoveMessagesToFolder,
    private val messageRepository: MessageRepository,
    private val changeConversationsReadStatus: ChangeConversationsReadStatus,
    private val conversationModeEnabled: ConversationModeEnabled,
    private val accountManager: AccountManager
) : ViewModel() {

    private val actionsMutableFlow = MutableStateFlow<MessageActionSheetAction>(MessageActionSheetAction.Default)
    val actionsFlow: StateFlow<MessageActionSheetAction>
        get() = actionsMutableFlow

    fun showLabelsManager(
        messageIds: List<String>,
        currentLocation: Constants.MessageLocationType,
        labelsSheetType: LabelsActionSheet.Type = LabelsActionSheet.Type.LABEL
    ) {
        viewModelScope.launch {
            val showLabelsManager = MessageActionSheetAction.ShowLabelsManager(
                messageIds,
                currentLocation.messageLocationTypeValue,
                labelsSheetType
            )
            actionsMutableFlow.value = showLabelsManager
        }
    }

    fun deleteMessage(messageIds: List<String>) {
        viewModelScope.launch {
            deleteMessage(
                messageIds, Constants.MessageLocationType.TRASH.messageLocationTypeValue.toString()
            )
        }
    }

    fun moveToInbox(
        messageIds: List<String>,
        currentFolder: Constants.MessageLocationType
    ) = moveMessagesToFolder(
        messageIds, Constants.MessageLocationType.INBOX.toString(),
        currentFolder.messageLocationTypeValue.toString()
    )

    fun moveToArchive(
        messageIds: List<String>,
        currentFolder: Constants.MessageLocationType
    ) = moveMessagesToFolder(
        messageIds, Constants.MessageLocationType.ARCHIVE.toString(),
        currentFolder.messageLocationTypeValue.toString()
    )

    fun moveToSpam(
        messageIds: List<String>,
        currentFolder: Constants.MessageLocationType
    ) = moveMessagesToFolder(
        messageIds, Constants.MessageLocationType.SPAM.toString(),
        currentFolder.messageLocationTypeValue.toString()
    )

    fun moveToTrash(
        messageIds: List<String>,
        currentFolder: Constants.MessageLocationType
    ) = moveMessagesToFolder(
        messageIds, Constants.MessageLocationType.TRASH.toString(),
        currentFolder.messageLocationTypeValue.toString()
    )

    fun starMessage(messageId: List<String>) = messageRepository.starMessages(messageId)

    fun unStarMessage(messageId: List<String>) = messageRepository.unStarMessages(messageId)

    fun markUnread(
        ids: List<String>,
        location: Constants.MessageLocationType
    ) {
        viewModelScope.launch {
            if (conversationModeEnabled(location)) {
                accountManager.getPrimaryUserId().first()?.let {
                    changeConversationsReadStatus(
                        ids,
                        ChangeConversationsReadStatus.Action.ACTION_MARK_UNREAD,
                        it,
                        location
                    )
                }
            } else {
                messageRepository.markUnRead(ids)
            }
        }.invokeOnCompletion {
            actionsMutableFlow.value = MessageActionSheetAction.ChangeReadStatus(false)
        }
    }

    fun markRead(
        ids: List<String>,
        location: Constants.MessageLocationType
    ) {
        viewModelScope.launch {
            if (conversationModeEnabled(location)) {
                accountManager.getPrimaryUserId().first()?.let {
                    changeConversationsReadStatus(
                        ids,
                        ChangeConversationsReadStatus.Action.ACTION_MARK_READ,
                        it,
                        location
                    )
                }
            } else {
                messageRepository.markRead(ids)
            }
        }.invokeOnCompletion {
            actionsMutableFlow.value = MessageActionSheetAction.ChangeReadStatus(true)
        }
    }

    fun showMessageHeaders(messageId: String) {
        viewModelScope.launch {
            val message = messageRepository.findMessageById(messageId)
            actionsMutableFlow.value = MessageActionSheetAction.ShowMessageHeaders(message?.header ?: EMPTY_STRING)
        }
    }
}