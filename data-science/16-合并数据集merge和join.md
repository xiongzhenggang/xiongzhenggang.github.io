## 合并数据集: merge和join
pandas提供的一项基本功能是其高性能的内存中联接和合并操作。如果您曾经使用过数据库，则应该熟悉这种类型的数据交互。其主要接口是pd.merge函数，我们将看到一些如何在实践中工作的示例。
为方便起见使用上文的display对象
```py
import pandas as pd
import numpy as np

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
### 关系代数

 
在pd.merge（）中实现的行为是所谓的关系代数的子集，关系代数是操作关系数据的一组正式规则，并构成了大多数数据库中可用的操作的概念基础。关系代数方法的优势在于它提出了几种原始运算，这些原始运算成为任何数据集上更复杂运算的基础。通过在数据库或其他程序中有效实施的基本操作词典，可以执行各种相当复杂的复合操作。

Pandas在pd.merge（）函数以及“Series ”和“Dataframe”的相关join（）方法中实现了几个基本构建块。正如我们将看到的，这些使您可以有效地链接来自不同来源的数据。
##  联接类别
pd.merge（）函数实现了多种连接类型：一对一，多对一和多对多连接。通过对pd.merge（）接口的相同调用可以访问所有三种类型的联接。联接的类型取决于输入数据的形式。在这里，我们将显示三种合并类型的简单示例，并在下面进一步讨论详细的选项。
### 一对一
也许最简单的合并表达式类型是一对一联接，它在许多方面与“合并数据集：Concat和Append”中看到的按列联接非常相似。作为一个具体的示例，请考虑以下两个DataFrame，其中包含有关公司中多个员工的信息：
```py
In [55]:  df1 = pd.DataFrame({'employee': ['Bob', 'Jake', 'Lisa', 'Sue'],
    ...:                     'group': ['Accounting', 'Engineering', 'Engineering', 'HR']})
    ...: df2 = pd.DataFrame({'employee': ['Lisa', 'Bob', 'Jake', 'Sue'],
    ...:                     'hire_date': [2004, 2008, 2012, 2014]})
    ...: display('df1', 'df2')
Out[55]: 
df1
  employee        group
0      Bob   Accounting
1     Jake  Engineering
2     Lisa  Engineering
3      Sue           HR

df2
  employee  hire_date
0     Lisa       2004
1      Bob       2008
2     Jake       2012
3      Sue       2014
```
* 要将这些信息组合到单个DataFrame中，我们可以使用pd.merge（）函数：
```py
In [56]: df3=pd.merge(df1,df2)
In [57]: df3
Out[57]: 
  employee        group  hire_date
0      Bob   Accounting       2008
1     Jake  Engineering       2012
2     Lisa  Engineering       2004
3      Sue           HR       2014
```
pd.merge（）函数识别出每个DataFrame都有一个“employee”列，并使用该列作为键来自动加入。合并的结果是一个新的DataFrame，它将来自两个输入的信息组合在一起。请注意，不一定必须保持每个列中的条目顺序：在这种情况下，“employee”列的顺序在df1和df2之间有所不同，而pd.merge（）函数可以正确地说明这一点。另外，请记住，除按索引合并的特殊情况外，一般而言合并会丢弃索引（请参阅一会儿讨论的left_index和right_index关键字）。
###  多对一
多对一联接是其中两个键列之一包含重复条目的联接。对于多对一的情况，生成的DataFrame将适当保留这些重复的条目。考虑以下多对一联接的示例：
```py
In [67]: 
    ...: 
    ...: df4 = pd.DataFrame({'group': ['Accounting', 'Engineering', 'HR'],
    ...:                     'supervisor': ['Carly', 'Guido', 'Steve']})
    ...: display('df3', 'df4', 'pd.merge(df3, df4)')
Out[67]: 
df3
  employee        group  hire_date
0      Bob   Accounting       2008
1     Jake  Engineering       2012
2     Lisa  Engineering       2004
3      Sue           HR       2014

df4
         group supervisor
0   Accounting      Carly
1  Engineering      Guido
2           HR      Steve

pd.merge(df3, df4)
  employee        group  hire_date supervisor
0      Bob   Accounting       2008      Carly
1     Jake  Engineering       2012      Guido
2     Lisa  Engineering       2004      Guido
3      Sue           HR       2014      Steve
```
所得的DataFrame具有带有“ supervisor”信息的附加列，其中该信息在输入所要求的一个或多个位置中重复。
### 多对多
在概念上，多对多连接有些混乱，但是定义得很清楚。如果左右数组中的键列都包含重复项，则结果是多对多合并。举一个具体的例子，这也许是最清楚的。考虑以下内容，在这里我们有一个数据框，显示与特定组相关的一项或多项技能。通过执行多对多联接，我们可以恢复与任何个人相关的技能：
```py
In [72]: 
    ...: 
    ...: df5 = pd.DataFrame({'group': ['Accounting', 'Accounting',
    ...:                               'Engineering', 'Engineering', 'HR', 'HR'],
    ...:                     'skills': ['math', 'spreadsheets', 'coding', 'linux',
    ...:                                'spreadsheets', 'organization']})
    ...: display('df1', 'df5', "pd.merge(df1, df5)")
