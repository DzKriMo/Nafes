package com.thekrimo.nafes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.thekrimo.nafes.databinding.FragmentConversationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConversationFragment : Fragment() {
    private lateinit var binding: FragmentConversationBinding
    private lateinit var receivedMessagesAdapter: ConversationAdapter
    private lateinit var currentUserUid: String
    private lateinit var therapistId: String
    private lateinit var therapistName: String
    private lateinit var chatsRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioRecorder: MediaRecorder
    private lateinit var audioFile: File
    private lateinit var audioUri: Uri
    private var isRecording = false
    private var gson = Gson()
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101
    private val PICK_FILE_REQUEST_CODE = 102
    private val RECORD_AUDIO_REQUEST_CODE = 103
    private val already = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        storageRef = FirebaseStorage.getInstance().reference  // Initialize Firebase Storage reference
        mediaPlayer = MediaPlayer()

        val therapistData = loadLocally()
        if (therapistData.isNotEmpty()){
            binding.contact.text = therapistData[0]
            therapistId = therapistData[1]
        }
        fetchTherapist()

        receivedMessagesAdapter = ConversationAdapter(requireContext(), currentUserUid, binding.recyclerViewReceivedMessages)

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
                requestCameraAndAudioPermissions()
            }
            openCustomTab("https://nafas-therapy.web.app/patient.html")
        }

        binding.clip.setOnClickListener {
            openFileChooser()
        }

        binding.record.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording()
                }
            }
            true
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"  // all file types
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    private fun saveMessageLocally(messageId: String, message: Message) {
        val sharedPreferences = requireActivity().getSharedPreferences("local_messages", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val messageJson = gson.toJson(message)
        editor.putString(messageId, messageJson)
        editor.apply()
    }

    private fun getSeenMessageIds(): Set<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("local_messages", Context.MODE_PRIVATE)
        return sharedPreferences.all.keys
    }



    private fun listenForReceivedMessages() {
        val sharedPreferences = requireActivity().getSharedPreferences("local_messages", Context.MODE_PRIVATE)
        val localMessagesMap = sharedPreferences.all
        if (!isNetworkConnected()) {
        // Load and display local messages first if there are any
            already.clear()
        for ((messageId, messageJson) in localMessagesMap) {
            try {
                if (messageJson is String) {
                    Log.d(TAG, "Processing local message JSON: $messageJson")
                    val message = gson.fromJson(messageJson, Message::class.java)
                    receivedMessagesAdapter.addMessage(message)
                    already.add(messageId)
                    binding.recyclerViewReceivedMessages.scrollToPosition(receivedMessagesAdapter.getLastItemPosition())
                } else {
                    Log.e(TAG, "Invalid message format: $messageJson")
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Error parsing local message JSON: $e")
            }
        }
        }
        // Ch}eck for internet connectivity before adding Firebase listener
        else{
            val receivedMessagesRef = chatsRef.child(currentUserUid).child(therapistId)
            receivedMessagesRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    try {
                        val message = snapshot.getValue(Message::class.java)
                        message?.let {

                                receivedMessagesAdapter.addMessage(it)
                                saveMessageLocally(snapshot.key ?: "", message) // Save as JSON string
                                binding.recyclerViewReceivedMessages.scrollToPosition(receivedMessagesAdapter.getLastItemPosition())

                        }
                    } catch (e: DatabaseException) {
                        Log.e(TAG, "Error converting message data: ${e.message}")
                        Log.d(TAG, "Snapshot value: ${snapshot.value}")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}")
                }
            })
        }
    }

    // Helper function to check internet connectivity
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    private fun loadLocalMessages(): Set<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("local_messages", Context.MODE_PRIVATE)
        return sharedPreferences.all.keys
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private  fun sendMessage(messageText: String) {
        val messageId = chatsRef.child(currentUserUid).child(therapistId).push().key
        val timestamp = getCurrentTimestamp()
        if (messageId != null) {
            val message = Message(currentUserUid, therapistId, messageText, "", "", timestamp)

            CoroutineScope(Dispatchers.Main).launch {
                saveMessageLocally(messageId, message)
                chatsRef.child(currentUserUid).child(therapistId).child(messageId).setValue(message).await()
                chatsRef.child(therapistId).child(currentUserUid).child(messageId).setValue(message).await()

                addContactIfNotExists(therapistId)
            }
        }
    }

    private fun addContactIfNotExists(therapistId: String) {
        val currentUserContactsRef =
            FirebaseDatabase.getInstance().getReference("Users/$currentUserUid/contacts")
        currentUserContactsRef.child(therapistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        currentUserContactsRef.child(therapistId).setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ConversationFragment", "Database error: ${error.message}")
                }
            })
    }

    private fun fetchTherapist() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        therapistName = snapshot.child("therapistName").getValue(String::class.java) ?: ""
                        therapistId = snapshot.child("therapistID").getValue(String::class.java) ?: ""
                        binding.contact.text = therapistName
                        saveLocaly(therapistName, therapistId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if data retrieval fails Kash nhar
                }

            })
        }
    }

    private fun saveLocaly(name: String, Id: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("therapist", name)
        editor.putString("id", Id)
        editor.apply()
    }

    private fun loadLocally(): List<String> {
        val sharedPreferences = requireActivity().getSharedPreferences("therapist_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("therapist", "") ?: ""
        val ID = sharedPreferences.getString("id", "") ?: ""
        return listOf(name, ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimestamp(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return currentDateTime.format(formatter)
    }

    private fun requestCameraAndAudioPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )
        val recordAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )

        val permissionsToRequest = ArrayList<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE || requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, handle accordingly
            } else {
                // Permissions denied, handle accordingly
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedFileUri = data.data
            if (selectedFileUri != null) {
                uploadFile(selectedFileUri)
            }
        }
    }

    private fun uploadFile(fileUri: Uri) {
        val fileName = getFileNameFromUri(fileUri)
        val fileRef = storageRef.child("files/${System.currentTimeMillis()}_${fileUri.lastPathSegment}")

        try {
            val inputStream = requireActivity().contentResolver.openInputStream(fileUri)
            inputStream?.let {
                fileRef.putStream(it)
                    .addOnSuccessListener { taskSnapshot ->
                        // :) hadi zrda 5 5, get the download URL
                        fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val fileUrl = downloadUri.toString()
                            saveFileUrlToDatabase(fileUrl, fileName)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle any errors kash nhar
                        Log.e("FileUpload", "Upload failed: ${exception.message}")
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveFileUrlToDatabase(fileUrl: String, fileName:String) {
        val messageId = chatsRef.child(currentUserUid).child(therapistId).push().key

        if (messageId != null) {
            val message = Message(currentUserUid, therapistId, fileName, fileUrl)
            chatsRef.child(currentUserUid).child(therapistId).child(messageId).setValue(message)
            chatsRef.child(therapistId).child(currentUserUid).child(messageId).setValue(message)

            addContactIfNotExists(therapistId)
        }
    }

    private fun openCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.setUrlBarHidingEnabled(true)
        builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireActivity(), Uri.parse(url))
    }

    fun hideWebView() {
        binding.web.visibility = View.GONE
    }

    fun isWebVisible(): Boolean {
        return binding.web.isVisible
    }

    fun onBackPressed() {
        if (isWebVisible()) {
            hideWebView()
        }
    }

    private fun createAudioFile(): File {
        val audioFileName = "audio_${System.currentTimeMillis()}.3gp"
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(audioFileName, ".mp3", storageDir)
    }

    private fun startRecording() {
        if (isRecording) return

        binding.bottom.setBackgroundResource(R.drawable.bttn)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
            return
        }

        audioFile = createAudioFile()
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Use MPEG-4 format
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)    // Use AAC encoder for better quality
            setAudioEncodingBitRate(128000)                   // Set bitrate (adjust as needed)
            setAudioSamplingRate(44100)                        // Set sampling rate (adjust as needed)
            setOutputFile(audioFile.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                Log.e("AudioRecord", "prepare() failed")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopRecording() {
        if (!isRecording) return

        binding.bottom.setBackgroundResource(R.drawable.defaultt)
        audioRecorder.apply {
            stop()
            release()
        }
        isRecording = false

        uploadAudio(Uri.fromFile(audioFile))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadAudio(audioUri: Uri) {
        val fileName = getFileNameFromUri(audioUri)
        val fileRef = storageRef.child("audio/${System.currentTimeMillis()}_${audioUri.lastPathSegment}")

        fileRef.putFile(audioUri)
            .addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val audioUrl = downloadUri.toString()
                    saveAudioUrlToDatabase(audioUrl, fileName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AudioUpload", "Upload failed: ${exception.message}")
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveAudioUrlToDatabase(audioUrl: String, fileName: String) {

        val timestamp = getCurrentTimestamp()
        val messageId = chatsRef.child(currentUserUid).child(therapistId).push().key

        if (messageId != null) {
            val message = Message(currentUserUid, therapistId, timestamp, "" ,audioUrl)
            chatsRef.child(currentUserUid).child(therapistId).child(messageId).setValue(message)
            chatsRef.child(therapistId).child(currentUserUid).child(messageId).setValue(message)

            addContactIfNotExists(therapistId)
        }
    }
    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                fileName = if (!displayName.isNullOrEmpty()) {
                    displayName
                } else {
                    uri.lastPathSegment ?: "file"
                }
            }
        }
        return fileName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroyView() {
        super.onDestroyView()
        stopRecording()
    }
}
