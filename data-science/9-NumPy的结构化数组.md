### NumPy的结构化数组
虽然通常我们的数据可以用同构的值数组很好地表示，但有时情况并非如此。本节演示了NumPy的结构化数组和记录数组的使用，它们为复合异构数据提供了有效的存储。虽然此处显示的模式对于简单操作很有用，但类似的场景通常适合使用Pandas数据框，

假设很多人都拥有几类数据（例如，姓名，年龄和体重），并且我们想要存储这些值以供Python程序使用。可以将它们存储在三个单独的数组中：
```py
In [1]: import numpy as np
In [2]: name = ['Alice', 'Bob', 'Cathy', 'Doug']
   ...: age = [25, 45, 37, 19]
   ...: weight = [55.0, 85.5, 68.0, 61.5]
```
但是这种做法太愚蠢了，因为这三个数组没有直接的联系，如果我们可以使用单个结构来存储所有这些数据，那将更加自然。 NumPy可以通过结构化数组（具有复合数据类型的数组）来处理此问题。

>In [3]: x = np.zeros(4, dtype=int)

类似的我们可以使用复合数据类型规范类似地创建结构化数组：

```py
In [5]: data = np.zeros(4,dtype={'names':('name','age','weight'),'formats':('U10','i4','f8')})

In [6]: data
Out[6]: 
array([('', 0, 0.), ('', 0, 0.), ('', 0, 0.), ('', 0, 0.)],
      dtype=[('name', '<U10'), ('age', '<i4'), ('weight', '<f8')])
```
* 在这里，“ U10”为“最大长度为10的Unicode字符串”，“ i4”转换为“ 4字节（即32位）整数”，而“ f8”转换为“ 8字节（即64位）浮点数” 
 
可以用之前的数组值列表填充该数组：
```py
In [7]: data['name']=name
In [8]: data['age']=age
In [9]: data['weight']=weight
# 如果没没有定义则会报错
In [10]: data['xxx']=weight
---------------------------------------------------------------------------
ValueError                                Traceback (most recent call last)
<ipython-input-10-02ced290f531> in <module>
----> 1 data['xxx']=weight
ValueError: no field of name xxx
# 限制data已经将数据关联起来了
In [11]: data
Out[11]: 
array([('Alice', 25, 55. ), ('Bob', 45, 85.5), ('Cathy', 37, 68. ),
       ('Doug', 19, 61.5)],
      dtype=[('name', '<U10'), ('age', '<i4'), ('weight', '<f8')])
```
当然也可以对data对相应数组的操作，例如取第二个的年龄
```py
In [19]: data[1]['age']
Out[19]: 45
```

* 请注意，如果您想执行任何比这更复杂的操作，则可能应该考虑下一章介绍的Pandas软件包。正如我们将看到的，Pandas提供了一个Dataframe对象，该对象是在NumPy数组上构建的结构，它提供了许多有用的数据操作功能，这些功能远比我们展示的多。
### 创建结构化数组
 
结构化数组数据类型可以通过多种方式指定。之前，我们看到了字典方法：
>np.dtype({'names':('name', 'age', 'weight'),'formats':('U10', 'i4', 'f8')})


为了清楚起见，可以使用Python类型或NumPy dtypes来指定数字类型：
```py
In [20]: np.dtype({'names':('name', 'age', 'weight'),'formats':((np.str_, 10), int, np.float32)})
Out[20]: dtype([('name', '<U10'), ('age', '<i4'), ('weight', '<f4')])
```
 
