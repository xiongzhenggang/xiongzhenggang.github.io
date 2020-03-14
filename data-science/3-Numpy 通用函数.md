### numpy 对数组的操作效率

>NumPy数组上的计算可能非常快，也可能非常慢。快速实现的关键是使用矢量化操作，通常通过NumPy的通用函数（ufuncs）实现。

#### 慢循环
```
 
Python的默认实现（CPython）执行某些操作的速度非常慢。这是由于语言的动态，解释性所致：
类型具有灵活性，因此无法像C和Fortran这样的语言将操作序列编译成有效的机器代码。最近，人们进行了各种尝试来解决这一弱点：著名的例子是PyPy项目，
它是Python的实时编译实现。 Cython项目，该项目将Python代码转换为可编译的C代码；还有Numba项目，该项目将Python代码段转换为快速LLVM字节码。
每种方法都有其优点和缺点，但是可以肯定地说，这三种方法都没有超越标准CPython引擎的范围和普及性。
```
* Python的相对呆板缓慢的操作，通常可以体现在一些重复的小操作中，下面展示

```ipyno
In [1]: import numpy as np
In [2]: np.random.seed(0)
In [3]: def compute_rec(values):
   ...:     output=np.empty(len(values))
   ...:     for i in range(len(values)):
   ...:         output[i]=1.0/values[i]
   ...:     return output
   ...: 

In [4]: values = np.random.randint(1, 10, size=5)
In [5]: compute_rec(values)
Out[5]: array([0.16666667, 1.        , 0.25      , 0.25      , 0.125     ])
# 小数组可以看到耗时很小只有12.2 µs左右
In [6]: %timeit  compute_rec(values)
12.2 µs ± 198 ns per loop (mean ± std. dev. of 7 runs, 100000 loops each)
# 这里我们创建一个大数组来看下
In [7]: big_arr = np.random.randint(1, 100, size=1000000)
# 当随着数组变大竟然耗时2.52 s左右，这相对其他静态语言实在太慢了
In [8]: %timeit compute_rec(big_arr)
2.52 s ± 235 ms per loop (mean ± std. dev. of 7 runs, 1 loop each)
```
计算这百万个操作并存储结果需要几秒钟！甚至现在的手机的处理速度都以Giga-FLOPS衡量时（即每秒数十亿次数字运算）。
不过事实证明，这里的瓶颈不是操操作系统作本身，而是CPython在循环的每个循环中必须执行的类型检查和函数分派。
每次计算倒数时，Python都会首先检查对象的类型，并动态查找要用于该类型的正确函数。如果我们使用的是已编译的代码（静态语言的优势），则在代码执行之前便会知道此类型规范，并且可以更有效地计算结果。

那我们有什么办法可以再这种情况下提高执行效率吗？ 当然，这里我们就用到了numpy的Ufuncs 操作

#### Ufunc 

>对于许多类型的操作，NumPy仅为此类静态类型的已编译例程提供了方便的接口。这称为向量化操作。这可以通过简单地对数组执行操作来实现，然后将其应用于每个元素。这种矢量化方法旨在将循环推入NumPy底层的编译层，从而大大提高了执行速度。

比较下面两种操作：

```ipyno
In [9]: compute_rec(values)
Out[9]: array([0.16666667, 1.        , 0.25      , 0.25      , 0.125     ])

In [10]: 1.0/values
Out[10]: array([0.16666667, 1.        , 0.25      , 0.25      , 0.125     ])
···
说明可以直接除法操作可以直接作用再数组上，那我们再比较下对大数组操作的耗时时间

```ipyno
In [15]: %timeit (1.0 / big_arr)
5.25 ms ± 129 µs per loop (mean ± std. dev. of 7 runs, 100 loops each)
```
* 执行时间几乎降低了三个数量级

NumPy中的矢量化操作是通过ufunc实现的，其主要目的是对NumPy数组中的值快速执行重复的操作。 Ufunc非常灵活–在我们看到标量和数组之间的操作之前.我们也可以在两个数组之间进行操作：
```ipyno
In [18]: np.arange(5) / np.arange(1,6)
# 每个对应的元素想除，要保证两个数组size保持一致
Out[18]: array([0.        , 0.5       , 0.66666667, 0.75      , 0.8       ])
```

而且ufunc操作不仅限于一维数组-它们还可以作用于多维数组：
```ipyno
In [26]: x = np.arange(9).reshape((3, 3))
    ...: 2 ** x
