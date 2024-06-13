package ch.heig.BLEChat

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ch.heig.BLEChat.Model.Message
import ch.heig.BLEChat.Model.User
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity(), BluetoothHelperListener {

    lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var globalChatFragment: GlobalChatFragment
    private lateinit var nearbyUsersFragment: NearbyUsersFragment
    private lateinit var sharedPreferences: SharedPreferences

    private var _username: String = ""
    var username: String
        get() = _username
        set(value) {
            _username = value
            onUsernameChanged(value)
        }

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        promptForUsername(username)
    }

    private fun initializeApp() {
        bluetoothHelper = BluetoothHelper(this, this)

        globalChatFragment = GlobalChatFragment()
        nearbyUsersFragment = NearbyUsersFragment()

        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        val adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> globalChatFragment
                    else -> nearbyUsersFragment
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "Global Chat"
                    else -> "Private Chat"
                }
            }
        }

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        // Check and request necessary permissions
        requestBluetoothPermissions()
    }

    private fun startBluetooth() {
        // Start advertising and discovery
        bluetoothHelper.startAdvertising(Strategy.P2P_CLUSTER, username)
        bluetoothHelper.startDiscovery(Strategy.P2P_CLUSTER)
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Bluetooth and Location permissions are required to use this app.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun promptForUsername(currentUsername: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Username")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(currentUsername)
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(64)
        input.filters = filterArray
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            username = input.text.toString().ifEmpty { "Anonymous" }
            dialog.dismiss()
        }

        Log.d("BLEChat", "Prompting for username, current: $currentUsername")

        builder.setCancelable(false)
        builder.show()
    }

    private fun onUsernameChanged(newUsername: String) {
        Log.d("BLEChat", "Username changed to: $newUsername")
        // Add any logic you want to execute when the username changes
        initializeApp()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothHelper.stopAdvertising()
        bluetoothHelper.stopDiscovery()
    }

    override fun onMessageReceived(message: Message) {
        runOnUiThread {
            globalChatFragment.onMessageReceived(message)
        }
    }

    override fun onUserDiscovered(user: User) {
        runOnUiThread {
            nearbyUsersFragment.onUserDiscovered(user)
        }
    }

    override fun onUserDisconnected(endpointId: String) {
        runOnUiThread {
            nearbyUsersFragment.onUserDisconnected(endpointId)
        }
    }

    fun openChatFragment(user: User) {
        val userChatFragment = UserChatFragment(user)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, userChatFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun requestBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_WIFI_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CHANGE_WIFI_STATE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        } else {
            // All permissions already granted
            startBluetooth()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted
                    startBluetooth()
                } else {
                    showPermissionDeniedDialog()
                }
            }
        }
    }
}
