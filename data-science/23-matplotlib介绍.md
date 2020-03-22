## Matplotlib可视化
 
现在，我们将深入研究Matplotlib软件包，以便在Python中进行可视化。 Matplotlib是一个基于NumPy数组的多平台数据可视化库，旨在与更广泛的SciPy堆栈配合使用。它由John Hunter于2002年构思，最初是IPython的补丁，用于通过IPython命令行通过gnuplot启用交互式MATLAB样式的绘图。 IPython的创建者Fernando Perez当时正忙于完成他的博士学位，并让John知道他几个月来都没有时间来审查补丁。 John以此为依据自行提出，Matplotlib程序包诞生了，2003年发布了0.1版。当它被选为太空望远镜科学研究所的绘图程序包（即哈勃望远镜背后的人们），这为Matplotlib的发展提供了财政支持，并大大扩展了其功能。
Matplotlib最重要的功能之一就是它能够与许多操作系统和图形后端完美兼容。 Matplotlib支持数十种后端和输出类型，这意味着无论使用哪种操作系统或所需的输出格式，都可以依靠它来工作。这种跨平台，从所有人到所有人的方法一直是Matplotlib的强大优势之一。它导致了庞大的用户基础，进而导致了活跃的开发人员基础以及Matplotlib的强大工具和在科学Python世界中的普遍存在。
然而，近年来，Matplotlib的界面和样式已开始显示其年龄。 R语言中的ggplot和ggvis等较新的工具，以及基于D3js和HTML5 canvas的Web可视化工具包，通常会使Matplotlib显得笨拙而过时。不过，我认为我们不能忽视Matplotlib作为经过良好测试的跨平台图形引擎的优势。最新的Matplotlib版本使设置新的全局绘图样式相对容易，人们一直在开发新的软件包，这些软件包基于其强大的内部组件通过更清洁，更现代的API驱动Matplotlib。例如， Seaborn（与Seaborn一起在可视化中讨论），[ggpy](http://yhat.github.io/ggpy/)，[HoloViews](http://holoviews.org/)，[Altair](http://altair-viz.github.io/)甚至Pandas本身都可以用作Matplotlib API的包装器。即使有这样的包装器，深入了解Matplotlib的语法以调整最终绘图输出仍然经常有用。因此，我相信Matplotlib本身仍将是数据可视化堆栈的重要组成部分，即使新工具意味着社区逐渐不再直接使用Matplotlib API也是如此。

### Matplotlib一般提示
在深入探讨使用Matplotlib创建可视化内容的细节之前，您应该了解一些有关使用该软件包的有用信息。
```py
In [1]: import matplotlib as mpl
   ...: import matplotlib.pyplot as plt
```
我们将在本章中看到的最常用的是plt接口。
### 设置风格
我们将使用plt.style指令为我们的图形选择适当的美学样式。在这里，我们将设置经典样式，以确保我们创建的图使用经典的Matplotlib样式：
>In [2]: plt.style.use('classic')
在本节中，我们将根据需要调整此样式。请注意，自Matplotlib 1.5版开始，支持此处使用的样式表。如果您使用的是Matplotlib的早期版本，则仅默认样式可用。
### show（）或没有show（）？如何显示图
您看不到的可视化没有多大用处，但是查看Matplotlib图表的方式取决于上下文。 Matplotlib的最佳用法因使用方式而异。大致而言，这三个在脚本中使用Matplotlib,，IPython终端或IPython笔记本中使用Matplotlib。
### 从脚本绘图
如果您在脚本中使用Matplotlib，则函数plt.show（）是您的朋友。 plt.show（）启动事件循环，查找所有当前活动的图形对象，并打开一个或多个显示您的图形的交互式窗口。
因此，例如，您可能有一个名为myplot.py的文件，其中包含以下内容：
```py
# ------- file: myplot.py ------
import matplotlib.pyplot as plt
import numpy as np
x = np.linspace(0, 10, 100)
plt.plot(x, np.sin(x))
plt.plot(x, np.cos(x))
plt.show()
```
然后执行该脚本
>python myplot.py
plt.show（）命令在后台做了很多事情，因为它必须与系统的交互式图形后端交互。该操作的细节在系统之间甚至在安装之间都可能有很大的不同，但是matplotlib会尽力向您隐藏所有这些细节。
需要注意的一件事：plt.show（）命令在每个Python会话中只能使用一次，并且最经常在脚本的结尾看到。多个show（）命令可能导致不可预测的后端相关行为，因此应避免使用。

### 从IPython Shell绘图
在IPython shell中交互使用Matplotlib会非常方便（请参阅IPython：超越常规Python）。如果您指定Matplotlib模式，则将IPython构建为可与Matplotlib配合使用。要启用此模式，可以在启动ipython之后使用％matplotlib magic命令：
```py
In [1]: %matplotlib
Using matplotlib backend: TkAgg
In [2]: import matplotlib.pyplot as plt
```
此时，任何plt plot命令都将导致图形窗口打开，并且可以运行其他命令来更新图形。某些更改（例如修改已绘制线条的属性）将不会自动绘制：要强制更新，请使用plt.draw（）。不需要在Matplotlib模式下使用plt.show（）。
### 从 IPython notebook作图
IPython笔记本是基于浏览器的交互式数据分析工具，可以将叙述，代码，图形，HTML元素以及更多内容组合到一个可执行文件中（请参阅[IPython：超越常规Python](https://jakevdp.github.io/PythonDataScienceHandbook/01.00-ipython-beyond-normal-python.html)）。
进入ipython notebook
>IPython notebook
进入后新建文件
可以使用％matplotlib命令在IPython Notebook中进行交互式绘图，其工作方式与IPython Shell类似。在IPython笔记本中，您还可以选择直接在笔记本中嵌入图形，其中有两个可能的选择：
        ％matplotlib笔记本将导致在笔记本中嵌入交互式地块
        ％matplotlib内联将导致您嵌入在笔记本中的绘图的静态图像
本文我们通常会选择内联％matplotlib：
>%matplotlib inline
运行此命令（每个内核/会话仅需要执行一次）后，笔记本中创建绘图的任何单元将嵌入所得图形的PNG图像：

```py
import numpy as np
x = np.linspace(0, 10, 100)

fig = plt.figure()
plt.plot(x, np.sin(x), '-')
plt.plot(x, np.cos(x), '--');
```

### 将图形保存到文件
Matplotlib的一个不错的功能是能够以多种格式保存图形。可以使用savefig（）命令来保存图形。例如，要将上一个图形另存为PNG文件

在savefig（）中，文件格式是从给定文件名的扩展名推断出来的。根据您安装的后端，可以使用许多不同的文件格式。通过使用图形画布对象的以下方法，可以找到系统支持的文件类型的列表

* ipython notebook的作图实现过程 [save image](./ipython_noteboo_save_image.ipynb)

### 两个接口的等价
Matplotlib的一个潜在混乱特性是它的双重接口：便利的MATLAB样式的基于状态的接口和功能更强大的面向对象的接口。我们将在此处快速突出显示两者之间的差异。
####  MATLAB风格的接口
* 我这里使用ipython shell方式作图
Matplotlib最初是为MATLAB用户编写的Python替代书，其语法大部分反映了这一事实。 MATLAB样式的工具包含在pyplot（plt）界面中。例如，以下代码对于MATLAB用户可能看起来非常熟悉：
```py
In [2]:  %matplotlib
Using matplotlib backend: TkAgg
In [3]: import matplotlib.pyplot as plt
In [6]: import numpy as np
In [7]: x = np.linspace(0, 10, 100)
In [9]: # 首先创建地块网格
   ...: # 轴将是两个轴对象的数组
   ...: fig, ax = plt.subplots(2)
   ...: #  plot() 方法作图
   ...: ax[0].plot(x, np.sin(x))
   ...: ax[1].plot(x, np.cos(x));
```
![图片.png](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/data-science/image/sincos.png)

对于更简单的图，使用哪种样式的选择主要取决于偏好，但是随着图变得更加复杂，面向对象的方法可能成为必需。在本章中，我们将根据最方便的方式在MATLAB风格的接口和面向对象的接口之间进行切换。在大多数情况下，差异仅与将plt.plot（）切换为ax.plot（）一样小，但是在以下各节中将重点介绍一些陷阱。