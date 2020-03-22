## 合并数据集：Concat和Append
一些最有趣的数据研究来自组合不同的数据源。这些操作涉及从两个不同数据集的非常直接的串联到正确处理数据集之间任何重叠的更复杂的数据库样式的联接和合并。 Series和DataFrames是在考虑这种操作的基础上构建的，Pandas包括使此类数据快速而直接地整理的功能和方法。
 
在这里，我们将看一下使用pd.concat函数将Series和DataFrames进行简单连接。稍后，我们将深入研究在Pandas中实现的更复杂的内存中合并和联接。
```py
In [1]: import pandas as pd
   ...: import numpy as np
```
 
为了方便起见，我们将定义此函数，该函数创建特定形式的DataFrame，将在下面使用：
```py
In [8]: def make_df(cols, ind):
   ...:     """创建 DataFrame"""
   ...:     data = {c: [str(c) + str(i) for i in ind]
   ...:             for c in cols}
   ...:     return pd.DataFrame(data, ind)
   ...: make_df('ABC', range(4))
Out[8]: 
    A   B   C
0  A0  B0  C0
1  A1  B1  C1
2  A2  B2  C2
3  A3  B3  C3
```
另外，我们将创建一个快速类，该类允许我们并排显示多个DataFrame。该代码利用了特殊的_repr_html_方法，IPython使用该方法来实现其丰富对象的显示：
```py
class display(object):
    """Display HTML representation of multiple objects"""
    template = """<div style="float: left; padding: 10px;">
    <p style='font-family:"Courier New", Courier, monospace'>{0}</p>{1}
    </div>"""
    def __init__(self, *args):
        self.args = args
        
    def _repr_html_(self):
        return '\n'.join(self.template.format(a, eval(a)._repr_html_())
                         for a in self.args)
    
    def __repr__(self):
        return '\n\n'.join(a + '\n' + repr(eval(a))
                           for a in self.args)
```
### 回忆：NumPy数组的串联
Series和DataFrame对象的串联与Numpy数组的串联非常相似，这可以通过[ NumPy数组的基础](./3-Numpy数组.md)中讨论的np.concatenate函数来完成。回想一下，您可以将两个或多个数组的内容合并为一个数组：
```py
In [12]: x = [1, 2, 3]
    ...: y = [4, 5, 6]
    ...: z = [7, 8, 9]
    ...: np.concatenate([x, y, z])
Out[12]: array([1, 2, 3, 4, 5, 6, 7, 8, 9])
```
第一个参数是要连接的数组的列表或元组。此外，它还带有axis关键字，该关键字使您可以指定将结果串联的轴：
```py
In [13]: x=np.arange(4).reshape(2,2)
In [14]: x
Out[14]: 
array([[0, 1],
       [2, 3]])
In [15]: np.concatenate([x,x],axis=0)
Out[15]: 
array([[0, 1],
       [2, 3],
       [0, 1],
       [2, 3]])
```
### pd.concat的简单串联
Pandas有一个函数pd.concat（），其语法与np.concatenate类似，但包含许多选项，我们将在后面进行讨论：
```py
In [16]: ser1 = pd.Series(['A', 'B', 'C'], index=[1, 2, 3])
    ...: ser2 = pd.Series(['D', 'E', 'F'], index=[4, 5, 6])
    ...: pd.concat([ser1, ser2])
Out[16]: 
1    A
2    B
3    C
4    D
5    E
6    F
dtype: object
```
 
它还可以连接更高维的对象，例如DataFrames：
```
In [17]: df1 = make_df('AB', [1, 2])
    ...: df2 = make_df('AB', [3, 4])
    ...: display('df1', 'df2', 'pd.concat([df1, df2])')
Out[17]: 
df1      
    A   B
1  A1  B1
2  A2  B2

df2
    A   B
3  A3  B3
4  A4  B4

pd.concat([df1, df2])
    A   B
1  A1  B1
2  A2  B2
3  A3  B
```
### 重复索引
 
