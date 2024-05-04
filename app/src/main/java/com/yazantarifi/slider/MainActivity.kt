package com.yazantarifi.slider

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.widget.doAfterTextChanged
import com.yazantarifi.slider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.singleMulti.setOnClickListener {
            startActivity(Intent(this, SingleSliderActivity::class.java))
        }

        binding.singleSingle.setOnClickListener {
            startActivity(Intent(this, SingleSingleActivity::class.java))
        }

    }


}