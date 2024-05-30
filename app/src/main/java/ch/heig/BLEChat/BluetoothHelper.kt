package ch.heig.BLEChat

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import ch.heig.BLEChat.Model.Message
import ch.heig.BLEChat.Model.User
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

interface BluetoothHelperListener {
    fun onMessageReceived(message: Message)
    fun onUserDiscovered(user: User)

}

class BluetoothHelper(private val context: Context, private val listener: BluetoothHelperListener) {

    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private var endpointConnectionCallback: ConnectionLifecycleCallback? = null
    private var payloadCallback: PayloadCallback? = null

    private var connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val connectedEndpoints = mutableSetOf<String>() // List to keep track of connected endpoints

    // Initialize Nearby Connections client
    init {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                val user = User(endpointId,info.endpointName)
                listener.onUserDiscovered(user)
                connectToEndpoint(user)

                Log.d("BluetoothHelper", "Endpoint found: $endpointId (${info.endpointName})")
            }

            override fun onEndpointLost(endpointId: String) {
                // Handle lost endpoints if necessary
                disconnectFromEndpoint(endpointId)
                Log.d("BluetoothHelper", "Endpoint lost: $endpointId")
            }
        }

        endpointConnectionCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Thread.sleep(200)
                connectionsClient.acceptConnection(endpointId, payloadCallback!!)

                Log.d("BluetoothHelper", "Connection initiated with: ${connectionInfo.endpointName}")
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                if (result.status.isSuccess) {
                    // Connection established
                    Log.d("BluetoothHelper", "Connection established with: $endpointId")
                } else {
                    // Connection failed
                    Log.d("BluetoothHelper", "Connection failed with: $endpointId")
                }
            }

            override fun onDisconnected(endpointId: String) {
                // Handle disconnection
                connectedEndpoints.remove(endpointId)
                Log.d("BluetoothHelper", "Disconnected from: $endpointId")
            }
        }

        payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                val receivedMessage = payload.asBytes()?.let {
                    deserializeMessage(it)
                }
                receivedMessage?.let {
                    Handler(Looper.getMainLooper()).post {
                        listener.onMessageReceived(receivedMessage)
                    }
                }
                Log.d("BluetoothHelper", "Payload received from: $endpointId")
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                // Handle payload transfer update
                Log.d("BluetoothHelper", "Payload transfer update from: $endpointId")
            }
        }
    }

    private fun deserializeMessage(bytes: ByteArray): Message? {
        return try {
            val messageStr = String(bytes)
            val parts = messageStr.split(":")
            if (parts.size == 2) {
                Message(parts[0], parts[1])
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Start discovering nearby devices
    fun startDiscovery(strategy: Strategy) {
        val options = DiscoveryOptions.Builder()
            .setStrategy(strategy)
            .build()

        connectionsClient.startDiscovery(
            context.packageName, endpointDiscoveryCallback!!, options
        ).addOnSuccessListener{
            Log.d("BluetoothHelper", "Discovery started")
        }.addOnFailureListener {
            e -> Log.e("BluetoothHelper", "Failed to start discovery: ${e.message}")
        }
    }

    // Stop discovering nearby devices
    fun stopDiscovery() {
        endpointDiscoveryCallback?.let {
            connectionsClient.stopDiscovery()
            endpointDiscoveryCallback = null
        }
        Log.d("BluetoothHelper", "Discovery stopped")
    }

    // Start advertising this device
    fun startAdvertising(strategy: Strategy, username: String) {
        val options = AdvertisingOptions.Builder()
            .setStrategy(strategy)
            .build()

        connectionsClient.startAdvertising(
            username, context.packageName, endpointConnectionCallback!!, options
        ).addOnSuccessListener {
            Log.d("BluetoothHelper", "Advertising started successfully")
        }.addOnFailureListener { e ->
            Log.e("BluetoothHelper", "Failed to start advertising: ${e.message}")
        }
    }

    // Stop advertising this device
    fun stopAdvertising() {
        endpointConnectionCallback?.let {
            connectionsClient.stopAdvertising()
            endpointConnectionCallback = null
        }
        Log.d("BluetoothHelper", "Advertising stopped")
    }

    // Connect to a remote endpoint
    fun connectToEndpoint(user: User) {
        connectionsClient.requestConnection(context.packageName, user.endpointId, endpointConnectionCallback!!)

        connectedEndpoints.add(user.endpointId)
        sendMessage(user.endpointId, Message("System", "New user connected : ${user.username}"))
        Log.d("BluetoothHelper", "Connecting to: ${user.endpointId} (${user.username})")
    }

    // Disconnect from a remote endpoint
    fun disconnectFromEndpoint(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        connectedEndpoints.remove(endpointId)
        Log.d("BluetoothHelper", "Disconnecting from: $endpointId")
    }

    // Send a message to all connected endpoints (broadcast)
    fun sendMessageToGlobalChat(message: Message) {
        val payload = Payload.fromBytes("${message.author}:${message.content}".toByteArray())
        connectedEndpoints.forEach { endpointId ->
            connectionsClient.sendPayload(endpointId, payload)
        }
        Log.d("BluetoothHelper", "Messages sent to all connected endpoints : ${connectedEndpoints.size}")
    }

    // Send a message to a specific endpoint
    fun sendMessage(endpointId: String, message: Message) {
        val payload = Payload.fromBytes("${message.author}:${message.content}".toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
        Log.d("BluetoothHelper", "Message sent to: $endpointId")
    }
}
