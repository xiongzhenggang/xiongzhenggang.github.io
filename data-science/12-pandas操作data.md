### Pandas 数据处理

NumPy的基本要素之一是能够执行基本元素快速操作，包括基本算术（加法，减法，乘法等）以及更复杂的操作（三角函数，指数和对数函数等）。 Pandas继承了NumPy的大部分功能，而我们在NumPy数组计算中引入的ufuncs：[通用函数](./4-Numpy通用函数.md)。

Pandas有一些有用的变化，但是：对于一元运算（例如求反和三角函数），这些ufunc会在输出中保留索引和列标签；对于二进制运算（例如加法和乘法），Pandas在将对象传递到时会自动对齐索引ufunc。这意味着保持数据的上下文并结合来自不同来源的数据（包括潜在的容易出错的任务和原始的NumPy数组）都将成为使用Pandas的万无一失的工具。我们还将看到一维Series结构和二维DataFrame结构之间存在定义明确的操作。

### ufunc索引保存
由于Pandas设计为可与NumPy一起使用，因此任何NumPy ufunc均可在Pandas Series和DataFrame对象上使用。让我们开始定义一个简单的Series和DataFrame来演示这一点：
```py
In [1]: import pandas as pd
   ...: import numpy as np
In [2]: rng = np.random.RandomState(42)
   ...: ser = pd.Series(rng.randint(0, 10, 4))
In [3]: ser
Out[3]: 
0    6
1    3
2    7
3    4
dtype: int32
##
In [11]: df = pd.DataFrame(rng.randint(0,10,(3,4)),columns=('A','B','C','D'))
In [12]: df
Out[12]: 
   A  B  C  D
0  7  4  3  7
1  7  2  5  4
2  1  7  5  1
```
如果我们对这些对象之一应用NumPy ufunc，则结果将是另一个保留了索引的Pandas对象：
```py
In [33]: np.exp(ser)
Out[33]: 
0     403.428793
1      20.085537
2    1096.633158
3      54.598150
dtype: float64
```
或者一些更复杂的运算
```py
In [34]: np.sin(df*np.pi/4)
Out[34]: 
          A             B         C             D
0 -0.707107  1.224647e-16  0.707107 -7.071068e-01
1 -0.707107  1.000000e+00 -0.707107  1.224647e-16
2  0.707107 -7.071068e-01 -0.707107  7.071068e-01
```
其他numpy的通用函数都可以适用
### Ufunc 索引对齐
对于对两个Series或DataFrame对象的二进制操作，Pandas将在执行操作的过程中对齐索引。在处理不完整的数据时，这非常方便，如下面的示例所示

