package com.example.stockinsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

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