package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.properties.Delegates
import android.view.animation.Animation
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bgColor = DEFAULT_BACKGROUND_COLOR
    private var textColor = DEFAULT_TEXT_COLOR
    private var progressBarColor = DEFAULT_PROGRESSBAR_COLOR
    private var widthSize = 0
    private var heightSize = 0
    private var arcSweepAngle = 0f
    private var progressBar = 0f

    private val progressBarPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val bgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 28.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private val circlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    private lateinit var valueAnimatorProgressBar: ValueAnimator
    private lateinit var valueAnimatorCircle: ValueAnimator

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed)
    { p, _, new ->
        when (new) {
            ButtonState.Loading -> {
                startAnimatingProgressBar()
            }
            ButtonState.Completed -> {
            }
            ButtonState.Clicked -> {
            }
        }
    }

    init {
        isClickable = true
        buttonState = ButtonState.Completed
        setupAttributes(attrs)
    }

    private fun startAnimatingProgressBar() {
        progressBar = 0f
        arcSweepAngle = 0f
        valueAnimatorProgressBar = ValueAnimator.ofFloat(0f, widthSize.toFloat())
        valueAnimatorCircle = ValueAnimator.ofFloat(0f, 360f)
        valueAnimatorProgressBar.setDuration(2000).apply {
            addUpdateListener {
                progressBar = it.animatedValue as Float
                invalidate()
            }
            start()
        }
        valueAnimatorCircle.setDuration(2000).apply {
            addUpdateListener {
                arcSweepAngle = it.animatedValue as Float
                invalidate()
            }

            doOnEnd {
                isClickable = true
                buttonState = ButtonState.Completed
            }

            start()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawPaint(bgPaint)
        if (buttonState == ButtonState.Completed) {
            canvas?.drawText(
                "Download", widthSize / 2f,
                heightSize / 2f + textPaint.textSize / 2f, textPaint
            )
            arcSweepAngle = 0f
        } else if (buttonState == ButtonState.Loading) {
            canvas?.drawRect(0f, 0f, progressBar, heightSize.toFloat(), progressBarPaint)
            canvas?.drawText(
                context.getString(R.string.button_loading), widthSize / 2f,
                heightSize / 2f + textPaint.textSize / 2f, textPaint
            )
            canvas?.drawArc(
                ((widthSize / 4) * 3 - 30).toFloat(), (heightSize / 2 - 30).toFloat(),
                ((widthSize / 4) * 3 + 30).toFloat(), (heightSize / 2 + 30).toFloat(), 0f,
                arcSweepAngle, true, circlePaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.LoadingButton,
            0, 0
        )
        bgColor = typedArray.getColor(
            R.styleable.LoadingButton_backgroundColor,
            DEFAULT_BACKGROUND_COLOR
        )
        textColor = typedArray.getColor(R.styleable.LoadingButton_textColor, DEFAULT_TEXT_COLOR)
        progressBarColor = typedArray.getColor(
            R.styleable.LoadingButton_progressBarColor,
            DEFAULT_PROGRESSBAR_COLOR
        )

        bgPaint.color = bgColor
        textPaint.color = textColor
        progressBarPaint.color = progressBarColor

        typedArray.recycle()
    }

    companion object {
        private const val DEFAULT_BACKGROUND_COLOR = Color.CYAN
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_PROGRESSBAR_COLOR = Color.BLUE
    }

}