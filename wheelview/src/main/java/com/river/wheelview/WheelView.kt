package com.river.wheelview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.yunlu.wheelview.R

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class WheelView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val textSize: Int
    private val selectedTextSize: Int
    private val textColor: Int
    private val selectedTextColor: Int
    private val itemHeight: Int
    private val itemCount: Int
    private var realItemHeight = 0
    private val selectedBgColor: Int

    private var mData = emptyList<String>()

    private val mLinearSnapHelper: LinearSnapHelper
    private var mAdapter: WheelViewAdapter? = null
    private val mPadCount: Int
    private val mSelectedBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var listener: OnWheelViewSelectedChanged? = null

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

        mPadCount = itemCount / 2
        mSelectedBackgroundPaint.color = selectedBgColor

        layoutManager = LinearLayoutManager(context)
        mLinearSnapHelper = LinearSnapHelper()
        mLinearSnapHelper.attachToRecyclerView(this)
        mAdapter = WheelViewAdapter()

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                adapter = mAdapter
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.AT_MOST) {
            realItemHeight = itemHeight
            val height = itemHeight * itemCount
            setMeasuredDimension(widthSize, height)
        } else if (heightMode == MeasureSpec.EXACTLY) {
            realItemHeight = heightSize / itemCount
        }
    }

    fun getData(position: Int) = mData[position + mPadCount]

    fun getData(): List<String> {
        return mData.subList(mPadCount, mData.size - mPadCount)
    }

    fun setData(data: List<String>) {
        val tempData = data.toMutableList()
        for (i in 0 until mPadCount) {
            tempData.add(0, PAD_STRING)
            tempData.add(PAD_STRING)
        }
        if (mAdapter?.selectedPosition ?: 0 >= tempData.size - mPadCount) {
            mAdapter?.selectedPosition = tempData.size - mPadCount - 1
        }

        mData = tempData

        mAdapter?.notifyDataSetChanged()
    }

    fun setSelectPosition(position: Int) {
        mAdapter?.selectedPosition = position + mPadCount
        (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            mAdapter?.refreshSelected()
            listener?.onChanged(getSelectedPosition())
        }
    }

    private fun getSelectedPosition(): Int {
        val findSnapView = mLinearSnapHelper.findSnapView(layoutManager) ?: return 0
        val position = (layoutManager as LinearLayoutManager).getPosition(findSnapView)
        return position - mPadCount
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val y = realItemHeight * mPadCount
        val rect = Rect(0, y, measuredWidth, y + realItemHeight)
        c.drawRect(rect, mSelectedBackgroundPaint)
    }

    fun setOnWheelViewSelectedChanged(listener: OnWheelViewSelectedChanged?) {
        this.listener = listener
    }

    inner class WheelViewAdapter : RecyclerView.Adapter<WheelViewAdapter.ViewHolder>() {
        var selectedPosition = mPadCount

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_wheel_view_item, parent, false)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, realItemHeight)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val seleted = selectedPosition == position
            (holder.itemView as TextView)?.let {
                it.text = if (mData[position] == PAD_STRING) "" else mData[position]
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (seleted) selectedTextSize.toFloat() else textSize.toFloat())
                it.setTextColor(if (seleted) selectedTextColor else textColor)
            }
        }

        override fun getItemCount() = mData.size

        fun refreshSelected() {
            val oldPosition = selectedPosition
            selectedPosition = getSelectedPosition() + mPadCount

            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        private const val PAD_STRING = "@PAD_STRING@"
    }
}