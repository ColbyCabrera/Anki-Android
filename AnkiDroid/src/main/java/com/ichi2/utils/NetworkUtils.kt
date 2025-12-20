/*
 *  Copyright (c) 2022 Brayan Oliveira <brayandso.dev@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.utils

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.ichi2.anki.AnkiDroidApp

object NetworkUtils {
    private val connectivityManager: ConnectivityManager?
        get() =
            AnkiDroidApp
                .instance
                .applicationContext
                .getSystemService()

    /**
     * @return whether the active network is metered
     * or false in case internet cannot be accessed
     */
    fun isActiveNetworkMetered(): Boolean =
        isOnline &&
            connectivityManager
                ?.isActiveNetworkMetered
                ?: true

    /**
     * @return whether is possible to access the internet
     */
    val isOnline: Boolean
        get() {
            val cm = connectivityManager ?: return false

            // ConnectivityManager.activeNetwork is for SDK ≥ 23
            val networkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            val isOnline =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            // internet capabilities can be checked if internet is temporarily disabled as well
            return isOnline && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
        }
}
