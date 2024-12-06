package com.anurag.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.util.ConnectionManager
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    lateinit var etFullName: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var progressBarRegister: ProgressBar
    private lateinit var toolbar: Toolbar
    lateinit var sharedPreferences: SharedPreferences

    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    var mobilePattern = "[6-9][0-9]{9}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        setContentView(R.layout.activity_register)

        setupToolbar()

        etFullName = findViewById(R.id.etFullName)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBarRegister = findViewById(R.id.progressBarRegister)


        btnRegister.setOnClickListener {

            etConfirmPassword.onEditorAction(EditorInfo.IME_ACTION_DONE)


            if (etFullName.text.toString().isEmpty())
                etFullName.error = "Enter Your Name"
            else if (etMobileNumber.text.toString().isEmpty())
                etMobileNumber.error = "Enter Mobile Number"
            else if (!etMobileNumber.text.toString().trim().matches(mobilePattern.toRegex()))
                etMobileNumber.error = "Enter a valid Mobile number"
            else if (etEmail.text.toString().isEmpty())
                etEmail.error = "Enter Email Id"
            else if (!etEmail.text.toString().trim().matches(emailPattern.toRegex()))
                etEmail.error = "Enter a valid Email Id"
            else if (etAddress.text.toString().isEmpty())
                etAddress.error = "Enter Delivery Address"
            else if (etPassword.text.toString().isEmpty())
                etPassword.error = "Enter Password"
            else if (etPassword.length() < 4)
                etPassword.error = "Weak Password"
            else if (etPassword.text.toString() != etConfirmPassword.text.toString())
                etConfirmPassword.error = "Passwords doesn't match!"
            else {
                progressBarRegister.visibility = View.VISIBLE
                sendRequest(
                    etFullName.text.toString(),
                    etMobileNumber.text.toString(),
                    etAddress.text.toString(),
                    etPassword.text.toString(),
                    etEmail.text.toString()
                )

//                Toast.makeText(
//                    this@RegisterActivity,
//                    "Successfully Registered",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
            }
        }


        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun sendRequest(
        name: String,
        phone: String,
        address: String,
        password: String,
        email: String
    ) {

        val url = "http://13.235.250.119/v2/register/fetch_result"
        val queue = Volley.newRequestQueue(this@RegisterActivity)
        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("address", address)
        jsonParams.put("email", email)

        if (ConnectionManager().isNetworkAvailable(this@RegisterActivity)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                jsonParams,
                Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {

                            val response = data.getJSONObject("data")
                            sharedPreferences.edit()
                                .putString("user_id", response.getString("user_id"))
                                .apply()
                            sharedPreferences.edit()
                                .putString("user_name", response.getString("name"))
                                .apply()
                            sharedPreferences.edit().putString(
                                "user_mobile_number",
                                response.getString("mobile_number")
                            ).apply()
                            sharedPreferences.edit()
                                .putString(
                                    "user_address",
                                    response.getString("address")
                                )
                                .apply()
                            sharedPreferences.edit()
                                .putString("user_email", response.getString("email"))
                                .apply()

                            savePreferences()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            progressBarRegister.visibility = View.GONE

                            startActivity(intent)
                            finish()
                        } else {
                            val responseMessageServer =
                                data.getString("errorMessage")
                            progressBarRegister.visibility = View.GONE
                            Toast.makeText(
                                this@RegisterActivity,
                                responseMessageServer,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        progressBarRegister.visibility = View.GONE
                        e.printStackTrace()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(this@RegisterActivity, "Volley Error!", Toast.LENGTH_SHORT)
                        .show()
                    progressBarRegister.visibility = View.GONE
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            progressBarRegister.visibility = View.GONE
            val dialog: AlertDialog.Builder =
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                this.finish()

            }
            dialog.setNegativeButton("Exit") { text, listener ->

                ActivityCompat.finishAffinity(this@RegisterActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register Yourself"
    }

    fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }

}