Out[72]: 
df1
  employee        group
0      Bob   Accounting
1     Jake  Engineering
2     Lisa  Engineering
3      Sue           HR

df5
         group        skills
0   Accounting          math
1   Accounting  spreadsheets
2  Engineering        coding
3  Engineering         linux
4           HR  spreadsheets
5           HR  organization

pd.merge(df1, df5)
  employee        group        skills
0      Bob   Accounting          math
1      Bob   Accounting  spreadsheets
2     Jake  Engineering        coding
3     Jake  Engineering         linux
4     Lisa  Engineering        coding
5     Lisa  Engineering         linux
6      Sue           HR  spreadsheets
7      Sue           HR  organization
```
这三种类型的联接可与其他Pandas工具一起使用，以实现各种各样的功能。但是实际上，数据集很少像我们在此使用的那样干净。在以下部分中，我们将考虑pd.merge（）提供的一些选项，这些选项使您能够调整联接操作的工作方式。
###  合并键的标准
我们已经看到了pd.merge（）的默认行为：它在两个输入之间查找一个或多个匹配的列名，并将其用作键。但是，列名通常不会很好地匹配，并且pd.merge（）提供了多种选项来处理此问题。
#### 关键字on
最简单的是，您可以使用on关键字显式指定键列的名称，该关键字采用列名或列名列表：
```py
In [73]: 
    ...: 
    ...: display('df1', 'df2', "pd.merge(df1, df2, on='employee')")
Out[73]: 
df1
  employee        group
0      Bob   Accounting
1     Jake  Engineering
2     Lisa  Engineering
3      Sue           HR

df2
  employee  hire_date
0     Lisa       2004
1      Bob       2008
2     Jake       2012
3      Sue       2014

pd.merge(df1, df2, on='employee')
  employee        group  hire_date
0      Bob   Accounting       2008
1     Jake  Engineering       2012
2     Lisa  Engineering       2004
3      Sue           HR       2014
```
 
仅当左侧和右侧数据框都具有指定的列名时，此选项才有效。
#### left_on和right_on关键字
 
有时您可能希望合并两个具有不同列名的数据集；例如，我们可能有一个数据集，其中雇员姓名被标记为“name”，而不是“employee”。在这种情况下，我们可以使用left_on和right_on关键字来指定两个列名称：
```py
In [74]: df3 = pd.DataFrame({'name': ['Bob', 'Jake', 'Lisa', 'Sue'],
    ...:                     'salary': [70000, 80000, 120000, 90000]})
    ...: display('df1', 'df3', 'pd.merge(df1, df3, left_on="employee", right_on="name")')
Out[74]: 
df1
  employee        group
0      Bob   Accounting
1     Jake  Engineering
2     Lisa  Engineering
3      Sue           HR

df3
   name  salary
0   Bob   70000
1  Jake   80000
2  Lisa  120000
3   Sue   90000

pd.merge(df1, df3, left_on="employee", right_on="name")
  employee        group  name  salary
0      Bob   Accounting   Bob   70000
1     Jake  Engineering  Jake   80000
2     Lisa  Engineering  Lisa  120000
3      Sue           HR   Sue   90000
```
 
结果有一个多余的列，我们可以根据需要删除该列，例如，通过使用DataFrames的drop（）方法：
```py
In [75]:pd.merge(df1, df3, left_on="employee", right_on="name").drop('name', axis=1)
Out[75]: 
  employee        group  salary
0      Bob   Accounting   70000
1     Jake  Engineering   80000
2     Lisa  Engineering  120000
3      Sue           HR   90000
```
#### left_index和right_index关键字
有时，您不想合并在列上，而是想合并在索引上。例如，您的数据可能如下所示：
```py
In [76]: df1a = df1.set_index('employee')
    ...: df2a = df2.set_index('employee')
    ...: display('df1a', 'df2a')
Out[76]: 
df1a
                group
employee
Bob        Accounting
Jake      Engineering
Lisa      Engineering
Sue                HR

df2a
          hire_date
employee
Lisa           2004
Bob            2008
Jake           2012
Sue            2014
```
通过在pd.merge（）中指定left_index和/或right_index标志，可以将索引用作合并的键：

```py
In [77]: display('df1a', 'df2a',
    ...:         "pd.merge(df1a, df2a, left_index=True, right_index=True)")
Out[77]: 
df1a
                group
employee
Bob        Accounting
Jake      Engineering
Lisa      Engineering
Sue                HR

df2a
          hire_date
employee
Lisa           2004
Bob            2008
Jake           2012
Sue            2014

pd.merge(df1a, df2a, left_index=True, right_index=True)
                group  hire_date
employee
Bob        Accounting       2008
Jake      Engineering       2012
Lisa      Engineering       2004
Sue                HR       2014
```
为了方便起见，DataFrames实现join（）方法，该方法执行默认情况下在索引上进行联接的合并：
```py
In [78]: display('df1a', 'df2a', 'df1a.join(df2a)')
Out[78]: 
df1a
                group
