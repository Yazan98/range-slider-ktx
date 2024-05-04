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
import kotlin.math.abs
import kotlin.math.round

/**
 * This RangeSlider Will Manage 2 Points of Progress Only
 * 1. From Slider Value
 * 2. To Slider Value
 *
 * This Custom View Will Take the Minimum and Maximum Values to the Slider
 * Then We Can Change the Thumbs Positions Inside This Range
 *
 * If The Thumbs X Coordinates became outside the Range the CustomView Internally
 * Will Arrange them to move them back to the Range, In case of Outside Update State
 * For Example User Input Outside Slider Range
 *
 * Useful Functions To Use In This View
 * 1, onUpdateValues -> Used to Update the Current Progress
 * 2. onUpdateRangeValues -> Used to Update the Minimum and Maximum Slider Range
 * 3. onAddRangeListener -> Used to Get the New Values When User Change the Progress
 * 4. onClearViewInstances -> To Call Once ViewHolder in the RecyclerView DeAttached or Fragment View Destroyed to Clear The Instances
 * 5. onUpdateStepSize -> Update Slider Step Size
 * 6. onUpdateColors -> To Update the Colors Inside the View
 * 7. onUpdateThumbInfo -> Update the Thumb Size and Colors Mode
 */
class RangeSliderView: View {

    companion object {
        private const val DEFAULT_CORNER_RADIUS_BACKGROUND = 20f
        private const val DEFAULT_RECTANGLE_HEIGHT = 30f
        private const val DEFAULT_THUMB_SIZE = 20f
        private const val DEFAULT_STEP_SIZE = 1f

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
    private var stepSize: Float = DEFAULT_STEP_SIZE
    private var sliderStepsIndexes: List<Float> = arrayListOf()

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
    private var prevThumbXPosition = 0f

    constructor(context: Context) : super(context) {
        initViewAttributes(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewAttributes(context, attrs)
    }

    /**
     * Main Entry Point for Variables Declaration In the Custom View
     * 1. Sizes
     * 2. Colors
     * 3. Radius
     * 4. Modes
     */
    private fun initViewAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            getDefaultViewAttributes(context)
        } else {
            getViewAttributes(context, attrs)
        }
    }

    /**
     * Main Entry Point of Drawing the UI
     * 1. We Draw the Disabled Background Color by the Height, width, Color
     * 2. We Init The Thumbs by the Initial Flow or the Prev Selection Flow, if there is a Prev Selection
     * 3. We Draw the Selected Line Between Thumbs Based on the Prev Step (Selected From, To Thumbs)
     * 4. We Draw The Thumbs Positions By Selected Coordinates Calculated by User Progress Calculated in Step 2
     */
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

    /**
     * Declare The Thumbs Positions By User Input Progress From, To
     */
    private fun onInitThumbsPositions() {
        if (fromThumbIndexX <= 0f) {
            fromThumbIndexX = getFromThumbIndex()
        }

        if (toThumbIndexX <= 0f) {
            toThumbIndexX = getToThumbIndex()
        }
    }

