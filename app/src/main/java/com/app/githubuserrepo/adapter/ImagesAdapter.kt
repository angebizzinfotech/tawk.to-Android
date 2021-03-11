package com.app.githubuserrepo.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.app.githubuserrepo.R
import com.app.githubuserrepo.`interface`.OnClickListen
import com.app.githubuserrepo.`interface`.PaginationAdapterCallback
import com.app.githubuserrepo.model.UserListResponseItem
import com.app.githubuserrepo.room.AppDb
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.app.githubuserrepo.utils.AppUtils
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ImagesAdapter(
    val context: Context,
    val onClickListen: OnClickListen

) :
    RecyclerView.Adapter<ImagesAdapter.BaseViewHolder<*>>() {


    // View Types
    private val ITEM = 0
    private val LOADING = 1

    private var userListItems: MutableList<UserListResponseItem>? =
        ArrayList<UserListResponseItem>()
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private val mCallback: PaginationAdapterCallback? = null

    fun getSonglists(): List<UserListResponseItem?>? {
        return userListItems
    }

    fun setSonglists(movieResults: List<UserListResponseItem?>?) {
        this.userListItems = movieResults as MutableList<UserListResponseItem>?
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        var viewHolder: BaseViewHolder<UserListResponseItem>? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem: View = inflater.inflate(R.layout.row_users, parent, false)
                viewHolder = UserViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading: View = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
        }
        return viewHolder!!
    }


    override fun getItemViewType(position: Int): Int {

        if (position == userListItems!!.size - 1 && isLoadingAdded)
            return LOADING
        else
            return ITEM
    }

    override fun getItemCount(): Int {
        return if (userListItems != null) {
            userListItems?.size!!
        } else
            0
    }


    fun add(r: UserListResponseItem?) {
        userListItems?.add(r!!)
        notifyItemInserted(userListItems!!.size - 1)
    }

    fun addAll(moveResults: List<UserListResponseItem?>) {
        for (result in moveResults) {
            add(result)
        }
    }

    fun updateList(moveResults: List<UserListResponseItem?>) {
        userListItems = moveResults as MutableList<UserListResponseItem>
        notifyDataSetChanged()
    }

    fun remove(r: UserListResponseItem?) {
        val position = userListItems!!.indexOf(r)
        if (position > -1) {
            userListItems!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }

    }

    fun getLastItem(): UserListResponseItem {

        return userListItems!![userListItems!!.size - 2]  //Size - 2 because we have added add loading footer
    }

    fun isEmpty(): Boolean {
        return itemCount == 0
    }

    fun getItem(position: Int): UserListResponseItem? {
        return userListItems!![position]
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(UserListResponseItem())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position = userListItems!!.size - 1
        val result: UserListResponseItem? = getItem(position)
        if (result != null) {
            userListItems!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, @Nullable errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(userListItems!!.size - 1)
        if (errorMsg != null) this.errorMsg = errorMsg
    }


    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        holder.setIsRecyclable(false)
        when (getItemViewType(position)) {

            ITEM -> {
                val holder: UserViewHolder =
                    holder as UserViewHolder

                val userListResponseItem = userListItems!![position]

                holder.bind(userListResponseItem, context = context, holder.adapterPosition)

                holder.cv_usercard.setOnClickListener(View.OnClickListener {
                    onClickListen.onDataClick(position, userListResponseItem.login!!)
                })


            }

            LOADING -> {
                val holder: LoadingVH =
                    holder as LoadingVH

                holder.bind(UserListResponseItem(), context = context, position = position)

                if (retryPageLoad) {
                    holder.mErrorLayout.visibility = View.VISIBLE
                    holder.mProgressBar.visibility = View.GONE
                    holder.mErrorTxt.text =
                        errorMsg ?: context.getString(R.string.error_msg_unknown)
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


    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(item: T, context: Context, position: Int)
    }

    class UserViewHolder(itemView: View) : BaseViewHolder<UserListResponseItem>(itemView) {

        val img_profile_image = itemView.findViewById<CircleImageView>(R.id.img_profile_image)
        val tv_username = itemView.findViewById<TextView>(R.id.tv_username)
        val iv_notes = itemView.findViewById<ImageView>(R.id.iv_notes)
        val cv_usercard = itemView.findViewById<CardView>(R.id.cv_usercard)
        val tv_url = itemView.findViewById<TextView>(R.id.tv_url)


        override fun bind(item: UserListResponseItem, context: Context, position: Int) {


            tv_username.text = item.login
            tv_url.text = item.url
            iv_notes.visibility = View.GONE

            val db = AppDb(context)
            val note = db.noteDao().getNote(item.id)
            if (note != null) {
                iv_notes.visibility = View.VISIBLE
            }

            if ((position + 1) % 4 == 0) {
                try {
                    var invertIm: Bitmap? = null
                    if (item.avatar_url!!.contains("http")) {
                        if (AppUtils.isNetworkAvailable(context)) {


                            GlobalScope.launch(Dispatchers.IO){

                                val url = URL(item.avatar_url)

                                val image = BitmapFactory.decodeStream(
                                    url.openConnection().getInputStream()
                                )

                                invertIm = invertImage(image)!!

                                launch(Dispatchers.Main){
                                    Glide.with(context.applicationContext).load(invertIm)
                                        .into(img_profile_image)
                                }
                            }
                        } else {
                            Glide.with(context.applicationContext).load(R.drawable.user_default)
                                .into(img_profile_image)
                        }

                    } else {

                        val bitmap = BitmapFactory.decodeFile(item.avatar_url)
                        invertIm = invertImage(bitmap)!!
                    }

                } catch (e: IOException) {
                    println(e)
                }
            } else {

                if (context.applicationContext != null) {

                    Glide.with(context.applicationContext).load(item.avatar_url)
                        .into(img_profile_image)
                }
            }
        }


        fun invertImage(src: Bitmap): Bitmap? {
            // create new bitmap with the same attributes(width,height)
            //as source bitmap
            val bmOut = Bitmap.createBitmap(src.width, src.height, src.config)
            // color info
            var A: Int
            var R: Int
            var G: Int
            var B: Int
            var pixelColor: Int
            // image size
            val height = src.height
            val width = src.width

            // scan through every pixel
            for (y in 0 until height) {
                for (x in 0 until width) {
                    // get one pixel
                    pixelColor = src.getPixel(x, y)
                    // saving alpha channel
                    A = Color.alpha(pixelColor)
                    // inverting byte for each R/G/B channel
                    R = 255 - Color.red(pixelColor)
                    G = 255 - Color.green(pixelColor)
                    B = 255 - Color.blue(pixelColor)
                    // set newly-inverted pixel to output image
                    bmOut.setPixel(x, y, Color.argb(A, R, G, B))
                }
            }

            // return final bitmap
            return bmOut
        }

    }

    class LoadingVH(itemView: View) : BaseViewHolder<UserListResponseItem>(itemView) {

        val mProgressBar = itemView.findViewById(R.id.loadmore_progress) as ProgressBar
        val mRetryBtn = itemView.findViewById(R.id.loadmore_retry) as ImageView
        val mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt) as TextView
        val mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout) as LinearLayout

        override fun bind(item: UserListResponseItem, context: Context, position: Int) {

        }
    }


}