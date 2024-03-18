package com.example.stockinsight.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R
import com.example.stockinsight.ui.fragment.signup.SignUpFragment

class OnboardingGetStartedFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_onboarding_get_started, container, false)

        val btnGetStarted = view.findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            // navigate to next fragment
            findNavController().navigate(R.id.action_onboardingGetStartedFragment_to_signUpFragment)
        }

        val btnSignIn = view.findViewById<Button>(R.id.btnSignIn)
        btnSignIn.setOnClickListener {
            // navigate to sign in fragment
            findNavController().navigate(R.id.action_onboardingGetStartedFragment_to_signInFragment)
        }

        return view
    }
}