package com.river.wheelview.ext

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.river.wheelview.WheelView2

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class WheelViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var renderData: List<List<String>> = emptyList()
    private var listener: OnWheelViewGroupSelectorChangedListener? = null
    private var selected = emptyArray<Int>()

    init {
        orientation = HORIZONTAL
    }

    /**
     * 设置数据源
     * @param data Array<out List<String>>
     */
    fun setData(vararg data: List<String>) {
        renderData = data.map { it }
        refreshData()
    }

    /**
     * 设置选中数据位置
     * @param positions Array<Int>
     */
    fun setSelectPosition(positions: Array<Int>) {
        selected = positions

        for (i in 0 until childCount) {
            (getChildAt(i) as WheelView2).setSelectedPosition(selected[i])
        }
    }

    /**
     * 设置监听器
     * @param listener OnWheelViewGroupSelectorChangedListener?
     */
    fun setOnWheelViewGroupSelectorChangedListener(listener: OnWheelViewGroupSelectorChangedListener?) {
        this.listener = listener
    }

    /**
     * 刷新轮组数据源
     */
    private fun refreshData() {
        removeAllViews()
        selected = Array(renderData.size) { 0 }
        renderData.forEachIndexed { index, list ->
            val wheelView = WheelView2(context)
            wheelView.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
                weight = 1f
            }
            wheelView.setOnWheelViewSelectedChanged { position ->
                if (selected[index] != position) {
                    selected[index] = position
                    listener?.onChanged(selected)
                }
            }
            wheelView.setData(list)
            addView(wheelView)
        }
    }

    fun interface OnWheelViewGroupSelectorChangedListener {
        fun onChanged(positions: Array<Int>)
    }
}