    /**
     * This Function Will Handle the Thumbs Movement
     * 1. You Can Move Thumbs Within the Slider Range
     * 2. You can Click on Any Area and The Slider Clicked Position Will Move the Correct Thumb
     *
     * Thumbs Movement Arragment Works on Both Modes and Each Update will notify the Screen by the Registered callback
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val eventValue = super.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                prevThumbXPosition = event.x
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
                if (stepSize == DEFAULT_STEP_SIZE) {
                    if (isFromCircleTouched) {
                        fromThumbIndexX = event.x
                        sliderFromProgress = getProgressValueByXCoordinates(fromThumbIndexX)
                        onValidateThumbsPositions(true, false, RangeSliderDirection.PREVIOUS)
                        progressListener?.onThumbMovement(sliderFromProgress, FROM_THUMB, true)
                        prevThumbXPosition = fromThumbIndexX
                    }

                    if (isToCircleTouched) {
                        toThumbIndexX = event.x
                        sliderToProgress = getProgressValueByXCoordinates(toThumbIndexX)
                        onValidateThumbsPositions(false, true, RangeSliderDirection.PREVIOUS)
                        progressListener?.onThumbMovement(sliderToProgress, TO_THUMB, true)
                        prevThumbXPosition = toThumbIndexX
                    }

                    progressListener?.onRangeProgress(sliderFromProgress, sliderToProgress, true)
                    postInvalidate()
                } else {
                    val currentX = event.x
                    val direction = getDirectionByXPosition(currentX, prevThumbXPosition)
                    if (direction == RangeSliderDirection.NEXT) {
                        if (currentX >= prevThumbXPosition) {
                            // Moving to the right (next index)
                            moveThumb(currentX, direction, event)
                            prevThumbXPosition = currentX
                        }
                    } else {
                        if (currentX <= prevThumbXPosition) {
                            // Moving to the left (previous index)
                            moveThumb(currentX, direction, event)
                            prevThumbXPosition = currentX
                        }
                    }
                }

                return true
            }
        }

        return eventValue
    }

    private fun moveThumb(currentX: Float, direction: RangeSliderDirection, event: MotionEvent) {
        val isFromCircleTouched = isCircleTouched(event, fromThumbIndexX)
        val isToCircleTouched = isCircleTouched(event, toThumbIndexX)

        if (isFromCircleTouched) {
            fromThumbIndexX = currentX
            sliderFromProgress = getProgressValueByXCoordinates(fromThumbIndexX)
            onValidateThumbsPositions(true, false, direction)
            progressListener?.onThumbMovement(sliderFromProgress, FROM_THUMB, true)
        }

        if (isToCircleTouched) {
            toThumbIndexX = currentX
            sliderToProgress = getProgressValueByXCoordinates(toThumbIndexX)
            onValidateThumbsPositions(false, true, direction)
            progressListener?.onThumbMovement(sliderToProgress, TO_THUMB, true)
        }

        progressListener?.onRangeProgress(sliderFromProgress, sliderToProgress, true)
        postInvalidate()
    }

    private fun getDirectionByXPosition(currentX: Float, prevX: Float): RangeSliderDirection {
        return if (currentX < prevX) RangeSliderDirection.PREVIOUS else RangeSliderDirection.NEXT
    }

    private fun onValidateThumbsPositions(isFromValidation: Boolean, isToValidation: Boolean, direction: RangeSliderDirection) {
        if (isFromValidation) {
            if (fromThumbIndexX >= toThumbIndexX) {
                fromThumbIndexX = toThumbIndexX
            }

            if (sliderFromProgress >= sliderToProgress) {
                sliderFromProgress = sliderToProgress
            }

            if (sliderFromProgress <= sliderMinimumValue) {
                sliderFromProgress = sliderMinimumValue
            }

            if (stepSize != DEFAULT_STEP_SIZE) {
                val targetValue = getTargetValueByStepSize(if (direction == RangeSliderDirection.PREVIOUS) sliderFromProgress.toInt().toFloat() else sliderFromProgress, direction)
                sliderFromProgress = targetValue ?: sliderFromProgress
                fromThumbIndexX = getFromThumbIndex()
            }
        }

        if (isToValidation) {
            if (toThumbIndexX <= fromThumbIndexX) {
                toThumbIndexX = fromThumbIndexX
            }

            if (sliderToProgress <= sliderFromProgress) {
                sliderToProgress = sliderFromProgress
            }

            if (sliderToProgress >= sliderMaximumValue) {
                sliderToProgress = sliderMaximumValue
            }

            if (stepSize != DEFAULT_STEP_SIZE) {
                val targetValue = getTargetValueByStepSize(sliderToProgress, direction)
                sliderToProgress = targetValue ?: sliderToProgress
                toThumbIndexX = getToThumbIndex()
            }
        }

        if (toThumbIndexX >= getViewWidth()) {
            toThumbIndexX = getViewWidth()
        }
    }

    /**
     * Decide The Touched X, Y Within the Range of the X Circle Coordinates
     * This Function Used for From, To Thumbs
     */
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

    private fun getToThumbIndex(): Float {
        // Calculate the width of the foreground rectangle based on progress percentage
        val maximumProgressIndex = (sliderToProgress - sliderMinimumValue) / (sliderMaximumValue - sliderMinimumValue) * 100
        return getViewWidth() * (maximumProgressIndex / 100)
    }

