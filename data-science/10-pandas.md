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
 
如果Series是具有灵活索引的一维数组的类似物，则DataFrame是具有灵活行索引和灵活列名的二维数组的类似物。就像您将二维数组视为对齐的一维列的有序序列一样，也可以将DataFrame视为对齐的Series对象的序列。在这里，“对齐”是指它们共享相同的索引。

为了演示这一点，我们首先构建一个新的SEries：
```py
In [4]: area_dict = {'California': 423967, 'Texas': 695662, 'New York': 141297,
   ...:              'Florida': 170312, 'Illinois': 149995}
In [5]: area=pd.Series(area_dict)
In [6]: area
Out[6]: 
California    423967
Texas         695662
New York      141297
Florida       170312
Illinois      149995
dtype: int64
In [7]: population_dict = {'California': 38332521,
   ...:                    'Texas': 26448193,
   ...:                    'New York': 19651127,
   ...:                    'Florida': 19552860,
   ...:                    'Illinois': 12882135}
   ...: population = pd.Series(population_dict)
   ...: population
Out[7]: 
California    38332521
Texas         26448193
New York      19651127
Florida       19552860
Illinois      12882135
dtype: int64
```
现在我们将这两个Series合并成为一个数组
```py
In [8]: states = pd.DataFrame({'population':population,'area':area})
In [9]: states
Out[9]: 
            population    area
California    38332521  423967
Texas         26448193  695662
New York      19651127  141297
Florida       19552860  170312
Illinois      12882135  149995
```
与Series对象类似，DataFrame具有index属性，该属性可访问索引标签：
```py
In [10]: states.index
Out[10]: Index(['California', 'Texas', 'New York', 'Florida', 'Illinois'], dtype='object')

In [11]: states.columns
Out[11]: Index(['population', 'area'], dtype='object')
# 类似数组访问
In [12]: states['area']['New York']
Out[12]: 141297
```
 
因此，可以将DataFrame视为二维NumPy数组的一般化，其中行和列都具有用于访问数据的一般化索引。
### DataFrame作为特殊字典
 
同样，我们也可以将DataFrame视为字典的一种特殊形式。在字典将键映射到值的地方，DataFrame将列名称映射到一系列列数据。例如，要求'area'属性返回包含我们之前看到的区域的Series对象：
```py
In [13]: states['area']
Out[13]: 
California    423967
Texas         695662
New York      141297
Florida       170312
Illinois      149995
Name: area, dtype: int64
#
In [15]: states[:'New York']
Out[15]: 
            population    area
California    38332521  423967
Texas         26448193  695662
New York      19651127  141297
```
* 可能出现的混乱点：在二维NumPy数组中，data [0]将返回第一行。对于DataFrame，data ['col0']将返回第一列。因此，最好将DataFrames视为广义字典，而不是广义数组，尽管两种查看情况的方法都可能有用。我们将探索在数据索引和选择中为DataFrames索引的更灵活的方法。
### 构建DataFrame 对象
Pandas DataFrame 的创建方式有很多种，这里给出几种常用的
1. 从单个的Series 对象创建
```py
In [16]: pd.DataFrame(population, columns=['population'])
Out[16]: 
            population
California    38332521
Texas         26448193
New York      19651127
Florida       19552860
Illinois      12882135
```
2. 从字典列表
```py
In [18]: data=[{'a':i,'b':i*2 }for i in range(6)]
In [19]: data
Out[19]: 
[{'a': 0, 'b': 0},
 {'a': 1, 'b': 2},
 {'a': 2, 'b': 4},
 {'a': 3, 'b': 6},
 {'a': 4, 'b': 8},
 {'a': 5, 'b': 10}]
## 
In [20]: pd.DataFrame(data)
Out[20]: 
   a   b
0  0   0
1  1   2
2  2   4
3  3   6
4  4   8
5  5  10
```
即使缺少字典中的某些键，Pandas也会用NaN（即“非数字”）值填充它们：
```py
In [21]: pd.DataFrame([{'a':2,'b':'dd'},{'c':'cc','b':2}])
Out[21]: 
     a   b    c
0  2.0  dd  NaN
1  NaN   2   cc
```
3. 从Series的字典中创建
```py
In [22]: pd.DataFrame({'population': population,
    ...:               'area': area})
Out[22]: 
            population    area
California    38332521  423967
Texas         26448193  695662
New York      19651127  141297
Florida       19552860  170312
Illinois      12882135  149995
```
4. 从numpy的二维数组中创建

```py
In [24]: pd.DataFrame(np.random.rand(3,2),columns=['zhangsan','lisi'],index=['语文','数学','英语'])
Out[24]: 
    zhangsan      lisi
语文  0.097589  0.247104
数学  0.471053  0.078950
英语  0.697091  0.657466
```
5. 从NumPy结构化数组
```py
In [25]: A = np.zeros(3, dtype=[('A', 'i8'), ('B', 'f8')])

In [26]: A
Out[26]: array([(0, 0.), (0, 0.), (0, 0.)], dtype=[('A', '<i8'), ('B', '<f8')])

In [27]: pd.DataFrame(A)
Out[27]: 
   A    B
0  0  0.0
1  0  0.0
2  0  0.0
```
### Pandas的索引对象
在这里我们已经看到Series和DataFrame对象都包含一个显式索引，该索引使您可以引用和修改数据。该Index对象本身就是一个有趣的结构，可以将其视为不可变数组或有序集（从技术上讲是多集，因为Index对象可能包含重复值）。这些视图在Index对象上可用的操作中产生了一些有趣的结果。作为一个简单的示例，让我们从整数列表构造一个Index：

```py
In [28]: ind=pd.Index([1,3,4])
In [29]: ind
Out[29]: Int64Index([1, 3, 4], dtype='int64')
```
#### Index 作为不可变数组

```py
In [30]: ind[:2]
Out[30]: Int64Index([1, 3], dtype='int64')
In [31]: ind[0]
Out[31]: 1
# 我们来修改试试
In [32]: ind[0]=8
## 结果会抛出异常
---------------------------------------------------------------------------
TypeError                                 Traceback (most recent call last)
<ipython-input-32-1dec7051a402> in <module>
----> 1 ind[0]=8

f:\softinstall\python\lib\site-packages\pandas\core\indexes\base.py in __setitem__(self, key, value)
   3908
   3909     def __setitem__(self, key, value):
-> 3910         raise TypeError("Index does not support mutable operations")
   3911
   3912     def __getitem__(self, key):

TypeError: Index does not support mutable operations

```
这种不变性使得在多个DataFrame和数组之间共享索引更加安全，而不会因无意间修改索引而产生副作用。
#### 索引作为有序集
Pandas对象旨在促进诸如跨数据集的联接之类的操作，这取决于集合算术的许多方面。 Index对象遵循Python的内置set数据结构使用的许多约定，因此可以用熟悉的方式计算并集，交集，差值和其他组合：
```py
In [33]: indA = pd.Index([1, 3, 5, 7, 9])
    ...: indB = pd.Index([2, 3, 5, 7, 11])

In [34]: indA & indB
Out[34]: Int64Index([3, 5, 7], dtype='int64')

In [35]: indA | indB
Out[35]: Int64Index([1, 2, 3, 5, 7, 9, 11], dtype='int64')
# 
In [36]: indA ^ indB
Out[36]: Int64Index([1, 2, 9, 11], dtype='int64')
# 也可以通过对象接口方法实现
In [37]: indA.intersection(indB)
Out[37]: Int64Index([3, 5, 7], dtype='int64')
```