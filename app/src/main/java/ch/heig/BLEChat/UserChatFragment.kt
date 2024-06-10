package ch.heig.BLEChat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message
import ch.heig.BLEChat.Model.User

class UserChatFragment(private var user: User) :  ChatFragment() {

    private lateinit var messages: MutableList<Message>
    private lateinit var privateMessages: MutableList<Message>
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var bluetoothHelper: BluetoothHelper


    private lateinit var privateMessagesAdapter: MessageAdapter
    private val privateMessagesList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        messages = mutableListOf()
        messagesAdapter = MessageAdapter(messages)

        privateMessages = mutableListOf()
        privateMessagesAdapter = MessageAdapter(privateMessagesList)


        val recyclerView = view.findViewById<RecyclerView>(R.id.chatsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = messagesAdapter

        val editTextMessage = view.findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = view.findViewById<Button>(R.id.buttonSend)

        bluetoothHelper = (activity as MainActivity).bluetoothHelper

        buttonSend.setOnClickListener {
            val messageContent = editTextMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                val message = Message((activity as MainActivity).username, messageContent)
                user.endpointId.let {
                    bluetoothHelper.sendMessage(it, message)
                }
                addMessageToChat(message)
                editTextMessage.text.clear()
            }
        }

        return view
    }

    override fun onMessageReceived(message: Message) {
        addMessageToChat(message)
    }

    override fun addMessageToChat(message: Message) {
        messages.add(message)
        messagesAdapter.notifyItemInserted(messages.size - 1)
        privateMessages.add(message)
        privateMessagesAdapter.notifyItemInserted(privateMessages.size - 1)
    }
}
