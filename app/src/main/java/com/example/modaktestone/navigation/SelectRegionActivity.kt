package com.example.modaktestone.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.SpinnerAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.modaktestone.MainActivity
import com.example.modaktestone.R
import com.example.modaktestone.databinding.ActivitySelectRegionBinding
import com.example.modaktestone.navigation.model.SpinnerModel
import com.example.modaktestone.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.regex.Pattern


class SelectRegionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectRegionBinding

    private lateinit var spinnerAdapterRegion: SpinnerAdapter
    private lateinit var spinnerAdapterSex: SpinnerAdapter
    private lateinit var spinnerAdapterBirth: SpinnerAdapter
    private val listOfRegion = ArrayList<SpinnerModel>()
    private val listOfSex = ArrayList<SpinnerModel>()
    private val listOfBirth = ArrayList<SpinnerModel>()
    var uid: String? = null
    var region: String? = null
    var sex: String? = null
    var birth: String? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRegionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //초기화
        uid = FirebaseAuth.getInstance().currentUser?.uid

        setupSpinnerRegion()
        setupSpinnerSex()
        setupSpinnerBirth()
        setupSpinnerHandler()

        binding.selectregionBtn.isEnabled = false
        binding.selectregionBtn.backgroundTintList =
            ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)


        //시작하기 버튼 클릭.
        binding.selectregionBtn.setOnClickListener { v ->
            var intent = Intent(v.context, CreateNameActivity::class.java)
            intent.putExtra("destinationRegion", region)
            intent.putExtra("destinationSex", sex)
            intent.putExtra("destinationBirth", birth)
            startActivity(intent)
        }

        //키보드 숨기기
        binding.layout.setOnClickListener {
            hideKeyboard()
        }


    }


    private fun setupSpinnerRegion() {
        var regionDatas = listOf(
            "지역",
            "서울",
            "부산",
            "대구",
            "인천",
            "광주",
            "대전",
            "울산",
            "경기",
            "강원",
            "충북",
            "충남",
            "전북",
            "전남",
            "경북",
            "경남",
            "제주"
        )

        for (i in regionDatas.indices) {
            val item = SpinnerModel(regionDatas[i])
            listOfRegion.add(item)
        }
        spinnerAdapterRegion =
            com.example.modaktestone.navigation.spinner.SpinnerAdapter(
                this,
                R.layout.item_spinner,
                listOfRegion
            )
        binding.selectregionSpinner.adapter = spinnerAdapterRegion
    }

    private fun setupSpinnerSex() {
        val sexDatas = listOf("성별", "남성", "여성")

        for (i in sexDatas.indices) {
            val item = SpinnerModel(sexDatas[i])
            listOfSex.add(item)
        }
        spinnerAdapterSex =
            com.example.modaktestone.navigation.spinner.SpinnerAdapter(
                this,
                R.layout.item_spinner,
                listOfSex
            )
        binding.selectregionSpinnerSex.adapter = spinnerAdapterSex
    }

    private fun setupSpinnerBirth() {
        val birthDatas = resources.getStringArray(R.array.spinner_birth)

        for (i in birthDatas.indices) {
            val item = SpinnerModel(birthDatas[i])
            listOfBirth.add(item)
        }
        spinnerAdapterBirth = com.example.modaktestone.navigation.spinner.SpinnerAdapter(
            this,
            R.layout.item_spinner,
            listOfBirth
        )
        binding.selectregionSpinnerBirth.adapter = spinnerAdapterBirth
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupSpinnerHandler() {
        binding.selectregionSpinnerSex.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item =
                        binding.selectregionSpinnerSex.getItemAtPosition(position) as SpinnerModel
                    if (item.name != "성별") {
                        sex = item.name
                        if (region != null && birth != null) {
                            binding.selectregionBtn.isEnabled = true
                            binding.selectregionBtn.backgroundTintList =
                                ContextCompat.getColorStateList(
                                    applicationContext,
                                    R.color.dots_color
                                )
                        }
                    } else {
                        binding.selectregionBtn.isEnabled = false
                        binding.selectregionBtn.backgroundTintList =
                            ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        binding.selectregionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item =
                        binding.selectregionSpinner.getItemAtPosition(position) as SpinnerModel
                    if (item.name != "region") {
                        region = item.name
                        if (birth != null && sex != null) {
                            binding.selectregionBtn.isEnabled = true
                            binding.selectregionBtn.backgroundTintList =
                                ContextCompat.getColorStateList(
                                    applicationContext,
                                    R.color.dots_color
                                )
                        }
                    } else {
                        binding.selectregionBtn.isEnabled = false
                        binding.selectregionBtn.backgroundTintList =
                            ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}

            }

        binding.selectregionSpinnerBirth.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item =
                        binding.selectregionSpinnerBirth.getItemAtPosition(position) as SpinnerModel
                    if (item.name != "출생연도") {
                        birth = item.name
                        if (region != null && sex != null) {
                            binding.selectregionBtn.isEnabled = true
                            binding.selectregionBtn.backgroundTintList =
                                ContextCompat.getColorStateList(
                                    applicationContext,
                                    R.color.dots_color
                                )
                        }
                    } else {
                        binding.selectregionBtn.isEnabled = false
                        binding.selectregionBtn.backgroundTintList =
                            ContextCompat.getColorStateList(applicationContext, R.color.whitegrey)

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }


    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}