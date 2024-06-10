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
    fun onUserDisconnected(endpointId: String)

}

class BluetoothHelper(
    private val context: Context,
    private val listener: BluetoothHelperListener,
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
) {

    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private var endpointConnectionCallback: ConnectionLifecycleCallback? = null
    private var payloadCallback: PayloadCallback? = null

    // List to keep track of connected endpoints Map of endpoint IDs to usernames
    private val connectedEndpoints = mutableMapOf<String,String>()

    // Initialize Nearby Connections client
    init {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                val user = User(endpointId,info.endpointName)
                connectedEndpoints[endpointId] = info.endpointName
                listener.onUserDiscovered(user)

                showSystemMessage("User ${info.endpointName} found...")
                Log.d("BluetoothHelper", "Endpoint found: $endpointId (${info.endpointName})")

                connectionsClient.requestConnection(context.packageName, user.endpointId, endpointConnectionCallback!!)

                sendMessage(user.endpointId, Message("System", "New user connected : ${user.username}"))
                Log.d("BluetoothHelper", "Connecting to: ${user.endpointId} (${user.username})")

            }

            override fun onEndpointLost(endpointId: String) {
                // Handle lost endpoints if necessary
                connectionsClient.disconnectFromEndpoint(endpointId)
                Log.d("BluetoothHelper", "Disconnecting from: $endpointId")

                showSystemMessage("User ${connectedEndpoints[endpointId]} lost :(")
                connectedEndpoints.remove(endpointId)
                listener.onUserDisconnected(endpointId)
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
                    // Add the endpoint to the list of connected endpoints
                    showSystemMessage("User ${connectedEndpoints[endpointId]} connected ! :)")
                } else {
                    // Connection failed
                    Log.d("BluetoothHelper", "Connection failed with: $endpointId")
                    showSystemMessage("User ${connectedEndpoints[endpointId]} failed to connect :(")
                }
            }

            override fun onDisconnected(endpointId: String) {
                // Handle disconnection
                showSystemMessage("User ${connectedEndpoints[endpointId]} disconnected.")
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
        }
        Log.d("BluetoothHelper", "Advertising stopped")
    }

    // Send a message to all connected endpoints (broadcast)
    fun sendMessageToGlobalChat(message: Message) {
        val payload = Payload.fromBytes("${message.author}:${message.content}".toByteArray())
        connectedEndpoints.forEach { endpoint ->
            connectionsClient.sendPayload(endpoint.key, payload)
        }
        Log.d("BluetoothHelper", "Messages sent to all connected endpoints : ${connectedEndpoints.size}")
    }

    // Send a message to a specific endpoint
    fun sendMessage(endpointId: String, message: Message) {
        val payload = Payload.fromBytes("[Private]${message.author}:${message.content}".toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
        Log.d("BluetoothHelper", "Message sent to: $endpointId")
    }

    // Show a message in the chat
    fun showSystemMessage(content: String) {
        listener.onMessageReceived(Message("[System]", content))
    }
}
