package com.elix360.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val items: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageHolder>() {

    class MessageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.messageText)
        val container: View = view.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageHolder(view)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val item = items[position]
        holder.text.text = item.text

        val lp = holder.text.layoutParams as ViewGroup.MarginLayoutParams
        if (item.fromUser) {
            holder.text.setBackgroundResource(R.drawable.bg_message_user)
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.text.layoutParams = lp
            holder.container.apply {
                gravity = android.view.Gravity.END
                setPadding(0, 0, 0, 0)
            }
        } else {
            holder.text.setBackgroundResource(R.drawable.bg_message_ai)
            holder.container.apply {
                gravity = android.view.Gravity.START
                setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun getItemCount() = items.size
}