复合类型也可以指定为元组列表：
```py
In [22]: np.dtype([('name', 'S10'), ('age', 'i4'), ('weight', 'f8')])
Out[22]: dtype([('name', 'S10'), ('age', '<i4'), ('weight', '<f8')])
```
如果类型的名称对您而言无关紧要，则可以在逗号分隔的字符串中单独指定类型：
```py
In [24]: np.dtype('S10,i4,f8')
Out[24]: dtype([('f0', 'S10'), ('f1', '<i4'), ('f2', '<f8')])
```
缩短的字符代之类型可能会看起来迷惑，但是它们是基于简单的原理构建的。第一个（可选）字符为<或>，分别表示“小段模式”或“ 大端模式”，并指定有效位的排序约定。下一个字符指定数据类型：字符，字节，整数，浮点数等（请参见下表）。最后一个或多个字符表示对象的大小（以字节为单位）。
```
Character 	Description 	Example
'b' 	Byte 	np.dtype('b')
'i' 	Signed integer 	np.dtype('i4') == np.int32
'u' 	Unsigned integer 	np.dtype('u1') == np.uint8
'f' 	Floating point 	np.dtype('f8') == np.int64
'c' 	Complex floating point 	np.dtype('c16') == np.complex128
'S', 'a' 	String 	np.dtype('S5')
'U' 	Unicode string 	np.dtype('U') == np.str_
'V' 	Raw data (void) 	np.dtype('V') == np.void
```
###  更高级的化合物类型

可以定义更高级的混合类型。例如，您可以创建一个类型，其中每个元素都包含数组或矩阵值。这里，我们将创建一个数据类型，该数据类型具有一个由3×3浮点矩阵组成的mat值
```py
In [26]: tp=np.dtype([('id','i8'),('mat','f8',(3,3))])
In [27]: X=np.zeros(1,dtype=tp)
In [28]: X
Out[28]: 
array([(0, [[0., 0., 0.], [0., 0., 0.], [0., 0., 0.]])],
      dtype=[('id', '<i8'), ('mat', '<f8', (3, 3))]) 
#mat 是可以包含一个3*3的数组,
In [30]: print(X['mat'][0])
[[0. 0. 0.]
 [0. 0. 0.]
 [0. 0. 0.]]
In [44]: X['mat'].shape
Out[44]: (1, 3, 3)
```
 
现在，X数组中的每个元素都包含一个id和一个3×3矩阵。为什么要使用此而不是简单的多维数组或Python字典？原因是此NumPy dtype直接映射到C结构定义，因此可以在适当编写的C程序中直接访问包含数组内容的缓冲区。如果你发现自己继承C或Fortran库编写Python接口去处理结构化数据的，那么可能会觉得结构化数组非常有用！

### RecordArrays：具有扭曲的结构化数组
NumPy还提供了np.recarray类，该类与刚刚描述的结构化数组几乎相同，但是具有一个附加功能：字段可以作为属性而不是作为字典键来访问。回想一下我们以前通过写以下文字来访问时代：
```py
In [45]: data['age']
Out[45]: array([25, 45, 37, 19])
```
如果我们将他作为record array替代的话，访问的操作会更简洁：
```py
In [46]: data_rec=data.view(np.recarray)
In [47]: data_rec.age
Out[47]: array([25, 45, 37, 19])
In [48]: data_rec.name
Out[48]: array(['Alice', 'Bob', 'Cathy', 'Doug'], dtype='<U10')
```
缺点是对于记录数组，即使使用相同的语法，访问字段也会涉及一些额外的开销。我们可以在这里看到：
```py
In [49]: %timeit data['age']
    ...: %timeit data_rec['age']
    ...: %timeit data_rec.age
199 ns ± 18 ns per loop (mean ± std. dev. of 7 runs, 10000000 loops each)
2.98 µs ± 326 ns per loop (mean ± std. dev. of 7 runs, 100000 loops each)
4.31 µs ± 768 ns per loop (mean ± std. dev. of 7 runs, 100000 loops each)
```
可以看的record array的操作相对结构数组耗时上了一个数量级

最后关于结构化数组和记录数组的这一节，因为它很好地介绍了我们将要介绍的下一个软件包：Pandas。在某些情况下，如此处讨论的结构化数组很容易了解，特别是在您使用NumPy数组映射到C，Fortran或另一种语言的二进制数据格式的情况下。对于结构化数据的日常使用，Pandas软件包是一个更好的选择，我们将在随后的章节中讨论它。