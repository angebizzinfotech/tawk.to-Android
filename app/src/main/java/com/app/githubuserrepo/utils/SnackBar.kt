package com.app.githubuserrepo.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.app.githubuserrepo.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout

object Snackbar {
    fun MakeInternetSnackbar(
        context: Context,
        view: View?
    ) {

        val snackbar =
            Snackbar.make(
                view!!,
                "",
                Snackbar.LENGTH_SHORT
            )
        // Get the Snackbar's layout view
        val layout = snackbar.view as SnackbarLayout
        // Hide the text
        val textView =
            layout.findViewById<View>(R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE
        val mInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate our custom view
        val snackView: View =
            mInflater.inflate(R.layout.snackbar_toast_floating, null)
        // Configure the view
        val textViewTop =
            snackView.findViewById<View>(R.id.snackbar_toast_text) as TextView
        textViewTop.text = context.getString(R.string.no_internet)
        textViewTop.setTextColor(Color.WHITE)

        //If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0, 0, 0, 0)

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        snackbar.show()
    }

    fun MakeInternetSnackbar(
        context: Context,
        view: View?,
        message: String?
    ) {
        val snackbar =
            Snackbar.make(
                view!!,
                "",
                Snackbar.LENGTH_SHORT
            )
        // Get the Snackbar's layout view
        val layout = snackbar.view as SnackbarLayout
        // Hide the text
        val textView =
            layout.findViewById<View>(R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE
        val mInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate our custom view
        val snackView: View =
            mInflater.inflate(R.layout.snackbar_toast_floating, null)
        // Configure the view
        val textViewTop =
            snackView.findViewById<View>(R.id.snackbar_toast_text) as TextView
        textViewTop.text = message
        textViewTop.setTextColor(Color.WHITE)

        //If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0, 0, 0, 0)

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        snackbar.show()
    }
}