    /**
     * This Function Will Draw a Single Thumb Color if the single Color in Xml was True
     */
    private fun onDrawSingleColorFromThumb(canvas: Canvas) {
        thumbColor?.let {
            thumbColor?.toArgb()?.let { fromThumbPaint.setColor(it) }
            fromThumbPaint.style = Paint.Style.FILL
        }

        canvas.drawCircle(fromThumbIndexX, getCenterPosition(), thumbSize, fromThumbPaint)
    }

    /**
     * This Function Will Draw a Multiple Thumb Colors if the single Color in Xml was False
     * This Will Draw a Nested Layers of the Thumb Color and Second Thumb Color
     */
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

    /**
     * This Function Will Draw a Multiple Thumb Colors if the single Color in Xml was False
     * This Will Draw a Nested Layers of the Thumb Color and Second Thumb Color
     */
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

    /**
     * This Function Will Draw a Single Thumb Color if the single Color in Xml was True
     */
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

    /**
     * Calculate the Progress Values When Moving Thumb by X Coordinates
     */
    private fun getProgressValueByXCoordinates(x: Float): Float {
        // Calculate the percentage of the x coordinate relative to the width of the view
        val width = getViewWidth()
        val percentage = x / width

        // Calculate the range of values
        val range = sliderMaximumValue - sliderMinimumValue

        // Calculate the value based on the percentage and the range
        return (percentage * range + sliderMinimumValue)
    }

    /**
     * Read the Values from Xml Attributes
     */
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

        stepSize = types.getFloat(R.styleable.RangeSliderView_slider_step_size, DEFAULT_STEP_SIZE)
        sliderFromProgress = types.getFloat(R.styleable.RangeSliderView_slider_from_progress, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderToProgress = types.getFloat(R.styleable.RangeSliderView_slider_to_progress, DEFAULT_SLIDER_TO_PROGRESS)

        sliderMinimumValue = types.getFloat(R.styleable.RangeSliderView_slider_min_value, DEFAULT_SLIDER_FROM_PROGRESS)
        sliderMaximumValue = types.getFloat(R.styleable.RangeSliderView_slider_max_value, DEFAULT_SLIDER_TO_PROGRESS)

        types.recycle()
    }

    /**
     * Read Default Values In case Xml Attributes Missing or View Created Inside Code Directly
     */
    private fun getDefaultViewAttributes(context: Context) {
        thumbSecondColor = Color.valueOf(context.getColor(R.color.background_color_screen))
        backgroundColor = Color.valueOf(context.getColor(R.color.background_color))
        sliderActiveColor = Color.valueOf(context.getColor(R.color.active_color))
        thumbColor = Color.valueOf(context.getColor(R.color.active_color))
        backgroundRadius = DEFAULT_CORNER_RADIUS_BACKGROUND
        rectangleHeight = DEFAULT_RECTANGLE_HEIGHT
        thumbSize = DEFAULT_THUMB_SIZE
        isThumbSingleColor = false
        stepSize = DEFAULT_STEP_SIZE

        sliderFromProgress = DEFAULT_SLIDER_FROM_PROGRESS
        sliderToProgress = DEFAULT_SLIDER_TO_PROGRESS

        sliderMinimumValue = DEFAULT_SLIDER_FROM_PROGRESS
        sliderMaximumValue = DEFAULT_SLIDER_TO_PROGRESS
    }

    /**
     * We Will Split the Maximum Number to be a Steps by Step Size
     * When the StepSize is not 1 that's mean we need to move in a steps
     */
    private fun getNumberOfValuesSteps(): List<Float> {
        val result = mutableListOf<Float>()
        var current = 0f

        while (current < sliderMaximumValue) {
            result.add(current)
            current += stepSize
        }

        return result
    }

