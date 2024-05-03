package com.yazantarifi.slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class RangeSliderView: View {

    companion object {
        private const val DEFAULT_CORNER_RADIUS_BACKGROUND = 20f
        private const val DEFAULT_RECTANGLE_HEIGHT = 30f

        private const val DEFAULT_SLIDER_FROM_PROGRESS = 0f
        private const val DEFAULT_SLIDER_TO_PROGRESS = 100f
    }

    /**
     * View Attributes to Draw the Slider from Xml Input
     */
    private var backgroundColor: Color? = null
    private var sliderActiveColor: Color? = null
    private var backgroundRadius: Float = 0f
    private var rectangleHeight: Float = 0f

    private var sliderFromProgress: Float = 0f
    private var sliderToProgress: Float = 0f
    private var sliderMinimumValue: Float = 0f
    private var sliderMaximumValue: Float = 0f

    /**
     * Background Color Rectangle Only Attributes
     * View Attributes to Draw and ReDraw
     * View Paths to Decide the Drawing Coordinates
     */
    private var backgroundColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundColorPath = Path()


    /**
     * Active Progress Rectangle Color Attributes
     * View Attributes to Draw and ReDraw
     * View Paths to Decide the Drawing Coordinates
     */
    private var activeProgressColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var activeProgressColorPath = Path()

    constructor(context: Context) : super(context) {
        initViewAttributes(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewAttributes(context, attrs)
    }

    private fun initViewAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            getDefaultViewAttributes(context)
        } else {
            getViewAttributes(context, attrs)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw the Background Rectangle
        onDrawBackgroundRectangle(canvas)

        // 2. Draw the Selected Progress in Active Color Based on Progress
        onDrawActiveProgressColors(canvas)
    }

    private fun onDrawActiveProgressColors(canvas: Canvas) {
        val width = width.toFloat()

        // remove All Prev Paths and Add New Rect Based on the New Coordinates
        activeProgressColorPath.reset()

        // Calculate the progress percentage
        val maximumProgressIndex = ((sliderFromProgress - sliderMinimumValue) / (sliderMaximumValue - sliderMinimumValue)) * 100

        // Calculate the width of the foreground rectangle based on progress percentage
        val maximumProgressWidth = width * (maximumProgressIndex / 100)

        // Draw the New Active Color Coordinates Based on the Colors
        activeProgressColorPath.addRoundRect(0f, 0f, maximumProgressWidth, rectangleHeight, backgroundRadius, backgroundRadius, Path.Direction.CW)

        // Add The Active Color to the Paint Based on the Progress
        sliderActiveColor?.toArgb()?.let { activeProgressColorPaint.setColor(it) }
        activeProgressColorPaint.style = Paint.Style.FILL

        // Draw the Active Color By Ready Coordinates
        canvas.drawPath(activeProgressColorPath, activeProgressColorPaint)

    }

    private fun onDrawBackgroundRectangle(canvas: Canvas) {
        val width = width.toFloat()

        // remove All Prev Paths and Add New Rect Based on the New Coordinates
        backgroundColorPath.reset()
        backgroundColorPath.addRoundRect(0f, 0f, width, rectangleHeight, backgroundRadius, backgroundRadius, Path.Direction.CW)

        // Add the Background Color to the Paint
        backgroundColor?.toArgb()?.let { backgroundColorPaint.setColor(it) }
        backgroundColorPaint.style = Paint.Style.FILL

        // Draw the Background Path based on the Paint with the Given Color
        canvas.drawPath(backgroundColorPath, backgroundColorPaint)
    }

    private fun getViewAttributes(context: Context, attrs: AttributeSet) {
        val types = context.obtainStyledAttributes(attrs, R.styleable.RangeSliderView, 0, 0)
        backgroundColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_slider_background, context.getColor(R.color.background_color)))
        sliderActiveColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_slider_active_color, context.getColor(R.color.active_color)))
        backgroundRadius = types.getDimension(R.styleable.RangeSliderView_corner_radius, DEFAULT_CORNER_RADIUS_BACKGROUND)
        rectangleHeight = types.getDimension(R.styleable.RangeSliderView_rectangle_height, DEFAULT_RECTANGLE_HEIGHT)

        sliderFromProgress = types.getFloat(R.styleable.RangeSliderView_slider_from_progress, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderToProgress = types.getFloat(R.styleable.RangeSliderView_slider_to_progress, DEFAULT_SLIDER_TO_PROGRESS)

        sliderMinimumValue = types.getFloat(R.styleable.RangeSliderView_slider_min_value, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderMaximumValue = types.getFloat(R.styleable.RangeSliderView_slider_max_value, DEFAULT_SLIDER_TO_PROGRESS)

        types.recycle()
    }

    private fun getDefaultViewAttributes(context: Context) {
        backgroundColor = Color.valueOf(context.getColor(R.color.background_color))
        sliderActiveColor = Color.valueOf(context.getColor(R.color.active_color))
        backgroundRadius = DEFAULT_CORNER_RADIUS_BACKGROUND
        rectangleHeight = DEFAULT_RECTANGLE_HEIGHT

        sliderFromProgress = DEFAULT_SLIDER_FROM_PROGRESS
        sliderToProgress = DEFAULT_SLIDER_TO_PROGRESS

        sliderMinimumValue = DEFAULT_SLIDER_FROM_PROGRESS
        sliderMaximumValue = DEFAULT_SLIDER_TO_PROGRESS
    }

}
