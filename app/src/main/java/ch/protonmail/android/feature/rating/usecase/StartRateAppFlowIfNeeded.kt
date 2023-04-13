/*
 * Copyright (c) 2022 Proton AG
 *
 * This file is part of Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see https://www.gnu.org/licenses/.
 */

package ch.protonmail.android.feature.rating.usecase

import ch.protonmail.android.feature.rating.MailboxScreenViewInMemoryRepository
import ch.protonmail.android.feature.rating.StartRateAppFlow
import ch.protonmail.android.featureflags.MailFeatureFlags
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import javax.inject.Inject

class StartRateAppFlowIfNeeded @Inject constructor(
    private val mailboxScreenViewsRepository: MailboxScreenViewInMemoryRepository,
    private val featureFlagManager: FeatureFlagManager,
    private val startRateAppFlow: StartRateAppFlow
) {

    suspend operator fun invoke(userId: UserId) {
        if (!isShowReviewFeatureFlagEnabled(userId)) {
            return
        }
        if (mailboxScreenViewsRepository.screenViewCount < MailboxScreenViewsThreshold) {
            return
        }
        startRateAppFlow()
        recordReviewFlowStarted(userId)
    }

    private suspend fun recordReviewFlowStarted(userId: UserId) {
        val featureFlag = getShowReviewFeatureFlag(userId)
        val offFeatureFlag = featureFlag.copy(defaultValue = false, value = false)
        featureFlagManager.update(offFeatureFlag)
    }

    private suspend fun isShowReviewFeatureFlagEnabled(userId: UserId) = getShowReviewFeatureFlag(userId).value

    private suspend fun getShowReviewFeatureFlag(
        userId: UserId
    ) = featureFlagManager.getOrDefault(
        userId,
        MailFeatureFlags.ShowReviewAppDialog.featureId,
        FeatureFlag.default(MailFeatureFlags.ShowReviewAppDialog.featureId.id, false)
    )

    companion object {
        private const val MailboxScreenViewsThreshold = 2
    }
}