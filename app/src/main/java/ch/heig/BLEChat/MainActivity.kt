package ch.heig.BLEChat

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heig.BLEChat.Model.Message


class MainActivity : AppCompatActivity() {
    private var recyclerViewMessages: RecyclerView? = null
    private var editTextMessage: EditText? = null
    private var buttonSend: Button? = null
    private var messageList: MutableList<Message> = ArrayList()
    private var messageAdapter: MessageAdapter = MessageAdapter(messageList)
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        promptForUsername()

        recyclerViewMessages = findViewById<RecyclerView>(R.id.recyclerViewMessages)
        editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        buttonSend = findViewById<Button>(R.id.buttonSend)

        recyclerViewMessages?.setLayoutManager(LinearLayoutManager(this))
        recyclerViewMessages?.setAdapter(messageAdapter)
        buttonSend?.setOnClickListener(View.OnClickListener {
            val message = editTextMessage?.text.toString().trim { it <= ' ' }
            if (message.isNotEmpty()) {
                messageList.add(Message(username!!, message))
                messageAdapter.notifyItemInserted(messageList.size - 1)
                recyclerViewMessages?.scrollToPosition(messageList.size - 1)
                editTextMessage?.setText("")
            }
        })
    }
    private fun promptForUsername() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Enter your username")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            username = input.text.toString().trim { it <= ' ' }
            if (username!!.isEmpty()) {
                username = "Anonymous"
            }
            Toast.makeText(this@MainActivity, "Username set to: $username", Toast.LENGTH_SHORT)
                .show()
        })
        builder.setCancelable(false)
        builder.show()
    }
}

