package com.thekrimo.nafes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thekrimo.nafes.databinding.FragmentConversationBinding

class ConversationFragment : Fragment() {
    private lateinit var binding: FragmentConversationBinding
    private lateinit var receivedMessagesAdapter: ConversationAdapter
    private lateinit var currentUserUid: String

    private lateinit var otherUserName: String
    private lateinit var chatsRef: DatabaseReference
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        otherUserName = arguments?.getString("otherUserName") ?: ""

        binding.contact.text = "Akram Mehdi Amamra"

        receivedMessagesAdapter = ConversationAdapter(currentUserUid)

        binding.recyclerViewReceivedMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = receivedMessagesAdapter
        }

        chatsRef = FirebaseDatabase.getInstance().getReference("Chats")

        listenForReceivedMessages()

        binding.buttonSendMessage.setOnClickListener {
            val message = binding.editTextMessageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.editTextMessageInput.text.clear()
            }
        }

        binding.startVideo.setOnClickListener {
            binding.web.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraPermission = ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                )
                val recordAudioPermission = ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.RECORD_AUDIO
                )

                if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                }

                if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_AUDIO_PERMISSION_REQUEST_CODE
                    )
                }
            }

            // Open the URL using Custom Tabs
            openCustomTab("https://nafes-59aaa.web.app")
        }
    }

    private fun listenForReceivedMessages() {
        val receivedMessagesRef = chatsRef.child(currentUserUid).child("qYXnP2VpJFeEIPtzEu25QjuqMDA2")
        receivedMessagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageData = snapshot.getValue(Message::class.java)
                messageData?.let { message ->
                    receivedMessagesAdapter.addMessage(message)
                    addContactIfNotExists("qYXnP2VpJFeEIPtzEu25QjuqMDA2")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(messageText: String) {
        val messageId = chatsRef.child(currentUserUid).child("qYXnP2VpJFeEIPtzEu25QjuqMDA2").push().key

        if (messageId != null) {
            val message = Message(currentUserUid, "qYXnP2VpJFeEIPtzEu25QjuqMDA2", messageText)
            val messageMap = HashMap<String, Any>()
            messageMap["message"] = messageText
            messageMap["receiverId"] = "qYXnP2VpJFeEIPtzEu25QjuqMDA2"
            messageMap["senderId"] = currentUserUid
            messageMap["timestamp"] = ServerValue.TIMESTAMP

            chatsRef.child(currentUserUid).child("qYXnP2VpJFeEIPtzEu25QjuqMDA2").child(messageId).setValue(messageMap)
            chatsRef.child("qYXnP2VpJFeEIPtzEu25QjuqMDA2").child(currentUserUid).child(messageId).setValue(messageMap)

            addContactIfNotExists("qYXnP2VpJFeEIPtzEu25QjuqMDA2")
        }
    }

    private fun addContactIfNotExists(otherUserId: String) {
        val currentUserContactsRef =
            FirebaseDatabase.getInstance().getReference("Users/$currentUserUid/contacts")
        currentUserContactsRef.child(otherUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // Add the other user as a contact for the current user
                        currentUserContactsRef.child(otherUserId).setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation
                }
            })
    }

    private fun openCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.setUrlBarHidingEnabled(true) // Enable hiding the address bar
        builder.setShareState(CustomTabsIntent.SHARE_STATE_ON) // Optional: Set the share button state

        val customTabsIntent = builder.build()

        customTabsIntent.launchUrl(requireActivity(), Uri.parse(url))
    }

    fun hideWebView() {
        binding.web.visibility = View.GONE
    }

    fun isWebVisible(): Boolean {
return binding.web.isVisible
    }
}
