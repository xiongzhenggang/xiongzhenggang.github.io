## 折半排序原理
	 /** 
	  * 折半排序原理：在将一个新元素插入到一个已经排好的序列中时，采用折半的方式找到元素的位置，是对直接插入排序的改进 
	  * 元素比较次数：元素比较的次数好于直接插入排序，平均比较次数为nlogn, 
	  * 元素交换次数：折半排序元素移动的次数与直接插入元素移动的次数相同，均与元素的初始序列有关 
	  * 
	     * 最差情况下，元素需要移动的次数为nlogn,即每次插入在有序序列的开头，所有元素都需要后移一位 
	     * 
	  * 空间占用情况：只占用一个临时变量，故空间占用率为O(1) 
	  * @param array 
	  * @return 
	  */  
代码如下：
```java
public class BinaryInsertSort {   

	 public static int[] binaryInsertSort(int[] array)  
	 {  
	  int i,j,len=array.length;  
	  int temp;  
	  int low,high,middle;  
	  for(i=1;i<len;i++)  
	  {  
	   low = 0;  
	   high = i-1;  
	   temp = array[i];  
	   while(high>=low)  
	   {  
	    middle = (low+high)/2; //去中间点  
	    if(temp>array[middle])  
	    {  
	     low = middle+1; //向右缩进  
	    }else{  
	     high = middle - 1; //向左缩进  
	    }  
	   }  
	   for(j=i-1;j>=low;j--) //将low与i-1之间的元素右移  
	   {  
	    array[j+1] = array[j];  
	   }  
	   array[low] = temp; //插入  
	  }  
	  return array;  
	 }  
	   
	 /*** 
	  * 打印排序结果 
	  */  
	 public static void print(int[] array )  
	 {  
	  for(int i=0;i<array.length;i++)  
	  {  
	   System.out.print(array[i]+",");  
	  }  
	 }  
	   
	 public static void main(String[] args) {  
	  int[] array = {1,5,9,3,4,18,7,6};  
	  BinaryInsertSort.print(BinaryInsertSort.binaryInsertSort(array));  
	 }  
	}  

```
