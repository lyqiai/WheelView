package com.river.wheelview.ext

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.river.wheelview.WheelView2
import java.text.SimpleDateFormat
import java.util.*


/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class DateWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    //View展示数据类型依次为年、月、日、时、分、秒
    private var type = booleanArrayOf(true, true, true, true, true, true)

    //最小选择日期
    private var min = Calendar.getInstance().apply {
        time = Date(0)
    }

    //最大选择日期
    private var max = Calendar.getInstance().apply {
        add(Calendar.YEAR, 100)
    }

    //监听器
    private var listener: OnDateChangedListener? = null

    //当前选中日期
    private var date: Calendar = Calendar.getInstance()

    init {
        //构建日期轮组组件
        for ((index, b) in type.withIndex()) {
            val wheelView = WheelView2(context)
            wheelView.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
                weight = 1f
            }
            wheelView.visibility = if (b) View.VISIBLE else View.GONE

            //监听轮组变更事件，设置对应（年月日时分秒），刷新对应轮组之后的UI状态，最后通过对比回调事件
            wheelView.setOnWheelViewSelectedChanged { position ->
                val old = Calendar.getInstance().apply { time = date.time }

                date.apply {
                    if (index == 0) {
                        set(Calendar.YEAR, getYearData()[position].toInt())
                    } else if (index == 1) {
                        set(Calendar.MONTH, getMonthData()[position].toInt() - 1)
                    } else if (index == 2) {
                        set(Calendar.DAY_OF_MONTH, getDayData()[position].toInt())
                    } else if (index == 3) {
                        set(Calendar.HOUR_OF_DAY, getHourData()[position].toInt())
                    } else if (index == 4) {
                        set(Calendar.MINUTE, getMinuteData()[position].toInt())
                    } else if (index == 5) {
                        set(Calendar.SECOND, getSecondData()[position].toInt())
                    }
                }
                Log.i("TAG", SimpleDateFormat("yyyy-MM-dd").format(date.time))
                checkDate()

                for (i in index + 1 until type.size) {
                    refresh(i)
                }

                if (!isSameDateTime(date, old)) {
                    listener?.onDateChanged(date)
                }
            }

            addView(wheelView)

            refresh(index)
        }
    }

    /**
     * 检查日期正确性，在轮组回调事件时先更新date，然后需调用该方法修复date
     */
    private fun checkDate() {
        if (date.before(min)) {
            date.time = min.time
        } else if (date.after(max)) {
            date.time = max.time
        }
    }

    /**
     * 获取年份
     * @return Int
     */
    private fun getYear(): Int {
        return date.get(Calendar.YEAR)
    }

    /**
     * 获取年份所在对应轮组位置
     * @return Int
     */
    private fun getYearPos(): Int {
        return getYearData().indexOf(date.get(Calendar.YEAR).toString())
    }

    /**
     * 获取月份
     * @return Int
     */
    private fun getMonth(): Int {
        return date.get(Calendar.MONTH)
    }

    /**
     * 获取月份所在对应轮组位置
     * @return Int
     */
    private fun getMonthPos(): Int {
        return getMonthData().indexOf((date.get(Calendar.MONTH) + 1).toString())
    }

    /**
     * 获取日
     * @return Int
     */
    private fun getDay(): Int {
        return date.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获取日所在对应轮组位置
     * @return Int
     */
    private fun getDayPos(): Int {
        return getDayData().indexOf(date.get(Calendar.DAY_OF_MONTH).toString())
    }

    /**
     * 获取小时24H
     * @return Int
     */
    private fun getHour(): Int {
        return date.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * 获取小时在轮组位置
     * @return Int
     */
    private fun getHourPos(): Int {
        return getHourData().indexOf(date.get(Calendar.HOUR_OF_DAY).toString())
    }

    /**
     * 获取分钟
     * @return Int
     */
    private fun getMinute(): Int {
        return date.get(Calendar.MINUTE)
    }

    /**
     * 获取分钟所在轮组位置
     * @return Int
     */
    private fun getMinutePos(): Int {
        return getMinuteData().indexOf(date.get(Calendar.MINUTE).toString())
    }

    /**
     * 获取秒
     * @return Int
     */
    private fun getSecond(): Int {
        return date.get(Calendar.SECOND)
    }

    /**
     * 获取秒所在轮组位置
     * @return Int
     */
    private fun getSecondPos(): Int {
        return getSecondData().indexOf(date.get(Calendar.SECOND).toString())
    }

    /**
     * 获取当前所选日期
     * @return (java.util.Calendar..java.util.Calendar?)
     */
    fun getDate() = Calendar.getInstance().apply { time = date.time }

    /**
     * 根据position刷新轮组UI
     * @param index Int 对应（年月日时分秒）
     * @return List<String> 返回此次刷新轮组所承载的数据
     */
    private fun refresh(index: Int): List<String> {
        val wheelView = getWheelViewAt(index)
        val data: List<String>

        if (index == 0) {
            data = getYearData()
            wheelView.setData(data, getYearPos())
        } else if (index == 1) {
            data = getMonthData()
            wheelView.setData(data, getMonthPos())
        } else if (index == 2) {
            data = getDayData()
            wheelView.setData(data, getDayPos())
        } else if (index == 3) {
            data = getHourData()
            wheelView.setData(data)
        } else if (index == 4) {
            data = getMinuteData()
            wheelView.setData(data)
        } else {
            data = getSecondData()
            wheelView.setData(data)
        }

        return data
    }

    /**
     * 刷新轮组UI
     */
    private fun refreshAll() {
        for (i in type.indices) {
            refresh(i)
        }
    }

    /**
     * 获取当前年份轮组数据
     * @return List<String>
     */
    private fun getYearData(): List<String> {
        val (maxYear, minYear) = max.get(Calendar.YEAR) to min.get(Calendar.YEAR)
        val data = mutableListOf<String>()
        for (i in minYear..maxYear) {
            data.add(i.toString())
        }
        return data
    }

    /**
     * 获取当前月份轮组数据
     * @return List<String>
     */
    private fun getMonthData(): List<String> {
        val minYear = min.get(Calendar.YEAR)
        val maxYear = max.get(Calendar.YEAR)
        val selectYear = getYear()

        var startMonth: Int
        var endMonth: Int
        if (minYear == selectYear && maxYear == selectYear) {
            startMonth = min.get(Calendar.MONTH)
            endMonth = min.get(Calendar.MINUTE)
        } else if (minYear == selectYear) {
            startMonth = min.get(Calendar.MONTH)
            endMonth = 11
        } else if (maxYear == selectYear) {
            startMonth = 0
            endMonth = max.get(Calendar.MONTH)
        } else {
            startMonth = 0
            endMonth = 11
        }

        val data = mutableListOf<String>()
        for (i in startMonth..endMonth) {
            data.add("${i + 1}")
        }
        return data
    }

    /**
     * 获取当前日轮组数据
     * @return List<String>
     */
    private fun getDayData(): List<String> {
        var startDay: Int
        var endDay: Int

        if (isSameMonth(date, min) && isSameMonth(date, max)) {
            startDay = min.get(Calendar.DAY_OF_MONTH)
            endDay = max.get(Calendar.DAY_OF_MONTH)
        } else if (isSameMonth(date, min)) {
            startDay = min.get(Calendar.DAY_OF_MONTH)
            endDay = getDaysInMonth(getMonth(), getYear())
        } else if (isSameMonth(date, max)) {
            startDay = 1
            endDay = max.get(Calendar.DAY_OF_MONTH)
        } else {
            startDay = 1
            endDay = getDaysInMonth(getMonth(), getYear())
        }

        val data = mutableListOf<String>()
        for (i in startDay..endDay) {
            data.add(i.toString())
        }
        return data
    }

    /**
     * 是否为同一月份
     * @param c1 Calendar?
     * @param c2 Calendar?
     * @return Boolean
     */
    private fun isSameMonth(c1: Calendar?, c2: Calendar?): Boolean {
        if (c1 == null || c2 == null) return false

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
    }

    /**
     * 获取月份日期数
     * @param month Int
     * @param year Int
     * @return Int
     */
    private fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
            Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
            Calendar.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> throw IllegalArgumentException("Invalid Month")
        }
    }

    /**
     * 获取小时轮组数据
     * @return List<String>
     */
    private fun getHourData(): List<String> {
        val data = mutableListOf<String>()
        for (i in 0..23) {
            data.add(i.toString())
        }
        return data
    }

    /**
     * 获取分钟轮组数据
     * @return List<String>
     */
    private fun getMinuteData(): List<String> {
        val data = mutableListOf<String>()
        for (i in 0..59) {
            data.add(i.toString())
        }
        return data
    }

    /**
     * 获取分钟轮组数据
     * @return List<String>
     */
    private fun getSecondData(): List<String> {
        val data = mutableListOf<String>()
        for (i in 0..59) {
            data.add(i.toString())
        }
        return data
    }

    /**
     * 是否同一时刻（精确到秒）
     * @param c1 Calendar?
     * @param c2 Calendar?
     * @return Boolean
     */
    private fun isSameDateTime(c1: Calendar?, c2: Calendar?): Boolean {
        if (c1 == null || c2 == null) {
            return false
        }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) &&
                c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE) &&
                c1.get(Calendar.SECOND) == c2.get(Calendar.SECOND)
    }

    /**
     * 设置ui类型，需要传入6位布尔数组依次对应年月日时分秒，True为展示对应轮组
     * @param type BooleanArray
     */
    fun setType(type: BooleanArray) {
        this.type = type
        for ((index, b) in type.withIndex()) {
            getChildAt(index).visibility = if (b) View.VISIBLE else View.GONE
        }
    }

    /**
     * 设置选中日期
     * @param calendar Calendar
     */
    fun setDate(calendar: Calendar) {
        date = Calendar.getInstance().apply { time = calendar.time }
        refreshAll()
    }

    /**
     * 设置最小时间范围
     * @param min Calendar
     */
    fun setMin(min: Calendar) {
        this.min = min
        refreshAll()
    }

    /**
     * 设置最大时间范围
     * @param max Calendar
     */
    fun setMax(max: Calendar) {
        this.max = max
        refreshAll()
    }

    /**
     * 设置时间范围
     * @param min Calendar
     * @param max Calendar
     */
    fun setRange(min: Calendar, max: Calendar) {
        this.min = min
        this.max = max
        refreshAll()
    }

    /**
     * 获取轮组组件
     * @param position Int
     * @return WheelView2
     */
    private fun getWheelViewAt(position: Int) = getChildAt(position) as WheelView2

    /**
     * 设置监听器
     * @param listener OnDateChangedListener?
     */
    fun setOnDateChangedListener(listener: OnDateChangedListener?) {
        this.listener = listener
    }

    fun interface OnDateChangedListener {
        fun onDateChanged(date: Calendar)
    }
}