package com.example.stockinsight.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.stockinsight.ui.fragment.OnboardingFragment
import com.example.stockinsight.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // display onboarding fragment

        val onboardingFragment = OnboardingFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, onboardingFragment).commit()
    }
}