package com.example.mymessenger.messages

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mymessenger.R
import com.example.mymessenger.models.ChatMessage
import com.example.mymessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

  companion object{
    val TAG = "ChatLogActivity"
  }

  val adapter = GroupAdapter<ViewHolder>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat_log)

    recycleview_chat_log.adapter = adapter

      val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
    val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

    supportActionBar?.title = user.username

    //setupDummyData()
    listenForMessage()

    send_button_chat_log.setOnClickListener{
      Log.d(TAG, "Attempt to send message....")
      performSendMessage()
    }
  }

  private fun listenForMessage() {
    val ref = FirebaseDatabase.getInstance().getReference("/message")

    ref.addChildEventListener(object: ChildEventListener{

      override fun onChildAdded(p0: DataSnapshot, p1: String?) {
        val chatMessage = p0.getValue(ChatMessage::class.java)

        if(chatMessage != null){
          Log.d(TAG, chatMessage.text)

          if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            adapter.add(ChatFromItem(chatMessage.text))
          } else {
            val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
            adapter.add(ChatToItem(chatMessage.text, toUser))
          }
        }

      }

      override fun onCancelled(p0: DatabaseError) {

      }

      override fun onChildChanged(p0: DataSnapshot, p1: String?) {

      }

      override fun onChildMoved(p0: DataSnapshot, p1: String?) {

      }

      override fun onChildRemoved(p0: DataSnapshot) {

      }

    })
  }

  private fun performSendMessage(){
    val text = edittext_chat_log.text.toString()

    val fromId = FirebaseAuth.getInstance().uid
    val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
    val toId = user.uid

    if (fromId == null) return

    val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

    val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
    reference.setValue(chatMessage)
      .addOnCompleteListener {
        Log.d(TAG, "Saved our chat message: ${reference.key}")
      }
  }

//  private fun setupDummyData() {
//    val adapter = GroupAdapter<ViewHolder>()
//
//    adapter.add(ChatFromItem("From messaaaaaage"))
//    adapter.add(ChatToItem("TO MESSAGEEEEEEEEEEE\nhello my brother"))
//    adapter.add(ChatFromItem("From messaaaaaage"))
//    adapter.add(ChatToItem("TO MESSAGEEEEEEEEEEE\nhello my brother"))
//    adapter.add(ChatFromItem("From messaaaaaage"))
//    adapter.add(ChatToItem("TO MESSAGEEEEEEEEEEE\nhello my brother"))
//
//    recycleview_chat_log.adapter = adapter
//  }
}

class ChatFromItem(val text: String): Item<ViewHolder>(){
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.textView2.text = text
  }

  override fun getLayout(): Int {
      return R.layout.chat_from_row
  }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.textView.text = text

    val uri = user.profileImageUrl
    val targetImageView = viewHolder.itemView.imageView_chat_to_row
    Picasso.get().load(uri).into(targetImageView)
  }
  override fun getLayout(): Int {
      return R.layout.chat_to_row
  }
}
