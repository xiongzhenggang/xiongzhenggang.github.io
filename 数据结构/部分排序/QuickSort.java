package xzg.paixun.com;

public class QuickSort {  
	 /** 
	  * 快速排序原理：在序列中找到基准元素，大于基准元素的放在右边，小于基准元素的放在左边，然后对基准元素两边再次进行排序 
	  * 快速排序是内部排序中最好的一个，平均情况下其效率为O(nlogn)，由于其采用递归方式，递归次数取决于递归树的高度log2n,因此其占用的空间为O(log2n) 
	  * 对于快速排序的优化： 
	  * 第一种方式：如果递归树两边的元素数量均等，那么递归时分配的栈占用的内存空间最小，因此可以适当优化选取的基准元素，取前端、中间点和尾端的中间值 
	  * 第二种方式：如果基准元素两边的元素数量小于等于7时采用直接插入排序，或者如果基准元素两边的元素小于某个值时，返回，最终对序列进行一次直接插入排序 
	  * 元素交换次数： 
	  * 最好情况： 
	  * 最坏情况：如果元素逆序，那么递归的次数为n次，占用的存储空间为O(n)，所需比较的次数为n(n-1)/2,效率低于直接插入排序 
	  * 空间占用情况： 
	  * @param array 
	  * @return 
	  */  
	 public int[] quickSort(int[] array,int left,int right)  
	 {  
	  if(right>left)  
	  {  
	   int pivotPos = partition(array,left,right);//获得基准下标  
	   quickSort(array,left,pivotPos-1); //对基准左边元素排序  
	   quickSort(array,pivotPos+1,right);//对基准右边元素排序  
	  }  
	  return array;  
	 }  
	   
	 /** 
	  * 获得基准下标 
	  * @param array 
	  * @param left 
	  * @param right 
	  * @return 
	  */  
	//为了方便选择数组最右边的的值为枢纽	
private int partition(int[] array,int left,int right)  
	 {  
	  int pivot = array[left];//设置基准元素
	  int pivotPos = left;//基准元素下标
	  for(int i=left+1;i<=right;i++)  
	  {  
	   System.out.println("基准元素："+pivot);  
	   if(pivot>array[i])  
	   {  
	    pivotPos++;  
	    System.out.println(i+"="+pivotPos);  
	    if(i!=pivotPos)  
	    {  
	     swap(array,pivotPos,i);//交换两个元素位置  
	    }  
	   }  
	  }  
	  array[left] = array[pivotPos];  
	  array[pivotPos] = pivot;  
	  return pivotPos;  
	 }  
	 /** 
	  * 交换两个元素位置 
	  * @param array 
	  * @param pivotPos 
	  * @param i 
	  */  
	 private void swap(int[] array,int pivotPos,int i)  
	 {  
	  int temp = array[pivotPos];  
	  array[pivotPos] = array[i];  
	  array[i] = temp;  
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
	  int[] array = {10,5,9,3,4,18,7,6};  
	  QuickSort qs = new QuickSort();  
	  qs.print(qs.quickSort(array, 0, array.length-1));  
	 }  
	}  
