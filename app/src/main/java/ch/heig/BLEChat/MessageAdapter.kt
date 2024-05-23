package ch.heig.BLEChat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.MessageAdapter.MessageViewHolder
import ch.heig.BLEChat.Model.Message
import kotlin.random.Random

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        val formattedMessage: String = message.author + ": " + message.content
        holder.textViewMessage!!.text = formattedMessage
        val color = generateColor(message.author)
        holder.textViewMessage!!.setTextColor(color)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewMessage: TextView? = null

        init {
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }

    private fun generateColor(username: String): Int {
        val random = Random(username.hashCode())
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}

