package com.abdapps.scriptmine.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network connectivity and provides information about connection type and quality
 * Used by sync system to make intelligent decisions about when and how to sync
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NetworkMonitor"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(getCurrentNetworkState())
    val networkState: Flow<NetworkState> = _networkState.asStateFlow()
    
    private val _isOnline = MutableStateFlow(isCurrentlyOnline())
    val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    /**
     * Observes network connectivity changes in real-time
     */
    fun observeNetworkChanges(): Flow<NetworkState> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                updateNetworkState()
                trySend(getCurrentNetworkState())
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost: $network")
                updateNetworkState()
                trySend(getCurrentNetworkState())
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d(TAG, "Network capabilities changed: $network")
                updateNetworkState()
                trySend(getCurrentNetworkState())
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Send initial state
        trySend(getCurrentNetworkState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
    
    /**
     * Gets the current network state
     */
    fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return if (networkCapabilities != null) {
            NetworkState(
                isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                connectionType = determineConnectionType(networkCapabilities),
                isMetered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
                signalStrength = getSignalStrength(networkCapabilities),
                isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            )
        } else {
            NetworkState(isConnected = false)
        }
    }
    
    /**
     * Checks if device is currently online
     */
    fun isCurrentlyOnline(): Boolean {
        return getCurrentNetworkState().isConnected
    }
    
    /**
     * Checks if current connection is WiFi
     */
    fun isWiFiConnected(): Boolean {
        return getCurrentNetworkState().connectionType == ConnectionType.WIFI
    }
    
    /**
     * Checks if current connection is metered (mobile data)
     */
    fun isMeteredConnection(): Boolean {
        return getCurrentNetworkState().isMetered
    }
    
    /**
     * Determines if network conditions are good for sync operations
     */
    fun isGoodForSync(): Boolean {
        val state = getCurrentNetworkState()
        return state.isConnected && 
               state.isValidated && 
               (state.connectionType == ConnectionType.WIFI || 
                state.signalStrength >= SignalStrength.GOOD)
    }
    
    /**
     * Determines if network conditions allow heavy sync operations
     */
    fun isGoodForHeavySync(): Boolean {
        val state = getCurrentNetworkState()
        return state.isConnected && 
               state.isValidated && 
               state.connectionType == ConnectionType.WIFI &&
               state.signalStrength >= SignalStrength.GOOD
    }
    
    /**
     * Gets recommended sync strategy based on current network conditions
     */
    fun getRecommendedSyncStrategy(): SyncStrategy {
        val state = getCurrentNetworkState()
        
        return when {
            !state.isConnected -> SyncStrategy.OFFLINE_ONLY
            
            state.connectionType == ConnectionType.WIFI && 
            state.signalStrength >= SignalStrength.GOOD -> SyncStrategy.FULL_SYNC
            
            state.connectionType == ConnectionType.WIFI -> SyncStrategy.INCREMENTAL_SYNC
            
            !state.isMetered && 
            state.signalStrength >= SignalStrength.GOOD -> SyncStrategy.INCREMENTAL_SYNC
            
            state.isMetered && 
            state.signalStrength >= SignalStrength.FAIR -> SyncStrategy.ESSENTIAL_ONLY
            
            else -> SyncStrategy.OFFLINE_ONLY
        }
    }
    
    /**
     * Updates internal network state
     */
    private fun updateNetworkState() {
        val newState = getCurrentNetworkState()
        _networkState.value = newState
        _isOnline.value = newState.isConnected
    }
    
    /**
     * Determines the connection type from network capabilities
     */
    private fun determineConnectionType(capabilities: NetworkCapabilities): ConnectionType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OTHER
        }
    }
    
    /**
     * Gets signal strength from network capabilities
     */
    private fun getSignalStrength(capabilities: NetworkCapabilities): SignalStrength {
        // Note: Signal strength detection is limited in Android
        // This is a simplified implementation
        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) -> SignalStrength.EXCELLENT
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> SignalStrength.GOOD
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> SignalStrength.FAIR
            
            else -> SignalStrength.POOR
        }
    }
}

/**
 * Represents the current state of network connectivity
 */
data class NetworkState(
    val isConnected: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE,
    val isMetered: Boolean = false,
    val signalStrength: SignalStrength = SignalStrength.POOR,
    val isValidated: Boolean = false
) {
    /**
     * Returns a human-readable description of the network state
     */
    fun getDescription(): String {
        return if (isConnected) {
            val meterInfo = if (isMetered) " (metered)" else ""
            val validationInfo = if (isValidated) " (validated)" else ""
            "${connectionType.displayName}$meterInfo$validationInfo - ${signalStrength.displayName}"
        } else {
            "Offline"
        }
    }
}

/**
 * Types of network connections
 */
enum class ConnectionType(val displayName: String) {
    NONE("No Connection"),
    WIFI("WiFi"),
    MOBILE("Mobile Data"),
    ETHERNET("Ethernet"),
    OTHER("Other")
}

/**
 * Signal strength levels
 */
enum class SignalStrength(val displayName: String) {
    POOR("Poor"),
    FAIR("Fair"),
    GOOD("Good"),
    EXCELLENT("Excellent")
}

/**
 * Recommended sync strategies based on network conditions
 */
enum class SyncStrategy {
    OFFLINE_ONLY,       // No sync, work offline only
    ESSENTIAL_ONLY,     // Sync only critical changes
    INCREMENTAL_SYNC,   // Sync recent changes
    FULL_SYNC          // Full synchronization
}