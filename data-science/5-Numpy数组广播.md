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
![图片.png](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/data-science/image/02.05-broadcasting.png)

这些图中额外的内存实际上并没有在操作过程中分配.这里时为了从概念理解。

### 广播得规则
 
NumPy中的广播遵循一套严格的规则来确定两个数组之间的交互：

    规则1：如果两个数组的维数不同，则维数较少的数组的形状将在其前（左侧）填充。
    规则2：如果两个数组的形状在任何维度上都不匹配，则将在该维度上形状等于1的数组拉伸以匹配其他形状。
    规则3：如果尺寸在任何维度上都不相同，且都不等于1，则会引发错误。
  
  ### 广播示例1
  下面详细来说明
  ```py
  In [23]: M = np.ones((2, 3))
    ...: a = np.arange(3)
  ```
* 首先创建得两个数组，M 为2行3列的二维数组，a为一个1行的一维数组
1. 首先根据规则1，我们看到数组a的维数较少，因此我们在数组的左侧填充了1维使其成为和M相同维度的二维数组：

    M.shape -> (2, 3)
    a.shape -> (1, 3)
2. 根据规则2，我们现在看到维度相同，但是尺寸不一致，因此我们拉伸该维度以使其匹配：

    M.shape -> (2, 3)
    a.shape -> (2, 3)
最终我们通过拉伸变换使其形状匹配，我们看到最终形状将是（2，3）：
```py
In [23]: M = np.ones((2, 3))
    ...: a = np.arange(3)
In [24]: M+a
Out[24]: 
array([[1., 2., 3.],
       [1., 2., 3.]])
```
### 广播示例2
让我们看下两个数组都需要拉伸变换来适应匹配的
```py
In [28]: a = np.arange(3).reshape((3, 1))
    ...: b = np.arange(3)
```
1. 首先我们创造一个，3*1的二维数组和一个一维数组

    a.shape = (3, 1)
    b.shape = (3,)
2. 规则1说我们必须填充b的形状使其形成二维数组（1行3列）：

    a.shape -> (3, 1)
    b.shape -> (1, 3)
 
3. 根据规则2，我们将每个升级，以匹配另一个数组的相应大小（都扩展成3*3的数组）：
```py
In [30]: a+b
Out[30]: 
array([[0, 1, 2],
       [1, 2, 3],
       [2, 3, 4]])
```
### 广播示例3
我们在看两个不匹配的数组
```py
In [31]: M = np.ones((3, 2))
    ...: a = np.arange(3)
    
 ```
考虑上面a和M，分析简略如下
首先a M

    M.shape = (3, 2)
    a.shape = (3,)

根据规则一，对a扩展成

    M.shape -> (3, 2)
    a.shape -> (1, 3)
根据规则2，对a 的行扩展

    M.shape -> (3, 2)
    a.shape -> (3, 3)

扩展后我们发现，两者不匹配执行
```py
In [32]: a+M
---------------------------------------------------------------------------
ValueError                                Traceback (most recent call last)
<ipython-input-32-60afc280ce5f> in <module>
```
*此处可能存在的混乱：可以想象通过将a的形状用右边而不是左边的形状填充来使a和M兼容。但这不是广播规则的工作方式！这种灵活性在某些情况下可能有用，但可能会导致歧义。如果想要右侧填充，则可以通过重塑数组来明确地做到这一点（我们将使用《 NumPy数组基础》中引入的np.newaxis关键字）：
```py
# 将a变换 成3*1的数组和M广播
In [34]: a[:, np.newaxis].shape
Out[34]: (3, 1)

In [35]: M + a[:, np.newaxis]
Out[35]: 
array([[1., 1.],
       [2., 2.],
       [3., 3.]])
```
*同样除了+ 还可以用于其他函数例如log等
### 广播操作练习

在上一节中，我们看到ufunc允许NumPy用户消除显式编写慢速Python循环的需要。广播扩展了此功能。一个常见的示例是将数据阵列居中时。假设您有一个包含10个观测值的数组，每个观测值包含3个值。，我们将其存储在10×3数组中：
```py
In [43]: a=np.arange(9).reshape((3,3))
In [44]: a
Out[44]:         
array([[0, 1, 2],
       [3, 4, 5],
       [6, 7, 8]])
#我们可以使用第一维上的均值合计来计算每个特征的均值：
In [46]: a.mean(0)
Out[46]: array([3., 4., 5.])
```
###绘制二维函数
广播非常有用的一个地方是基于二维函数显示图像。如果我们要定义一个函数z= f（x，y），可以使用广播来计算整个网格中的函数
这里我们用py代码执行
```py
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import numpy as np
#我们将使用Matplotlib绘制此二维数组（这些工具将在“密度和轮廓图”中进行全面讨论）：
import matplotlib.pyplot as plt
x=np.linspace(0,5,50)
y=np.linspace(0,5,50)[:,np.newaxis]
z=np.sin(x)**2 + np.cos(6+y*x)*np.cos(x)
plt.imshow(z, origin='lower', extent=[0, 5, 0, 5],cmap='viridis')
plt.colorbar();
plt.show()  #关键的地方
```
![z函数图像.png](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/data-science/image/Figure_1.png)
