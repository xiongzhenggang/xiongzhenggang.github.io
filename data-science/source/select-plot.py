#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import numpy as np
import matplotlib.pyplot as plt
import seaborn; seaborn.set()  # for plot styling
mean = [0, 0]
cov = [[1, 2],
       [2, 5]]
X = np.random.multivariate_normal(mean, cov, 100)
indices = np.random.choice(X.shape[0], 20, replace=False)
print(indices)
selection = X[indices]  # fancy indexing here
print(selection)
plt.scatter(X[:, 0], X[:, 1], alpha=0.3)
plt.scatter(selection[:, 0], selection[:, 1],
            facecolor='none', s=200);

plt.show()