package com.omarituto.wallpaper.fragments

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omarituto.wallpaper.MainActivity
import com.omarituto.wallpaper.R
import com.squareup.picasso.Picasso


class AddWallpaper : Fragment() {

    internal var et_url: EditText? = null
    internal var et_description: EditText? = null
    internal var btn_load: Button? = null
    internal var btn_publish: Button? = null
    internal lateinit var btn_how: Button
    internal var load_was_clicked: Boolean = false
    internal lateinit var spinner_type: Spinner
    internal lateinit var context: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.removeAllViews()
        val view = inflater.inflate(R.layout.fragment_add_wallpaper, container, false)
        context = inflater.context

        et_url = view.findViewById(R.id.et_url)
        et_description = view.findViewById(R.id.et_description)
        btn_load = view.findViewById(R.id.btn_load)
        btn_how = view.findViewById(R.id.btn_how)
        btn_publish = view.findViewById(R.id.btn_publish)
        spinner_type = view.findViewById(R.id.spinner_type)
        val iv_preview: ImageView = view.findViewById(R.id.iv_preview)

        load_categories_spinner()

        var url = ""
        btn_load!!.setOnClickListener(View.OnClickListener {
            url = et_url!!.text.toString().trim()
            if (checkInput(url)) {
                toast(getString(R.string.url_filed_empty))
                return@OnClickListener
            }

            if (url.contains("drive.google.com/file/d/")
                    || url.contains("/view")
                    || url.contains("drive.google.com/open?id=")) {

                if (url.contains("drive.google.com/file/d/"))
                    url = url.replace("file/d/", "uc?id=")

                if (url.contains("drive.google.com/open?id="))
                    url = url.replace("open?id=", "uc?id=")

                if (url.contains("/view")) {
                    url = url.split("/view")[0]
                }
            }

            //optimize picture with space in xml imageview
            val display = (context as Activity).windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            var width = size.x
            var height = size.y
            val iv_height = iv_preview.getHeight()
            val diff = height / iv_height
            width = width / diff
            height = iv_height

            Picasso.with(context)
                    .load(url)
                    .resize(width, height)
                    .placeholder(R.drawable.loading) // Your dummy image...
                    .into(iv_preview, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            et_url!!.isEnabled = false
                            load_was_clicked = true
                        }

                        override fun onError() {
                            // Unable to load image, may be due to incorrect URL, no network...
                            toast(getString(R.string.url_not_valid))
                        }
                    })
        })

        btn_how.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(getString(R.string.how))
                    .setMessage(getString(R.string.voila_comment))
                    .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.more_info)) { dialog, which ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri
                                .parse("https://drive.google.com/open?id=1NQDkD0ykSLge6yny0JS2n1ETGKCy0sKholvTYx6U-k0")))
                    }.show()
        }

        btn_publish!!.setOnClickListener(View.OnClickListener {
            if (!load_was_clicked) {
                toast(getString(R.string.click_on_load_firstly))
                val animation = AnimationUtils.loadAnimation(context, R.anim.rotate_btn_load_on_non_click)
                btn_load!!.startAnimation(animation)
                return@OnClickListener
            }
            val description = et_description!!.text.toString()
            val typeSelected = spinner_type.selectedItemId.toString()

            if (checkInput(description) || description.length <= 8) {
                toast(getString(R.string.submit_valid_description))
                return@OnClickListener
            }

            btn_publish!!.isEnabled = false

            val userUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
            val myPref: SharedPreferences = context.getSharedPreferences("account_info", Context.MODE_PRIVATE)
            val username = myPref.getString("username", "Anonymous")

            val myRefPictures: DatabaseReference = FirebaseDatabase.getInstance().getReference("pictures")
            val pushKey = myRefPictures.child(typeSelected).push().key!!
            myRefPictures.child(typeSelected).child(pushKey).child("url").setValue(url)
            myRefPictures.child(typeSelected).child(pushKey).child("description").setValue(description)
            myRefPictures.child(typeSelected).child(pushKey).child("poster_name").setValue(username)
            myRefPictures.child(typeSelected).child(pushKey).child("poster_uid").setValue(userUID)

            val myRefUser: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
            myRefUser.child(userUID).child("posts").child(pushKey).setValue(typeSelected)

            toast(getString(R.string.published_success))
            val fragment: Fragment = MyProfileFragment()
            val fragmentManager: FragmentManager = getFragmentManager()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment, fragment)
            fragmentTransaction.commit()
        })

        return view
    }

    private fun load_categories_spinner() {
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                MainActivity.arraySpinnerType)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_type.setAdapter(adapter)
    }

    private fun checkInput(url: String): Boolean {
        if (TextUtils.isEmpty(url))
            return true
        else
            return false
    }

    private fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}