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
class LinkedWheelViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var data: List<Data> = emptyList()
    private var deep = 0
    private var listener: OnWheelViewSelectedChanged? = null

    init {
        orientation = HORIZONTAL
    }

    /**
     * 初始化view
     */
    private fun initView() {
        removeAllViews()
        for (index in 0 until deep) {
            val wheelView = WheelView2(context)
            wheelView.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
                weight = 1f
            }
            wheelView.setOnWheelViewSelectedChanged { position ->
                for (i in index + 1 until deep) {
                    refresh(i)
                }

                listener?.onChanged(IntArray(deep) { getWheelViewAt(it).getSelectedPosition() })
            }
            addView(wheelView)
        }
    }

    /**
     * 设置数据源
     * @param data List<Data>
     */
    fun setData(data: List<Data>) {
        this.data = data

        calcDeep()

        initView()

        fillData()
    }

    private fun fillData() {
        for (index in 0 until deep) {
            getWheelViewAt(index).setData(getData(index))
        }
    }

    /**
     * 设置选中数据位置
     * @param position IntArray
     */
    fun setSelectPosition(position: IntArray) {
        position.forEachIndexed { index, pos ->
            refresh(index)
            getWheelViewAt(index).setSelectedPosition(pos)
        }
    }

    /**
     * 计算轮组个数
     */
    private fun calcDeep() {
        if (data == null) deep = 0

        deep = 0
        var temp: Data? = data?.get(0)
        while (temp != null) {
            deep++
            temp = temp.children?.get(0)
        }
    }

    /**
     * 刷新轮组数据源
     * @param index Int
     */
    private fun refresh(index: Int) {
        getWheelViewAt(index).setData(getData(index), 0)
    }


    private fun getData(index: Int): List<String> {
        if (index == 0) {
            return data.map { it.text }
        }

        var temp = data[getWheelViewAt(0).getSelectedPosition()]

        for (i in 1 until index) {
            temp = temp.children!![getWheelViewAt(i).getSelectedPosition()]
        }

        return temp.children!!.map { it.text }
    }

    private fun getWheelViewAt(position: Int) = getChildAt(position) as WheelView2

    /**
     * 设置轮组监听器
     * @param listener OnWheelViewSelectedChanged?
     */
    fun setOnWheelViewSelectedChanged(listener: OnWheelViewSelectedChanged?) {
        this.listener = listener
    }

    data class Data(val text: String, val children: List<Data>? = null)

    fun interface OnWheelViewSelectedChanged {
        fun onChanged(position: IntArray)
    }
}