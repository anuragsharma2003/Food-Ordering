package com.anurag.foodrunner.activity
// TODO: Add progress Bar for login btn
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurag.foodrunner.R
import com.anurag.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegister: TextView
    lateinit var btnLogin: Button
    lateinit var progressBarLogin: ProgressBar

    var mobilePattern = "[6-9][0-9]{9}"

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        setContentView(R.layout.activity_login)

        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        title = "Food Runner"

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)
        btnLogin = findViewById(R.id.btnLogin)
        progressBarLogin = findViewById(R.id.progressBarLogin)

        btnLogin.setOnClickListener {
            progressBarLogin.visibility = View.VISIBLE
            if (etMobileNumber.text.isBlank() && etPassword.text.isBlank()) {
                etMobileNumber.error = "Enter Mobile Number"
                etPassword.error = "Enter Password"
                progressBarLogin.visibility = View.GONE
            } else if (etMobileNumber.text.isBlank()) {
                etMobileNumber.error = "Enter Mobile Number"
                progressBarLogin.visibility = View.GONE
            } else if (etPassword.text.isBlank()) {
                etPassword.error = "Enter Password"
                progressBarLogin.visibility = View.GONE
            } else {
                progressBarLogin.visibility = View.VISIBLE
                etPassword.onEditorAction(EditorInfo.IME_ACTION_DONE)

                val mobileNumber = etMobileNumber.text.toString()
                val password = etPassword.text.toString()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)

                if (validations(etMobileNumber.text.toString(), etPassword.text.toString())) {
                    progressBarLogin.visibility = View.VISIBLE
                    if (ConnectionManager().isNetworkAvailable(this@LoginActivity)) {

                        val queue = Volley.newRequestQueue(this@LoginActivity)
                        val url = "http://13.235.250.119/v2/login/fetch_result/"
                        val jsonParams = JSONObject()
                        jsonParams.put("mobile_number", mobileNumber)
                        jsonParams.put("password", password)

                        val jsonObjectRequest =
                            object : JsonObjectRequest(
                                Request.Method.POST,
                                url,
                                jsonParams,
                                Response.Listener {
                                    try {
                                        val data = it.getJSONObject("data")
                                        val success = data.getBoolean("success")
                                        if (success) {
                                            btnLogin.isEnabled = false
                                            btnLogin.isClickable = false


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
                                                .putString(
                                                    "user_email",
                                                    response.getString("email")
                                                )
                                                .apply()

                                            savePreferences()
                                            println("ID: ${response.getString("user_id")}")
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            progressBarLogin.visibility = View.GONE
                                            val responseMessageServer =
                                                data.getString("errorMessage")

                                            if (responseMessageServer == "Mobile Number not registered") {
                                                etMobileNumber.error =
                                                    "Mobile Number not registered"
                                            } else if (responseMessageServer == "Incorrect password") {
                                                etPassword.error = "Incorrect Password"
                                            } else {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    responseMessageServer,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                },
                                Response.ErrorListener {
                                    progressBarLogin.visibility = View.GONE
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Volley error occurred",
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
                    } else {
                        progressBarLogin.visibility = View.GONE
                        val dialog: AlertDialog.Builder =
                            AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        dialog.setTitle("Error")
                        dialog.setIcon(R.drawable.app_logo)
                        dialog.setMessage("Internet Connection Found")
                        dialog.setPositiveButton("Open Settings") { _, _ ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            this.finish()

                        }
                        dialog.setNegativeButton("Exit") { _, _ ->

                            ActivityCompat.finishAffinity(this@LoginActivity)
                        }
                        dialog.create()
                        dialog.show()
                    }
                }
            }
        }

        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

        txtRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

    }

    fun savePreferences() {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun validations(phone: String, password: String): Boolean {
        progressBarLogin.visibility = View.GONE
        return if (!phone.trim().matches(mobilePattern.toRegex())) {
            etMobileNumber.error = "Enter a valid Mobile Number"
            false
        } else
            true
    }
}