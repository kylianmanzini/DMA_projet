package ch.heig.BLEChat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message
import ch.heig.BLEChat.Model.User

class NearbyUsersFragment : Fragment() {

    private lateinit var userAdapter: ArrayAdapter<String>
    private val userList = mutableListOf<User>()
    private lateinit var listViewUsers: ListView

    private lateinit var privateMessagesAdapter: MessageAdapter
    private val privateMessagesList = mutableListOf<Message>()
    private lateinit var privateRecyclerView: RecyclerView

    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    private var selectedUserEndpointId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_users, container, false)

        // Set up ListView for nearby users
        userAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            userList.map { it.username })
        listViewUsers = view.findViewById(R.id.listViewUsers)
        listViewUsers.adapter = userAdapter

        listViewUsers.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val user = userList[position]
            selectedUserEndpointId = user.endpointId
            // Clear previous messages when switching to a new user
            privateMessagesList.clear()
            privateMessagesAdapter.notifyDataSetChanged()
        }

        // Set up RecyclerView for private messages
        privateRecyclerView = view.findViewById(R.id.privateRecyclerView)
        privateRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        privateMessagesAdapter = MessageAdapter(privateMessagesList)
        privateRecyclerView.adapter = privateMessagesAdapter

        // Set up message input
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSend = view.findViewById(R.id.buttonSend)
        buttonSend.setOnClickListener {
            val messageContent = editTextMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                val message = Message( (activity as MainActivity).username, messageContent) // Adjust the author as necessary
                privateMessagesList.add(message)
                privateMessagesAdapter.notifyDataSetChanged()
                editTextMessage.text.clear()

                // Send the message to the selected user
                selectedUserEndpointId?.let { endpointId ->
                    val user = userList.find { it.endpointId == endpointId }
                    if (user != null) {
                        (activity as MainActivity).bluetoothHelper.sendMessage(
                            user.endpointId,
                            message
                        )
                    }
                }
            }
        }

        return view
    }

    fun onUserDiscovered(user: User) {
        userList.add(user)
        userAdapter.clear()
        userAdapter.addAll(userList.map { it.username })
        userAdapter.notifyDataSetChanged()

        Log.d("NearbyUsersFragment", "User discovered: ${user.username}")
        Log.d("NearbyUsersFragment", "User list: $userList")
        Log.d("NearbyUsersFragment", "User adapter: $userAdapter")
    }

    fun onUserDisconnected(endpointId: String) {
        val index = userList.indexOfFirst { it.endpointId == endpointId }
        if (index != -1) {
            userList.removeAt(index)
            userAdapter.clear()
            userAdapter.addAll(userList.map { it.username })
            userAdapter.notifyDataSetChanged()
        }
    }

    fun onMessageReceived(message: Message) {
        privateMessagesList.add(message)
        privateMessagesAdapter.notifyDataSetChanged()
    }
}
