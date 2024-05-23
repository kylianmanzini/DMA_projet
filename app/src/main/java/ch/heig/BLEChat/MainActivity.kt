package ch.heig.BLEChat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private var recyclerViewMessages: RecyclerView? = null
    private var editTextMessage: EditText? = null
    private var buttonSend: Button? = null
    private var messageList: MutableList<String> = ArrayList()
    private var messageAdapter: MessageAdapter = MessageAdapter(messageList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewMessages = findViewById<RecyclerView>(R.id.recyclerViewMessages)
        editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        buttonSend = findViewById<Button>(R.id.buttonSend)

        recyclerViewMessages?.setLayoutManager(LinearLayoutManager(this))
        recyclerViewMessages?.setAdapter(messageAdapter)
        buttonSend?.setOnClickListener(View.OnClickListener {
            val message = editTextMessage?.getText().toString().trim { it <= ' ' }
            if (message.isNotEmpty()) {
                messageList.add(message)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                recyclerViewMessages?.scrollToPosition(messageList.size - 1)
                editTextMessage?.setText("")
            }
        })
    }
}

