package com.omarituto.wallpaper.fragments

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omarituto.wallpaper.R
import com.omarituto.wallpaper.RecyclerViewAdapter
import com.omarituto.wallpaper.model.ListItemPictures

import java.util.ArrayList

class MyProfileFragment : Fragment() {

    internal var adapter: RecyclerViewAdapter? = null
    internal var manager: GridLayoutManager? = null
    internal var tv_name: TextView? = null
    internal var tv_descrition: TextView? = null
    internal var tv_info: TextView? = null
    internal var iv_profile: ImageView? = null
    internal var btn_upload: Button? = null

    internal var reference: DatabaseReference? = null
    internal var eventListener: ValueEventListener? = null
    internal var refInfo: DatabaseReference? = null
    internal var elInfo: ValueEventListener? = null

    internal var listPosts = ArrayList<ClassListPosts>()
    internal var listItemPictures = ArrayList<ListItemPictures>()
    internal var username = ""
    internal var description = ""
    internal var uid = "local"
    internal var numFolowers = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        container?.removeAllViews()
        val view = inflater.inflate(R.layout.fragment_myprofile, container, false)
        val context: Context = inflater.context

        initialisation(context, view)

        try {
            uid = arguments.getString("uid")!!
        } catch (e: Exception) {
        }

        when (uid) {
            "local" -> {
                val myPref: SharedPreferences = context.getSharedPreferences("account_info", Context.MODE_PRIVATE)
                username = myPref.getString("username", "Anonymous")!!
                description = myPref.getString("description", "Empty case")!!

                loadAccountInfo(context)
                btn_upload!!.setOnClickListener(View.OnClickListener {
                    val fragment: Fragment = AddWallpaper()
                    val fragmentManager: FragmentManager = getFragmentManager()
                    val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment, fragment)
                    fragmentTransaction.commit()
                })

            }

            else -> {
                loadAccountWithUID(context)
                btn_upload!!.setOnClickListener {
                    when (btn_upload!!.text) {
                        getString(R.string.folowed) -> {
                            btn_upload!!.text = getString(R.string.folowed)
                            val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
                            val userUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
                            ref.child(uid).child("folowers").child(userUID).removeValue()
                            loadAccountWithUID(context)
                            btn_upload!!.text = getString(R.string.suivre)
                        }

                        else -> {
                            btn_upload!!.text = getString(R.string.suivre)
                            val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
                            val userUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
                            val myPref: SharedPreferences = context.getSharedPreferences("account_info", Context.MODE_PRIVATE)
                            ref.child(uid).child("folowers").child(userUID).setValue(myPref.getString("username", "Anonymous"))
                            loadAccountWithUID(context)
                            btn_upload!!.text = getString(R.string.folowed)
                        }
                    }
                }
            }
        }

        tv_info!!.setOnClickListener {
            if (numFolowers == 0)
                return@setOnClickListener

            val fragment = CategoryFragment()
            val fragmentManager = getFragmentManager()
            val fragmentTransaction = fragmentManager.beginTransaction()
            val bundle = Bundle()
            bundle.putInt("numFolowers", numFolowers)
            if (uid.equals("local"))
                bundle.putString("uid", FirebaseAuth.getInstance()!!.currentUser!!.uid)
            else
                bundle.putString("uid", uid)
            fragment.arguments = bundle
            fragmentTransaction.replace(R.id.fragment, fragment)
            fragmentTransaction.commit()
        }

        return view
    }

    private fun loadAccountWithUID(context: Context) {

        refInfo = FirebaseDatabase.getInstance().getReference("users")
        elInfo = refInfo!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val userUID = uid
                val myUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
                val child = dataSnapshot.child(userUID)
                numFolowers = dataSnapshot.child(userUID).child("folowers").childrenCount.toInt()
                val numPosts = dataSnapshot.child(userUID).child("posts").childrenCount
                tv_info!!.text = "$numFolowers ${getString(R.string.folowers)}, $numPosts ${getString(R.string.posts)}"

                tv_name!!.text = dataSnapshot.child(userUID).child("username").getValue(String::class.java)
                tv_descrition!!.text = dataSnapshot.child(userUID).child("description").getValue(String::class.java)

                if (child.child("folowers").hasChild(myUID))
                    btn_upload!!.text = getString(R.string.folowed)
                else
                    btn_upload!!.text = getString(R.string.suivre)

                listPosts.clear()
                for (snapPosts in dataSnapshot.child(userUID).child("posts").children) {
                    listPosts.add(ClassListPosts(snapPosts.key!!, snapPosts.getValue(String::class.java).toString()))
                }

                getPictures(context)
                refInfo!!.removeEventListener(elInfo!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)} \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initialisation(context: Context, view: View) {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y
        spanCount = width / 320 + 1
        width = width / 3 - 12
        height = height / 3 - 12

        iv_profile = view.findViewById(R.id.iv_profile)
        tv_name = view.findViewById(R.id.tv_name)
        tv_descrition = view.findViewById(R.id.tv_descrition)
        tv_info = view.findViewById(R.id.tv_info)
        btn_upload = view.findViewById(R.id.btn_upload)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView!!.setHasFixedSize(true)
        manager = GridLayoutManager(context, 3)
        recyclerView!!.layoutManager = manager
    }

    data class ClassListPosts(var key: String, var type: String)

    private fun loadAccountInfo(context: Context) {
        tv_name!!.text = username
        tv_descrition!!.text = description

        refInfo = FirebaseDatabase.getInstance().getReference("users")
        elInfo = refInfo!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val userUID = FirebaseAuth.getInstance()!!.currentUser!!.uid
                numFolowers = dataSnapshot.child(userUID).child("folowers").childrenCount.toInt()
                val numPosts = dataSnapshot.child(userUID).child("posts").childrenCount
                tv_info!!.text = "$numFolowers ${getString(R.string.folowers)}, $numPosts ${getString(R.string.posts)}"

                for (snapPosts in dataSnapshot.child(userUID).child("posts").children) {
                    listPosts.add(ClassListPosts(snapPosts.key!!, snapPosts.getValue(String::class.java).toString()))
                }

                getPictures(context)
                refInfo!!.removeEventListener(elInfo!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)} \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getPictures(context: Context) {
        reference = FirebaseDatabase.getInstance().getReference("pictures")
        eventListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                listItemPictures.clear()
                for (i in 0..(listPosts.size - 1)) {
                    val itemSnapshot = dataSnapshot.child(listPosts.get(i).type).child(listPosts.get(i).key)
                    listItemPictures.add(ListItemPictures(itemSnapshot.child("url").getValue(String::class.java).toString(),
                            itemSnapshot.child("poster_name").getValue(String::class.java).toString(),
                            itemSnapshot.child("poster_uid").getValue(String::class.java).toString(),
                            itemSnapshot.child("description").getValue(String::class.java).toString()))
                }

                adapter = RecyclerViewAdapter(context, listItemPictures, 3, true)
                recyclerView!!.adapter = adapter
                reference!!.removeEventListener(eventListener!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "${getString(R.string.error)} \n" + databaseError.message,
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        var recyclerView: RecyclerView? = null
        var width: Int = 0
        var height: Int = 0
        var spanCount: Int = 0
    }

}
