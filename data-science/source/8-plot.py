#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import seaborn; seaborn.set() # Plot styling
import numpy as np
# 创建随机0-1 之间得十个点
X=np.random.randn(10,2)
plt.scatter(X[:, 0], X[:, 1], s=100);
plt.show()