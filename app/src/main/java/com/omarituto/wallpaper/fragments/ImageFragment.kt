package com.omarituto.wallpaper.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.omarituto.wallpaper.ImagePreview
import com.omarituto.wallpaper.MainActivity
import com.omarituto.wallpaper.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.IOException

class ImageFragment : Fragment() {

    internal var iv_wallpaper: ImageView? = null
    internal var ll_contain_infos: LinearLayout? = null
    internal var tv_publisher_name: TextView? = null
    internal var tv_description: TextView? = null
    internal var btn_set_wallpaper: Button? = null

    var bitmap_url = ""
    var publisher_name = ""
    var description = ""
    var publisher_uid = ""
    var type = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.removeAllViews()
        val view = inflater.inflate(R.layout.fragment_image_preview, container, false)
        val context: Context = inflater.context

        if (TextUtils.isEmpty(ImagePreview.isLoaded))
            return view

        iv_wallpaper = view.findViewById(R.id.iv_wallpaper)
        ll_contain_infos = view.findViewById(R.id.ll_contain_infos)
        tv_description = view.findViewById(R.id.tv_description)
        tv_publisher_name = view.findViewById(R.id.tv_publisher_name)
        btn_set_wallpaper = view.findViewById(R.id.btn_set_wallpaper)

        try {
            bitmap_url = arguments.getString("url")!!
            publisher_name = arguments.getString("poster_name")!!
            description = arguments.getString("description")!!
            publisher_uid = arguments.getString("poster_uid")!!
            type = arguments.getInt("type")
        } catch (e: Exception) {
            return view
        }

        tv_description!!.text = description
        tv_publisher_name!!.text = getString(R.string.published_by) + " " + publisher_name

        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y

        iv_wallpaper!!.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height))
        Picasso.with(context).load(bitmap_url).resize(width, height).into(iv_wallpaper)

        btn_set_wallpaper!!.setOnClickListener(View.OnClickListener {

            Picasso.with(context).load(bitmap_url).resize(width, height).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    if (Build.VERSION.SDK_INT >= 24) {

                        AlertDialog.Builder(context)
                                .setTitle(getString(R.string.set_wallpaper_for))
                                .setPositiveButton(getString(R.string.home_screen)) { dialog, which ->
                                    try {
                                        wallpaperManager.setBitmap(bitmap)
                                        Toast.makeText(context, getString(R.string.wallpaper_changed), Toast.LENGTH_SHORT).show()
                                    } catch (ex: IOException) {
                                        ex.printStackTrace()
                                    }
                                }
                                .setNegativeButton(getString(R.string.lock_screen)) { dialog, which ->
                                    try {
                                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                                        Toast.makeText(context, getString(R.string.wallpaper_changed), Toast.LENGTH_SHORT).show()
                                    } catch (ex: IOException) {
                                        ex.printStackTrace()
                                    }
                                }
                                .setNeutralButton(getString(R.string.both)) { dialog, which ->
                                    try {
                                        wallpaperManager.setBitmap(bitmap)
                                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                                        Toast.makeText(context, getString(R.string.wallpaper_changed), Toast.LENGTH_SHORT).show()
                                    } catch (ex: IOException) {
                                        ex.printStackTrace()
                                    }
                                }
                                .show()
                    } else {
                        try {
                            wallpaperManager.setBitmap(bitmap)
                            Toast.makeText(context, getString(R.string.wallpaper_changed), Toast.LENGTH_SHORT).show()
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }

                }

                override fun onBitmapFailed(errorDrawable: Drawable) {
                    Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable) {

                }
            })
        })

        tv_publisher_name!!.setOnClickListener {

            val userUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
            if (userUID.equals(publisher_uid) || MainActivity.numOpenedImageFragment > 1)
                return@setOnClickListener

            val fragment = MyProfileFragment()
            val fragmentManager = getFragmentManager()
            val fragmentTransaction = fragmentManager.beginTransaction()
            val bundle = Bundle()
            bundle.putString("uid", publisher_uid)
            fragment.arguments = bundle
            fragmentTransaction.replace(R.id.fragment, fragment)
            fragmentTransaction.commit()
            ImagePreview.isProfileOpened = true
        }

        return view
    }
}