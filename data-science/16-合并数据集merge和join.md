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