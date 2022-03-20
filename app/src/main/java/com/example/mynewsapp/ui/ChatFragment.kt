package com.example.mynewsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.R
import com.example.mynewsapp.adapter.MessageListAdapter
import com.example.mynewsapp.model.Message
import com.example.mynewsapp.model.User
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class ChatFragment : Fragment() {

    private val chatViewModel: ChatViewModel by activityViewModels()
    lateinit var adapter: MessageListAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var sendMsgButton: Button
    lateinit var msgInput: EditText
    companion object {
        val TAG = "ChatFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        chatViewModel.getMessages()
        chatViewModel.checkIsSignIn()

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview_chat)
        sendMsgButton = view.findViewById(R.id.button_gchat_send)
        msgInput = view.findViewById(R.id.edit_gchat_message)
        //change toolbar title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "${chatViewModel.stockNo} chat room"

        chatViewModel.currentLoginUser.observe(viewLifecycleOwner, { firebaseUser ->
            val user = User(firebaseUser.uid, null)
            adapter = MessageListAdapter()
            adapter.currentUser = user
            recyclerView.adapter = adapter

        })


        chatViewModel.messageListLiveDate.observe(viewLifecycleOwner, {
            //Log.d(TAG, it.toString())
            adapter.messageList = it
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.messageList.lastIndex)
        })

        sendMsgButton.setOnClickListener {
            val messageContent = msgInput.text.toString()
            val newMessage = Message(
                sender = User(id = chatViewModel.currentLoginUser.value!!.uid, nickname = "anonymous"),
                messageContent = messageContent,
                createdAt = null
            )

            val messageRepresentation: HashMap<String, Any?> = hashMapOf( "created" to Date(),
                "senderId" to newMessage.sender.id,
                "senderName" to newMessage.sender.nickname,
                "content" to newMessage.messageContent
            )
            chatViewModel.sendMessage(messageRepresentation)
            msgInput.text.clear()
        }
    }

}