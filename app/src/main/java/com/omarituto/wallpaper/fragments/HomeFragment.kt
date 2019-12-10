package com.omarituto.wallpaper.fragments

import android.app.Activity
import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.omarituto.wallpaper.R
import com.omarituto.wallpaper.RecyclerViewAdapter
import com.omarituto.wallpaper.model.ListItemPictures
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    internal var recyclerView: RecyclerView? = null
    internal var adapter: RecyclerViewAdapter? = null
    internal var manager: GridLayoutManager? = null

    internal var reference: DatabaseReference? = null
    internal var eventListener: ValueEventListener? = null
    internal var list = ArrayList<String>()
    internal var listItemPictures = ArrayList<ListItemPictures>()
    internal var spanCount: Int = 0
    internal lateinit var dialog: ProgressDialog

    internal var category: String = "ui design"
    internal var from: String = "start"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.removeAllViews()
        val view = inflater.inflate(R.layout.home_fragment_layout, container, false)
        val context: Context = inflater.context

        try {
            from = arguments.getString("from")!!
            category = arguments.getString("category")!!
        } catch (e: Exception) {
        }

        dialog = ProgressDialog(context)
        showProgressDialog()

        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y
        spanCount = width / 320 + 1
        width = width / 3 - 12
        height = height / 3 - 12


        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView!!.setHasFixedSize(true)
        manager = GridLayoutManager(context, 3)
        recyclerView!!.layoutManager = manager

        reference = FirebaseDatabase.getInstance().getReference("pictures")
        eventListener = reference!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                when (from) {
                    "category" -> {
                        for (firstChild in dataSnapshot.child(category).children) {
                            listItemPictures.add(ListItemPictures(firstChild.child("url")
                                    .getValue(String::class.java).toString(),
                                    firstChild.child("poster_name").getValue(String::class.java).toString(),
                                    firstChild.child("poster_uid").getValue(String::class.java).toString(),
                                    firstChild.child("description").getValue(String::class.java).toString()))
                        }
                    }
                    else -> {

                        for (childPictures in dataSnapshot.children) {
                            for (childCategory in childPictures.children) {
                                listItemPictures.add(ListItemPictures(childCategory.child("url")
                                        .getValue(String::class.java).toString(),
                                        childCategory.child("poster_name").getValue(String::class.java).toString(),
                                        childCategory.child("poster_uid").getValue(String::class.java).toString(),
                                        childCategory.child("description").getValue(String::class.java).toString()))

                            }
                        }
                        listItemPictures = desorderItemInArrayList(listItemPictures)
                    }
                }

                adapter = RecyclerViewAdapter(context, listItemPictures, 1, true)
                recyclerView!!.adapter = adapter

                dismisssProgressDialog()
                reference!!.removeEventListener(eventListener!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)}: \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })

        return view
    }

    private fun desorderItemInArrayList(listItemPictures: ArrayList<ListItemPictures>): ArrayList<ListItemPictures> {
        val insertedItemId: ArrayList<Int> = ArrayList()
        val newListITemPictures: ArrayList<ListItemPictures> = ArrayList()
        val finalSize = listItemPictures.size
        var randomId = 0
        val random = Random()

        while (!(newListITemPictures.size).equals(finalSize)) {
            randomId = random.nextInt(finalSize)

            if (!insertedItemId.contains(randomId)) {
                insertedItemId.add(randomId)
                newListITemPictures.add(listItemPictures[randomId])
            }
        }

        return newListITemPictures
    }

    private fun showProgressDialog() {
        dialog.setMessage(getString(R.string.please_wait))
        dialog.show()
    }

    private fun dismisssProgressDialog() {
        dialog.dismiss()
    }

    companion object {
        var width: Int = 0
        var height: Int = 0
    }
}
