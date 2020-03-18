#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import seaborn; seaborn.set() # Plot styling
import numpy as np

X=np.random.randn(10,2)
differences = X[:, np.newaxis, :] - X[np.newaxis, :, :]
sq_differences = differences ** 2
dist_sq = sq_differences.sum(-1)
# draw lines from each point to its two nearest neighbors
plt.scatter(X[:, 0], X[:, 1], s=100)
K = 2
nearest_partition = np.argpartition(dist_sq, K + 1, axis=1)
#循环取原数组每一个点和该点对应最近的K+1个点，画图
for i in range(X.shape[0]):
    for j in nearest_partition[i, :K+1]:
        # plot a line from X[i] to X[j]
        # zip()函数的定义从参数中的多个迭代器取元素组合成一个新的迭代器；
        #*zip()函数是zip()函数的逆过程，将zip对象变成原先组合前的数据。
        plt.plot(*zip(X[j], X[i]), color='black')
plt.show()