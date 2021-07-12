package com.example.modaktestone.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.modaktestone.LoginActivity
import com.example.modaktestone.MainActivity
import com.example.modaktestone.databinding.FragmentAccountBinding
import com.example.modaktestone.databinding.ItemContentBinding
import com.example.modaktestone.navigation.account.*
import com.example.modaktestone.navigation.model.ContentDTO
import com.example.modaktestone.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var uid: String? = null
    var currentUserUid: String? = null

    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val view = binding.root

//        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_account, container, false)
//        return super.onCreateView(inflater, container, savedInstanceState)

        //초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        uid = arguments?.getString("destinationUid")


        //이름과 글 수 카운트, 프로필 가져오기
        getName()
        getPostCount()
        getProfileImage()

        //로그아웃 버튼 클릭
        binding.accountBtnLogout.setOnClickListener {
            activity?.finish()
            startActivity(Intent(activity, LaunchActivity::class.java))
            clearToken(currentUserUid!!)
            auth?.signOut()
        }

        //각 버튼 클릭
        binding.accountBtnMyContent.setOnClickListener { v ->
            var intent = Intent(v.context, MyContentActivity::class.java)
            startActivity(intent)
        }
        binding.accountBtnMyComment.setOnClickListener { v ->
            var intent = Intent(v.context, MyCommentActivity::class.java)
            startActivity(intent)
        }
        binding.accountBtnMyFavorite.setOnClickListener { v ->
            var intent = Intent(v.context, MyFavoriteActivity::class.java)
            startActivity(intent)
        }
        binding.accountBtnNotice.setOnClickListener { v ->
            var intent = Intent(v.context, NoticeActivity::class.java)
            startActivity(intent)
        }
        binding.accountBtnInquiry.setOnClickListener { v ->
            var intent = Intent(v.context, MyInquiryActivity::class.java)
            startActivity(intent)
        }

        binding.accountIvProfile.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }



        return view
    }

    inner class AccountFragmentRecyclerViewAdapter :
        RecyclerView.Adapter<AccountFragmentRecyclerViewAdapter.CustomViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("contents")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener

                    //get data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    binding.accountTvPostcount.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        inner class CustomViewHolder(val binding: ItemContentBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AccountFragmentRecyclerViewAdapter.CustomViewHolder {
            val binding =
                ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: AccountFragmentRecyclerViewAdapter.CustomViewHolder,
            position: Int
        ) {
            holder.binding.contentTextviewUsername.text = contentDTOs[position].userName

            holder.binding.contentTextviewTitle.text = contentDTOs[position].title

            holder.binding.contentTextviewExplain.text = contentDTOs[position].explain

            holder.binding.contentTextviewTimestamp.text =
                contentDTOs[position].timestamp.toString()

            holder.binding.contentTextviewCommentcount.text =
                contentDTOs[position].commentCount.toString()

            holder.binding.contentTextviewFavoritecount.text =
                contentDTOs[position].favoriteCount.toString()
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }

    fun getName() {
        firestore?.collection("users")?.document(currentUserUid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var nameDTO = documentSnapshot.toObject(UserDTO::class.java)
                binding.accountTvNickname.text = nameDTO?.userName.toString()
            }
    }

    fun getPostCount() {
        var postDTOs: ArrayList<ContentDTO> = arrayListOf()
        firestore?.collection("contents")?.whereEqualTo("uid", currentUserUid)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreExeption ->
                if (querySnapshot == null) return@addSnapshotListener
                postDTOs.clear()
                for (snapshot in querySnapshot.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    postDTOs.add(item!!)
                    binding.accountTvPostcount.text = postDTOs.size.toString()
                }
            }
    }

    private fun clearToken(uid: String) {
        FirebaseDatabase.getInstance().getReference("tokens").child(uid).removeValue()
    }

    fun getProfileImage() {
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(documentSnapshot==null)return@addSnapshotListener
                if(documentSnapshot?.data!!["profileUrl"] != null){
                    var url = documentSnapshot?.data!!["profileUrl"]
                    if(activity!=null){
                        Glide.with(activity!!
                        ).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
                    }
                }
            }
    }
}