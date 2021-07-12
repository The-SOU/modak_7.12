package com.example.modaktestone.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.modaktestone.MainActivity
import com.example.modaktestone.R
import com.example.modaktestone.databinding.ActivityCreateNameBinding
import com.example.modaktestone.navigation.AccountFragment.Companion.PICK_PROFILE_FROM_ALBUM
import com.example.modaktestone.navigation.model.UserDTO
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.regex.Pattern

class CreateNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNameBinding

    var uid: String? = null
    private var region: String? = null
    private var sex: String? = null
    private var birth: String? = null
    var url: Any? = null



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        uid = FirebaseAuth.getInstance().currentUser?.uid


        //인텐트 값 받기
        region = intent.getStringExtra("destinationRegion")
        sex = intent.getStringExtra("destinationSex")
        birth = intent.getStringExtra("destinationBirth")

        //버튼 기본적 비활성화
        binding.createnameBtn.isEnabled = false
        binding.createnameBtn.backgroundTintList =
            ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)

        //edittext 가능한 글자 유형. 영어/한글/숫자
        val filterKorea =
            InputFilter { source, start, end, dest, dstart, dend ->
                val ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-힣]+\$")
                if (!ps.matcher(source).matches()) {
                    ""
                } else null
            }
        binding.createnameEdittext.filters = arrayOf(filterKorea, LengthFilter(10))



        //edittext listener
        binding.createnameEdittext.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.createnameEdittext.text.toString().equals("")) {
                    binding.createnameBtn.isEnabled = false
                    binding.createnameBtn.backgroundTintList =
                        ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)
                } else {
                    binding.createnameBtn.isEnabled = true
                    binding.createnameBtn.backgroundTintList =
                        ContextCompat.getColorStateList(applicationContext, R.color.dots_color)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

        //edittext 엔터키 막기
        binding.createnameEdittext.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> if (keyCode == KEYCODE_ENTER) true else false })

        binding.createnameBtn.setOnClickListener {
            usernameAndRegion()
            moveMainPage()
        }

        binding.createnameLayout.setOnClickListener {
            hideKeyboard()
        }


    }

    fun usernameAndRegion() {
        var userDTO = UserDTO()
        userDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        userDTO.region = region
        userDTO.sex = sex
        userDTO.birth = birth
        userDTO.userName = binding.createnameEdittext.text.toString()
        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
    }

    fun moveMainPage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }



}

