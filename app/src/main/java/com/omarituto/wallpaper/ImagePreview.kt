package com.omarituto.wallpaper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omarituto.wallpaper.fragments.ImageFragment


class ImagePreview : AppCompatActivity() {

    var url = ""
    var poster_name = ""
    var description = ""
    var poster_uid = ""
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        val intent = getIntent().extras!!
        url = intent.getString("url")!!
        poster_name = intent.getString("poster_name")!!
        description = intent.getString("description")!!
        poster_uid = intent.getString("poster_uid")!!
        type = intent.getInt("type")

        MainActivity.numOpenedImageFragment += 1
        openImageFragment()

    }

    private fun openImageFragment() {
        val fragment = ImageFragment()
        val fragmentManager = getFragmentManager()
        val fragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("url", url)
        bundle.putString("poster_name", poster_name)
        bundle.putString("description", description)
        bundle.putString("poster_uid", poster_uid)
        bundle.putInt("type", type)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.fragment, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        var isLoaded = "ok"
        var isProfileOpened = false
    }

    override fun onBackPressed() {
        if (isProfileOpened) {
            openImageFragment()
            isProfileOpened = false
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        if (MainActivity.numOpenedImageFragment > 0)
            MainActivity.numOpenedImageFragment -= 1
        super.onDestroy()
    }
}
