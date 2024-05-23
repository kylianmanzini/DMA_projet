package ch.heig.BLEChat
import android.content.Context
import android.os.Handler
import android.os.Looper
import ch.heig.BLEChat.Model.Message
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

interface BluetoothHelperListener {
    fun onMessageReceived(message: Message)
}

class BluetoothHelper(private val context: Context, private val listener: BluetoothHelperListener) {

    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private var endpointConnectionCallback: ConnectionLifecycleCallback? = null
    private var payloadCallback: PayloadCallback? = null

    private var connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    // Initialize Nearby Connections client
    init {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // Handle discovered endpoints
            }

            override fun onEndpointLost(endpointId: String) {
                // Handle lost endpoints
            }
        }

        endpointConnectionCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Handle connection initiation
                connectionsClient.acceptConnection(endpointId, payloadCallback!!)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                // Handle connection result
            }

            override fun onDisconnected(endpointId: String) {
                // Handle disconnection
            }
        }

        payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // Handle received payload (message)
                val receivedMessage = payload.asBytes()?.let {
                    deserializeMessage(it)
                }
                receivedMessage?.let {
                    // Notify listener on the main thread
                    Handler(Looper.getMainLooper()).post {
                        listener.onMessageReceived(receivedMessage)
                    }
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                // Handle payload transfer update
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
        connectionsClient.startDiscovery(context.packageName, endpointDiscoveryCallback!!, DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    // Stop discovering nearby devices
    fun stopDiscovery() {
        endpointDiscoveryCallback?.let {
            connectionsClient.stopDiscovery()
            endpointDiscoveryCallback = null
        }
    }

    // Start advertising this device
    fun startAdvertising(strategy: Strategy) {
        connectionsClient.startAdvertising(context.packageName, context.packageName, endpointConnectionCallback!!, AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    // Stop advertising this device
    fun stopAdvertising() {
        endpointConnectionCallback?.let {
            connectionsClient.stopAdvertising()
            endpointConnectionCallback = null
        }
    }

    // Connect to a remote endpoint
    fun connectToEndpoint(endpointId: String) {
        connectionsClient.requestConnection(context.packageName, endpointId, endpointConnectionCallback!!)
    }

    // Disconnect from a remote endpoint
    fun disconnectFromEndpoint(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
    }

    // Send a message (payload) to a remote endpoint
    fun sendMessage(endpointId: String, message: Message) {
        val payload = Payload.fromBytes("${message.author}:${message.content}".toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }
}
