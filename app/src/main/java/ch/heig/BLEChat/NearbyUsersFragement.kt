package ch.heig.BLEChat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import ch.heig.BLEChat.Model.User

class NearbyUsersFragment : Fragment() {

    private lateinit var userAdapter: ArrayAdapter<String>
    private val userList = mutableListOf<User>()
    private lateinit var listViewUsers: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_users, container, false)

        userAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList.map { it.username })
        listViewUsers = view.findViewById(R.id.listViewUsers)
        listViewUsers.adapter = userAdapter

        listViewUsers.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val user = userList[position]
            (activity as MainActivity).openChatFragment(user.endpointId)
        }

        return view
    }

    fun onUserDiscovered(user: User) {
        userList.add(user)
        userAdapter.notifyDataSetChanged()
    }

    fun onUserDisconnected(endpointId: String) {
        val index = userList.indexOfFirst { it.endpointId == endpointId }
        if (index != -1) {
            userList.removeAt(index)
        }
    }
}
