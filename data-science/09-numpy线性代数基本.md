
## numpy 线性代数的应用
* numpy.linalg 线性方程方法
```
dot	两个数组的点积，即元素对应相乘。
vdot	两个向量的点积
inner	两个数组的内积
matmul	两个数组的矩阵积
determinant	数组的行列式
solve	求解线性矩阵方程
inv	计算矩阵的乘法逆矩阵
```
### 简单数组运算
1. 转置矩阵 transpose
```py
 import numpy as np
 In [18]: a = array([[1.0, 2.0], [3.0, 4.0]])
    ...: print (a)
[[1. 2.]
 [3. 4.]]
In [19]: a.transpose()
Out[19]: 
array([[1., 3.],
       [2., 4.]])
```
2. 单位矩阵 
```py
In [27]: u = eye(2) # 
    ...: print(u)
[[1. 0.]
 [0. 1.]]
```
3. 矩阵乘法 dot
```py
In [28]: c=np.arange(4).reshape((2,2))

In [29]: c
Out[29]: 
array([[0, 1],
       [2, 3]])
In [31]: c.dot(c)
Out[31]: 
array([[ 2,  3],
       [ 6, 11]])
```
4. 矩阵的迹trace
```py
In [32]: np.trace(c)
Out[32]: 3
```
5. 求解行列式 np.linalg.det
```py
In [34]: c
Out[34]: 
array([[0, 1],
       [2, 3]])
In [41]: np.linalg.det(c)
Out[41]: -2.0
```
6. 逆矩阵 linalg.inv
```py
In [34]: c
Out[34]: 
array([[0, 1],
       [2, 3]])

In [35]: np.linalg.inv(c)
Out[35]: 
array([[-1.5,  0.5],
       [ 1. ,  0. ]])
```
7. 特征值和特征向量 linalg.eig（n阶方阵存在λ 存在非零列向量α  使得A*α=λ*α => （λ*E-A)α=0 存在非零解的冲要条件为 系数行列式|λ*E-A|=0，那么λ为特征值 α为特征向量)
返回特征值和特征向量
```py
In [40]: eigvalue, eigvector = np.linalg.eig(c)
    ...: print (eigvalue, eigvector)
[-0.56155281  3.56155281]
 [[-0.87192821 -0.27032301]
 [ 0.48963374 -0.96276969]]
```
8. 求解线性方程
假设方程
x + y + z = 6
2y + 5z = -4
2x + 5y - z = 27
用矩阵表示为 AX = B
或
X = A^(-1)B
求解如下
```py
In [42]: A = np.array([[1,1,1],[0,2,5],[2,5,-1]])

In [43]: A
Out[43]: 
array([[ 1,  1,  1],
       [ 0,  2,  5],
       [ 2,  5, -1]])

In [44]: B = np.array([[6],[-4],[27]])
In [45]: x = np.linalg.solve(A,B) # x=A^(-1)B
In [46]: x
Out[46]: 
array([[ 5.],
       [ 3.],
       [-2.]])
```
### 矩阵类
直接使用矩阵类来操作
1. 创建矩阵
```py
In [52]: A = np.matrix('1.0 2.0; 3.0 4.0')

In [53]: A
Out[53]: 
matrix([[1., 2.],
        [3., 4.]])
In [54]: B = np.matrix(c)

In [55]: c
Out[55]: 
array([[0, 1],
       [2, 3]])

In [56]: B
Out[56]: 
matrix([[0, 1],
        [2, 3]])
```
2. 转置矩阵
```py
In [61]: A
Out[61]: 
matrix([[1., 2.],
        [3., 4.]])

In [62]: A.T
Out[62]: 
matrix([[1., 3.],
        [2., 4.]])
```
3. 乘法
```py
In [63]: X = matrix('5.0 7.0')
    ...: Y = X.T
    ...: Y
Out[63]: 
matrix([[5.],
        [7.]])

In [64]: X*Y
Out[64]: matrix([[74.]])
```
4. 矩阵的逆矩阵
```py
In [76]: A
Out[76]: 
matrix([[1., 2.],
        [3., 4.]])

In [77]: A.dot(A.I)
Out[77]: 
matrix([[1.0000000e+00, 0.0000000e+00],
        [8.8817842e-16, 1.0000000e+00]])
```
5. 解线性方程组
```py
In [79]: y=np.matrix(np.array([[3],[4]]))

In [80]: y
Out[80]: 
matrix([[3],
        [4]])

In [81]: A
Out[81]: 
matrix([[1., 2.],
        [3., 4.]])

In [82]: np.linalg.solve(A,y)
Out[82]: 
matrix([[-2. ],
        [ 2.5]])
```