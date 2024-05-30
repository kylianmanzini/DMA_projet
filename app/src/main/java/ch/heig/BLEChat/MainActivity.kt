package ch.heig.BLEChat

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
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
import android.Manifest

class MainActivity : AppCompatActivity(), BluetoothHelperListener {

    lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var globalChatFragment: GlobalChatFragment
    private lateinit var nearbyUsersFragment: NearbyUsersFragment
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var username: String

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("ChatApp", Context.MODE_PRIVATE)

        username = sharedPreferences.getString("username", "Anonymous").toString()

        promptForUsername(username)
        initializeApp()
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
                    else -> "Nearby Users"
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
        sharedPreferences = getSharedPreferences("ChatApp", MODE_PRIVATE)
        username = sharedPreferences.getString("username", "Anonymous").toString()

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

    private fun promptForUsername(username: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Username")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(username)
        builder.setView(input)

        sharedPreferences = getSharedPreferences("ChatApp", MODE_PRIVATE)

        builder.setPositiveButton("OK") { dialog, _ ->
            this.username = input.text.toString().ifEmpty { "Anonymous" }
            sharedPreferences.edit().putString("username", this.username).apply()
            dialog.dismiss()
        }

        Log.d("BLEChat", "Username: $username")

        builder.setCancelable(false)
        builder.show()
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

    fun openChatFragment(endpointId: String) {
        val chatFragment = ChatFragment()
        chatFragment.setEndpointId(endpointId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatFragment)
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
