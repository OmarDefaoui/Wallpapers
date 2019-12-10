package com.omarituto.wallpaper

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class CreateAccountActivity : AppCompatActivity() {

    internal var editTextEmail: EditText? = null
    internal var editTextCode: EditText? = null
    internal var et_username: EditText? = null
    internal var et_description: EditText? = null
    internal var textViewCreateAcount: TextView? = null
    internal var textViewShowEmail: TextView? = null
    internal var buttonCreateSignIn: Button? = null
    internal var buttonChangePassword: Button? = null
    internal var buttonVerifyEmail: Button? = null
    internal var buttonSignOut: Button? = null
    internal var linearLayoutCreateInfo: LinearLayout? = null
    internal var linearLayoutCreateSignIn: LinearLayout? = null
    internal var linearLayoutAccountParams: LinearLayout? = null
    internal var progressDialog: ProgressDialog? = null

    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setCancelable(false)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextCode = findViewById(R.id.editTextCode)
        textViewCreateAcount = findViewById(R.id.textViewCreateAcount)
        buttonCreateSignIn = findViewById(R.id.buttonCreateSignIn)
        et_username = findViewById(R.id.et_username)
        et_description = findViewById(R.id.et_description)

        linearLayoutCreateInfo = findViewById(R.id.linearLayoutCreateInfo)
        linearLayoutCreateSignIn = findViewById(R.id.linearLayoutCreateSignIn)
        linearLayoutAccountParams = findViewById(R.id.linearLayoutAccountParams)

        textViewShowEmail = findViewById<View>(R.id.textViewShowEmail) as TextView
        buttonChangePassword = findViewById<View>(R.id.buttonChangePassword) as Button
        buttonVerifyEmail = findViewById<View>(R.id.buttonVerifyEmail) as Button
        buttonSignOut = findViewById<View>(R.id.buttonSignOut) as Button

        buttonCreateSignIn!!.setOnClickListener {
            val email = editTextEmail!!.text.toString().trim()
            val password = editTextCode!!.text.toString().trim()

            val isNotEmpty = verifyInput(email, password)
            if (isNotEmpty) {
                if (buttonCreateSignIn!!.text.toString() == getString(R.string.create_account)) {
                    if (et_username!!.text.toString().trim() != "" && et_description!!.text.toString().trim() != "") {

                        createAccount(email, password)
                    } else {
                        if (et_username!!.text.toString().trim() == "")
                            et_username!!.error = getString(R.string.requiered)
                        else
                            et_description!!.error = getString(R.string.requiered)
                    }
                } else
                    signIn(email, password)
            }
        }

        textViewCreateAcount!!.setOnClickListener {
            linearLayoutCreateInfo!!.visibility = View.VISIBLE
            textViewCreateAcount!!.visibility = View.INVISIBLE
            buttonCreateSignIn!!.text = getString(R.string.create_account)
        }

        buttonChangePassword!!.setOnClickListener {
            resetPassword(textViewShowEmail!!.text.toString())
        }
        buttonVerifyEmail!!.setOnClickListener {
            val user = mAuth!!.currentUser
            user!!.sendEmailVerification()
                    .addOnCompleteListener(this@CreateAccountActivity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@CreateAccountActivity,
                                    getString(R.string.verification_mail_sent) + " " + user.email!!,
                                    Toast.LENGTH_SHORT).show()
                            buttonVerifyEmail!!.visibility = View.INVISIBLE
                        } else {
                            Toast.makeText(this@CreateAccountActivity,
                                    getString(R.string.faild_send_verification_mail), Toast.LENGTH_SHORT).show()
                        }
                    }
        }
        buttonSignOut!!.setOnClickListener { signOut() }

        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    private fun resetPassword(email: String) {
        showProgressDialog()

        mAuth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser
                        Toast.makeText(this@CreateAccountActivity,
                                getString(R.string.email_sent_to) + " " + user!!.email!!, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CreateAccountActivity,
                                getString(R.string.unable_send_reset_pass_mail), Toast.LENGTH_SHORT).show()
                    }
                    hideProgressDialog()
                }
    }

    private fun signOut() {
        mAuth!!.signOut()
        updateUI(null)
    }

    private fun signIn(email: String, password: String) {
        showProgressDialog()

        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@CreateAccountActivity, getString(R.string.sign_up_success), Toast.LENGTH_SHORT).show()
                        val user = mAuth!!.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(this@CreateAccountActivity, getString(R.string.sign_up_faild),
                                Toast.LENGTH_SHORT).show()
                    }
                    hideProgressDialog()
                }
    }

    private fun createAccount(email: String, password: String) {
        showProgressDialog()

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@CreateAccountActivity, getString(R.string.create_account_success),
                                Toast.LENGTH_SHORT).show()
                        val user = mAuth!!.currentUser
                        val userUID: String = user!!.uid

                        val username = et_username!!.text.toString().trim()
                        val description = et_description!!.text.toString().trim()

                        mDatabase = FirebaseDatabase.getInstance().getReference("users")
                        mDatabase!!.child(userUID).child("username").setValue(username)
                        mDatabase!!.child(userUID).child("description").setValue(description)
                        mDatabase!!.child(userUID).child("email").setValue(editTextEmail!!.text.toString().trim())

                        val myPref: SharedPreferences = getSharedPreferences("account_info", Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = myPref.edit()
                        editor.putString("username", username)
                        editor.putString("description", description)
                        editor.commit()

                        updateUI(user)
                    } else {
                        Toast.makeText(this@CreateAccountActivity,
                                getString(R.string.verify_input_creation_account), Toast.LENGTH_SHORT).show()
                    }

                    hideProgressDialog()
                }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        hideKeyboard()

        if (user != null) {

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            linearLayoutCreateSignIn!!.layoutParams = params

            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            linearLayoutAccountParams!!.layoutParams = params1

            textViewShowEmail!!.text = getString(R.string.email) + " " + user.email!!
            if (!user.isEmailVerified)
                buttonVerifyEmail!!.visibility = View.VISIBLE
            else
                buttonVerifyEmail!!.visibility = View.INVISIBLE

            mDatabase = FirebaseDatabase.getInstance().getReference("users")
            valueEventListener = mDatabase!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val username = dataSnapshot.child(user.uid).child("username").getValue(String::class.java)
                    val description = dataSnapshot.child(user.uid).child("description").getValue(String::class.java)

                    textViewShowEmail!!.text = textViewShowEmail!!.text.toString() + "\n" +
                            getString(R.string.username) + " : " + username
                    val myPref: SharedPreferences = getSharedPreferences("account_info", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = myPref.edit()
                    editor.putString("username", username)
                    editor.putString("description", description)
                    editor.commit()
                    mDatabase!!.removeEventListener(valueEventListener!!)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

        } else {
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            linearLayoutCreateSignIn!!.layoutParams = params1

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            linearLayoutAccountParams!!.layoutParams = params

            linearLayoutCreateInfo!!.visibility = View.INVISIBLE
            textViewCreateAcount!!.visibility = View.VISIBLE
            buttonCreateSignIn!!.text = getString(R.string.sign_in)
        }
    }

    private fun verifyInput(str1: String, str2: String): Boolean {
        if (TextUtils.isEmpty(str1)) {
            editTextEmail!!.error = getString(R.string.requiered)
            return false
        } else {
            editTextEmail!!.error = null
            return true
        }
        if (TextUtils.isEmpty(str2)) {
            editTextCode!!.error = getString(R.string.requiered)
            return false
        } else {
            editTextCode!!.error = null
            return true
        }
    }

    private fun hideProgressDialog() {
        progressDialog!!.dismiss()
    }

    private fun showProgressDialog() {
        progressDialog!!.setMessage(getString(R.string.please_wait))
        progressDialog!!.show()
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