np.concatenate和pd.concat之间的一个重要区别是，即使结果将具有重复的索引，Pandas串联也会保留索引！考虑以下简单示例：
```py
In [24]: x = make_df('AB', [0, 1])
    ...: y = make_df('AB', [2, 3])
    ...: y.index = x.index  # make duplicate indices!
    ...: display('x', 'y', 'pd.concat([x, y])')
Out[24]: 
x
    A   B
0  A0  B0
1  A1  B1

y
    A   B
0  A2  B2
1  A3  B3

pd.concat([x, y])
    A   B
0  A0  B0
1  A1  B1
0  A2  B2
1  A3  B3
```
注意结果中重复的索引。尽管这在DataFrames中有效，但结果通常是不希望的。 pd.concat（）为我们提供了几种处理方法。
#### 捕捉重复为错误
如果您只想验证pd.concat（）结果中的索引不重叠，则可以指定verify_integrity标志。将此设置为True时，如果存在重复的索引，则串联将引发异常。这是一个示例，为清楚起见，我们将捕获并打印错误消息：
```py
In [25]: try:
    ...:     pd.concat([x, y], verify_integrity=True)
    ...: except ValueError as e:
    ...:     print("ValueError:", e)
    ...: 
ValueError: Indexes have overlapping values: Int64Index([0, 1], dtype='int64')
```
#### 忽略索引
有时索引本身并不重要，您希望将其简单地忽略掉。可以使用ignore_index标志指定此选项。将此设置为true时，串联将为所得的Series创建一个新的整数索引：
```py
In [26]: display('x', 'y', 'pd.concat([x, y], ignore_index=True)')
Out[26]: 
x        
    A   B
0  A0  B0
1  A1  B1

y
    A   B
0  A2  B2
1  A3  B3

pd.concat([x, y], ignore_index=True)
    A   B
0  A0  B0
1  A1  B1
2  A2  B2
3  A3  B3
```
#### 添加MultiIndex键

另一种选择是使用keys选项为数据源指定标签。结果将是包含数据的按层次结构索引的系列：
```py
In [27]: display('x', 'y', "pd.concat([x, y], keys=['x', 'y'])")
Out[27]: 
x
    A   B
0  A0  B0
1  A1  B1

y
    A   B
0  A2  B2
1  A3  B3

pd.concat([x, y], keys=['x', 'y'])
      A   B
x 0  A0  B0
  1  A1  B1
y 0  A2  B2
  1  A3  B3
```
结果是一个倍增索引的DataFrame，我们可以使用“分层索引”中讨论的工具将这些数据转换为我们感兴趣的表示形式。

#### 与连接串联
 
在我们刚刚看过的简单示例中，我们主要是将DataFrame与共享列名连接在一起。实际上，来自不同来源的数据可能具有不同的列名集，在这种情况下pd.concat提供了几个选项。考虑以下两个DataFrame的串联，它们有一些（但不是全部！）列相同：
```py
In [28]:  df5 = make_df('ABC', [1, 2])
    ...: df6 = make_df('BCD', [3, 4])
    ...: display('df5', 'df6', 'pd.concat([df5, df6])')
Out[28]: 
df5
    A   B   C
1  A1  B1  C1
2  A2  B2  C2

df6
    B   C   D
3  B3  C3  D3
4  B4  C4  D4

pd.concat([df5, df6])
     A   B   C    D
1   A1  B1  C1  NaN
2   A2  B2  C2  NaN
3  NaN  B3  C3   D3
4  NaN  B4  C4   D4
```
默认情况下，没有可用数据的条目将填充NA值。要更改此设置，我们可以为串联函数的join和join_axes参数指定几个选项之一。默认情况下，联接是输入列的并集（join ='outer'），但是我们可以使用join='inner'将其更改为列的交集：
```py
In [29]:display('df5', 'df6',
    ...:         "pd.concat([df5, df6], join='inner')")
Out[29]: 
df5
    A   B   C
1  A1  B1  C1
2  A2  B2  C2

df6
    B   C   D
3  B3  C3  D3
4  B4  C4  D4

pd.concat([df5, df6], join='inner')
    B   C
1  B1  C1
2  B2  C2
3  B3  C3
4  B4  C4
```
####  append() 方法
由于直接数组串联非常普遍，因此Series和DataFrame对象具有append方法，该方法可以在较少的击键中完成相同的操作。例如，您可以直接调用df1.append（df2）而不是调用pd.concat（[df1，df2]）：
```py
In [36]: display('df1', 'df2', 'df1.append(df2)')
Out[36]: 
df1
    A   B
1  A1  B1
2  A2  B2

df2
    A   B
3  A3  B3
4  A4  B4

df1.append(df2)
    A   B
1  A1  B1
2  A2  B2
3  A3  B3
4  A4  B4
```
* 与Python列表的append（）和extend（）方法不同，Pandas中的append（）方法不会修改原始对象，而是会使用合并后的数据创建一个新对象。这也不是一种非常有效的方法，因为它涉及创建新的索引和数据缓冲区。因此，如果您打算执行多个追加操作，通常最好构建一个DataFrames列表并将它们一次全部传递给concat（）函数。

[pandas官方更多信息](http://pandas.pydata.org/pandas-docs/stable/merging.html)