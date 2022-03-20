package com.example.mynewsapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.mynewsapp.model.Message
import com.example.mynewsapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

class ChatViewModel: ViewModel() {
    var stockNo: String = ""
    val db = Firebase.firestore
    val auth: FirebaseAuth = Firebase.auth
    var currentLoginUser: MutableLiveData<FirebaseUser> = MutableLiveData()

    private var channelID: String? = null
    var messageListLiveDate: MutableLiveData<MutableList<Message>> = MutableLiveData()

    companion object {
        val TAG = "ChatViewModel"
    }

    fun checkIsExistingChannel(channelName: String) {
        var channelReference: CollectionReference = db.collection("channels")

        var isExisting = false


        channelReference.whereEqualTo("name", channelName).get()
            .addOnSuccessListener { snapshot ->

                for (document in snapshot) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                }


                if (snapshot.size() > 0) {
                    isExisting = true
                    channelID = snapshot.documents[0].id
                    Log.d(TAG, "checkIsExistingChannel channel id...$channelID")
                    return@addOnSuccessListener
                } else {
                    createChannel(channelName)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
    private fun createChannel(channelName: String) {

        var channelReference: CollectionReference =
            db.collection("channels")

        val data = hashMapOf(
            "name" to channelName
        )
        channelReference.add(data)
            .addOnSuccessListener { documentRef ->
                Log.d(TAG, "${documentRef.id}")
                channelID = documentRef.id
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "create channel error:", e)
            }


    }
    fun getMessages(): List<Message> {
        val reference = db.collection("channels/$channelID/thread")
        val messageList = mutableListOf<Message>()
        Log.d(TAG, "channel id...$channelID")
        reference.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            snapshot?.documentChanges?.forEach {
                val message = handleDocumentChange(it)
                messageList.add(message)
                Log.d(TAG, "message...$message")
//                messageListLiveDate.value?.add(message)
            }
            messageList.sortBy { message ->  message.createdAt}
            Log.d(TAG, "messageList... $messageList")

            messageListLiveDate.value = messageList

            Log.d(TAG, "messageList... ${messageListLiveDate.value}")

        }
        return messageList
    }
    private fun handleDocumentChange(change: DocumentChange): Message {
        lateinit var message: Message
        when(change.type) {
            DocumentChange.Type.ADDED -> {
                val data = change.document.data
                val sender = User(id = data["senderId"] as String, nickname = data["senderName"] as String)
                val content = data["content"] as String
                val sentDate = data["created"] as Timestamp //Timestamp is an object

                message = Message(sender = sender, messageContent = content, createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(sentDate.seconds),TimeZone.getDefault().toZoneId()))

            }
        }
        return message
    }
    fun signIn() {
        auth.signInAnonymously().addOnCompleteListener() { authResult ->
            if (authResult.isSuccessful) {
                currentLoginUser.value = auth.currentUser
                Log.d(TAG, "sign in successfully")
            } else {
                Log.w(TAG, "signInAnonymously:failure", authResult.exception)
            }
        }
    }

    fun checkIsSignIn() {
        if (auth.currentUser != null) {
            currentLoginUser.value = auth.currentUser
        } else {
            signIn()
        }

    }

    fun sendMessage(message: HashMap<String, Any?>) {
        val reference = db.collection("channels/$channelID/thread")
        reference.add(message)
            .addOnSuccessListener { documentRef ->
            Log.d(TAG,"successfully add new document: ${documentRef.id}")
        }
            .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
    }
}