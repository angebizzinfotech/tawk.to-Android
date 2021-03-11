/*
package com.app.githubuserrepo.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.app.vedicstudents.RoomDB.songEntity
import com.app.vedicstudents.RoomDB.AppDb
import com.app.vedicstudents.Constant.Constants
import com.app.vedicstudents.PreferenceManagerClass
import com.app.vedicstudents.PreferenceManagerClass.get
import com.app.vedicstudents.R
import com.app.vedicstudents.activity.AddToPlaylistScreenActivity
import com.app.vedicstudents.activity.BaseActivity
import com.app.vedicstudents.activity.LoginActivity
import com.app.vedicstudents.download.FileDownloader
import com.app.vedicstudents.interfaces.PaginationAdapterCallback
import com.app.vedicstudents.model.Song
import com.app.vedicstudents.utils.AppUtils
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.OkHttpClient

class PaginationAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String? = PaginationAdapter::class.java.simpleName

    // View Types
    private val ITEM = 0
    private val LOADING = 1

    private var movieResults: MutableList<Song>? = ArrayList<Song>()
    private var isLoadingAdded = false
    private var retryPageLoad = false

    private val mCallback: PaginationAdapterCallback? = null

    private var errorMsg: String? = null

    val db = AppDb(context)
    var disposable = Disposable.disposed()
    val fileDownloader by lazy {
        FileDownloader(
            OkHttpClient.Builder().build()
        )
    }

    fun getMovies(): List<Song?>? {
        return movieResults
    }

    fun setMovies(movieResults: List<Song?>?) {
        this.movieResults = movieResults as MutableList<Song>?
    }

    companion object {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem: View = inflater.inflate(R.layout.layout__sub_category, parent, false)
                viewHolder = TypeViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading: View = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {

            ITEM -> {
                val holder: TypeViewHolder =
                    holder as TypeViewHolder


                val playlistModel = movieResults?.get(position)!!
                playlistModel.category!!.name
                playlistModel.subcategory

                holder.recent_category_list_title.text = playlistModel.title
                holder.recent_category_list_artist.text = playlistModel.performer!!.name

                if (playlistModel.is_contribute == 1 && !playlistModel.is_purchased!!) {
                    holder.iv_premium_icon.visibility = View.VISIBLE
                } else {
                    holder.iv_premium_icon.visibility = View.GONE
                }

                var genrevalue = ""
                if (playlistModel.genres_values == null || playlistModel.genres_values!!.getName() == null || playlistModel.genres_values!!.getName() == "") {
                    genrevalue = context!!.getString(R.string.genre_slash)
                } else {
                    genrevalue = playlistModel.genres_values!!.getName()!!
                }

                holder.recent_category_list_genres.text = genrevalue

                if (playlistModel.is_download!!) {
                    holder.img_category_download.visibility = View.GONE
                    holder.img_category_already_downloaded.visibility = View.VISIBLE
                } else {
                    holder.img_category_download.visibility = View.VISIBLE
                    holder.img_category_already_downloaded.visibility = View.GONE
                }

                holder.img_add_category_list.setOnClickListener(View.OnClickListener {

                    val prefs = PreferenceManagerClass.defaultPrefs(context)

                    //Check if audio is is_Contribute , if yes  then  check whether user is premium or not
                    if (playlistModel.is_contribute == 1) {
                        if (playlistModel.is_purchased!! || prefs[Constants.IS_USER_SAFFRON, false]) {

                            showPlaylistScreen(playlistModel)
                        } else {
                            navigateSubscriptionFragment(playlistModel)
                        }

                    } else {

                        if (prefs[Constants.IS_LOGGEDIN, false]) {

                            showPlaylistScreen(playlistModel)
                        } else {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)

                        }
                    }
                })

                holder.img_category_download.setOnClickListener(View.OnClickListener {

                    val prefs = PreferenceManagerClass.defaultPrefs(context)


                    if (playlistModel.is_contribute == 1) {

                        //Check if audio is premium or not
                        if (playlistModel.is_purchased!! || prefs[Constants.IS_USER_SAFFRON, false] || playlistModel.is_contribute != 1) {

                            */
/**
                             * save song data in Database and Audio image , Audio key , Audio data will be store separately
                             *//*

                            val db_song_id = db.songDao().getSongId(playlistModel.id!!)
                            Handler(Looper.getMainLooper()).postDelayed({

                                if (db_song_id != null && db_song_id == playlistModel.id) {
                                    Toast.makeText(
                                        context,
                                        "Already Downloaded",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    */
