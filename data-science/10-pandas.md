### padas 
在上一章中，我们详细介绍了NumPy及其ndarray对象，该对象在Python中提供了密集型数组的有效存储和操作。在这里，我们将通过详细研究Pandas库提供的数据结构来建立这种知识。 Pandas是在NumPy之上构建的更新程序包，并提供了DataFrame的有效实现。 DataFrame本质上是具有附加的行和列标签的多维数组，并且通常具有异构类型和/或丢失的数据。除了为标签数据提供方便的存储接口外，Pandas还实现了数据库框架和电子表格程序用户熟悉的许多强大的数据操作。

如我们所见，NumPy的ndarray数据结构为通常为干净，组织良好的数据类型提供了计算基本功能。尽管它很好地满足了这个目的，但是当我们需要更大的灵活性（例如，给数据打标签，处理丢失的数据等）以及尝试将操作map结构不能与其他元素以广播的方式处理（例如，分组，数据透视等），其中的每一项都是分析我们周围世界以多种形式提供的结构较简单的数据的重要内容，。pandas，尤其是其Series和DataFrame对象，建立在NumPy数组结构的基础上，可以高效地访问占用数据科学家大量时间的这类“数据处理”任务。

在本章中，我们将重点介绍有效使用Series，DataFrame和相关结构的机制
### Installing and Using Pandas
在系统上安装Pandas需要安装NumPy，如果从源代码构建库，则需要适当的工具来编译在其上构建Pandas的C和Cython源代码。可以在[Pandas文档](http://pandas.pydata.org/)中找到有关此安装的详细信息。如果使用了Anaconda，那么已经安装了Pandas。
查看是否安装
```py
In [1]: import pandas
   ...: pandas.__version__
Out[1]: '1.0.1'
```
下面开始使用
```py
import pandas as pd
```
### 关于内置文档的提醒

在阅读本章时，请不要忘记IPython使您能够快速浏览包的内容（通过使用tab-completion功能）以及各种功能的文档（使用？字符）。 
```py
In [3]: pd.<TAB>
In [4]: pd?
```
### 初始介绍

在最基本的层面上，Pandas对象可以看作是NumPy结构化数组的增强版本，其中的行和列是使用标签而不是简单的整数索引来标识的。正如我们将在本章中看到的那样，Pandas在基本数据结构之上提供了许多有用的工具，方法和功能，但是后面几乎所有内容都需要了解这些结构是什么。因此，在介绍之前，先介绍一下这三个基本的Pandas数据结构：Series，DataFrame和Index。

```py
In [1]: import numpy as np
   ...: import pandas as pd
```

###  Pandas Series 对象

pandas series是一维数组的索引数据。可以从列表或数组中创建它，如下所示：
```py
In [2]: data = pd.Series([3,0.5,2,4])
In [3]: data
Out[3]: 
0    3.0
1    0.5
2    2.0
3    4.0
dtype: float64
```
正如我们在输出中看到的那样，Series同时包装了一系列值和一系列索引，我们可以使用值和索引属性对其进行访问。这些值只是一个和NumPy相似的数组：
```py
In [5]: data.values
Out[5]: array([3. , 0.5, 2. , 4. ])
```
索引是pd.Index类型的类似数组的对象，稍后详细讨论。
```py
In [6]: data.index
Out[6]: RangeIndex(start=0, stop=4, step=1)
```
与使用NumPy数组一样，关联的索引可以通过熟悉的Python方括号表示法访问数据：
```py
In [10]: data[0:2]
Out[10]: 
0    3.0
1    0.5
dtype: float64
In [10]: data[0:2]
Out[10]: 
0    3.0
1    0.5
dtype: float64
```
### Series作为广义的NumPy数组
从到目前为止我们所看到的，看起来Series对象基本上可以与一维NumPy数组互换。本质上的区别是索引的存在：虽然Numpy数组具有用于访问值的隐式定义的整数索引，但Pandas系列具有与值相关联的显式定义的索引。
此明确的索引定义为Series对象提供了附加功能。例如，索引不必是整数，而可以由任何所需类型的值组成。例如，如果我们愿意，我们可以使用字符串作为索引：
```py
In [11]: data=pd.Series([3,4,6],index=['a','b','c'])
In [12]: data['a']
Out[12]: 3
```
### Series作为特殊词典
可以将Pandas系列想像成Python字典的一种特殊化。字典是将任意键映射到一组任意值的结构，而Series是将类型键映射到一组类型值的结构。这种输入很重要：就像NumPy数组后面的特定于类型的编译代码使其对于某些操作而言，其效率要比Python列表要高，而Pandas Series的类型信息使其对于某些操作而言，其效率要比Python字典高得多。
通过直接从Python字典构造Series对象，可以更清楚地将Series-as-dictionary类推为类：
```py
In [14]: my_dict={'zhangsan':22,'lisi':24,'wangwu':25}
In [15]: name_series=pd.Series(my_dict)
In [16]: name_series
Out[16]: 
zhangsan    22
lisi        24
wangwu      25
dtype: int64
```
默认情况Series会将字典的key作为索引，除此之外Series还可以像数组一样操作数组
```py
In [18]: name_series['zhangsan']
Out[18]: 22

In [19]: name_series['zhangsan':'wangwu']
Out[19]: 
zhangsan    22
lisi        24
wangwu      25
dtype: int64
```
### 创建Series
上面我们已经通过下面方式生成Series
>pd.Series(data, index=index)
除此series 中data还可以根据index扩展
```py
In [21]: pd.Series(5,index=[1,2,3])
Out[21]: 
1    5
2    5
3    5
dtype: int64
```
### DataFrame 作为numpy的通用数组