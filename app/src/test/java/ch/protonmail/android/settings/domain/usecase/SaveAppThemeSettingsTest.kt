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

package ch.protonmail.android.settings.domain.usecase

import ch.protonmail.android.settings.domain.DeviceSettingsRepository
import ch.protonmail.android.settings.domain.model.AppThemeSettings
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

class SaveAppThemeSettingsTest {

    private val repository: DeviceSettingsRepository = mockk(relaxUnitFun = true)
    private val saveAppThemeSettings = SaveAppThemeSettings(repository)

    @Test
    fun `save correct data in the repository`() = runBlockingTest {
        // given
        val expected = AppThemeSettings.LIGHT

        // when
        saveAppThemeSettings(expected)

        // then
        coVerify { repository.saveAppThemeSettings(expected) }
    }
}
