package com.yazantarifi.slider

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.yazantarifi.slider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.slider.let {
            it.onUpdateRangeValues(0f, 100f)

            binding.fromValue.setText(0f.toString())
            binding.toValue.setText(100f.toString())

            it.onAddRangeListener(object : RangeSliderListener {
                override fun onRangeProgress(
                    fromValue: Float,
                    toValue: Float,
                    isFromUser: Boolean
                ) {
                    if (isFromUser) {
                        binding.fromValue.clearFocus()
                        binding.toValue.clearFocus()

                        binding.fromValue.setText(fromValue.toString())
                        binding.toValue.setText(toValue.toString())
                    }
                }

                override fun onThumbMovement(value: Float, thumbIndex: Int, isFromUser: Boolean) {

                }
            })
        }

    }

}