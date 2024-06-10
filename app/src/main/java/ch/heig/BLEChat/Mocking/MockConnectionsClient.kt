package ch.heig.BLEChat

import com.google.android.gms.common.api.Status
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.mockito.Mockito

class MockConnectionsClient : ConnectionsClient {

    private val mockConnectionsClient = Mockito.mock(ConnectionsClient::class.java)

    override fun startDiscovery(
        serviceId: String,
        endpointDiscoveryCallback: EndpointDiscoveryCallback,
        options: DiscoveryOptions
    ): Task<Void> {
        // Simulate discovery of endpoints
        endpointDiscoveryCallback.onEndpointFound("endpoint1", DiscoveredEndpointInfo(serviceId, "User1"))
        endpointDiscoveryCallback.onEndpointFound("endpoint2", DiscoveredEndpointInfo(serviceId, "User2"))
        return Tasks.forResult(null)
    }

    override fun stopDiscovery() {
        // No-op implementation for stopping discovery
    }

    override fun stopAdvertising() {
        // No-op implementation for stopping advertising
    }

    override fun stopAllEndpoints() {
        // No-op implementation for stopping all endpoints
    }

    override fun getApiKey(): ApiKey<ConnectionsOptions> {
        TODO("Not yet implemented")
    }

    override fun acceptConnection(endpointId: String, payloadCallback: PayloadCallback): Task<Void> {
        // Simulate accepting connection
        return Tasks.forResult(null)
    }

    override fun rejectConnection(endpointId: String): Task<Void> {
        // Simulate rejecting connection
        return Tasks.forResult(null)
    }

    override fun requestConnection(
        name: String,
        endpointId: String,
        connectionLifecycleCallback: ConnectionLifecycleCallback
    ): Task<Void> {
        // Simulate connection request
        connectionLifecycleCallback.onConnectionInitiated(
            endpointId,
            ConnectionInfo(name, false.toString(), true)
        )
        connectionLifecycleCallback.onConnectionResult(
            endpointId,
            ConnectionResolution(Status(Status.RESULT_SUCCESS.statusCode))
        )
        return Tasks.forResult(null)
    }

    override fun cancelPayload(p0: Long): Task<Void> {
        // No-op implementation for cancelling payload
        return Tasks.forResult(null)
    }

    override fun sendPayload(endpointId: String, payload: Payload): Task<Void> {
        // Simulate sending payload
        return Tasks.forResult(null)
    }

    override fun requestConnection(p0: ByteArray, p1: String, p2: ConnectionLifecycleCallback): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun requestConnection(
        p0: String,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: ConnectionOptions
    ): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun requestConnection(
        p0: ByteArray,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: ConnectionOptions
    ): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun sendPayload(p0: MutableList<String>, p1: Payload): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun startAdvertising(
        p0: String,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: AdvertisingOptions
    ): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun startAdvertising(
        p0: ByteArray,
        p1: String,
        p2: ConnectionLifecycleCallback,
        p3: AdvertisingOptions
    ): Task<Void> {
        // No-op implementation
        return Tasks.forResult(null)
    }

    override fun disconnectFromEndpoint(endpointId: String) {
        // No-op implementation
    }
}
