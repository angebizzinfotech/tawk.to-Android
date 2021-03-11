package com.app.githubuserrepo.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.githubuserrepo.R
import com.app.githubuserrepo.`interface`.OnClickListen
import com.app.githubuserrepo.adapter.ImagesAdapter
import com.app.githubuserrepo.model.UserListResponse
import com.app.githubuserrepo.model.UserListResponseItem
import com.app.githubuserrepo.room.AppDb
import com.app.githubuserrepo.utils.*
import com.app.vedicstudents.Retrofit.ApiInterface
import com.app.vedicstudents.Retrofit.BASE_URL
import com.ethanhua.skeleton.Skeleton
import com.facebook.shimmer.ShimmerFrameLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: ImagesAdapter
    private lateinit var rv_userlist: RecyclerView
    private lateinit var progress_circular: ProgressBar
    private lateinit var tv_search: EditText
    private lateinit var receiver: BroadcastReceiver

    private val PAGE_START = 0
    private var isLoading = false
    private var isLastPage = false

    private var currentPage = PAGE_START
    companion object{
        val ACTIVITY_CODE = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        setBroadcast()

        //adapter
        adapter = ImagesAdapter(this@MainActivity, object : OnClickListen {
            override fun onDataClick(position: Int, data: String) {

                if (AppUtils.isNetworkAvailable(this@MainActivity)) {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra("username", data)
                    startActivityForResult(intent, ACTIVITY_CODE)
                } else {

                    Snackbar.MakeInternetSnackbar(this@MainActivity, findViewById(R.id.rl_userlist))
                }

            }
        })
        linearLayoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        rv_userlist.layoutManager = linearLayoutManager
        rv_userlist.itemAnimator = DefaultItemAnimator()
        rv_userlist.adapter = adapter

        rv_userlist.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {

                if (tv_search.text.trim().isEmpty()) {
                    isLoading = true
                    currentPage += 1
                    val lastItem = adapter.getLastItem()

                    loadNextPage(lastItem.id!!)
                }
            }

            override fun getTotalPageCount(): Int {
                return 1000
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        if (AppUtils.isNetworkAvailable(this)) {

//            callUserListApi()
        } else {
            getOfflineData()
        }


    }

    private fun setBroadcast() {

        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                if (intent?.action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {

                    // when user is offline mode and enable internet or wifi
                    onNetworkConnectionChanged(AppUtils.isNetworkAvailable(applicationContext))

                }
            }
        }
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (receiver != null)
            unregisterReceiver(receiver)
    }

    private fun getOfflineData() {
        val db = AppDb(this)
        val userList = db.userDao().getAllUsers()
        adapter.addAll(userList)

    }

    fun onNetworkConnectionChanged(isConnected: Boolean) {

        if (isConnected) {
            //if No Internet Redirect to Download Fragment
            if(adapter.isEmpty()){
                callUserListApi()
            }else{
                val lastindex= adapter.getLastItem()
                loadNextPage(lastindex.id)
            }

        } else {

            showDialog("")
        }
    }

    fun showDialog(title: String) {
        val alertDialog: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.app_name))
        alertDialog.setMessage(getString(R.string.no_internet))
        alertDialog.setPositiveButton(
            getString(R.string.ok)
        ) { _, _ ->

        }

        val alert: androidx.appcompat.app.AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun init() {

        rv_userlist = findViewById(R.id.rv_userlist)
        progress_circular = findViewById(R.id.progress_circular)
        tv_search = findViewById(R.id.tv_search)
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout)


        tv_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.clear()
                val db = AppDb(this@MainActivity)
                if (s!!.isNotEmpty()) {

                    val query = "%$s%"
                    val userList = db.userDao().getSearchUser(query)
                    Log.e("TAG", "===onTextChanged: " + userList.size)

                    adapter.addAll(userList)
                } else {
                    val userList = db.userDao().getAllUsers()

                    adapter.addAll(userList)
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("users", adapter.getSonglists() as ArrayList)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val list:List<UserListResponseItem> = savedInstanceState.getParcelableArrayList("users")!!;

        adapter.clear()
        adapter.addAll(list)
    }


    private fun callUserListApi() {
        currentPage = PAGE_START
        isLoading = false
        isLastPage = false
        adapter.clear()

        if (AppUtils.isNetworkAvailable(this)) {


            shimmerFrameLayout.visibility = View.VISIBLE;
            shimmerFrameLayout.startShimmer()



            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiInterface::class.java)

            val userCall = service.getUserlists("0")!!

            Log.e("TAG", "===callUserListApi: " )
            userCall!!.enqueue(object : Callback<UserListResponse> {
                override fun onResponse(
                    call: Call<UserListResponse>,
                    response: Response<UserListResponse>,
                ) {
                    shimmerFrameLayout.visibility = View.GONE;
                    shimmerFrameLayout.stopShimmer()
                    if (response.isSuccessful) {

                        val searchResponse = response.body()!!

                        setData(searchResponse)
                        storeUserData(searchResponse)

                    } else {
                        adapter.clear()
                        getOfflineData()
                        Snackbar.MakeInternetSnackbar(
                            this@MainActivity,
                            findViewById(R.id.rl_userlist),
                            getString(R.string.no_user_found)
                        )
                    }
                }

                override fun onFailure(call: Call<UserListResponse>, t: Throwable) {
                    shimmerFrameLayout.visibility = View.GONE;
                    shimmerFrameLayout.stopShimmer()
                    adapter.clear()
                    Log.e("TAG", "onFailure: ")

                }
            })
        } else {
            Snackbar.MakeInternetSnackbar(
                this,
                findViewById(R.id.rl_userlist)
            )
        }
    }


    private fun setData(userListResponse: UserListResponse) {

        adapter.addAll(userListResponse)
        adapter.addLoadingFooter()
    }

    private fun loadNextPage(page: String) {

        if (AppUtils.isNetworkAvailable(this)) {

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiInterface::class.java)

            val userCall = service.getUserlists(since = page)!!


            userCall.enqueue(object : Callback<UserListResponse> {
                override fun onResponse(
                    call: Call<UserListResponse>,
                    response: Response<UserListResponse>,
                ) {
                    adapter.removeLoadingFooter()
                    isLoading = false
                    if (response.isSuccessful) {

                        val searchResponse = response.body()!!
                        setData(searchResponse)
                        storeUserData(searchResponse)

                    }
                }

                override fun onFailure(call: Call<UserListResponse>, t: Throwable) {
                    Log.e("TAG", "onFailure: ")

                }
            })
        } else {
            Snackbar.MakeInternetSnackbar(
                this,
                findViewById(R.id.rl_userlist)
            )
        }
    }

    private fun storeUserData(userListResponse: UserListResponse) {

        GlobalScope.launch(Dispatchers.IO) {

            val db = AppDb(this@MainActivity)

            for (item in userListResponse.withIndex()) {

                db.userDao().saveUser(item.value)

            }

            for (item in userListResponse) {
                saveAudioImageToDatabase(this@MainActivity, item)

            }
        }
    }

    @SuppressLint("NewApi")
    fun saveAudioImageToDatabase(context: Context, userListResponseItem: UserListResponseItem) {
        val db = AppDb(context)

        var disposable = Disposable.disposed()
        val fileDownloader by lazy {
            FileDownloader(
                OkHttpClient.Builder().build()
            )
        }

        /**
         * generate Secret Key for encrypting
         */
        var yourKey: SecretKey? = null
        yourKey = FileDownloader.generateKey()

        /**
         * Create temp File in cache directory
         */
        val filenm = userListResponseItem.login + ".png"


        val path = context.filesDir.path + File.separator + filenm;
        val file = File(path)
        file.parentFile.mkdirs()

        disposable = fileDownloader.download(
            userListResponseItem.avatar_url.toString(),
            file,
            yourKey
        )
            .throttleFirst(2, TimeUnit.SECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                /**
                 * Downloading
                 */

            }, {
                /**
                 * Download Failed
                 */


            }) {
                /**
                 * Download Complete , save image path to database
                 */

                db.userDao().updateUser(
                    userListResponseItem.id,
                    RealPathUtil.getRealPath(context, Uri.fromFile(file)).toString(),
                )
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ACTIVITY_CODE){
            adapter.notifyDataSetChanged()
        }
    }
}