###  NumPy
1. python list和Numpy的list
```ipyno
In [10]: L3 = [True, "2", 3.0, 4]
In [11]: [type(item) for item in L3]
Out[11]: [bool, str, float, int]
```
python这种灵活性是要付出一定的代价：要允许这些灵活的类型，列表中的每个项目都必须包含自己的类型信息，引用计数和其他信息-也就是说，每个项目都是一个完整的Python对象。在所有变量都是同一类型的特殊情况下，许多信息都是多余的：将数据存储在固定类型的数组中会更加有效。下图说明了动态类型列表和固定类型（NumPy样式）数组之间的区别：
![图片.png](https://upload-images.jianshu.io/upload_images/7779493-d53d33c02f0147c5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
从上面实现可以看到，数组本质上包含一个指向一个连续数据块的指针。另一方面，Python列表包含一个指向一组指针的指针，每个指针都指向一个完整的Python对象，例如我们之前看到的Python整数。同样，python列表的优点是灵活性：由于每个列表元素都是包含数据和类型信息的完整结构，因此可以用任何所需类型的数据填充列表。固定类型的NumPy样式的数组缺乏这种灵活性，但是在存储和处理数据方面效率更高。

 
Python提供了几种不同的选项来将数据存储在高效的固定类型数据缓冲区中。内置的数组模块（自Python 3.3起可用）可用于创建统一类型的密集数组：
```ipyno
In [12]: 
    ...: import array
    ...: L = list(range(10))
    ...: A = array.array('i', L)
    ...: A
    ...: 
    ...: 
Out[12]: array('i', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
```
>这里的i表示数组类型是integer

ython的数组对象提供了基于数组的数据的有效存储，但是NumPy包的ndarray对象更加有用，NumPy对该数据进行了有效的操作。我们将在后面的部分中探讨这些操作。下面，我们将演示创建NumPy数组的几种方法。
```ipyno
In [14]: import numpy as np
In [15]: np.array([1,3,5,9])
Out[15]: array([1, 3, 5, 9])
```
与Python列表不同的是，NumPy限于所有包含相同类型的数组。如果类型不匹配，则NumPy将在可能的情况下向上转换（此处，向上转换为浮点型整数）甚至会转换Unicode类型如下：
```ipyno

In [16]: np.array([1,3,5,'3'])
Out[16]: array(['1', '3', '5', '3'], dtype='<U11')

In [17]: np.array([3.14, 4, 2, 3])
Out[17]: array([3.14, 4.  , 2.  , 3.  ])
# 指定类型
In [18]: np.array([1, 2, 3, 4], dtype='float32')
Out[18]: array([1., 2., 3., 4.], dtype=float32)
```
另外numpy的array还可以创建多维数组
```ipyno
In [19]: np.array([range(i,i+4) for i in [1,2,3]])
Out[19]: 
array([[1, 2, 3, 4],
       [2, 3, 4, 5],
       [3, 4, 5, 6]])
```
特别是对于较大的数组，使用内置在NumPy中使用常规从头开始创建数组会更有效率。下面几个例子说明
```ipyno
In [20]: # 长度为10，默认填充0的int类型数组
    ...: np.zeros(10, dtype=int)
Out[20]: array([0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
#创建默认为1 的3*5 float类型的数组
In [21]: np.ones((3,5),dtype=float)
Out[21]: 
array([[1., 1., 1., 1., 1.],
       [1., 1., 1., 1., 1.],
       [1., 1., 1., 1., 1.]])
In [22]: # Create a 3x5 array 指定使用 3.14填充
    ...: np.full((3, 5), 3.14)
Out[22]: 
array([[3.14, 3.14, 3.14, 3.14, 3.14],
       [3.14, 3.14, 3.14, 3.14, 3.14],
       [3.14, 3.14, 3.14, 3.14, 3.14]])
# 创建从1到20步长2的数组
In [24]: np.arange(1,10,2)
Out[24]: array([1, 3, 5, 7, 9])

In [26]: #从0到1的长度，取间隔相同的四个点
    ...: np.linspace(0, 1, 4)
Out[26]: array([0.        , 0.33333333, 0.66666667, 1.        ])

In [29]: # Create a 3x3 array of uniformly distributed
    ...: # random values between 0 and 1
    ...: np.random.random((3, 3))
Out[29]: 
array([[0.42978944, 0.54364465, 0.54672853],
       [0.51082736, 0.97954524, 0.80516577],
       [0.50711922, 0.71589174, 0.85489927]])
In [31]: # 0-10 3*3 的随机整数
    ...: np.random.randint(0, 10, (3, 3))
Out[31]: 
array([[2, 4, 6],
       [5, 4, 6],
       [6, 9, 7]])

In [35]: #返回一个2维数组，对角线上1，其他位置为零。
    ...: np.eye(2, dtype=int)
Out[35]: 
array([[1, 0],
       [0, 1]])

```
2.  NumPy 的标准类型
  
NumPy数组包含单个类型的值。因为NumPy是用C内置的，所以C，Fortran和其他相关语言的用户会熟悉这些类型。

> 在创建NumPy 数据可以指定类型
```ipyno
In [39]: np.zeros(3,dtype='int8')
Out[39]: array([0, 0, 0], dtype=int8)
```
或者用numpy指定
```ipyno
In [40]: np.zeros(3,dtype=np.int8)
Out[40]: array([0, 0, 0], dtype=int8)
```
具体类型可参考：
```
Data type 	Description
bool_ 	Boolean (True or False) stored as a byte
int_ 	Default integer type (same as C long; normally either int64 or int32)
intc 	Identical to C int (normally int32 or int64)
intp 	Integer used for indexing (same as C ssize_t; normally either int32 or int64)
int8 	Byte (-128 to 127)
int16 	Integer (-32768 to 32767)
int32 	Integer (-2147483648 to 2147483647)
int64 	Integer (-9223372036854775808 to 9223372036854775807)
uint8 	Unsigned integer (0 to 255)
uint16 	Unsigned integer (0 to 65535)
uint32 	Unsigned integer (0 to 4294967295)
uint64 	Unsigned integer (0 to 18446744073709551615)
float_ 	Shorthand for float64.
float16 	Half precision float: sign bit, 5 bits exponent, 10 bits mantissa
float32 	Single precision float: sign bit, 8 bits exponent, 23 bits mantissa
float64 	Double precision float: sign bit, 11 bits exponent, 52 bits mantissa
complex_ 	Shorthand for complex128.
complex64 	Complex number, represented by two 32-bit floats
complex128 	Complex number, represented by two 64-bit floats
```