    /**
     * This Function Will Search about the Neatest Step by the Step Size
     * We have an Array of Values That Represent the Values By Step Size
     * [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
     *
     * Input Value 10 -> Next Movement Direction Will Fund 20
     * Back Direction Will find 0
     *
     * Logic Related to The Max Value in case of Last Step and the Step Number is not Match the Max Value
     * We Check if the Value has a Next Step but this Next Step does not Match a New Step AKA Decimal Points
     * We Move it to the Last Index Whatever the Remaining Value is
     */
    private fun getTargetValueByStepSize(value: Float, direction: RangeSliderDirection): Float {
        val lastIndex = sliderStepsIndexes.size - 1 // Get the index of the last item in the steps list

        var nearest: Float? = null
        var minDifference = Float.MAX_VALUE

        for (i in sliderStepsIndexes.indices) {
            val number = sliderStepsIndexes[i]
            val difference = abs(value - number)

            if (direction == RangeSliderDirection.NEXT) {
                // Check if the current item is the last one and there is no next item
                if (i == lastIndex && number <= value) {
                    return sliderStepsIndexes[lastIndex]
                }
                // If not last item, proceed with normal processing
                if (number > value && difference < minDifference) {
                    nearest = number
                    minDifference = difference
                }
            } else {
                // If direction is PREVIOUS, proceed with normal processing
                if (number < value && difference < minDifference) {
                    nearest = number
                    minDifference = difference
                }
            }
        }

        if (round(value) >= findLastValue() && direction == RangeSliderDirection.NEXT) {
            return sliderMaximumValue
        }

        return nearest ?: value // Return the same value if no nearest value is found
    }

    /**
     * Logic Related to The Max Value in case of Last Step and the Step Number is not Match the Max Value
     * We Check if the Value has a Next Step but this Next Step does not Match a New Step AKA Decimal Points
     * We Move it to the Last Index Whatever the Remaining Value is
     */
    private fun findLastValue(): Float {
        var value = sliderMaximumValue
        repeat(1) {
            value -= stepSize
        }
        return value
    }

    /**
     * Call This Function in case of User Input to Update the Slider Progress
     * This Will Update the Slider Progress
     */
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
            sliderToProgress = toValue
        }

        fromThumbIndexX = getFromThumbIndex()
        toThumbIndexX = getToThumbIndex()

        progressListener?.onRangeProgress(fromValue, toValue, false)
        invalidate()
    }

    /**
     * Initialization Function and The Starter Code Before do Anything
     * Call This Function when you Setup the Slider
     * This Will Add the Range from, To To the Slider
     */
    fun onUpdateRangeValues(minimumValue: Float, maximumValue: Float) {
        this.sliderMinimumValue = minimumValue
        this.sliderMaximumValue = maximumValue

        this.sliderFromProgress = minimumValue
        this.sliderToProgress = maximumValue

        fromThumbIndexX = getFromThumbIndex()
        toThumbIndexX = getToThumbIndex()

        if (stepSize != DEFAULT_STEP_SIZE) {
            sliderStepsIndexes = getNumberOfValuesSteps()
        }

        invalidate()
    }

    /**
     * Initialization Step, Add the Step Size to The Slider in Lazy Load
     * Use This Function if you want to Split the Slider into Sections
     */
    fun onUpdateStepSize(stepSize: Float) {
        this.stepSize = stepSize
        sliderStepsIndexes = getNumberOfValuesSteps()
        invalidate()
    }

    /**
     * Register The Function in case you need to get the New Progress Values
     */
    fun onAddRangeListener(listener: RangeSliderListener) {
        progressListener = listener
    }

    /**
     * Call This Function when the View Destroyed
     * 1. Fragment onViewDestroy
     * 2. Activity onDestroy
     * 3. RecyclerView when ViewHolder DeAttached from View
     */
    fun onClearViewInstances() {
        progressListener = null
        sliderStepsIndexes = arrayListOf()
    }

    /**
     * Use This Function if you need to Update the Colors From Code or View Created Inside Code, Not Xml
     */
    fun onUpdateColors(
        backgroundColor: Color,
        activeColor: Color,
        thumbColor: Color,
        secondThumbColor: Color
    ) {
        this.backgroundColor = backgroundColor
        this.thumbColor = thumbColor
        this.thumbSecondColor = secondThumbColor
        this.sliderActiveColor = activeColor
        invalidate()
    }

    /**
     * Use This Function if you need to Update the Thumb Size
     * Input: isSingleColor to Check if the Thumbs Drawn in One Color Only or 2 Colors
     */
    fun onUpdateThumbInfo(thumbSize: Float, isSingleColor: Boolean) {
        this.isThumbSingleColor = isSingleColor
        this.thumbSize = thumbSize
        invalidate()
    }

    fun getSliderFromValue(): Float {
        return sliderFromProgress
    }

    fun getSliderToValue(): Float {
        return sliderToProgress
    }

    private fun getCenterPosition(): Float {
        return (height / 2).toFloat()
    }

    private fun getViewWidth(): Float {
        return width - DEFAULT_PADDING_VALUE
    }

}
