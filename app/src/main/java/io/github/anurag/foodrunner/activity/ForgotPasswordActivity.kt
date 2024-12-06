package com.anurag.foodrunner.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnReset: Button
    lateinit var progressBarForgot: ProgressBar
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        /*This method is also user created to setup the toolbar*/
        setupToolbar()

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        btnReset = findViewById(R.id.btnReset)
        progressBarForgot = findViewById(R.id.progressBarForgot)
        var intent = Intent(this@ForgotPasswordActivity, ForgotNextActivity::class.java)

//        progressDialog = view.findViewById(R.id.forgotPasswordInputProgressDialog)

        btnReset.setOnClickListener {

            etEmail.onEditorAction(EditorInfo.IME_ACTION_DONE)

            if (etMobileNumber.text.isBlank()) {
                etMobileNumber.error = "Mobile Number Missing"
            } else {
                if (etEmail.text.isBlank()) {
                    etEmail.error = "Email Missing"
                } else {

                    if (ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {
                        try {
                            val loginUser = JSONObject()

                            loginUser.put("mobile_number", etMobileNumber.text)
                            loginUser.put("email", etEmail.text)

                            val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
                            val url = "http://13.235.250.119/v2/forgot_password/fetch_result/"

                            progressBarForgot.visibility = View.VISIBLE

                            val jsonObjectRequest = object : JsonObjectRequest(
                                Request.Method.POST,
                                url,
                                loginUser,
                                Response.Listener
                                {
                                    val responseJsonObjectData = it.getJSONObject("data")
                                    val success = responseJsonObjectData.getBoolean("success")

                                    if (success) {
                                        val firstTry =
                                            responseJsonObjectData.getBoolean("first_try")

                                        if (firstTry) {
                                            Toast.makeText(
                                                this@ForgotPasswordActivity,
                                                "OTP sent",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            callForgotPasswordNext()

                                        } else {
                                            Toast.makeText(
                                                this@ForgotPasswordActivity,
                                                "OTP sent already",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            callForgotPasswordNext()
                                        }

                                    } else {
                                        val responseMessageServer =
                                            responseJsonObjectData.getString("errorMessage")
                                        Toast.makeText(
                                            this@ForgotPasswordActivity,
                                            responseMessageServer.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                    progressBarForgot.visibility = View.GONE
                                },
                                Response.ErrorListener
                                {
                                    println(it)
                                    Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        "Some Error occurred!",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    progressBarForgot.visibility = View.GONE

                                }) {

                                override fun getHeaders(): MutableMap<String, String> {
                                    val headers = HashMap<String, String>()
                                    headers["Content-type"] = "application/json"
                                    headers["token"] = "9bf534118365f1"
                                    return headers
                                }
                            }
                            queue.add(jsonObjectRequest)

                        } catch (e: JSONException) {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Some unexpected error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val alterDialog: AlertDialog.Builder =
                            AlertDialog.Builder(this, R.style.AlertDialogStyle)

                        alterDialog.setTitle("No Internet")
                        alterDialog.setMessage("Internet Connection can't be established!")
                        alterDialog.setPositiveButton("Open Settings")
                        { _, _ ->
                            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(settingsIntent)

                        }
                        alterDialog.setNegativeButton("Exit")
                        { _, _ ->
                            ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                        }
                        alterDialog.create()
                        alterDialog.show()
                    }
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
            finishAffinity()
        }

    }

    fun callForgotPasswordNext() {
        val intent = Intent(this@ForgotPasswordActivity, ForgotNextActivity::class.java)
        intent.putExtra("mobile_number", etMobileNumber.text.toString())
        startActivity(intent)
        finish()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reset Password"
    }

    override fun onResume() {
        if (!ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {
            val alterDialog: AlertDialog.Builder =
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()

        }

        super.onResume()
    }

}