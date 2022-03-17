## WheelView or WheelView2

基础轮组控件，可配置属性如下

| textSize | dimension | 字号 |
| --- | --- | --- |
| selectedTextSize | dimension | 选中字号 |
| textColor | color | 字色 |
| selectedTextColor | color | 选中字色 |
| itemCount | integer | 展示条目数量 |
| itemHeight | dimension | 条目高度 |
| selectedBgColor | color | 选中背景颜色 |

对外方法如下

| fun getData(position: Int): String | 获取对应下标数据 |
| --- | --- |
| fun getData(): List<String> | 获取数据源 |
| fun setData(data: List<String>) | 设置数据源 |
| fun setSelectPosition(position: Int) | 设置选中位置 |
| fun setOnWheelViewSelectedChanged(listener: OnWheelViewSelectedChanged?) | 设置变更监听器 |

## DateWheelView

日期轮组控件，对外方法如下

| fun getDate(): Calendar？ | 获取当前所选日期 |
| --- | --- |
| fun setType(type: BooleanArray) | 设置ui类型，需要传入6位布尔数组依次对应年月日时分秒，True为展示对应轮组 |
| fun setDate(calendar: Calendar) | 设置选中日期 |
| fun setMin(min: Calendar) | 设置最小时间范围 |
| fun setMax(max: Calendar) | 设置最大时间范围 |
| fun setRange(min: Calendar, max: Calendar) | 设置时间范围 |
| fun setOnDateChangedListener(listener: OnDateChangedListener?) | 设置监听器 |

## LinkedWheelViewGroup

联动轮组控件，对外方法如下

| fun setData(data: List<Data>) | 设置数据源 |
| --- | --- |
| fun setOnWheelViewSelectedChanged(listener: OnWheelViewSelectedChanged?) | 设置变更监听器 |
| fun setSelectPosition(position: IntArray) | 设置选中数据位置 |

```kotlin
data class Data(
	val text: String, 
	val value: Any, 
	val children: List<Data>? = null
)
```

## WheelViewGroup

无联动轮组，对外方法如下

| fun setData(vararg data: List<String>) | 设置数据源 |
| --- | --- |
| fun setSelectPosition(positions: Array<Int>) | 设置选中数据位置 |
| fun setOnWheelViewGroupSelectorChangedListener(listener: OnWheelViewGroupSelectorChangedListener?) | 设置监听器 |

## 集成
根目录build.gradle添加：

```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
```

模块build.gradl添加：

```groovy
dependencies {
        implementation 'com.github.lyqiai:wheelview:0.0.1'
}
```