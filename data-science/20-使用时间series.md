## 使用时间series
Pandas是在财务建模的背景下开发的，因此，正如您可能期望的那样，它包含用于处理日期，时间和时间索引数据的相当广泛的工具集。日期和时间数据有几种形式，我们将在这里讨论：
    时间戳记会参考特定的时间点（例如，2015年7月4日上午7:00）。
    时间间隔和时间段指的是特定起点和终点之间的时间长度；例如2015年。时间段通常是指时间间隔的一种特殊情况，其中每个时间间隔的长度均一且不重叠（例如，由24天组成的长达24小时的时间段，包括天数）。
    时间增量或持续时间指的是确切的时间长度（例如，持续时间为22.56秒）。

在本节中，我们将介绍如何使用Pandas中的每种类型的日期/时间数据。本小节绝不是Python或Pandas中可用的时间序列工具的完整指南，而是旨在概述您作为用户应该如何使用时间序列。我们将首先简要讨论用于处理Python中日期和时间的工具，然后再更具体地讨论Pandas提供的工具。在列出一些更深入的资源之后，我们将回顾一些在熊猫中使用时间序列数据的简短示例。
### python 中的Dates and Times
Python世界有许多可用的日期，时间，增量和时间跨度表示。虽然Pandas提供的时间序列工具通常对数据科学应用程序最有用，但是查看它们与Python中使用的其他软件包的关系会很有帮助。
#### Python日期和时间：datetime和dateutil
Python处理日期和时间的基本对象位于内置的datetime模块中。与第三方dateutil模块一起，您可以使用它在日期和时间上快速执行一系列有用的功能。例如，您可以使用datetime类型手动构建日期：
```py
In [4]: datetime(2000,3,12,12,45,12)
Out[4]: datetime.datetime(2000, 3, 12, 12, 45, 12)
```
 
或者，使用dateutil模块，您可以解析各种字符串格式的日期：
```py
In [5]: from dateutil import parser
   ...: date = parser.parse("4th of July, 2015")
   ...: date
Out[5]: datetime.datetime(2015, 7, 4, 0, 0)
```
拥有日期时间对象后，您可以执行类似打印星期几的操作：
```py
In [6]: date.strftime('%A')
Out[6]: 'Saturday'
```
 
