package com.example.stockinsight.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.stockinsight.R

class OnboardingFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        val btnNext = view.findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_onboardingGetStartedFragment)
        }

        return view
    }
}