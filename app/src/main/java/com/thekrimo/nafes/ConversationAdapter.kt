package com.thekrimo.nafes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConversationAdapter(
    private val currentUserUid: String,
    private val messagesList: MutableList<Message> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.reci, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                val sentViewHolder = holder as SentMessageViewHolder
                sentViewHolder.bindSentMessage(message)
            }
            VIEW_TYPE_RECEIVED -> {
                val receivedViewHolder = holder as ReceivedMessageViewHolder
                receivedViewHolder.bindReceivedMessage(message)
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
        notifyDataSetChanged()
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sentMessageTextView: TextView = itemView.findViewById(R.id.sentmessage)

        fun bindSentMessage(message: Message) {
            sentMessageTextView.text = message.message
            // Additional binding logic for sent messages
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val receivedMessageTextView: TextView = itemView.findViewById(R.id.receivedMessage)

        fun bindReceivedMessage(message: Message) {
            receivedMessageTextView.text = message.message
            // Additional binding logic for received messages
        }
    }
}