/**
                                     * Disable BackPress
                                     *//*

                                    Constants.NO_OF_DOWNLOADING =
                                        Constants.NO_OF_DOWNLOADING + 1
                                    AppUtils.sendBroadcastBackPress(context, false)
                                    Log.e(TAG,
                                        "===onBindViewHolder: no of down: ${Constants.NO_OF_DOWNLOADING}")


                                    //showing progress bar & hide download button
                                    holder.progressBar.visibility=View.VISIBLE
                                    holder.img_category_download.visibility=View.GONE
                                    //saving song details in database
                                    AppUtils.saveSongToDatabase(context, playlistModel)
                                    //saving song image by downloading , encoding and store in database
                                    AppUtils.saveAudioImageToDatabase(context, playlistModel)

                                    if (playlistModel.full_audio != null && playlistModel.full_audio != "") {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.full_audio.toString(),
                                            fullAudio = true,
                                            halfAudio = false,
                                            quaterlyAudio = false,
                                            playlistModel = playlistModel
                                        )
                                    } else if (playlistModel.half_audio != null && playlistModel.half_audio != "") {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.half_audio.toString(),
                                            fullAudio = false,
                                            halfAudio = true,
                                            quaterlyAudio = false,
                                            playlistModel = playlistModel
                                        )

                                    } else {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.quarterly_audio.toString(),
                                            fullAudio = false,
                                            halfAudio = false,
                                            quaterlyAudio = true,
                                            playlistModel = playlistModel
                                        )
                                    }
                                }

                            }, 300)
                        } else {
                            if (Constants.NO_OF_DOWNLOADING < 1) {
                                navigateSubscriptionFragment(playlistModel)
                            }
                        }
                    } else {
                        if (prefs[Constants.IS_LOGGEDIN, false]) {
                            */
/**
                             * save song data in Database and Audio image , Audio key , Audio data will be store separately
                             *//*

                            val db_song_id = db.songDao().getSongId(playlistModel.id!!)
                            Handler(Looper.getMainLooper()).postDelayed({

                                if (db_song_id != null && db_song_id == playlistModel.id) {
                                    Toast.makeText(
                                        context,
                                        "Already Downloaded",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    */
