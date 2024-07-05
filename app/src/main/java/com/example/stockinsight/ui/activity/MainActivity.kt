package com.example.stockinsight.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.stockinsight.R
import com.example.stockinsight.utils.Constants
import com.example.stockinsight.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.stockinsight.utils.requestManyPermissions
import java.util.Locale

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        var selectedLanguage = prefs.getString("selected_language", "SYSTEM_DEFAULT")
        if (selectedLanguage == "SYSTEM_DEFAULT") {
            selectedLanguage = Locale.getDefault().language
        }
        if (selectedLanguage != null) {
            applyLocale(selectedLanguage)
        }

        val bottomNavigationViewLayout: LinearLayout = findViewById(R.id.bottom_navigation_layout)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the BottomNavigationView labels programmatically
        val menu = bottomNavigationView.menu
        menu.findItem(R.id.home).title = getString(R.string.home)
        menu.findItem(R.id.watchlist).title = getString(R.string.watchlist)
        menu.findItem(R.id.user).title = getString(R.string.lbl_settings)

        if (sessionManager.isLoggedIn()) {
            bottomNavigationViewLayout.visibility = View.VISIBLE
        } else {
            bottomNavigationViewLayout.visibility = View.GONE
//            navController.navigate(R.id.onboardingFragment)
        }

        navController.navigate(R.id.homeFragment)

        // Initialize currentOrder with the order of the start destination
        var currentOrder = 1

        bottomNavigationView.setOnNavigationItemSelectedListener {
            // Assign an order to each item
            val itemOrder = mapOf(
                R.id.home to 1, R.id.watchlist to 2, R.id.user to 3
            )

            val selectedOrder = itemOrder[it.itemId]

            val navOptions = if (selectedOrder != null && selectedOrder > currentOrder) {
                // If the selected item's order is greater than the current item's order, use the first set of animations
                NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right).build()
            } else if (selectedOrder == currentOrder) {
                // If the selected item's order is equal to the current item's order, use the second set of animations
                NavOptions.Builder().setEnterAnim(R.anim.fade_in).setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in).setPopExitAnim(R.anim.fade_out).build()
            } else {
                // Otherwise, use the second set of animations
                NavOptions.Builder().setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right).setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left).build()
            }

            when (it.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeFragment, null, navOptions)
                    currentOrder = itemOrder[it.itemId] ?: currentOrder // Update currentOrder
                    true
                }

                R.id.watchlist -> {
                    navController.navigate(R.id.watchlistFragment, null, navOptions)
                    currentOrder = itemOrder[it.itemId] ?: currentOrder // Update currentOrder
                    true
                }

                R.id.user -> {
                    navController.navigate(R.id.settingsFragment, null, navOptions)
                    currentOrder = itemOrder[it.itemId] ?: currentOrder // Update currentOrder
                    true
                }

                else -> false
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onboardingFragment -> bottomNavigationViewLayout.visibility = View.GONE
                R.id.onboardingGetStartedFragment -> bottomNavigationViewLayout.visibility =
                    View.GONE

                R.id.signInFragment -> bottomNavigationViewLayout.visibility = View.GONE
                R.id.signUpFragment -> bottomNavigationViewLayout.visibility = View.GONE
                else -> bottomNavigationViewLayout.visibility = View.VISIBLE
            }
        }

        requestManyPermissions(
            this, arrayOf(
                Manifest.permission.POST_NOTIFICATIONS
            ), Constants.REQUEST_CODE
        )
    }

    private fun applyLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}