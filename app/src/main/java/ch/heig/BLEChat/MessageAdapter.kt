package ch.heig.BLEChat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message
import kotlin.random.Random

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
        private val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)

        fun bind(message: Message) {
            textViewAuthor.text = message.author
            textViewContent.text = message.content
            textViewAuthor.setTextColor(generateColor(message.author))
        }

        private fun generateColor(username: String): Int {
            val random = Random(username.hashCode())
            return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }

}

