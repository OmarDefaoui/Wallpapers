package com.omarituto.wallpaper

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.omarituto.wallpaper.fragments.CategoryFragment
import com.omarituto.wallpaper.fragments.HomeFragment
import com.omarituto.wallpaper.fragments.MyProfileFragment
import com.omarituto.wallpaper.model.ListItemPictures
import com.squareup.picasso.Picasso

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    internal var context: Context
    internal var list: ArrayList<String> = ArrayList()
    internal var listPictures: ArrayList<ListItemPictures> = ArrayList()
    internal var type: Int = 0
    internal var withData = false

    constructor(context: Context, list: ArrayList<String>) {
        this.context = context
        this.list = list
    }

    constructor(context: Context, list: ArrayList<String>, type: Int) {
        this.context = context
        this.list = list
        this.type = type
    }

    constructor(context: Context, listPictures: ArrayList<ListItemPictures>, type: Int,
                withData: Boolean) {
        this.context = context
        this.listPictures = listPictures
        this.type = type
        this.withData = withData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            1 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, null)
                return ViewHolder1(view)
            }
            2 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_category, null)
                return ViewHolder2(view)
            }
            4 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_folowers, null)
                return ViewHolder4(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, null)
                return ViewHolder2(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            1 -> {
                val holder1 = holder as ViewHolder1
                //Picasso.with(context).load(list.get(position)).resize(320,480).into(holder1.iv_test);
                if (type == 1)
                    Picasso.with(context).load(listPictures[position].url)
                            .resize(HomeFragment.width, HomeFragment.height).into(holder1.iv_wallpaper)
                else if (type == 3) {
                    Picasso.with(context).load(listPictures[position].url)
                            .resize(MyProfileFragment.width, MyProfileFragment.height)
                            .into(holder1.iv_wallpaper)
                }

                holder1.iv_wallpaper.setOnClickListener {
                    val intent = Intent(context, ImagePreview::class.java)
                    intent.putExtra("url", listPictures[position].url)
                    intent.putExtra("poster_name", listPictures[position].poster_name)
                    intent.putExtra("description", listPictures[position].description)
                    intent.putExtra("poster_uid", listPictures[position].poster_uid)
                    intent.putExtra("type", type)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val bundle: Bundle = ActivityOptions.makeSceneTransitionAnimation(context as Activity,
                                holder1.iv_wallpaper,
                                holder1.iv_wallpaper.getTransitionName())
                                .toBundle()
                        context.startActivity(intent, bundle)
                    } else
                        context.startActivity(intent)
                    return@setOnClickListener
                }
            }

            2 -> {
                val holder2 = holder as ViewHolder2

                val testResponse = list[position].split(" . ")
                holder2.tv_category.text = testResponse[0].trim()
                holder2.tv_category_num_elmts.text = testResponse[1].trim()
                Picasso.with(context).load(testResponse[2].trim()).into(holder2.iv_category)

                holder2.ll_category.setOnClickListener {
                    CategoryFragment.isClicked = true
                    val bundle = Bundle()
                    bundle.putString("category", testResponse[3].trim())
                    bundle.putString("from", "category")

                    val fragment = HomeFragment()
                    val fragmentManager = (context as Activity).fragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragment.arguments = bundle
                    fragmentTransaction.replace(R.id.fragment, fragment)
                    fragmentTransaction.commit()
                }
            }

            4 -> {
                val holder4 = holder as ViewHolder4

                val testResponse = list[position].split(" . ")
                val folower_uid = testResponse[0].trim()
                val folower_username = testResponse[1].trim()
                holder4.tv_folower_username.text = folower_username

                holder4.tv_folower_username.setOnClickListener {
                    if (folower_uid.equals(FirebaseAuth.getInstance()!!.currentUser!!.uid))
                        return@setOnClickListener

                    val fragment = MyProfileFragment()
                    val fragmentManager = (context as Activity).fragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    val bundle = Bundle()
                    bundle.putString("uid", folower_uid)
                    fragment.arguments = bundle
                    fragmentTransaction.replace(R.id.fragment, fragment)
                    fragmentTransaction.commit()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (withData)
            return listPictures.size
        else
            return list.size
    }

    override fun getItemViewType(position: Int): Int {
        //1: home 2: category 3: myprofile 4: folowers
        return if (type == 3) 1 else type
    }

    class ViewHolder1(view: View) : RecyclerView.ViewHolder(view) {

        internal var iv_wallpaper: ImageView

        init {
            iv_wallpaper = view.findViewById(R.id.iv_wallpaper)
        }
    }

    class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

        internal var tv_category: TextView
        internal var tv_category_num_elmts: TextView
        internal var iv_category: ImageView
        internal var ll_category: LinearLayout

        init {
            tv_category = view.findViewById(R.id.tv_category)
            iv_category = view.findViewById(R.id.iv_category)
            ll_category = view.findViewById(R.id.ll_category)
            tv_category_num_elmts = view.findViewById(R.id.tv_category_num_elmts)
        }
    }

    class ViewHolder4(view: View) : RecyclerView.ViewHolder(view) {

        internal var tv_folower_username: TextView

        init {
            tv_folower_username = view.findViewById(R.id.tv_folower_username)
        }
    }
}
