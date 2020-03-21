#!/usr/bin/env python3
# -*- coding: utf-8 -*-
## T层楼N个鸡蛋，假如鸡蛋从k层掉落刚好摔碎。那么最少需要多少次
import numpy as np
#1 设有T层楼N个鸡蛋，最少M次。计算M
#2 利用递归的原理，我们需要知道，如果只有一层楼则只需要一次。如果只有一个几点那么我们需要T次。这是递归的结束
#3  递归的核心点，在选取丢下的K层，能试N减少或者T减少最终到达递归最初我们知道的点  Mk=max{M(k,N-1),M(T-k,N)}+1
def recurs(T,N):
    # 设次数M
    if N<=1:
        return T
    if T<=1:
        return 1
    sortList=[]
    for i in range(1,T+1):
        maxk=max(recurs(i-1,N-1),recurs(T-i,N))+1
        sortList.append(maxk)
    sortList.sort()
    return sortList[0]
# 递归深度太大会导致程序缓慢甚至崩溃，用循环替代
# def range_methond(T,N):

if __name__ == "__main__":
    print(recurs(20,6))
# def minMk(T,N):
