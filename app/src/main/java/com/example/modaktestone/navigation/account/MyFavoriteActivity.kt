package com.example.modaktestone.navigation.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modaktestone.databinding.ActivityBoardcontentBinding
import com.example.modaktestone.databinding.ItemContentBinding
import com.example.modaktestone.navigation.DetailContentActivity
import com.example.modaktestone.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat

class MyFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardcontentBinding

    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardcontentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid

        val toolbar = binding.myToolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayShowCustomEnabled(true)
        ab.setDisplayHomeAsUpEnabled(true)
        binding.boardcontentTextviewBoardname.text = "좋아요 한 글"

        //리사이클러뷰 어댑터
        binding.boardcontentRecyclerview.adapter = MyFavoriteRecyclerViewAdapter()
        binding.boardcontentRecyclerview.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class MyFavoriteRecyclerViewAdapter :
        RecyclerView.Adapter<MyFavoriteRecyclerViewAdapter.CustomViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        //컨텐츠들 줄세우기.
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("contents")?.whereEqualTo("favorites", hashMapOf(uid to true))
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    println("hello $uid")
                    if (documentSnapshot == null) return@addSnapshotListener
                    contentDTOs.clear()
                    contentUidList.clear()

                    for (snapshot in documentSnapshot.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        inner class CustomViewHolder(val binding: ItemContentBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyFavoriteRecyclerViewAdapter.CustomViewHolder {
            val binding =
                ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: MyFavoriteRecyclerViewAdapter.CustomViewHolder,
            position: Int
        ) {
            val safePosition = holder.adapterPosition
            holder.binding.contentTextviewTitle.text = contentDTOs!![safePosition].title

            holder.binding.contentTextviewExplain.text = contentDTOs!![safePosition].explain

            holder.binding.contentTextviewUsername.text = contentDTOs!![safePosition].userName

            holder.binding.contentTextviewTimestamp.text =
                SimpleDateFormat("MM/dd HH:mm").format(contentDTOs!![safePosition].timestamp)

            holder.binding.contentTextviewCommentcount.text =
                contentDTOs!![safePosition].commentCount.toString()

            holder.binding.contentTextviewFavoritecount.text =
                contentDTOs!![safePosition].favoriteCount.toString()

            holder.binding.contentLinearLayout.setOnClickListener { v ->
                var intent = Intent(v.context, DetailContentActivity::class.java)
                if (contentDTOs[safePosition].anonymity.containsKey(contentDTOs[safePosition].uid)) {
                    intent.putExtra("destinationUsername", "익명")
                } else {
                    intent.putExtra("destinationUsername", contentDTOs[safePosition].userName)
                }
                intent.putExtra("destinationTitle", contentDTOs[safePosition].title)
                intent.putExtra("destinationExplain", contentDTOs[safePosition].explain)
                intent.putExtra(
                    "destinationTimestamp",
                    SimpleDateFormat("MM/dd HH:mm").format(contentDTOs[safePosition].timestamp)
                )
                intent.putExtra(
                    "destinationCommentCount",
                    contentDTOs[safePosition].commentCount.toString()
                )
                intent.putExtra(
                    "destinationFavoriteCount",
                    contentDTOs[safePosition].favoriteCount.toString()
                )
                intent.putExtra("destinationUid", contentDTOs[safePosition].uid)
                intent.putExtra("contentUid", contentUidList[safePosition])
                startActivity(intent)

            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}