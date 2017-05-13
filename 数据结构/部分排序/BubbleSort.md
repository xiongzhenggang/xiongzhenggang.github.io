## 冒泡排序

	 /**  
	  * 冒泡排序原理：冒泡排序的关键点在于元素两两比较并交换位置，每一轮的比较可以确定一个最大或者最小元素  
	  * 冒泡排序时间复杂度：其需要比较的次数为n(n-1)/2 空间占用率为O(1)  
	  * 在最差的情况下即元素逆序其需要比较的次数和元素移动的次数相等  
	  *  
	  * 当然可以对冒泡排序进行一定的优化，添加一个标志位，如果一次循环下来，没有发生任何元素位置的交换，  
代码如下：
```java
public class BubbleSort {  
	  *这是序列为有序，结束冒泡比较  
	  * @param array  
	  */  
	 public int[] bubbleSort(int[] array)  
	 {  
	  int i,j,temp,len=array.length;  
	  boolean flag=true;  
	  for(i=0;i<len&&flag;i++)  
	  {  
	   flag = false; //如果在一次循环中该关键字始终为true，那么该序列为有序序列，循环终止  
	   for(j=len-1;j>i;j--)  
	   {  
	    if(array[j-1]>array[j]) //比较并交换  
	    {  
	     temp = array[j-1];  
	     array[j-1] = array[j];  
	     array[j] = temp;  
	     flag = true;  
	    }  
	   }  
	  }  
	  return array;  
	 }  
	 /*** 
	  * 打印排序结果 
	  */  
	 public void print(int[] array )  
	 {  
	  for(int i=0;i<array.length;i++)  
	  {  
	   System.out.print(array[i]+",");  
	  }  
	 }  
	   
	 public static void main(String[] args) {  
	  BubbleSort bs = new BubbleSort();  
	  int[] array = {1,5,9,3,4,18,7,6};  
	  bs.print(bs.bubbleSort(array));  
	 }  
	}  
```
