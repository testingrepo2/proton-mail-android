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

package ch.protonmail.android.di

import ch.protonmail.android.core.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.humanverification.presentation.CaptchaApiHost
import me.proton.core.humanverification.presentation.utils.HumanVerificationVersion

@Module
@InstallIn(SingletonComponent::class)
object HumanVerificationModule {

    @Provides
    // Can be either HumanVerificationVersion.HV2 or HV3.
    fun provideHumanVerificationVersion() = HumanVerificationVersion.HV3

    @Provides
    @CaptchaApiHost
    // Legacy Captcha api host dependency. Should point to 'api.$HOST'.
    fun provideCaptchaApiHost(): String = Constants.API_HOST
}
