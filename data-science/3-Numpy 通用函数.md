### numpy 对数组的操作效率
NumPy数组上的计算可能非常快，也可能非常慢。快速实现的关键是使用矢量化操作，通常通过NumPy的通用函数（ufuncs）实现。

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

那我们有什么办法可以再这种情况下提高执行效率吗？ 当然，这里我们就用到了numpy的Ufuncs的函数
#### Ufunc

  对于许多类型的操作，NumPy仅为此类静态类型的已编译例程提供了方便的接口。这称为向量化操作。这可以通过简单地对数组执行操作来实现，
然后将其应用于每个元素。这种矢量化方法旨在将循环推入NumPy底层的编译层，从而大大提高了执行速度。
