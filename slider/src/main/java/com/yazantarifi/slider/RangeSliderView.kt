package com.yazantarifi.slider

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RangeSliderView: View {

    companion object {
        private const val DEFAULT_CORNER_RADIUS_BACKGROUND = 20f
        private const val DEFAULT_RECTANGLE_HEIGHT = 30f
        private const val DEFAULT_THUMB_SIZE = 20f

        private const val DEFAULT_SLIDER_FROM_PROGRESS = 0f
        private const val DEFAULT_SLIDER_TO_PROGRESS = 100f
        private const val DEFAULT_PADDING_VALUE = 45f

        const val FROM_THUMB = 0
        const val TO_THUMB = 0
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

    private var thumbColor: Color? = null
    private var thumbSecondColor: Color? = null
    private var thumbSize: Float = 0f
    private var isThumbSingleColor: Boolean = false
    private var progressListener: RangeSliderListener? = null

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

    /**
     * Active Thumbs Attributes
     * 1. From Thumb
     * 2. To Thumb
     */
    private var fromThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var fromThumbSecondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var toThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var toThumbSecondPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var fromThumbIndexX: Float = 0f
    private var toThumbIndexX: Float = 0f

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

        // 2. Init Thumbs Positions
        onInitThumbsPositions()

        // 3. Draw the Selected Progress in Active Color Based on Progress
        onDrawActiveProgressColors(canvas)

        // 4. Draw the Thumbs By Active Progress
        if (isThumbSingleColor) {
            onDrawSingleColorFromThumb(canvas)
            onDrawSingleColorToThumb(canvas)
        } else {
            onDrawMultipleColorFromThumb(canvas)
            onDrawMultipleColorToThumb(canvas)
        }
    }

    private fun onInitThumbsPositions() {
        if (fromThumbIndexX <= 0f) {
            fromThumbIndexX = getFromThumbIndex()
        }

        if (toThumbIndexX <= 0f) {
            toThumbIndexX = getToThumbIndex()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val eventValue = super.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val isFromCircleTouched = isCircleTouched(event, fromThumbIndexX)
                val isToCircleTouched = isCircleTouched(event, toThumbIndexX)

                if (!isFromCircleTouched && !isToCircleTouched) {
                    val newXPosition = event.x
                    if (newXPosition <= (getViewWidth() / 2)) {
                        fromThumbIndexX = newXPosition
                        sliderFromProgress = getProgressValueByXCoordinates(fromThumbIndexX)
                    } else {
                        toThumbIndexX = newXPosition
                        sliderToProgress = getProgressValueByXCoordinates(toThumbIndexX)
                    }

                    postInvalidate()
                }

                progressListener?.onRangeProgress(sliderFromProgress, sliderToProgress, true)

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // Circles Should Move
                val isFromCircleTouched = isCircleTouched(event, fromThumbIndexX)
                val isToCircleTouched = isCircleTouched(event, toThumbIndexX)
                if (isFromCircleTouched) {
                    fromThumbIndexX = event.x
                    sliderFromProgress = getProgressValueByXCoordinates(fromThumbIndexX)
                    onValidateThumbsPositions(true, false)
                    progressListener?.onThumbMovement(sliderFromProgress, FROM_THUMB, true)
                }

                if (isToCircleTouched) {
                    toThumbIndexX = event.x
                    sliderToProgress = getProgressValueByXCoordinates(toThumbIndexX)
                    onValidateThumbsPositions(false, true)
                    progressListener?.onThumbMovement(sliderToProgress, TO_THUMB, true)
                }

                progressListener?.onRangeProgress(sliderFromProgress, sliderToProgress, true)
                postInvalidate()

                return true
            }
        }

        return eventValue
    }

    private fun onValidateThumbsPositions(isFromValidation: Boolean, isToValidation: Boolean) {
        if (isFromValidation) {
            if (fromThumbIndexX >= toThumbIndexX) {
                fromThumbIndexX = toThumbIndexX
            }

            if (sliderFromProgress >= sliderToProgress) {
                sliderFromProgress = sliderToProgress
            }
        }

        if (isToValidation) {
            if (toThumbIndexX <= fromThumbIndexX) {
                toThumbIndexX = fromThumbIndexX
            }

            if (sliderToProgress <= sliderFromProgress) {
                sliderToProgress = sliderFromProgress
            }
        }

        if (sliderToProgress >= sliderMaximumValue) {
            sliderToProgress = sliderMaximumValue
        }

        if (sliderFromProgress <= sliderMinimumValue) {
            sliderFromProgress = sliderMinimumValue
        }

        if (toThumbIndexX >= getViewWidth()) {
            toThumbIndexX = getViewWidth()
        }
    }

    private fun isCircleTouched(event: MotionEvent, circleX: Float): Boolean {
        val newXPosition = event.x
        val newYPosition = event.y

        val dx = Math.pow((newXPosition - circleX).toDouble(), 2.0)
        val dy = Math.pow((newYPosition - getCenterPosition()).toDouble(), 2.0)

        return dx + dy < Math.pow(thumbSize.toDouble(), 2.0)
    }

    private fun getFromThumbIndex(): Float {
        // Calculate the width of the foreground rectangle based on progress percentage
        return DEFAULT_PADDING_VALUE + getViewWidth() * ((sliderFromProgress - sliderMinimumValue) / (sliderMaximumValue - sliderMinimumValue))
    }

    private fun onDrawSingleColorFromThumb(canvas: Canvas) {
        thumbColor?.let {
            thumbColor?.toArgb()?.let { fromThumbPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize, fromThumbPaint)
    }

    private fun onDrawMultipleColorFromThumb(canvas: Canvas) {
        fromThumbSecondPaint?.let {
            thumbSecondColor?.toArgb()?.let { fromThumbSecondPaint.setColor(it) }
            toThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(fromThumbIndexX, getCenterPosition(), thumbSize + 5, fromThumbSecondPaint)

        thumbColor?.let {
            thumbColor?.toArgb()?.let { fromThumbPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(fromThumbIndexX, getCenterPosition(), thumbSize, fromThumbPaint)

        fromThumbSecondPaint?.let {
            thumbSecondColor?.toArgb()?.let { fromThumbSecondPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(fromThumbIndexX, getCenterPosition(), thumbSize - 5, fromThumbSecondPaint)

        thumbColor?.let {
            thumbColor?.toArgb()?.let { fromThumbPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(fromThumbIndexX, getCenterPosition(), thumbSize - 10, fromThumbPaint)
    }

    private fun getToThumbIndex(): Float {
        // Calculate the width of the foreground rectangle based on progress percentage
        val maximumProgressIndex = (sliderToProgress - sliderMinimumValue) / (sliderMaximumValue - sliderMinimumValue) * 100
        return getViewWidth() * (maximumProgressIndex / 100)
    }

    private fun onDrawMultipleColorToThumb(canvas: Canvas) {

        toThumbSecondPaint?.let {
            thumbSecondColor?.toArgb()?.let { toThumbSecondPaint.setColor(it) }
            toThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize + 5, toThumbSecondPaint)

        thumbColor?.let {
            thumbColor?.toArgb()?.let { toThumbPaint.setColor(it) }
            toThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize, toThumbPaint)

        toThumbSecondPaint?.let {
            thumbSecondColor?.toArgb()?.let { toThumbSecondPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize - 5, fromThumbSecondPaint)

        thumbColor?.let {
            thumbColor?.toArgb()?.let { toThumbPaint.setColor(it) }
            toThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize - 10, toThumbPaint)

    }

    private fun onDrawSingleColorToThumb(canvas: Canvas) {
        thumbColor?.let {
            thumbColor?.toArgb()?.let { toThumbPaint.setColor(it) }
            toThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(toThumbIndexX, getCenterPosition(), thumbSize, toThumbPaint)
    }

    private fun onDrawActiveProgressColors(canvas: Canvas) {
        // Remove all previous paths and add new rectangle based on the new coordinates
        activeProgressColorPath.reset()

        // Draw the new active color coordinates based on the colors
        activeProgressColorPath.addRoundRect(fromThumbIndexX, (height / 2) - (rectangleHeight / 2), toThumbIndexX, (height / 2) + (rectangleHeight / 2), backgroundRadius, backgroundRadius, Path.Direction.CW)

        // Add the active color to the Paint based on the progress
        sliderActiveColor?.toArgb()?.let { activeProgressColorPaint.setColor(it) }
        activeProgressColorPaint.style = Paint.Style.FILL

        // Draw the active color by ready coordinates
        canvas.drawPath(activeProgressColorPath, activeProgressColorPaint)
    }

    private fun onDrawBackgroundRectangle(canvas: Canvas) {
        val width = getViewWidth()

        // remove All Prev Paths and Add New Rect Based on the New Coordinates
        backgroundColorPath.reset()
        backgroundColorPath.addRoundRect(0f, (height / 2) - (rectangleHeight / 2), width, (height / 2) + (rectangleHeight / 2), backgroundRadius, backgroundRadius, Path.Direction.CW)

        // Add the Background Color to the Paint
        backgroundColor?.toArgb()?.let { backgroundColorPaint.setColor(it) }
        backgroundColorPaint.style = Paint.Style.FILL

        // Draw the Background Path based on the Paint with the Given Color
        canvas.drawPath(backgroundColorPath, backgroundColorPaint)
    }

    private fun getProgressValueByXCoordinates(x: Float): Float {
        // Calculate the percentage of the x coordinate relative to the width of the view
        val width = getViewWidth()
        val percentage = x / width

        // Calculate the range of values
        val range = sliderMaximumValue - sliderMinimumValue

        // Calculate the value based on the percentage and the range
        return (percentage * range + sliderMinimumValue)
    }

    private fun getViewAttributes(context: Context, attrs: AttributeSet) {
        val types = context.obtainStyledAttributes(attrs, R.styleable.RangeSliderView, 0, 0)
        thumbColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_thumb_color, context.getColor(R.color.active_color)))
        thumbSecondColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_thumb_second_color, context.getColor(R.color.background_color_screen)))
        backgroundColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_slider_background, context.getColor(R.color.background_color)))
        sliderActiveColor = Color.valueOf(types.getColor(R.styleable.RangeSliderView_slider_active_color, context.getColor(R.color.active_color)))
        backgroundRadius = types.getDimension(R.styleable.RangeSliderView_corner_radius, DEFAULT_CORNER_RADIUS_BACKGROUND)
        rectangleHeight = types.getDimension(R.styleable.RangeSliderView_rectangle_height, DEFAULT_RECTANGLE_HEIGHT)
        thumbSize = types.getDimension(R.styleable.RangeSliderView_thumb_size, DEFAULT_THUMB_SIZE)
        isThumbSingleColor = types.getBoolean(R.styleable.RangeSliderView_thumb_single_color, false)

        sliderFromProgress = types.getFloat(R.styleable.RangeSliderView_slider_from_progress, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderToProgress = types.getFloat(R.styleable.RangeSliderView_slider_to_progress, DEFAULT_SLIDER_TO_PROGRESS)

        sliderMinimumValue = types.getFloat(R.styleable.RangeSliderView_slider_min_value, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderMaximumValue = types.getFloat(R.styleable.RangeSliderView_slider_max_value, DEFAULT_SLIDER_TO_PROGRESS)

        types.recycle()
    }

    private fun getDefaultViewAttributes(context: Context) {
        thumbSecondColor = Color.valueOf(context.getColor(R.color.background_color_screen))
        backgroundColor = Color.valueOf(context.getColor(R.color.background_color))
        sliderActiveColor = Color.valueOf(context.getColor(R.color.active_color))
        thumbColor = Color.valueOf(context.getColor(R.color.active_color))
        backgroundRadius = DEFAULT_CORNER_RADIUS_BACKGROUND
        rectangleHeight = DEFAULT_RECTANGLE_HEIGHT
        thumbSize = DEFAULT_THUMB_SIZE
        isThumbSingleColor = false

        sliderFromProgress = DEFAULT_SLIDER_FROM_PROGRESS
        sliderToProgress = DEFAULT_SLIDER_TO_PROGRESS

        sliderMinimumValue = DEFAULT_SLIDER_FROM_PROGRESS
        sliderMaximumValue = DEFAULT_SLIDER_TO_PROGRESS
    }

    fun onUpdateValues(fromValue: Float, toValue: Float) {
        if (fromValue <= sliderMinimumValue) {
            sliderFromProgress = sliderMinimumValue
        } else if (fromValue >= sliderMaximumValue) {
            sliderFromProgress = sliderMaximumValue
        } else {
            sliderFromProgress = fromValue
        }

        if (toValue <= sliderMinimumValue) {
            sliderToProgress = sliderMinimumValue
        } else if (toValue >= sliderMaximumValue) {
            sliderToProgress = sliderMaximumValue
        } else {
            sliderToProgress = fromValue
        }

        fromThumbIndexX = getFromThumbIndex()
        toThumbIndexX = getToThumbIndex()

        progressListener?.onRangeProgress(fromValue, toValue, false)
        invalidate()
    }

    fun onUpdateRangeValues(minimumValue: Float, maximumValue: Float) {
        this.sliderMinimumValue = minimumValue
        this.sliderMaximumValue = maximumValue

        this.sliderFromProgress = minimumValue
        this.sliderToProgress = maximumValue

        fromThumbIndexX = getFromThumbIndex()
        toThumbIndexX = getToThumbIndex()

        invalidate()
    }

    fun onAddRangeListener(listener: RangeSliderListener) {
        progressListener = listener
    }

    fun onClearViewInstances() {
        progressListener = null
    }

    private fun getCenterPosition(): Float {
        return (height / 2).toFloat()
    }

    private fun getViewWidth(): Float {
        return width - DEFAULT_PADDING_VALUE
    }

}
