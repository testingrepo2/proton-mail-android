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

package ch.protonmail.android.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import ch.protonmail.android.core.NetworkConnectivityManager
import ch.protonmail.android.utils.extensions.filter
import ch.protonmail.android.worker.PingWorker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import timber.log.Timber
import javax.inject.Inject

internal const val NETWORK_CHECK_DELAY = 800L
/**
 * Use case responsible for scheduling Worker that sends a ping message through [PingWorker]
 * and processes the result.
 */
class SendPing @Inject constructor(
    private val workerEnqueuer: PingWorker.Enqueuer,
    private val connectivityManager: NetworkConnectivityManager
) {

    private fun isInternetAvailable(): Boolean {
        Timber.v("isInternetAvailable check ${connectivityManager.isInternetConnectionPossible()}")
        return connectivityManager.isInternetConnectionPossible()
    }

    operator fun invoke(): LiveData<Boolean> {
        Timber.v("SendPing invoked")
        return liveData {
            emit(isInternetAvailable())
            emitSource(getPingState(workerEnqueuer.enqueue()))

            // get system connection events
            connectivityManager.isConnectionAvailableFlow()
                .filter { !it } // only disconnections
                .collect { emit(it) }
        }
    }

    private fun getPingState(workInfoLiveData: LiveData<WorkInfo?>): LiveData<Boolean> {
        return workInfoLiveData
            .filter { it?.state?.isFinished == true }
            .map { workInfo ->
                Timber.v(
                    "SendPing State: ${workInfo?.state} Net: ${connectivityManager.isInternetConnectionPossible()}"
                )
                workInfo?.state == WorkInfo.State.SUCCEEDED
            }
    }
}
