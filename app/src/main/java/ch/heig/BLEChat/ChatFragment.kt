package ch.heig.BLEChat

import androidx.fragment.app.Fragment
import ch.heig.BLEChat.Model.Message

abstract class ChatFragment : Fragment() {
    abstract fun onMessageReceived(message: Message)

    abstract fun addMessageToChat(message: Message)
}