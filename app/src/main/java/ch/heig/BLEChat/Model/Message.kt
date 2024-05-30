package ch.heig.BLEChat.Model

data class Message(val author: String, val content: String)
data class User(val endpointId: String, val username: String)


