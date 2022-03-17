package com.river.wheelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewTreeObserver
import com.yunlu.wheelview.R
import java.lang.Integer.max
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class WheelView2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    //字号
    private val textSize: Int
    //选中字号
    private val selectedTextSize: Int
    //字色
    private val textColor: Int
    //选中字色
    private val selectedTextColor: Int
    //子项预期高度
    private val itemHeight: Int
    //子项真实高度
    private var realItemHeight = 0
    //子项显示数，默认5，必须为奇数
    private val itemCount: Int
    //渲染子项数
    private val realCount: Int
    //选中子项背景色
    private val selectedBgColor: Int
    //子项字体画笔
    private val textPaint = Paint()
    //子项背景画笔
    private val centerPaint = Paint()
    //数据集
    private var data = emptyList<String>()
    //滚动值
    private var translate = 0
    //自动滚动Scroller，测量完成需调用setItemHeight(height: Int)
    private val scroller = WheelScroller(context)
    //计算滑动速度
    private var velocityTracker: VelocityTracker? = null
    //选中更改监听器
    private var listener: OnWheelViewSelectedChanged? = null
    //默认选中0
    private var position = 0
    //是否测量完成
    private var measured = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.WheelView, defStyleAttr,
            R.style.DefaultWheelStyle
        )
        textSize = typedArray.getDimensionPixelSize(
            R.styleable.WheelView_textSize, context.resources.getDimensionPixelSize(
                R.dimen.wheel_view_text_size
            ))
        selectedTextSize = typedArray.getDimensionPixelSize(
            R.styleable.WheelView_selectedTextSize, context.resources.getDimensionPixelSize(
                R.dimen.wheel_view_selected_text_size
            ))
        textColor = typedArray.getColor(
            R.styleable.WheelView_textColor, context.resources.getColor(
                R.color.color_wheel_view_text_color
            ))
        selectedTextColor = typedArray.getColor(
            R.styleable.WheelView_selectedTextColor, context.resources.getColor(
                R.color.color_wheel_view_selected_text_color
            ))
        itemHeight = typedArray.getDimensionPixelSize(
            R.styleable.WheelView_itemHeight, context.resources.getDimensionPixelSize(
                R.dimen.wheel_view_item_height
            ))
        itemCount = typedArray.getInt(R.styleable.WheelView_itemCount, 5)
        selectedBgColor = typedArray.getColor(
            R.styleable.WheelView_selectedBgColor, context.resources.getColor(
                R.color.color_wheel_view_selected_bg_color
            ))
        typedArray.recycle()

        realCount = itemCount + 1

        textPaint.color = textColor
        textPaint.textSize = textSize.toFloat()
        textPaint.textAlign = Paint.Align.CENTER

        centerPaint.color = selectedBgColor
        centerPaint.style = Paint.Style.FILL
        centerPaint.isAntiAlias = true
    }

    /**
     * 高度测量模式为AT_MOST时
     * 容器高度为itemHeight * itemCount,itemHeight即真实高度
     * 高度测量模式为EXACTLY时
     * 因为容器高度已经确定，所以需要计算真实高度即容器高度/itemCount
     * @param widthMeasureSpec Int
     * @param heightMeasureSpec Int
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.EXACTLY) {
            realItemHeight = heightSize / itemCount
        } else {
            realItemHeight = itemHeight
            val height = itemHeight * itemCount
            setMeasuredDimension(widthSize, height)
        }

        scroller.setItemHeight(realItemHeight)

        measured = true
    }

    /**
     * 设置数据源
     * @param data List<String>
     */
    fun setData(data: List<String>, position: Int? = null) {
        this.data = data

        if (position == null && this.position >= data.size) {
            this.position = data.size - 1
        } else if (position != null){
            this.position = position
        }

        if (measured)
            setSelectedPosition(this.position, false)
        else
            viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setSelectedPosition(this@WheelView2.position, false)
                }
            })
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        //画布垂直方向移动至中间
        canvas.translate(0f, itemCount / 2 * realItemHeight.toFloat())
        //绘制选中背景
        canvas.drawRect(Rect(0, 0, measuredWidth, realItemHeight), centerPaint)
        //绘制数据源,绘制个数为固定的realCount，
        for (pos in 0 until realCount) {
            val itemPosition = getPositionByRenderPosition(pos)
            val text = getText(itemPosition)
            val translate = getTranslate(pos)

            val rect = Rect(0, translate, measuredWidth, translate + realItemHeight)
            val fontMetrics = textPaint.fontMetrics
            val bottomLine = rect.centerY() - (fontMetrics.descent + fontMetrics.ascent / 2)

            canvas.drawText(text, rect.width() / 2f, bottomLine, textPaint.apply {
                textSize = getTextSize(rect)
                color = getTextColor(rect)
            })
        }
    }

    /**
     * 根据偏移获取字体大小
     * @param rect Rect
     * @return Float
     */
    private fun getTextSize(rect: Rect): Float {
        val min = 0
        val max = realItemHeight
        val center = (max - min) / 2

        if (rect.centerY() in min..max) {
            val progress = (rect.centerY() - center).absoluteValue / center.toFloat()
            return selectedTextSize- (selectedTextSize - textSize) * progress
        }
        return textSize.toFloat()
    }

    /**
     * 根据偏移获取字体颜色
     * @param rect Rect
     * @return Int
     */
    private fun getTextColor(rect: Rect): Int {
        val min = 0
        val max = realItemHeight

        if (rect.centerY() in min..max) {
            return selectedTextColor
        }
        return textColor
    }

    /**
     * 根据position获取子项文本
     * @param position Int
     * @return String
     */
    private fun getText(position: Int): String {
        if (position < 0 || position >= data.size) {
            return ""
        }
        return data[position]
    }

    /**
     * 根据渲染position获取真实position
     * @param position Int
     * @return Int
     */
    private fun getPositionByRenderPosition(position: Int) : Int {
        val minFlag = -(itemCount / 2 + 1) * realItemHeight
        return if (translate < minFlag) (translate - minFlag).absoluteValue / realItemHeight + 1 + position else position
    }

    /**
     * 计算渲染子项偏移
     * @param position Int
     * @return Int
     */
    private fun getTranslate(position: Int): Int {
        val minFlag = -(itemCount / 2 + 1) * realItemHeight
        return (if (translate < minFlag) -itemCount / 2 * realItemHeight + translate % realItemHeight else translate) + position * realItemHeight
    }

    private var lastX = 0f
    private var lastY = 0f
    private var actionPointId = MotionEvent.INVALID_POINTER_ID

    /**
     * 处理手势进行偏移计算
     * @param event MotionEvent
     * @return Boolean
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)

                actionPointId = event.getPointerId(0)
                lastX = event.x
                lastY = event.y
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                scroller.abortAnimation()
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)

                actionPointId = event.getPointerId(event.actionIndex)
                lastX = event.getX(event.findPointerIndex(actionPointId))
                lastY = event.getY(event.findPointerIndex(actionPointId))
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                scroller.abortAnimation()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (actionPointId == MotionEvent.INVALID_POINTER_ID) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }

                val x = event.getX(event.findPointerIndex(actionPointId))
                val y = event.getY(event.findPointerIndex(actionPointId))

                val disX = x - lastX
                val disY = y - lastY


                if (disY.absoluteValue >= disX.absoluteValue) {
                    val max = 0
                    val min = -(data.size - 1) * realItemHeight

                    if (translate + disY <= min || translate + disY >= max) {
                        parent.requestDisallowInterceptTouchEvent(false)
                        return false
                    }

                    translate += disY.toInt()

                    translate = min(translate, max)
                    translate = max(translate, min)

                    lastX = x
                    lastY = y

                    velocityTracker?.addMovement(event)

                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)

                actionPointId = MotionEvent.INVALID_POINTER_ID

                velocityTracker?.let {
                    it.computeCurrentVelocity(1000)
                    scroller.fling(
                        0,
                        translate,
                        0,
                        it.yVelocity.toInt(),
                        0,
                        0,
                        -(data.size - 1) * realItemHeight,
                        0
                    )
                    invalidate()
                }
                return false
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * 根据scroller计算更新偏移重绘,停止偏移时回调事件
     */
    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val max = 0
            val min = -(data.size - 1) * realItemHeight
            if (scroller.currY > max || scroller.currY < min) {
                scroller.abortAnimation()
                return
            }

            translate = scroller.currY
            invalidate()
        } else {
            if (actionPointId == MotionEvent.INVALID_POINTER_ID && listener != null && position != calcPosition()) {
                position = calcPosition()

                listener?.onChanged(position)
            }
        }
    }

    /**
     * 获取当前选中的position
     * @return Int
     */
    fun getSelectedPosition() = position

    private fun calcPosition(): Int{
        if (realItemHeight == 0) return position

        return translate.absoluteValue / realItemHeight
    }

    /**
     * 设置选中position
     * @param position Int
     * @param animation Boolean
     */
    fun setSelectedPosition(position: Int, animation: Boolean = false) {
        assert(position in data.indices) { "position($position) must in range of (0,${data.size})" }
        this.position = position

        val oldTranslate = translate
        translate = -realItemHeight * position

        scroller.startScroll(0, oldTranslate, 0, translate - oldTranslate, if (animation) WheelScroller.DEFAULT_DURATION else 0)
        invalidate()
    }

    /**
     * 设置监听器
     * @param listener OnWheelViewSelectedChanged?
     */
    fun setOnWheelViewSelectedChanged(listener: OnWheelViewSelectedChanged?) {
        this.listener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker?.recycle()
    }
}