employee
Bob        Accounting
Jake      Engineering
Lisa      Engineering
Sue                HR

df2a
          hire_date
employee
Lisa           2004
Bob            2008
Jake           2012
Sue            2014

df1a.join(df2a)
                group  hire_date
employee
Bob        Accounting       2008
Jake      Engineering       2012
Lisa      Engineering       2004
Sue                HR       2014
```
如果您想混合使用索引和列，可以将left_index与right_on结合使用，或者将left_on与right_index结合使用以获得所需的行为：
```py
In [79]: display('df1a', 'df3', "pd.merge(df1a, df3, left_index=True, right_on='name')")
Out[79]: 
df1a
                group
employee
Bob        Accounting
Jake      Engineering
Lisa      Engineering
Sue                HR

df3
   name  salary
0   Bob   70000
1  Jake   80000
2  Lisa  120000
3   Sue   90000

pd.merge(df1a, df3, left_index=True, right_on='name')
         group  name  salary
0   Accounting   Bob   70000
1  Engineering  Jake   80000
2  Engineering  Lisa  120000
3           HR   Sue   9000
```
所有这些选项还可以与多个索引和/或多个列一起使用。此行为的界面非常直观。
 
### 指定联接的设置算法
在前面的所有示例中，我们都忽略了执行连接时的一个重要注意事项：连接中使用的设置算法的类型。当一个值出现在一个键列中而不出现在另一个键列中时，将出现此消息。考虑以下示例：
```py
In [80]:  df6 = pd.DataFrame({'name': ['Peter', 'Paul', 'Mary'],
    ...:                     'food': ['fish', 'beans', 'bread']},
    ...:                    columns=['name', 'food'])
    ...: df7 = pd.DataFrame({'name': ['Mary', 'Joseph'],
    ...:                     'drink': ['wine', 'beer']},
    ...:                    columns=['name', 'drink'])
    ...: display('df6', 'df7', 'pd.merge(df6, df7)')
Out[80]: 
df6
    name   food
0  Peter   fish
1   Paul  beans
2   Mary  bread

df7
     name drink
0    Mary  wine
1  Joseph  beer

pd.merge(df6, df7)
   name   food drink
0  Mary  bread  wine
```
在这里，我们合并了两个只有一个“name”条目的数据集：Mary。默认情况下，结果包含两组输入的交集。这就是所谓的内部联接。我们可以使用how关键字（默认为“ inner”）来明确指定：
>pd.merge(df6, df7, how='inner')
how关键字的其他选项是“ outer”，“ left”和“ right”。外部联接通过输入列的并集返回联接，并使用NA填充所有缺少的值：
```py
In [81]: display('df6', 'df7', "pd.merge(df6, df7, how='outer')")
Out[81]: 
df6
    name   food
0  Peter   fish
1   Paul  beans
2   Mary  bread

df7
     name drink
0    Mary  wine
1  Joseph  beer

pd.merge(df6, df7, how='outer')
     name   food drink
0   Peter   fish   NaN
1    Paul  beans   NaN
2    Mary  bread  wine
3  Joseph    NaN  beer
```
左联接和右联接返回联接分别在左条目和右条目上。例如：
>display('df6', 'df7', "pd.merge(df6, df7, how='left')")
右链接类似使用how='right'
### 重叠的列名称：关键字的后缀
最后，您可能最终遇到两个输入DataFrame具有冲突的列名的情况。考虑以下示例：
```py
In [82]:df8 = pd.DataFrame({'name': ['Bob', 'Jake', 'Lisa', 'Sue'],
    ...:                     'rank': [1, 2, 3, 4]})
    ...: df9 = pd.DataFrame({'name': ['Bob', 'Jake', 'Lisa', 'Sue'],
    ...:                     'rank': [3, 1, 4, 2]})
    ...: display('df8', 'df9', 'pd.merge(df8, df9, on="name")')
Out[82]: 
df8
   name  rank
0   Bob     1
1  Jake     2
2  Lisa     3
3   Sue     4

df9
   name  rank
0   Bob     3
1  Jake     1
2  Lisa     4
3   Sue     2

pd.merge(df8, df9, on="name")
   name  rank_x  rank_y
0   Bob       1       3
1  Jake       2       1
2  Lisa       3       4
3   Sue       4       2
```
因为输出会有两个冲突的列名，所以合并功能会自动附加后缀_x或_y以使输出列唯一。如果这些默认值不合适，则可以使用suffixes关键字指定自定义后缀：
```py
In [83]:display('df8', 'df9', 'pd.merge(df8, df9, on="name", suffixes=["_L", "_R"])')
Out[83]: 
df8
   name  rank
0   Bob     1
1  Jake     2
2  Lisa     3
3   Sue     4

df9
   name  rank
0   Bob     3
1  Jake     1
2  Lisa     4
3   Sue     2

pd.merge(df8, df9, on="name", suffixes=["_L", "_R"])
   name  rank_L  rank_R
0   Bob       1       3
1  Jake       2       1
2  Lisa       3       4
3   Sue       4       2
```
这些后缀适用于任何可能的连接模式，并且在有多个重叠的列时也适用。