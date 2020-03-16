#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import numpy as np
import pandas as pd
# 使用 pandas 提取 下雨的英尺数作为numpu数组
rainfall = pd.read_csv('../data/Seattle2014.csv')['PRCP'].values
inches = rainfall / 254.0  # 1/10mm -> inches
inches.shape
## 根据数组作图
import matplotlib.pyplot as plt
#Seaborn其实是在matplotlib的基础上进行了更高级的API封装
import seaborn; 
seaborn.set(color_codes=True)#设定颜色
# seaborn.distplot(x, bins=20, kde=False, rug=True);#设置了20个矩形条
plt.hist(inches, 40);
plt.show();