package com.app.githubuserrepo.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.githubuserrepo.R
import com.app.githubuserrepo.model.Notes
import com.app.githubuserrepo.model.ProfileResponse
import com.app.githubuserrepo.room.AppDb
import com.app.githubuserrepo.utils.AppUtils
import com.app.githubuserrepo.utils.Snackbar
import com.app.vedicstudents.Retrofit.ApiInterface
import com.app.vedicstudents.Retrofit.BASE_URL
import com.bumptech.glide.Glide
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ProfileActivity : AppCompatActivity()  , View.OnClickListener{


    private var username: String = ""
    private lateinit var tv_follower: TextView
    private lateinit var tv_following: TextView
    private lateinit var tv_name: TextView
    private lateinit var tv_company: TextView
    private lateinit var iv_profile:ImageView

    private lateinit var tv_blog: TextView
    private lateinit var btn_save: Button
    private lateinit var et_notes: EditText
    private lateinit var iv_back:ImageView
    private lateinit var tv_toolbar_title :TextView
    private var userid :String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        username = intent.getStringExtra("username")!!

        init()

        callProfileApi()
    }

    private fun callProfileApi() {


        if (AppUtils.isNetworkAvailable(this)) {



            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiInterface::class.java)

            val userCall = service.getProfile(username = username)!!


            userCall!!.enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>,
                ) {
                    if (response.isSuccessful) {

                        val searchResponse = response.body()!!

                        tv_follower.text = searchResponse.followers
                        tv_following.text = searchResponse.following
                        tv_name.text = searchResponse.name
                        tv_company.text = searchResponse.company
                        tv_blog.text = searchResponse.blog
                        userid = searchResponse.id!!

                        val db = AppDb(this@ProfileActivity)
                        val notes = db.noteDao().getNote(searchResponse.id)
                        if(notes!=null) {
                            et_notes.setText(notes.notes_text)
                        }


                        Glide.with(this@ProfileActivity).load(searchResponse.avatar_url).into(
                            iv_profile
                        )


                    } else {

                        Snackbar.MakeInternetSnackbar(
                            this@ProfileActivity,
                            findViewById(R.id.rl_profile),
                            getString(R.string.no_user_found)
                        )
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("TAG", "onFailure: ")

                }
            })
        } else {
            Snackbar.MakeInternetSnackbar(
                this,
                findViewById(R.id.rl_profile)
            )
        }
    }

    private fun init() {

        tv_follower = findViewById(R.id.tv_follower)
        tv_following = findViewById(R.id.tv_following)
        tv_name = findViewById(R.id.tv_name)
        tv_company = findViewById(R.id.tv_company)
        tv_blog = findViewById(R.id.tv_blog)
        btn_save = findViewById(R.id.btn_save)
        et_notes = findViewById(R.id.et_notes)
        iv_profile = findViewById(R.id.iv_profile)
        iv_back = findViewById(R.id.iv_back)
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title)

        tv_toolbar_title.text = username

        btn_save.setOnClickListener(this)
        iv_back.setOnClickListener(this)
    }



    override fun onClick(v: View?) {

        when(v!!.id){
            R.id.iv_back ->{
                finish()
            }

            R.id.btn_save -> {

                val db = AppDb(this@ProfileActivity)
                if (userid != "") {
                        hideKeyboard(window.decorView.findViewById(android.R.id.content))
                    if (et_notes.text.trim().isNotEmpty()) {
                        val note = Notes()
                        note.notes_text = et_notes.text.trim().toString()
                        note.user_id = userid

                        db.noteDao().saveNote(note)
                        Toast.makeText(
                            this@ProfileActivity,
                            "Profile Save Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(MainActivity.ACTIVITY_CODE)
                        finish()
                    } else {
                        //if user make changes in note and make it empty then it will have to delete that note
                        val notes = db.noteDao().getNote(userid)
                        if (notes.user_id == userid) {
                            db.noteDao().deleteNote(userid)
                            Toast.makeText(
                                this@ProfileActivity,
                                "Profile Save Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    }

    private fun hideKeyboard(view: View) {
        view?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}