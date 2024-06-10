package ch.heig.BLEChat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message

class GlobalChatFragment : Fragment() {

    private lateinit var messages: MutableList<Message>
    private lateinit var adapter: MessageAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var username: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_global_chat, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("ChatApp", Context.MODE_PRIVATE)

        messages = mutableListOf()
        adapter = MessageAdapter(messages)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val editTextMessage = view.findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = view.findViewById<Button>(R.id.buttonSend)

        // Get BluetoothHelper instance from MainActivity
        bluetoothHelper = (activity as MainActivity).bluetoothHelper

        buttonSend.setOnClickListener {
            val messageContent = editTextMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                username = sharedPreferences.getString("username", "Anonymous").toString()
                val message = Message(username, messageContent)
                bluetoothHelper.sendMessageToGlobalChat(message)
                addMessageToChat(message)
                editTextMessage.text.clear()
            }
        }

        return view
    }

    fun onMessageReceived(message: Message) {
        addMessageToChat(message)
    }

    private fun addMessageToChat(message: Message) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }
}
