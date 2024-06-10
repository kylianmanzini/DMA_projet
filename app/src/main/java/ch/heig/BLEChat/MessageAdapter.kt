package ch.heig.BLEChat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val authorTextView: TextView = view.findViewById(R.id.textAuthor)
        val contentTextView: TextView = view.findViewById(R.id.textContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.authorTextView.text = message.author
        holder.contentTextView.text = message.content
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
