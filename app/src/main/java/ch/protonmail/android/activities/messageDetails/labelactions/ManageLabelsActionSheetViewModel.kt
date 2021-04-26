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

package ch.protonmail.android.activities.messageDetails.labelactions

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.activities.messageDetails.labelactions.domain.GetAllLabels
import ch.protonmail.android.activities.messageDetails.labelactions.domain.MoveMessagesToFolder
import ch.protonmail.android.activities.messageDetails.labelactions.domain.StandardFolderLocation
import ch.protonmail.android.activities.messageDetails.labelactions.domain.UpdateLabels
import ch.protonmail.android.core.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ManageLabelsActionSheetViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val getAllLabels: GetAllLabels,
    private val userManager: UserManager,
    private val updateLabels: UpdateLabels,
    private val moveMessagesToFolder: MoveMessagesToFolder
) : ViewModel() {

    private val initialLabelsSelection = savedStateHandle.get<List<String>>(
        ManageLabelsActionSheet.EXTRA_ARG_MESSAGE_CHECKED_LABELS
    ) ?: emptyList()

    private val labelsSheetType = savedStateHandle.get<ManageLabelsActionSheet.Type>(
        ManageLabelsActionSheet.EXTRA_ARG_ACTION_SHEET_TYPE
    ) ?: ManageLabelsActionSheet.Type.LABEL

    private val messageIds = savedStateHandle.get<List<String>>(ManageLabelsActionSheet.EXTRA_ARG_MESSAGES_IDS)
        ?: emptyList()

    private val labelsMutableFlow = MutableStateFlow(emptyList<ManageLabelItemUiModel>())
    private val actionsResultMutableFlow = MutableStateFlow<ManageLabelActionResult>(ManageLabelActionResult.Default)

    val labels: StateFlow<List<ManageLabelItemUiModel>>
        get() = labelsMutableFlow

    val actionsResult: StateFlow<ManageLabelActionResult>
        get() = actionsResultMutableFlow

    init {
        viewModelScope.launch {
            labelsMutableFlow.value = getAllLabels.invoke(initialLabelsSelection, labelsSheetType)
        }
    }

    fun onLabelClicked(model: ManageLabelItemUiModel) {

        if (model.labelType == ManageLabelsActionSheet.Type.FOLDER.typeInt) {
            onFolderClicked(model.labelId)
        } else {
            // label type clicked
            val updatedLabels = labels.value.map { label ->
                if (label.labelId == model.labelId) {
                    Timber.v("Label: ${label.labelId} was clicked")
                    label.copy(isChecked = model.isChecked?.not())
                } else {
                    label
                }
            }

            val selectedLabelsCount = updatedLabels.filter { it.isChecked == true }
            if (selectedLabelsCount.isNotEmpty() &&
                userManager.didReachLabelsThreshold(selectedLabelsCount.size)
            ) {
                actionsResultMutableFlow.value =
                    ManageLabelActionResult.ErrorLabelsThresholdReached(userManager.getMaxLabelsAllowed())
            } else {
                labelsMutableFlow.value = updatedLabels
                actionsResultMutableFlow.value = ManageLabelActionResult.Default
            }
        }
    }

    fun onDoneClicked(shallMoveToArchive: Boolean = false) {
        if (labelsSheetType == ManageLabelsActionSheet.Type.LABEL) {
            onLabelDoneClicked(messageIds, shallMoveToArchive)
        } else {
            throw IllegalStateException("This action is unsupported for type $labelsSheetType")
        }
    }

    private fun onLabelDoneClicked(messageIds: List<String>, shallMoveToArchive: Boolean) {
        if (messageIds.isNotEmpty()) {
            viewModelScope.launch {
                val selectedLabels = labels.value
                    .filter { it.isChecked == true }
                    .map { it.labelId }
                Timber.v("Selected labels: $selectedLabels messageId: $messageIds")
                messageIds.forEach { messageId ->
                    updateLabels(
                        messageId,
                        selectedLabels
                    )
                }

                if (shallMoveToArchive) {
                    moveMessagesToFolder(messageIds, StandardFolderLocation.Archive.id)
                }

                actionsResultMutableFlow.value = ManageLabelActionResult.LabelsSuccessfullySaved
            }
        } else {
            Timber.i("Cannot continue messages list is null or empty!")
        }
    }

    private fun onFolderClicked(selectedFolderId: String) {
        moveMessagesToFolder(messageIds, selectedFolderId)
        actionsResultMutableFlow.value = ManageLabelActionResult.MessageSuccessfullyMoved
    }
}