在最后一行，我们使用了一种标准的字符串格式代码来打印日期（“％A”），可以在Python的datetime文档的[strftime部分](https://docs.python.org/3/library/datetime.html#strftime-and-strptime-behavior)中阅读该代码。其他有用的日期实用程序的文档可以在[dateutil的在线文档](http://labix.org/python-dateutil)中找到。 pytz是一个需要注意的相关软件包，其中包含用于处理最让人头痛的时间序列数据的工具：时区。
datetime和dateutil的强大功能在于它们的灵活性和简单的语法：您可以使用这些对象及其内置方法轻松地执行几乎任何您可能感兴趣的操作。当您希望使用大型数组时，它们会崩溃。日期和时间：就像Python数值变量的列表与NumPy样式类型的数字数组相比是次优的，Python日期时间对象的列表与编码日期的类型数组相比次优。
### 类型化的时间数组：NumPy的datetime64
Python的datetime格式的弱点启发了NumPy团队向NumPy添加了一组本地时间序列数据类型。 datetime64 dtype将日期编码为64位整数，因此可以非常紧凑地表示日期数组。 datetime64需要非常特定的输入格式：

```py
In [7]:import numpy as np
   ...: date = np.array('2015-07-04', dtype=np.datetime64)
   ...: date
Out[7]: array('2015-07-04', dtype='datetime64[D]')
```
一旦格式化了该日期，就可以对其快速执行矢量化操作：
```py
In [8]: date + np.arange(12)
Out[8]: 
array(['2015-07-04', '2015-07-05', '2015-07-06', '2015-07-07',
       '2015-07-08', '2015-07-09', '2015-07-10', '2015-07-11',
       '2015-07-12', '2015-07-13', '2015-07-14', '2015-07-15'],
      dtype='datetime64[D]')
```
由于NumPy datetime64数组中的类型统一，因此与直接使用Python的datetime对象进行操作相比，这种类型的操作可以更快地完成，尤其是当数组变大时（在NumPy Arrays的计算中引入了这种类型的矢量化：通用职能）。
datetime64和timedelta64对象的一个​​细节是它们建立在基本时间单位上。由于datetime64对象限制为64位精度，因此可编码时间范围是此基本单位的264倍。换句话说，datetime64在时间分辨率和最大时间跨度之间进行了权衡。
例如，如果您希望时间分辨率为1纳秒，那么您只有足够的信息来编码264纳秒或不到600年的范围。 NumPy将从输入中推断出所需的单位；例如，这是一个基于日期的日期时间：
```py
In [9]: np.datetime64('2015-07-04')
Out[9]: numpy.datetime64('2015-07-04')
#这是基于分钟的日期时间：
In [10]: np.datetime64('2020-04-02 20:33:22')
Out[10]: numpy.datetime64('2020-04-02T20:33:22')
```
请注意，时区会在执行代码的计算机上自动设置为本地时间。您可以使用多种格式代码之一来强制使用任何所需的基本单位。例如，在这里，我们将强制基于纳秒的时间：
```py
In [11]:np.datetime64('2015-07-04 12:59:59.50', 'ns')
Out[11]: numpy.datetime64('2015-07-04T12:59:59.500000000')
```
下表摘自NumPy datetime64文档，列出了可用的格式代码以及它们可以编码的相对和绝对时间跨度：
    Code 	Meaning 	Time span (relative) 	Time span (absolute)
    Y 	Year 	± 9.2e18 years 	[9.2e18 BC, 9.2e18 AD]
    M 	Month 	± 7.6e17 years 	[7.6e17 BC, 7.6e17 AD]
    W 	Week 	± 1.7e17 years 	[1.7e17 BC, 1.7e17 AD]
    D 	Day 	± 2.5e16 years 	[2.5e16 BC, 2.5e16 AD]
    h 	Hour 	± 1.0e15 years 	[1.0e15 BC, 1.0e15 AD]
    m 	Minute 	± 1.7e13 years 	[1.7e13 BC, 1.7e13 AD]
    s 	Second 	± 2.9e12 years 	[ 2.9e9 BC, 2.9e9 AD]
    ms 	Millisecond 	± 2.9e9 years 	[ 2.9e6 BC, 2.9e6 AD]
    us 	Microsecond 	± 2.9e6 years 	[290301 BC, 294241 AD]
    ns 	Nanosecond 	± 292 years 	[ 1678 AD, 2262 AD]
    ps 	Picosecond 	± 106 days 	[ 1969 AD, 1970 AD]
    fs 	Femtosecond 	± 2.6 hours 	[ 1969 AD, 1970 AD]
    as 	Attosecond 	± 9.2 seconds 	[ 1969 AD, 1970 AD]
对于我们在现实世界中看到的数据类型，有用的默认值为datetime64 [ns]，因为它可以以适当的精度对有用的现代日期范围进行编码。
最后，我们将注意到，尽管datetime64数据类型解决了内置Python datetime类型的一些缺陷，但它缺少datetime尤其是dateutil提供的许多便捷方法和功能。可以在[NumPy的datetime64](http://docs.scipy.org/doc/numpy/reference/arrays.datetime.html)文档中找到更多信息。
###  pandas的日期和时间：两全其美
Pandas建立在刚才讨论的所有工具的基础上，提供了一个Timestamp对象，该对象将datetime和dateutil的易用性与numpy.datetime64的有效存储和矢量化接口结合在一起。从一组这些Timestamp对象中，Pandas可以构造一个DatetimeIndex，该索引可用于为Series或DataFrame中的数据建立索引。我们将在下面看到许多示例。
例如，我们可以使用pandas工具从上面重复演示。我们可以解析格式灵活的字符串日期，并使用格式代码输出星期几：
```py
In [14]: import pandas as pd
    ...: date = pd.to_datetime("4th of July, 2015")
    ...: date
Out[14]: Timestamp('2015-07-04 00:00:00')
#
In [15]: date.strftime("%A")
Out[15]: 'Saturday'
```
此外，我们可以直接在同一对象上执行NumPy风格的矢量化操作
```py
In [17]:  date + pd.to_timedelta(np.arange(12), 'D')
Out[17]: 
DatetimeIndex(['2015-07-04', '2015-07-05', '2015-07-06', '2015-07-07',
               '2015-07-08', '2015-07-09', '2015-07-10', '2015-07-11',
               '2015-07-12', '2015-07-13', '2015-07-14', '2015-07-15'],
              dtype='datetime64[ns]', freq=None)
```
在下一节中，我们将详细介绍如何使用Pandas提供的工具来处理时间序列数据。
### pandas时间序列：按时间编制索引
 
Pandas时间序列工具真正变得有用的地方是当您开始按时间戳索引数据时。例如，我们可以构造一个具有时间索引数据的Series对象：
```py
In [18]: index = pd.DatetimeIndex(['2014-07-04', '2014-08-04',
    ...:                           '2015-07-04', '2015-08-04'])
    ...: data = pd.Series([0, 1, 2, 3], index=index)
    ...: data
Out[18]: 
2014-07-04    0
2014-08-04    1
2015-07-04    2
2015-08-04    3
dtype: int64
```
现在，我们已经在系列中获得了这些数据，我们可以使用在上一节中讨论的任何系列索引模式，将可以强制转换为日期的值传递给：
```py
In [21]: data['2015']
Out[21]: 
2015-07-04    2
2015-08-04    3
dtype: int64
```
### Pandas 时间 Series得数据结构
本节将介绍用于处理时间序列数据的基本Pandas数据结构
对于时间戳，Pandas提供了时间戳类型。如前所述，它实际上是Python本机datetime的替代，但它基于更有效的numpy.datetime64数据类型。关联的索引结构是DatetimeIndex。
对于时间段，Pandas提供了“时间段”类型。这将基于numpy.datetime64编码固定频率的间隔。关联的索引结构是PeriodIndex。
对于时间增量或持续时间，Pandas提供了Timedelta类型。 Timedelta是基于numpy.timedelta64的Python本地datetime.timedelta类型的更有效替代。关联的索引结构是TimedeltaIndex。

这些日期/时间对象中最基本的是Timestamp和DatetimeIndex对象。尽管可以直接调用这些类对象，但更常见的是使用pd.to_datetime（）函数，该函数可以解析各种格式。将单个日期传递给pd.to_datetime（）会产生一个时间戳；默认情况下，传递一系列日期会产生DatetimeIndex
```py
In [22]: dates = pd.to_datetime([datetime(2015, 7, 3), '4th of July, 2015',
    ...:                        '2015-Jul-6', '07-07-2015', '20150708'])
    ...: dates
Out[22]: 
DatetimeIndex(['2015-07-03', '2015-07-04', '2015-07-06', '2015-07-07',
               '2015-07-08'],
              dtype='datetime64[ns]', freq=None)
```
可以使用to_period（）函数并添加频率代码将任何DatetimeIndex转换为PeriodIndex。在这里，我们将使用“ D”来表示每日频率,也可以使用前面的Y、M等：
```py
In [24]: dates.to_period('D')
Out[24]: 
PeriodIndex(['2015-07-03', '2015-07-04', '2015-07-06', '2015-07-07',
             '2015-07-08'],
            dtype='period[D]', freq='D')
```
例如，当从另一个日期减去日期时，将创建一个TimedeltaIndex：
```py
In [25]: dates-dates[0]
Out[25]: TimedeltaIndex(['0 days', '1 days', '3 days', '4 days', '5 days'], dtype='timedelta64[ns]', freq=None)
```
### 常规序列：pd.date_range（）
为了使常规日期序列的创建更加方便，Pandas为此提供了一些功能：pd.date_range（）用于时间戳，pd.period_range（）用于周期，pd.timedelta_range（）用于时间增量。我们已经看到Python的range（）和NumPy的np.arange（）将起点，终点和可选的步长转换为序列。同样，pd.date_range（）接受开始日期，结束日期和可选的频率代码以创建常规的日期序列。默认情况下，频率为一天：
```py
In [26]: pd.date_range('2015-07-03', '2015-07-10')
Out[26]: 
DatetimeIndex(['2015-07-03', '2015-07-04', '2015-07-05', '2015-07-06',
               '2015-07-07', '2015-07-08', '2015-07-09', '2015-07-10'],
              dtype='datetime64[ns]', freq='D')
```
另外，日期范围可以不指定起点和终点，而可以指定起点和一个间隔数(默认是天，可以额外指定)：
```py
In [27]: pd.date_range('2019-09-03',periods=5)
Out[27]: 
DatetimeIndex(['2019-09-03', '2019-09-04', '2019-09-05', '2019-09-06',
               '2019-09-07'],
              dtype='datetime64[ns]', freq='D')
## 指定间隔为月
In [28]: pd.date_range('2019-09-03',periods=5,freq='M')
Out[28]: 
DatetimeIndex(['2019-09-30', '2019-10-31', '2019-11-30', '2019-12-31',
               '2020-01-31'],
              dtype='datetime64[ns]', freq='M')
```
## 时间频率和偏移量
这些pandas时间序列工具的基础是频率或日期偏移量的概念。正如我们在上面看到的D（天）和H（小时）代码一样，我们可以使用此类代码指定任何所需的频率间隔。下表总结了可用的主要代码：
```
D 	Calendar day 	B 	Business day
W 	Weekly 		
M 	Month end 	    BM 	Business month end
Q 	Quarter end 	BQ 	Business quarter end
A 	Year end 	    BA 	Business year end
H 	Hours 	        BH 	Business hours
T 	Minutes 		
S 	Seconds 		
L 	Milliseonds 		
U 	Microseconds 		
N 	nanoseconds 	
```
在指定时间段的末尾标记了每月，每季度和每年的频率。通过在任何一个后缀中添加S后缀，它们将在开头被标记：
    MS月开始 BMS商业月开始
    QS季度开始 BQS商业季度开始
    AS年开始 BAS业务年开始
此外，您可以通过添加后缀三个字母的月份代码来更改用于标记任何季度或年度代码的月份：

    Q-JAN, BQ-FEB, QS-MAR, BQS-APR, etc.
    A-JAN, BA-FEB, AS-MAR, BAS-APR, etc.
同样，可以通过添加三个字母的工作日代码来修改每周频率的分割点：
    W-SUN, W-MON, W-TUE, W-WED 等
最重要的是，代码可以与数字组合以指定其他频率。例如，对于2小时30分钟的频率，我们可以按以下方式组合小时（H）和分钟（T）代码：
```py
In [30]: pd.timedelta_range(0,periods=8,freq='2H30T')
Out[30]: 
TimedeltaIndex(['00:00:00', '02:30:00', '05:00:00', '07:30:00', '10:00:00',
                '12:30:00', '15:00:00', '17:30:00'],
               dtype='timedelta64[ns]', freq='150T')
```
所有这些短代码都涉及pandas时间序列偏移量的特定实例，可以在pd.tseries.offsets模块中找到它们。例如，我们可以直接创建一个工作日偏移量，如下所示：
```py
In [31]: from pandas.tseries.offsets import BDay
    ...: pd.date_range('2015-07-01', periods=5, freq=BDay())
Out[31]: 
DatetimeIndex(['2015-07-01', '2015-07-02', '2015-07-03', '2015-07-06',
               '2015-07-07'],
              dtype='datetime64[ns]', freq='B')
```