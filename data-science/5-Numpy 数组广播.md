###  广播

广播允许在不同大小的数组上执行加减乘除的二进制运算 例如

```py
In [1]: import numpy as np

In [2]: a = np.array([0, 1, 2])
   ...: b = np.array([5, 5, 5])

In [3]: a*b
Out[3]: array([ 0,  5, 10])
```
NumPy广播的优点是在复制值得过程中没有占用额外得空间，但是在我们考虑广播时，它是一种有用的思维模型。
例如如下对三维数组数值扩展
```
In [8]: m=np.ones((3,3))
In [9]: 3+m
Out[9]: 
array([[4., 4., 4.],
       [4., 4., 4.],
       [4., 4., 4.]])
```
两个数组相加扩展
```py
In [17]: a = np.arange(3)
    ...: b = np.arange(3)[:, np.newaxis]
    ...: print(a)
    ...: print(b)
[0 1 2]
[[0]   
 [1]   
 [2]]  
# 两个数组相加（注意数组非矩阵）
In [18]:a + b
Out[18]: 
array([[0, 1, 2],
       [1, 2, 3],
       [2, 3, 4]])
```
就像我们拉伸或广播一个值以匹配另一个值的形状一样，这里拉伸了a和b以匹配一个通用形状，结果是一个二维数组！
下图显示了这些示例的几何形状（可以在附录中找到生成该图的代码，并改编自astroML文档中发布的源）。
![图片.png](https://jakevdp.github.io/PythonDataScienceHandbook/figures/02.05-broadcasting.png)


