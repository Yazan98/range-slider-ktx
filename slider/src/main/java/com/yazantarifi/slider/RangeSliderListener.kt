package com.yazantarifi.slider

interface RangeSliderListener {

    fun onRangeProgress(fromValue: Float, toValue: Float, isFromUser: Boolean)

    fun onThumbMovement(value: Float, thumbIndex: Int, isFromUser: Boolean)

}
