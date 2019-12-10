package com.omarituto.wallpaper

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.omarituto.wallpaper.fragments.CategoryFragment
import com.omarituto.wallpaper.fragments.HomeFragment
import com.omarituto.wallpaper.fragments.MyProfileFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    internal var fragment: Fragment? = null
    internal var fragmentManager: FragmentManager? = null
    internal var fragmentTransaction: FragmentTransaction? = null
    internal var page = "category"

    internal var checkInternetBR = CheckInternetBroadCastReceiver()

    private val mOnNavigationItemSelectedListener = BottomNavigationView
            .OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_category -> {
                        fragment = CategoryFragment()
                        fragmentManager = getFragmentManager()
                        fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.fragment, fragment)
                        fragmentTransaction!!.commit()
                        page = "category"
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_home -> {
                        fragment = HomeFragment()
                        fragmentManager = getFragmentManager()
                        fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.fragment, fragment)
                        fragmentTransaction!!.commit()
                        page = "home"
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_my_account -> {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            startActivity(Intent(this, CreateAccountActivity::class.java))
                            return@OnNavigationItemSelectedListener false
                        }
                        fragment = MyProfileFragment()
                        fragmentManager = getFragmentManager()
                        fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction!!.replace(R.id.fragment, fragment)
                        fragmentTransaction!!.commit()
                        page = "my_profile"
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.setSelectedItemId(R.id.navigation_category)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.menu_account -> {
                startActivity(Intent(this, CreateAccountActivity::class.java))
                return true
            }

            R.id.share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val shareSubText = getString(R.string.subText)
                val shareBodyText = "https://play.google.com/store/apps/details?id=com.omarituto.wallpaper"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareSubText + "\n\n" + shareBodyText)
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
                return true
            }

            R.id.more -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri
                        .parse("https://play.google.com/store/apps/developer?id=NORDEF")))
                return true
            }

            R.id.rate -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri
                        .parse("https://play.google.com/store/apps/details?id=com.omarituto.wallpaper")))
                return true
            }
            else -> return true
        }
    }

    override fun onBackPressed() {
        if (page.equals("category") && CategoryFragment.isClicked) {
            CategoryFragment.isClicked = false
            fragment = CategoryFragment()
            fragmentManager = getFragmentManager()
            fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction!!.replace(R.id.fragment, fragment)
            fragmentTransaction!!.commit()
        } else
            super.onBackPressed()
    }

    companion object {
        var numOpenedImageFragment = 0
        var arraySpinnerType = ArrayList<String>()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(checkInternetBR, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(checkInternetBR)
    }
}