/**
                                     * Disable BackPress
                                     *//*

                                    Constants.NO_OF_DOWNLOADING =
                                        Constants.NO_OF_DOWNLOADING + 1
                                    AppUtils.sendBroadcastBackPress(context, false)
                                    Log.e(TAG,
                                        "===onBindViewHolder: no of down: ${Constants.NO_OF_DOWNLOADING}")

                                    //showing progress bar & hide download button
                                    holder.progressBar.visibility=View.VISIBLE
                                    holder.img_category_download.visibility=View.GONE
                                    //saving song details in database
                                    AppUtils.saveSongToDatabase(context, playlistModel)
                                    //saving song image by downloading , encoding and store in database
                                    AppUtils.saveAudioImageToDatabase(context, playlistModel)

                                    if (playlistModel.full_audio != null && playlistModel.full_audio != "") {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.full_audio.toString(),
                                            fullAudio = true,
                                            halfAudio = false,
                                            quaterlyAudio = false,
                                            playlistModel = playlistModel
                                        )
                                    } else if (playlistModel.half_audio != null && playlistModel.half_audio != "") {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.half_audio.toString(),
                                            fullAudio = false,
                                            halfAudio = true,
                                            quaterlyAudio = false,
                                            playlistModel = playlistModel
                                        )

                                    } else {

                                        AppUtils.getSignedUrl(
                                            context,
                                            holder.progressBar,
                                            holder.img_category_already_downloaded,
                                            holder.img_category_download,
                                            playlistModel.id.toString(),
                                            playlistModel.quarterly_audio.toString(),
                                            fullAudio = false,
                                            halfAudio = false,
                                            quaterlyAudio = true,
                                            playlistModel = playlistModel
                                        )
                                    }
                                }

                            }, 300)
                        } else {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }
                    }


                })

                holder.layout_main.setOnClickListener(View.OnClickListener {


                    val pref = PreferenceManagerClass.defaultPrefs(context)

                    var currentPlayingID = ""
                    if (BaseActivity.mPlayerAdapter!!.isMediaPlayer()) {
                        val current = BaseActivity.mPlayerAdapter!!.getCurrentSong()

                        if (current is Song) {
                            currentPlayingID = current.id!!
                        } else {
                            val current = current as songEntity

                            currentPlayingID = current.audio_id
                        }
                    }

                    var startSong: Boolean
                    startSong = currentPlayingID != movieResults!![holder.adapterPosition].id


                    AppUtils.playSong(context = context,
                        playlistModel.is_contribute,
                        startSong,
                        movieResults!![position],
                        movieResults!!.toMutableList(),
                        playlistModel.is_purchased!!
                    )

                })

                if (context != null) {
                    Glide.with(context).load(playlistModel.image)
                        .into(holder.img_category_list)
                }
            }

            LOADING -> {
                val holder: LoadingVH =
                    holder as LoadingVH

                if (retryPageLoad) {
                    holder.mErrorLayout.visibility = View.VISIBLE
                    holder.mProgressBar.visibility = View.GONE
                    holder.mErrorTxt.text =
                        errorMsg ?: context!!.getString(R.string.error_msg_unknown)
                } else {
                    holder.mErrorLayout.visibility = View.GONE
                    holder.mProgressBar.visibility = View.VISIBLE
                }

                holder.mErrorLayout.setOnClickListener(View.OnClickListener {
                    showRetry(true, null)
                    mCallback?.retryPageLoad()
                })
            }
        }
    }


    private fun navigateSubscriptionFragment(song: Song) {
        val bundle = Bundle()
        bundle.putString("category_id", song.category!!.id)
        bundle.putString("category_name", song.category!!.name)
        bundle.putString("sub_category_id", song.subcategory!!.id)
        bundle.putString("sub_category_name", song.subcategory!!.name)
        bundle.putString("audio_id", song.id)
        bundle.putString("audio_title", song.title)
        BaseActivity.navController.navigate(R.id.subScriptionPlanFragment, bundle)
    }

    private fun showPlaylistScreen(playlistModel: Song) {


        val intent = Intent(context, AddToPlaylistScreenActivity::class.java)
        intent.putExtra("audio_id", playlistModel.id)

        context.startActivity(intent)


    }

    override fun getItemCount(): Int {
        return if (movieResults != null) {
            movieResults?.size!!
        } else
            0
    }

    override fun getItemViewType(position: Int): Int {

        return if (position == movieResults!!.size - 1 && isLoadingAdded)
            LOADING
        else
            ITEM
    }

    */
/*
        Helpers - Pagination
   _________________________________________________________________________________________________
    *//*

    fun add(r: Song?) {
        movieResults?.add(r!!)
        notifyItemInserted(movieResults!!.size - 1)
    }

    fun addAll(moveResults: List<Song?>) {
        for (result in moveResults) {
            add(result)
        }
    }

    fun remove(r: Song?) {
        val position = movieResults!!.indexOf(r)
        if (position > -1) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun isEmpty(): Boolean {
        return itemCount == 0
    }

    fun getItem(position: Int): Song? {
        return movieResults!![position]
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Song())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position = movieResults!!.size - 1
        val result: Song? = getItem(position)
        if (result != null) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    */
/**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     *//*

    fun showRetry(show: Boolean, @Nullable errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(movieResults!!.size - 1)
        if (errorMsg != null) this.errorMsg = errorMsg
    }


    class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val layout_main = itemView.findViewById(R.id.layout_main) as LinearLayout
        val img_category_list = itemView.findViewById(R.id.img_category_list) as ImageView
        val img_add_category_list = itemView.findViewById(R.id.img_add_category_list) as Button
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
        val img_category_download =
            itemView.findViewById(R.id.img_category_list_download) as Button
        val recent_category_list_title =
            itemView.findViewById(R.id.recent_category_list_title) as TextView
        val recent_category_list_genres =
            itemView.findViewById(R.id.recent_category_list_genres) as TextView
        val recent_category_list_artist =
            itemView.findViewById(R.id.recent_category_list_artist) as TextView

        val img_category_already_downloaded =
            itemView.findViewById<Button>(R.id.img_category_already_downloaded)

        val iv_premium_icon = itemView.findViewById<ImageView>(R.id.iv_premium_icon)
    }

    class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mProgressBar = itemView.findViewById<ProgressBar>(R.id.loadmore_progress)
        val mRetryBtn = itemView.findViewById<ImageView?>(R.id.loadmore_retry)
        val mErrorTxt = itemView.findViewById<TextView?>(R.id.loadmore_errortxt)
        val mErrorLayout = itemView.findViewById<LinearLayout?>(R.id.loadmore_errorlayout)
    }
}*/
