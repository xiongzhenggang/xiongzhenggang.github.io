package xzg.paixun.com;

public class InsertSort {  
	 /** 
	  * 直接插入排序原理：将新元素插入到已排好序的子序列 
	  * 元素比较次数：O(n2) 
	  * 元素交换次数： 
	  * 最好情况：对于一个从小到大的序列，要求以从小到大顺序输出，那么每次插入时只需比较一次，所以总共比较O(n),元素移动次数为0 
	  * 最坏情况：对于一个从小到大的序列，要求反序输出，那么需要比较的次数为1,2,3,4......n-1,总的比较次数为n(n-1)/2,元素移动次数与比较次数相等 
	  * 空间占用情况：只需利用一个临时变量，故空间占用率为O(1); 
	  * @param array 
	  * @return 
	  */  
	 public static int[] insertSort(int[] array)  
	 {  
	  int i,j,len=array.length;  
	  int temp;//设置临时变量  
	  for(i=1;i<len;i++)  
	  {  
	   temp = array[i];  
	   for(j=i-1;j>=0&&array[j]>temp;j--)
	   {  
		System.out.println("==="+j);
	    array[j+1] = array[j];  
	   }  
	   array[j+1] = temp;  
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
	  int[] array = {22,5,9,3,4,18,7,6};  
	  InsertSort.print(InsertSort.insertSort(array));  
	 }  
	}  