package com.example.modaktestone.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modaktestone.R
import com.example.modaktestone.databinding.FragmentAlarmBinding
import com.example.modaktestone.navigation.model.AlarmDTO
import com.example.modaktestone.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import java.text.SimpleDateFormat

class AlarmFragment : Fragment() {
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private var uid: String? = null
    var alarmUidList: ArrayList<String> = arrayListOf()

    var firestore: FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        val view = binding.root

        //초기화
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerViewAdapter()
        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(this.activity)

        //툴바 설정
        val toolbar = binding.myToolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setDisplayShowTitleEnabled(false)
        ab!!.setDisplayShowCustomEnabled(true)

        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOList.clear()
                    alarmUidList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                        alarmUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //아이템 코멘트를 그대로 쓸 것이다
            val view = LayoutInflater.from(activity).inflate(R.layout.item_alarm, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView

            when (alarmDTOList[position].kind) {
                //공감 알람일 때
                0 -> {
                    val str_0 = alarmDTOList[position].userName + getString(R.string.alarm_favorite)
                    view.alarm_tv_content.text = str_0
                    view.alarm_tv_timestamp.text =
                        SimpleDateFormat("MM/dd HH:mm").format(alarmDTOList!![position].timestamp)
                    //이미 읽었다면 하양색으로 하
                    if(alarmDTOList[position].didYouRead.containsKey(uid)){
                        view.alarm_layout.setBackgroundColor(Color.WHITE)
                    }

                }
                //댓글 알람일 때
                1 -> {
                    val str_1 = alarmDTOList[position].userName + getString(R.string.alarm_comment)
                    view.alarm_tv_content.text = str_1
                    view.alarm_tv_timestamp.text =
                        SimpleDateFormat("MM/dd HH:mm").format(alarmDTOList!![position].timestamp)
                    if(alarmDTOList[position].didYouRead.containsKey(uid)){
                        view.alarm_layout.setBackgroundColor(Color.WHITE)
                    }
                }
            }
            //알림 클릭 시 해당페이지로 화면 이동
            view.alarm_tv_content.setOnClickListener { v ->
                firestore?.collection("contents")
                    ?.document(alarmDTOList[position]!!.contentUid.toString())
                    ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                        if (documentSnapshot == null) return@addSnapshotListener
                        var contentDTO = documentSnapshot.toObject(ContentDTO::class.java)

                        var intent = Intent(v.context, DetailContentActivity::class.java)
                        if (contentDTO!!.anonymity.containsKey(contentDTO.uid)) {
                            intent.putExtra("destinationUsername", "익명")
                        } else {
                            intent.putExtra("destinationUsername", contentDTO.userName)
                        }
                        intent.putExtra("destinationTitle", contentDTO.title)
                        intent.putExtra("destinationExplain", contentDTO.explain)
                        intent.putExtra(
                            "destinationTimestamp",
                            SimpleDateFormat("MM/dd HH:mm").format(contentDTO.timestamp)
                        )
                        intent.putExtra(
                            "destinationCommentCount",
                            contentDTO.commentCount.toString()
                        )
                        intent.putExtra(
                            "destinationFavoriteCount",
                            contentDTO.favoriteCount.toString()
                        )
                        intent.putExtra("destinationUid", contentDTO.uid)
                        intent.putExtra("contentUid", alarmDTOList[position]!!.contentUid)
                        startActivity(intent)


                    }
            }
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }

    fun checkRead() {
        for(snapshot in alarmUidList){
            var tsDoc = firestore?.collection("alarms")?.document(snapshot)
            firestore?.runTransaction { transaction ->
                var alarmDTO = transaction.get(tsDoc!!).toObject(AlarmDTO::class.java)
                alarmDTO!!.didYouRead[uid!!] = true
                transaction.set(tsDoc, alarmDTO)
                return@runTransaction
            }
        }

    }

    //화면이 끝날 때 이 1을 추가해서 다음 화면에서 반영된 모습으로 나오게 나온다. 이번화면에는 반영안되게.
    override fun onDestroyView() {
        super.onDestroyView()
        checkRead()
    }
}