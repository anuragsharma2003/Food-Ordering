package com.anurag.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.anurag.foodrunner.R
import com.anurag.foodrunner.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var previousMenuItem: MenuItem? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        init()
        setupToolbar()
        setupActionBarToggle()
        displayHome()

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val view =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.drawer_header, null)

        val userName: TextView = view.findViewById(R.id.txtUserFullName)
        val userPhone: TextView = view.findViewById(R.id.txtUserPhone)


        userName.text = sharedPreferences.getString("user_name", null)
        val phone = "+91-${sharedPreferences.getString("user_mobile_number", null)}"
        userPhone.text = phone
        navigationView.addHeaderView(view)

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }

            item.isCheckable = true
            item.isChecked = true

            previousMenuItem = item


            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 100)

            val fragmentTransaction = supportFragmentManager.beginTransaction()

            when (item.itemId) {

                R.id.home -> {
                    displayHome()
                }

                R.id.myProfile -> {
                    val profileFragment = MyProfileFragment()
                    fragmentTransaction.replace(R.id.frame, profileFragment)
                    fragmentTransaction.commit()
                    supportActionBar?.title = "My profile"
                }

                R.id.favRes -> {
                    val favFragment = FavouritesFragment()
                    fragmentTransaction.replace(R.id.frame, favFragment)
                    fragmentTransaction.commit()
                    supportActionBar?.title = "Favorite Restaurants"
                }

                R.id.orderHistory -> {
                    val orderHistory = OrderHistoryFragment()
                    fragmentTransaction.replace(R.id.frame, orderHistory)
                    fragmentTransaction.commit()
                    supportActionBar?.title = "Order History"
                }

                R.id.faqs -> {
                    val faqFragment = FaqFragment()
                    fragmentTransaction.replace(R.id.frame, faqFragment)
                    fragmentTransaction.commit()
                    supportActionBar?.title = "Frequently Asked Questions"
                }

                R.id.logout -> {
                    val builder: android.app.AlertDialog.Builder =
                        android.app.AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want exit?")
                        .setPositiveButton("Yes") { _, _ ->
                            ActivityCompat.finishAffinity(this)
                            val logoutIntent = Intent(this@MainActivity, LoginActivity::class.java)
                            drawerLayout.closeDrawers()
                            startActivity(logoutIntent)
                            sharedPreferences.edit().clear().apply()
                            finish()
                        }
                        .setNegativeButton("No") { _, _ ->
                            displayHome()
                        }
                        .create()
                        .show()
                }

            }
            return@setNavigationItemSelectedListener true
        }
    }


    private fun displayHome() {
        val fragment = HomeFragment(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }


    private fun setupActionBarToggle() {
        actionBarDrawerToggle =
            object : ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.openDrawer,
                R.string.closeDrawer
            ) {
                override fun onDrawerStateChanged(newState: Int) {
                    super.onDrawerStateChanged(newState)
                    val pendingRunnable = Runnable {
                        val inputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    }
                    Handler().postDelayed(pendingRunnable, 50)
                }
            }

        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

    }

    private fun setupToolbar() {

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun init() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openHome() {
        val fragment = HomeFragment(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()

        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            !is HomeFragment -> openHome()

            else -> finishAffinity()
        }
    }
}