#### Series 索引对齐
```py
In [35]: area = pd.Series({'Alaska': 1723337, 'Texas': 695662,
    ...:                   'California': 423967}, name='area')
    ...: population = pd.Series({'California': 38332521, 'Texas': 26448193,
    ...:                         'New York': 19651127}, name='population')

In [36]: population / area
Out[36]: 
Alaska              NaN
California    90.413926
New York            NaN
Texas         38.018740
dtype: float64
```
结果数组包含两个输入数组的索引的并集，可以使用标准Python集合算术对这些索引进行确定：
```py
In [37]: area.index|population.index
Out[37]: Index(['Alaska', 'California', 'New York', 'Texas'], dtype='object')
```
任何一项都没有条目的项目都标有NaN或“不是数字”，这是Pandas标记缺失数据的方式。对于任何Python的内置算术表达式，都是通过这种方式实现索引匹配的；默认情况下，所有缺少的值都用NaN填充：
```py
In [38]: A = pd.Series([2, 4, 6], index=[0, 1, 2])
    ...: B = pd.Series([1, 3, 5], index=[1, 2, 3])
In [39]: A+B
Out[39]: 
0    NaN
1    5.0
2    9.0
3    NaN
dtype: float64
```
如果使用NaN值不是理想结果的话，可以使用适当的对象方法代替运算符来修改填充值。例如，调用A.add（B）等效于调用A + B，但允许对A或B中可能缺少的任何元素的填充值进行可选的显式指定：
```py
In [40]: A.add(B,fill_value=0)
Out[40]: 
0    2.0
1    5.0
2    9.0
3    5.0
dtype: float64
```
#### DataFrame 索引对其
对DataFrame执行操作时，列和索引都会发生类似的对齐方式：
```py
In [45]:A = pd.DataFrame(rng.randint(0, 20, (2, 2)),
    ...:                  columns=list('AB'))
    ...: B = pd.DataFrame(rng.randint(0, 10, (3, 3)),
    ...:                  columns=list('BAC'))
In [46]: A
Out[46]: 
    A   B
0   0  11
1  11  16

In [47]: B
Out[47]: 
   B  A  C
0  9  2  6
1  3  8  2
2  4  2  6
```
```py
In [48]: A+B
Out[48]: 
      A     B   C
0   2.0  20.0 NaN
1  19.0  19.0 NaN
2   NaN   NaN NaN
```
与Series一样，我们可以使用关联对象的算术方法，并传递任何所需的fill_value来代替缺少的条目。在这里，我们将填充A中所有值的平均值（通过首先堆叠A的行来计算）：
```py
In [53]: fill = A.stack().mean()
    ...: A.add(B, fill_value=fill)
Out[53]: 
      A     B     C
0   2.0  20.0  15.5
1  19.0  19.0  11.5
2  11.5  13.5  15.5
```
下表列出了Python运算符及其等效的Pandas对象方法：
```
+ 	add()
- 	sub(), subtract()
* 	mul(), multiply()
/ 	truediv(), div(), divide()
// 	floordiv()
% 	mod()
** 	pow()
```
#### Ufunc 功能：DataFrame和Series之间的操作
在DataFrame和Series之间执行操作时，将类似地维护索引和列对齐。 DataFrame和Series之间的操作类似于二维NumPy数组和一维NumPy数组之间的操作。考虑一个常见的操作，我们在其中发现二维数组及其行之一的区别：
```py
In [54]: A = rng.randint(10, size=(3, 4))
    ...: A
Out[54]: 
array([[4, 8, 6, 1],
       [3, 8, 1, 9],
       [8, 9, 4, 1]])

In [55]: A - A[0]
Out[55]: 
array([[ 0,  0,  0,  0],
       [-1,  0, -5,  8],
       [ 4,  1, -2,  0]])
```
根据NumPy的广播规则（请参阅数组计算：[广播](./5-Numpy数组广播.md)），二维数组与其行之一之间的减法是逐行应用的。
在Pandas中，约定默认情况下类似地按行操作：
```py
In [56]: df = pd.DataFrame(A, columns=list('QRST'))
    ...: df - df.iloc[0]
Out[56]: 
   Q  R  S  T
0  0  0  0  0
1 -1  0 -5  8
2  4  1 -2  0
```
如果要改为按列操作，则可以在指定axis关键字的同时使用前面提到的对象方法：
```py
In [57]: df.subtract(df['Q'],axis=0)
Out[57]: 
   Q  R  S  T
0  0  4  2 -3
1  0  5 -2  6
2  0  1 -4 -7
```
与上述操作类似，这些DataFrame / Series操作将自动在两个元素之间对齐索引：
```py
In [64]: halfrow=df.iloc[0,::2]

In [65]: halfrow
Out[65]: 
Q    4
S    6
Name: 0, dtype: int32

In [71]: df - halfrow
Out[71]: 
     Q   R    S   T
0  0.0 NaN  0.0 NaN
1 -1.0 NaN -5.0 NaN
2  4.0 NaN -2.0 NaN
```
索引和列的保留和对齐意味着对Pandas中的数据进行的操作将始终保持数据上下文，从而避免了在处理原始NumPy数组中的异构数据和/或未对齐数据时可能出现的错误类型。