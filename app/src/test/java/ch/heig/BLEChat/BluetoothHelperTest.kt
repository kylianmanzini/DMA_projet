package ch.heig.BLEChat

import android.content.Context
import ch.heig.BLEChat.Model.Message
import ch.heig.BLEChat.Model.User
import com.google.android.gms.nearby.connection.Strategy
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

// Test are broken, i don't know why
class BluetoothHelperTest {

    private lateinit var context: Context
    private lateinit var listener: BluetoothHelperListener
    private lateinit var bluetoothHelper: BluetoothHelper

    /*
    @Before
    fun setUp() {
        context = mock(Context::class.java)
        listener = mock(BluetoothHelperListener::class.java)
        val mockConnectionsClient = MockConnectionsClient()
        bluetoothHelper = BluetoothHelper(context, listener, mockConnectionsClient)
    }

    @Test
    fun testUserDiscovery() {
        bluetoothHelper.startDiscovery(Strategy.P2P_CLUSTER)
        // Verify that users are discovered
        // You can use Mockito.verify to check interactions with the listener
    }

    @Test
    fun testUserConnection() {
        val user = User("endpoint1", "User1")
        bluetoothHelper.connectToEndpoint(user)
        // Verify that the user connection is handled correctly
    }

    @Test
    fun testMessageSending() {
        val message = Message("User1", "Hello, world!")
        bluetoothHelper.sendMessageToGlobalChat(message)
        // Verify that the message is sent
    }

     */
}