Out[26]: 
array([[  1,   2,   4],
       [  8,  16,  32],
       [ 64, 128, 256]], dtype=int32)
```
通过ufunc使用矢量化的计算几乎总是比使用Python循环实现的计算效率更高，尤其是随着数组大小的增加。每当在Python脚本中看到这样的循环时，都应该考虑是否可以将其替换为向量化表达式。
#### Ufuncs 更多应用

>Ufunc有两种形式：一元ufunc（在单个输入上运行）和二元ufunc（在两个输入上运行）。我们将在这里看到这两种功能的示例。


##### 数组算术
NumPy的ufunc使用起来非常自然，因为它们利用了Python的本机算术运算符。可以使用标准的加，减，乘和除法：
```ipyno
In [27]: x = np.arange(4)
    ...: print("x     =", x)
    ...: print("x + 5 =", x + 5)
    ...: print("x - 5 =", x - 5)
    ...: print("x * 2 =", x * 2)
    ...: print("x / 2 =", x / 2)
    ...: print("x // 2 =", x // 2)  # floor division
x     = [0 1 2 3]
x + 5 = [5 6 7 8]
x - 5 = [-5 -4 -3 -2]
x * 2 = [0 2 4 6]
x / 2 = [0.  0.5 1.  1.5]
x // 2 = [0 0 1 1]
```
我们甚至可以将数组当作变量参与运算
```
In [30]: (x+2)*3
Out[30]: array([ 6,  9, 12, 15])
```
这些便捷的操作符很多都时依赖相应的方法，如下
```ipy
In [31]: x+2
Out[31]: array([2, 3, 4, 5])

In [32]: np.add(x,2)
Out[32]: array([2, 3, 4, 5])
```
下面时numpy的操作符对应的方法
```
+ 	np.add 	Addition (e.g., 1 + 1 = 2)
- 	np.subtract 	Subtraction (e.g., 3 - 2 = 1)
- 	np.negative 	Unary negation (e.g., -2)
* 	np.multiply 	Multiplication (e.g., 2 * 3 = 6)
/ 	np.divide 	Division (e.g., 3 / 2 = 1.5)
// 	np.floor_divide 	Floor division (e.g., 3 // 2 = 1)
** 	np.power 	Exponentiation (e.g., 2 ** 3 = 8)
% 	np.mod 	Modulus/remainder (e.g., 9 % 4 = 1)
```
#### 绝对值
```ipy
In [33]: x = np.array([-2, -1, 0, 1, 2])
    ...: abs(x)
Out[33]: array([2, 1, 0, 1, 2])
```
* 这里的abs就是np.absolute的别名，也可以使用np.absolute(x)或者np.abs(x)
 当数组为复数时，绝对值则取的时复数的模（大小）
 ```ipy
 In [36]: np.abs(x)
Out[36]: array([2.23606798, 3.60555128, 6.70820393])
```
#### 三角函数
NumPy提供了大量有用的函数，三角函数是对数据科学家最有用的一些函数。我们将从定义一个角度数组开始：
从0-pi 截取三个点
```
In [45]: theta = np.linspace(0, np.pi, 3)
In [46]: theta
Out[46]: array([0.        , 1.57079633, 3.14159265])
# 计算 o pi/2 和 pi 的sin cos tan 值
In [47]: print("sin(theta) = ", np.sin(theta))
    ...: print("cos(theta) = ", np.cos(theta))
    ...: print("tan(theta) = ", np.tan(theta))
sin(theta) =  [0.0000000e+00 1.0000000e+00 1.2246468e-16]
cos(theta) =  [ 1.000000e+00  6.123234e-17 -1.000000e+00]
tan(theta) =  [ 0.00000000e+00  1.63312394e+16 -1.22464680e-16]
```
同样也可以计算反三角函数 np.arcsin(x) np.arccos(x) np.arctan(x)

#### 指数和对数

