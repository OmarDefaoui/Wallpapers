package com.omarituto.wallpaper.fragments

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.omarituto.wallpaper.MainActivity
import com.omarituto.wallpaper.R
import com.omarituto.wallpaper.RecyclerViewAdapter
import java.util.*

class CategoryFragment : Fragment() {

    internal var recyclerView: RecyclerView? = null
    internal var adapter: RecyclerViewAdapter? = null
    internal var manager: GridLayoutManager? = null

    internal var reference: DatabaseReference? = null
    internal var eventListener: ValueEventListener? = null
    internal var list = ArrayList<String>()
    internal var numFolowers = -1
    internal var uid = ""
    internal lateinit var context: Context
    internal lateinit var dialog: ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.removeAllViews()
        val view = inflater.inflate(R.layout.home_fragment_layout, container, false)
        context = inflater.context

        try {
            numFolowers = arguments.getInt("numFolowers")
            uid = arguments.getString("uid")!!
        } catch (e: Exception) {
        }

        dialog = ProgressDialog(context)
        showProgressDialog()

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView!!.setHasFixedSize(true)
        manager = GridLayoutManager(context, 1)
        recyclerView!!.layoutManager = manager

        if (numFolowers.equals(-1)) {
            if (MainActivity.arraySpinnerType.size == 0)
                loadSpinnerType()
            else
                loadCategories()
        } else {
            loadFolowers()
        }

        return view
    }

    private fun loadCategories() {
        reference = FirebaseDatabase.getInstance().getReference("pictures")
        eventListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (firstChild in dataSnapshot.children) {
                    var key = ""
                    for (keySecondChild in firstChild.children) {
                        key = keySecondChild.key.toString()
                        break
                    }

                    list.add(MainActivity.arraySpinnerType[firstChild.key!!.toInt()]
                            + " . " + firstChild.childrenCount + " . " +
                            firstChild.child(key).child("url").getValue(String::class.java)
                            + " . " + firstChild.key)
                }

                adapter = RecyclerViewAdapter(context, list, 2)
                recyclerView!!.adapter = adapter
                dismisssProgressDialog()
                reference!!.removeEventListener(eventListener!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)}: \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadFolowers() {
        reference = FirebaseDatabase.getInstance().getReference("users")
        eventListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (firstChild in dataSnapshot.child(uid).child("folowers").children) {
                    list.add(firstChild.key + " . " + firstChild.getValue(String::class.java))
                }

                adapter = RecyclerViewAdapter(context, list, 4)
                recyclerView!!.adapter = adapter
                dismisssProgressDialog()
                reference!!.removeEventListener(eventListener!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)}: \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSpinnerType() {
        var eventListener: ValueEventListener? = null
        val reference = FirebaseDatabase.getInstance().getReference("categories")
        eventListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val defaultDeviceLang: String = Locale.getDefault().language
                val arraySpinnerType = ArrayList<String>()

                if (dataSnapshot.hasChild(defaultDeviceLang)) {

                    for (child in dataSnapshot.child(defaultDeviceLang).children) {
                        arraySpinnerType.add(child.value.toString())
                    }
                } else {

                    for (child in dataSnapshot.child("en").children) {
                        arraySpinnerType.add(child.value.toString())
                    }
                }

                MainActivity.arraySpinnerType = arraySpinnerType
                loadCategories()
                reference.removeEventListener(eventListener!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)}: \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProgressDialog() {
        dialog.setMessage(getString(R.string.please_wait))
        dialog.show()
    }

    private fun dismisssProgressDialog() {
        dialog.dismiss()
    }

    companion object {
        var isClicked = false
    }
}
