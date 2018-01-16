## 堆排序 
```java
	 /** 
	  * 堆排序原理：首先构建一个最大堆，然后将堆顶元素和末尾元素进行交换，交换之后自上而下进行一次排序, 
	  * 堆排序时间复杂度为O(nlog2n),只是用了一个临时变量，所以空间占用率为O(1) 
	  * 最好情况：时间复杂度：O(nlogn) 
	  * 最坏情况：时间复杂度：O(nlogn) 
	  * 空间占用情况： 
	  * @param array 
	  * @return 
	  */
```
代码如下：
```java  
public class HeapSort {  

	 public int[] heapSort(int[] array)  
	 {  
	  int len = array.length;//获取元素个数  
	  for(int i=len/2;i>=0;i--) //构建最大堆  
	  {  
	   adjustHeap(array,i,len);  
	  }  
	   
	  for(int i=len-1;i>=0;i--)  
	  {  
	   swap(array,0,i);//将堆顶元素与array[i]交换位置  
	   adjustHeap(array,0,i);//从堆顶自上而下进行调整  
	  }  
	   
	  return array;  
	 }  
	 /** 
	  * 对最大堆进行自上而下的调整 
	  * @param array 
	  * @param k 指定的位置 
	  * @param len 待调整元素数量 
	  */  
	 public void adjustHeap(int[] array,int k,int len)  
	 {  
	  int temp = array[k];  
	  int i=k,j=2*i+1;  
	  while(j<len)  
	  {  
	   if((j+1)<len&&array[j+1]>array[j])//如果右子女存在并且大于左子女，指针++  
	   {  
	    j++;  
	   }  
	   if(temp>array[j])//如果当前定点大于子女，无需调整  
	   {  
	    break;  
	   }  
	   array[i] = array[j];  
	   i = j; //移向下一个节点  
	   j = 2*j+1;  
	  }  
	  array[i] = temp;  
	 }  
	 /** 
	  * 进行元素调整 
	  */  
	 public void swap(int[] array,int i,int k)  
	 {  
	  int temp = array[i];  
	  array[i] = array[k];  
	  array[k] = temp;  
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
	  HeapSort hs = new HeapSort();  
	  int[] array = {1,5,9,3,4,18,7,6};  
	  hs.print(hs.heapSort(array));  
	 }  
	}  
```
