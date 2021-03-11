package com.app.githubuserrepo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.TypedValue
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.app.githubuserrepo.R
import java.io.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


internal object AppUtils {


    private val TAG: String = AppUtils::class.java.simpleName
    val LOLLIPOP = 21

    fun ShowPickerDialog(
        dialogBuilder: AlertDialog.Builder, title: String, items: Array<String>,
        selected: Int, listener: DialogInterface.OnClickListener,
    ) {

        dialogBuilder.setTitle(title)
        dialogBuilder.setSingleChoiceItems(items, selected, listener)
        val alert = dialogBuilder.create()
        alert.show()
    }


    fun ShowMessageBox(dialogBuilder: AlertDialog.Builder, title: String, message: String) {
        ShowMessageBox(
            dialogBuilder,
            title,
            message,
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
    }

    private fun ShowMessageBox(
        dialogBuilder: AlertDialog.Builder,
        title: String,
        message: String,
        listener: DialogInterface.OnClickListener,
    ) {
        val alertDialog = dialogBuilder.create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Close", listener)

        if (!alertDialog.isShowing)
            alertDialog.show()
    }

    fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            if (runningProcesses != null) {
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }


    fun printToast(context: Context, message: String) {

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isDownloadManagerAvailable(context: Context?): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun trimCache(context: Context) {
        try {
            val dir = context.cacheDir
            deleteDir(dir, context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?, context: Context): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]), context)
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else {
            false
        }
    }

    fun getStringfromSecret(secretKey: SecretKey): String {

        return android.util.Base64.encodeToString(secretKey.encoded, android.util.Base64.DEFAULT)
    }

    fun getSecretfromString(stringKey: String): SecretKey {
        val decodedKey: ByteArray = android.util.Base64.decode(
            stringKey,
            android.util.Base64.DEFAULT
        )
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    fun isDropboxUri(uri: Uri): Boolean {
        return "com.dropbox.android.FileCache" == uri.authority || "com.dropbox.product.android.dbapp.document_provider.documents" == uri.authority
    }

    fun getDriveFilePath(uri: Uri, context: Context): String? {
        val returnCursor: Cursor =
            context.contentResolver.query(uri, null, null, null, null)!!

        /**
         * Get the column indexes of the data in the Cursor,
         * * move to the first row in the Cursor, get the data,
         * * and display it.
         * */
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val file: File = File(context.getCacheDir(), name)
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
        }
        return file.path
    }

    fun spToPx(sp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.getResources().getDisplayMetrics()
        ).toInt()
    }

    fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics)
            .toInt()
    }

    fun dpToSp(dp: Float, context: Context): Int {
        return (dpToPx(dp, context) / context.resources.displayMetrics.scaledDensity).toInt()
    }


    fun readFile(file: File): ByteArray {
        var contents: ByteArray? = null

        val size = file.length()
        contents = ByteArray(size.toInt())

        try {
            val buf = BufferedInputStream(
                FileInputStream(file)
            )
            try {
                buf.read(contents)
                buf.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return contents
    }


    fun getByteArrayFromFile(context: Context, file: File) {

        val size: Int = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {

            e.printStackTrace()
        } catch (e: IOException) {

            e.printStackTrace()
        }
    }


    fun deleteTempFile(context: Context, filename: String, file: File) {
        //delete temp created file cache.mp3
        if (file.exists()) {
            val isDel = file.delete()

        }
    }


    fun getBitmapIconFromDrawable(context: Context, resource: Int): Bitmap {

        var drawable = ContextCompat.getDrawable(context, resource)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable!!.draw(canvas)

        return bitmap

//        return BitmapFactory.decodeResource(context.resources, resource)
    }

    fun getBitmapIconFromDrawable1(context: Context, drawable: Drawable): Bitmap {


        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable!!.draw(canvas)

        return bitmap

//        return BitmapFactory.decodeResource(context.resources, resource)
    }

    fun <T> List<T>.toImmutableList(): List<T> {
        if (this is ImmutableList<T>) {
            return this
        } else {
            return ImmutableList(this)
        }
    }

    class ImmutableList<T>(private val inner: List<T>) : List<T> by inner


    @SuppressLint("DefaultLocale")
    fun convertNumberintoKthousandMmillion(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
        return java.lang.String.format("%.1f %c",
            count / Math.pow(1000.0, exp.toDouble()),
            "kmgtpe"[exp - 1])
    }

    fun manageErrorResponse(errorBody: String, context: Context?) {

        showDialog(msg = errorBody, context = context as Activity)
    }

    private fun showDialog(msg: String, context: Activity) {

        val alertDialog: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        alertDialog.setTitle(context.getString(R.string.app_name))
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton(
            context.getString(R.string.ok)
        ) { _, _ ->

//            logout(context = context as Activity)
        }
        /*alertDialog.setNegativeButton(
            context.getString(R.string.cancel)
        ) { _, _ -> }*/
        val alert: androidx.appcompat.app.AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()

    }



    fun sendBroadcast(context: Context, isFreezeScreen: Boolean) {

        val intent = Intent("com.app.vedicstudents.FreezeScreen")
        intent.putExtra("isFreezeScreen", isFreezeScreen)
        context.sendBroadcast(intent)
    }

    fun sendBroadcastBackPress(context: Context, disableBottomNavigationCLick: Boolean) {

        val intent = Intent("com.app.vedicstudents.BackPress")
        intent.putExtra("disableBottomNavigationCLick", disableBottomNavigationCLick)
        context.sendBroadcast(intent)
    }
}
