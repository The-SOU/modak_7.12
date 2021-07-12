package com.example.modaktestone.navigation

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modaktestone.R
import com.example.modaktestone.databinding.ActivityBoardcontentBinding
import com.example.modaktestone.databinding.ItemContentBinding
import com.example.modaktestone.navigation.model.ContentDTO
import com.example.modaktestone.navigation.model.UserDTO
import com.example.modaktestone.navigation.util.ProgressDialogSecond
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.koin.android.ext.android.bind
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BoardContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardcontentBinding

    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null

    var destinationCategory: String? = null

    var region: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardcontentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //인텐트값 받기
        if (intent.hasExtra("destinationCategory")) {
            destinationCategory = intent.getStringExtra("destinationCategory")
        } else {
            Toast.makeText(this, "전달된 이름이 없다", Toast.LENGTH_SHORT).show()
        }


        //게시판 이름
        binding.boardcontentTextviewBoardname.text = destinationCategory

        binding.boardcontentRecyclerview.adapter = BoardContentRecyclerViewAdapter()
        binding.boardcontentRecyclerview.layoutManager = LinearLayoutManager(this)



        //글쓰기 버튼 클릭할 때
        binding.boardcontentBtnUpload.setOnClickListener { v ->
            var intent = Intent(v.context, AddContentActivity::class.java)
            intent.putExtra("selectedCategory", destinationCategory)
            startActivity(intent)
            finish()
        }

        //툴바 설정
        val toolbar = binding.myToolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayShowCustomEnabled(true)
        ab.setDisplayHomeAsUpEnabled(true)
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

    inner class BoardContentRecyclerViewAdapter :
        RecyclerView.Adapter<BoardContentRecyclerViewAdapter.CustomViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        //컨텐츠들 줄세우기.
        var contentUidList: ArrayList<String> = arrayListOf()


        init {

            firestore?.collection("users")?.document(auth?.currentUser?.uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null) return@addSnapshotListener
                    var userDTO = documentSnapshot.toObject(UserDTO::class.java)
                    region = userDTO?.region
                    firestore?.collection("contents")
                        ?.whereEqualTo("contentCategory", destinationCategory)?.orderBy("timestamp", Query.Direction.DESCENDING)
                        ?.addSnapshotListener { querySnapshot, firebaseFirestoreExeption ->
                            if (querySnapshot == null) return@addSnapshotListener
                            contentDTOs.clear()
                            contentUidList.clear()

                            for (snapshot in querySnapshot!!.documents) {
                                var item = snapshot.toObject(ContentDTO::class.java)
                                contentDTOs.add(item!!)
                                contentUidList.add(snapshot.id)
                            }
                            notifyDataSetChanged()
                        }
                }
        }


        inner class CustomViewHolder(val binding: ItemContentBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BoardContentRecyclerViewAdapter.CustomViewHolder {
            val binding =
                ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: BoardContentRecyclerViewAdapter.CustomViewHolder,
            position: Int
        ) {
            holder.binding.contentTextviewTitle.text = contentDTOs!![position].title

            holder.binding.contentTextviewExplain.text = contentDTOs!![position].explain

            if (contentDTOs!![position].anonymity.containsKey(contentDTOs!![position].uid)) {
                holder.binding.contentTextviewUsername.text = "익명"
            } else {
                holder.binding.contentTextviewUsername.text = contentDTOs!![position].userName
            }


            holder.binding.contentTextviewTimestamp.text =
                SimpleDateFormat("MM/dd HH:mm").format(contentDTOs!![position].timestamp)

            holder.binding.contentTextviewCommentcount.text =
                contentDTOs!![position].commentCount.toString()

            holder.binding.contentTextviewFavoritecount.text =
                contentDTOs!![position].favoriteCount.toString()

            //글을 클릭 했을 때
            holder.binding.contentLinearLayout.setOnClickListener { v ->
                var intent = Intent(v.context, DetailContentActivity::class.java)
                if (contentDTOs[position].anonymity.containsKey(contentDTOs[position].uid)) {
                    intent.putExtra("destinationUsername", "익명")
                } else {
                    intent.putExtra("destinationUsername", contentDTOs[position].userName)
                }
                intent.putExtra("destinationTitle", contentDTOs[position].title)
                intent.putExtra("destinationExplain", contentDTOs[position].explain)
                intent.putExtra(
                    "destinationTimestamp",
                    SimpleDateFormat("MM/dd HH:mm").format(contentDTOs[position].timestamp)
                )
                intent.putExtra(
                    "destinationCommentCount",
                    contentDTOs[position].commentCount.toString()
                )
                intent.putExtra(
                    "destinationFavoriteCount",
                    contentDTOs[position].favoriteCount.toString()
                )
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationImage", contentDTOs[position].imageUrl)
                startActivity(intent)
            }


        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }


    fun getRegion() {
        var uid = auth?.currentUser?.uid
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var userDTO = documentSnapshot.toObject(UserDTO::class.java)
                region = userDTO?.region
            }
    }
}