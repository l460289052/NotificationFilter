package com.example.notificationfilter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.notificationfilter.databinding.ActivityFilterBinding

class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createView()
    }

    private fun createView() {
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}