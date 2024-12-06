package com.anurag.foodrunner.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class ForgotNextActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmForgotPassword: EditText
    lateinit var progressBarConfirm: ProgressBar
    lateinit var btnConfirm: Button
    var mobileNumber: String? = "No Mobile Number"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_next)

        title = "Forgot Password Data"

        if (intent != null) {
            mobileNumber = intent.getStringExtra("mobile_number")
        }

        println("Mobile Number: $mobileNumber")

        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmForgotPassword = findViewById(R.id.etConfirmNewPassword)
        btnConfirm = findViewById(R.id.btnConfirm)
        progressBarConfirm = findViewById(R.id.progressBarConfirm)

        btnConfirm.setOnClickListener {
            if (etOTP.text.isBlank()) {
                etOTP.error = "OTP missing"
            } else {
                if (etNewPassword.text.isBlank()) {
                    etNewPassword.error = "Password Missing"
                } else {
                    if (etConfirmForgotPassword.text.isBlank()) {
                        etConfirmForgotPassword.error = "Confirm Password Missing"
                    } else {
                        if ((etNewPassword.text.toString()
                                .toInt() == etConfirmForgotPassword.text.toString().toInt())
                        ) {
                            if (ConnectionManager().isNetworkAvailable(this@ForgotNextActivity)) {
                                progressBarConfirm.visibility = View.VISIBLE

                                try {
                                    val loginUser = JSONObject()

                                    loginUser.put("mobile_number", mobileNumber.toString())
                                    loginUser.put("password", etNewPassword.text.toString())
                                    loginUser.put("otp", etOTP.text.toString())

                                    val queue = Volley.newRequestQueue(this@ForgotNextActivity)
                                    val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                                    val jsonObjectRequest = object : JsonObjectRequest(
                                        Request.Method.POST,
                                        url,
                                        loginUser,
                                        Response.Listener
                                        {
                                            val responseJsonObjectData = it.getJSONObject("data")
                                            val success =
                                                responseJsonObjectData.getBoolean("success")

                                            if (success) {
                                                val serverMessage =
                                                    responseJsonObjectData.getString("successMessage")

                                                Toast.makeText(
                                                    this@ForgotNextActivity,
                                                    serverMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                passwordChanged()
                                            } else {
                                                val responseMessageServer =
                                                    responseJsonObjectData.getString("errorMessage")
                                                Toast.makeText(
                                                    this@ForgotNextActivity,
                                                    responseMessageServer.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            progressBarConfirm.visibility = View.GONE
                                        },
                                        Response.ErrorListener
                                        {
                                            progressBarConfirm.visibility = View.GONE
                                            Toast.makeText(
                                                this@ForgotNextActivity,
                                                "mSome Error occurred!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                        this@ForgotNextActivity,
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
                                    ActivityCompat.finishAffinity(this@ForgotNextActivity)
                                }
                                alterDialog.create()
                                alterDialog.show()
                            }
                        } else {
                            etConfirmForgotPassword.error = "Passwords don't match"
                        }
                    }
                }
            }

        }


    }

    fun passwordChanged() {
        startActivity(Intent(this@ForgotNextActivity, LoginActivity::class.java))
    }

    override fun onResume() {

        if (!ConnectionManager().isNetworkAvailable(this@ForgotNextActivity)) {
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
                ActivityCompat.finishAffinity(this@ForgotNextActivity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}