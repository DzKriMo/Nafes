package com.thekrimo.nafes

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ConversationAdapter(
    private val context: Context,
    private val currentUserUid: String,
    private val recyclerView: RecyclerView,
    private val messagesList: MutableList<Message> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = RecyclerView.NO_POSITION // Initially no item is playing

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(inflater.inflate(R.layout.sent, parent, false))
            VIEW_TYPE_RECEIVED -> ReceivedMessageViewHolder(inflater.inflate(R.layout.reci, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                val sentViewHolder = holder as SentMessageViewHolder
                sentViewHolder.bindSentMessage(message, position)
            }
            VIEW_TYPE_RECEIVED -> {
                val receivedViewHolder = holder as ReceivedMessageViewHolder
                receivedViewHolder.bindReceivedMessage(message, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messagesList[position]
        return if (message.senderId == currentUserUid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    fun addMessage(message: Message) {
        messagesList.add(message)
        notifyItemInserted(messagesList.size - 1)
    }

    fun getLastItemPosition(): Int {
        return messagesList.size - 1
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playButton: Button = itemView.findViewById(R.id.playButton)
        private val sentMessageTextView: TextView = itemView.findViewById(R.id.sentmessage)

        fun bindSentMessage(message: Message, position: Int) {
            sentMessageTextView.text = message.message

            if (message.audioUrl.isNotEmpty()) {
                playButton.visibility = View.VISIBLE
                updatePlayButtonIcon(position)
                playButton.setOnClickListener {
                    if (currentPlayingPosition == position) {
                        stopAudio(position)
                    } else {
                        playAudio(message.audioUrl, position)
                    }
                }
            } else {
                playButton.visibility = View.GONE
            }

            sentMessageTextView.setOnClickListener {
                if (message.messageLink.isNotEmpty()) {
                    openMessageLink(itemView.context, message.messageLink)
                }
            }
            if (message.messageLink.isNotEmpty()) {
                sentMessageTextView.paintFlags = sentMessageTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                sentMessageTextView.paintFlags = sentMessageTextView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            sentMessageTextView.setOnLongClickListener {
                val clipboardManager = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", message.message)
                clipboardManager.setPrimaryClip(clip)
                true
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playButton: Button = itemView.findViewById(R.id.playButton)
        private val receivedMessageTextView: TextView = itemView.findViewById(R.id.receivedMessage)

        fun bindReceivedMessage(message: Message, position: Int) {
            receivedMessageTextView.text = message.message

            if (message.audioUrl.isNotEmpty()) {
                playButton.visibility = View.VISIBLE
                updatePlayButtonIcon(position)
                playButton.setOnClickListener {
                    if (currentPlayingPosition == position) {
                        stopAudio(position)
                    } else {
                        playAudio(message.audioUrl, position)
                    }
                }
            } else {
                playButton.visibility = View.GONE
            }

            receivedMessageTextView.setOnClickListener {
                if (message.messageLink.isNotEmpty()) {
                    openMessageLink(itemView.context, message.messageLink)
                }
            }


            if (message.messageLink.isNotEmpty()) {
                receivedMessageTextView.paintFlags = receivedMessageTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                receivedMessageTextView.paintFlags = receivedMessageTextView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }

            receivedMessageTextView.setOnLongClickListener {
                val clipboardManager = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", message.message)
                clipboardManager.setPrimaryClip(clip)
                true
            }
        }
    }

    private fun playAudio(audioUrl: String, position: Int) {
        stopAudio(currentPlayingPosition)

        currentPlayingPosition = position

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    updatePlayButtonIcon(position)
                }
                setOnCompletionListener {
                    stopAudio(currentPlayingPosition)
                    updatePlayButtonIcon(position)
                    currentPlayingPosition = RecyclerView.NO_POSITION
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAudio(position: Int) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            mediaPlayer = null
            currentPlayingPosition = RecyclerView.NO_POSITION
            updatePlayButtonIcon(position)
        }
    }

    private fun updatePlayButtonIcon(position: Int) {
        val playIcon = if (currentPlayingPosition == position) R.drawable.stop else R.drawable.play
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        if (viewHolder != null) {
            if (viewHolder is SentMessageViewHolder) {
                viewHolder.playButton.setBackgroundResource(playIcon)
            } else if (viewHolder is ReceivedMessageViewHolder) {
                viewHolder.playButton.setBackgroundResource(playIcon)
            }
        }
    }



    private fun openMessageLink(context: Context, link: String) {
        val imageExtensions = arrayOf("png", "jpg", "jpeg", "gif", "bmp")
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(link)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)

        if ((mimeType != null && imageExtensions.any { mimeType.contains(it) }) || link.contains("IMG")|| link.contains("img")||link.contains("image")|| link.contains("jpg")|| link.contains("jpeg")|| link.contains("png")|| link.contains("gif")|| link.contains("svg")) {
            val imageView = ImageView(context)
            Picasso.get().load(link).into(imageView)

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(imageView)

            val alertDialog = AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Download") { _, _ ->
                    downloadFile(context, link)
                }
                .create()

            alertDialog.show()
        } else {
            openCustomTab(link)
        }
    }

    private fun openCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.setUrlBarHidingEnabled(true)
        builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    private fun downloadFile(context: Context, fileUrl: String) {
        val fileName = Uri.parse(fileUrl).lastPathSegment ?: "file"
        val fileExtension = fileName.substringAfterLast('.', "")

        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading File")
        request.setDescription("Downloading $fileName")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.$fileExtension")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Downloading $fileName...", Toast.LENGTH_SHORT).show()
    }
}