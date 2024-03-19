package com.example.stockinsight.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.stockinsight.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // display onboarding fragment using nav host fragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.onboardingFragment)

        val bottomNavigationViewLayout: LinearLayout = findViewById(R.id.bottom_navigation_layout)

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

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Initialize currentOrder with the order of the start destination
        var currentOrder = 1

        bottomNavigationView.setOnNavigationItemSelectedListener {
            // Assign an order to each item
            val itemOrder = mapOf(
                R.id.home to 1,
                R.id.watchlist to 2,
                R.id.news to 3,
                R.id.user to 4
            )

            val selectedOrder = itemOrder[it.itemId]

            val navOptions = if (selectedOrder != null && selectedOrder > currentOrder) {
                // If the selected item's order is greater than the current item's order, use the first set of animations
                NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build()
            } else {
                // Otherwise, use the second set of animations
                NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left)
                    .build()
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

                R.id.news -> {
                    navController.navigate(R.id.newsFragment, null, navOptions)
                    currentOrder = itemOrder[it.itemId] ?: currentOrder // Update currentOrder
                    true
                }

                R.id.user -> {
                    navController.navigate(R.id.userFragment, null, navOptions)
                    currentOrder = itemOrder[it.itemId] ?: currentOrder // Update currentOrder
                    true
                }

                else -> false
            }
        }
    }
}