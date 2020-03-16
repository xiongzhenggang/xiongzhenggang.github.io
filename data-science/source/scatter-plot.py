#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import numpy as np
import matplotlib.pyplot as plt
import seaborn; seaborn.set()  # for plot styling
mean = [0, 0]
cov = [[1, 2],
       [2, 5]]
X = np.random.multivariate_normal(mean, cov, 100)
X.shape
plt.scatter(X[:, 0], X[:, 1]);
plt.show()