```java
package com.xzg.heap;
/**
 * @TIME 2017年5月2日
 * 堆的特点：1、它是完全二叉树，也就是除了最后一层节点，其他都是满的2、它常常使用数组实现3、堆中的每一个节点都要满足
 * 堆的条件。也就是每个节点的关键字都要大于或等于这个子节点的关键字（左右子节点的大小并不确定）
 * 这里使用数组模拟树，需要明白如果当前节点为x 则父节点为(x-1)/2它的 子节点分别是2*x+1和2*x+2
 */
/**
 * @author hasee
 * @TIME 2017年5月2日
 * 注意类的隐藏和实例创建
 */
public class Heap {
	private Node[] heapArray;
	//初始最大堆
	private int maxSize;
	//当前节点下标
	private int currentSize;
	/**
	 * @param mx
	 * 构造器初始化
	 */
	public Heap(int mx){
		this.maxSize = mx;
		this.currentSize = 0;
		this.heapArray = new Node[maxSize];
	}
		/**
		 * 判断是否为空
		 */
		public boolean isEmpty(){
			return currentSize == 0;
		}
		/**
		 * @param key
		 * @return
		 * 
		 */
		public boolean insert(int key){
			//超出返回false
			if(currentSize == maxSize)
				return false;
			//初始化新节点
			Node newNode = new Node(key);
			//将新节点插入到最后
			heapArray[currentSize] = newNode;
			//将当前节点数量增加1，然后使用下面方法向上筛选
			trickleup(currentSize++);
			return true;
		}
		/**
		 * @param index
		 * 向上筛选算法，的参数时新插入节点的下标，找到这个位置的父节点，然后把这个节点的父节点保存到bottom变量中
		 * 在while循环内，变量index沿着路劲向根节点方向上移，只要没有达到根（index>0），且父节点的idata小于这个节点的
		 *while就一直循环。
		 */
		public void trickleup(int index){
			//父节点下标
			int parent = (index-1)/2;
			//bottom保存着最末端的节点，也就是新插入的节点
			Node bottom = heapArray[index];
			//存在父节点且小于当前节点时，向上移动
			while(index >0 && heapArray[parent].getKey() < bottom.getKey()){
				//将父节点下移
				heapArray[index] = heapArray[parent];
				index = parent;
				//继续寻找。父节点的父节点
				parent = (parent-1)/2;
			}
			//当循环结束后，将保存要插入的节点，插入
			heapArray[index]  = bottom;
		}
		/**
		 * @return
		 * 移除和插入不同的是，移除根节点，然后将最后的节点补上作为根节点。
		 * 以根节点开始向下移动，与向上移动不同的地方就是向下移动，需要考虑
		 * 判断左右子节点大小，来确定向那边移动
		 */
		public Node remove(){
			//保存根节点
			Node root = heapArray[0];
			//将最后的节点替换到根节点
			heapArray[0] = heapArray[--currentSize];
			//开始移动
			trickledown(0);
			return root;
		}
		/**
		 * @param index
		 * 首先把下标为index 的节点保存到top。如果是remove调用它index为0。
		 * 循环条件，只要当前节点还有子节点就一直循环，在循环内部，得到最大的子节点
		 * largeChild，然后与largeChild比较是否下移
		 */
		public void trickledown(int index){
			int largeChild ;
			Node top = heapArray[index];
			//如果有子节点
			while(index<currentSize/2){
				int leftChild = 2*index +1;
				int rightChild = leftChild =1;
				//找到最大节点
				if(rightChild<currentSize&&heapArray[leftChild].getKey()<heapArray[rightChild].getKey())
					largeChild = rightChild;
				else
					largeChild = leftChild;
				//top是否大于largeChild
				if(top.getKey()>= heapArray[largeChild].getKey())
					break;
				//替换大于当前节点的子节点
				heapArray[index] = heapArray[largeChild];
				//重置index作为下一次循环的起始条件
				index = largeChild;
			}
			//循环结束，将保存的top恢复到适合的位置
			heapArray[index] = top;
		}
		/**
		 * @param index
		 * @param newValue
		 * @return
		 * 本方法更改指定节点的数值后，并且有trickledown和	trickleup方法对堆做相应的调整
		 */
		public boolean change(int index,int newValue){
			if(index<0 || index > currentSize)
				return false;
			//获取原来数值大小，通过与当前值大小比较后来判断是向上移还是下移
			int oldValue = heapArray[index].getKey();
			heapArray[index].setKey(newValue);
			if(newValue>oldValue)
				//更改的大于之前的，放到最后上浮
				trickleup(index);
			else
				//小于下沉
				trickledown(index);
			return true;
		}
/**
 * 展示
 */
public void display(){
	System.out.println("heapArray: ");
	for(int m = 0;m<currentSize;m++)
		if(heapArray[m]!=null)
			System.out.println(heapArray[m].getKey()+" ");
		else
			System.out.println("--");
}	
/*最后与堆排序关联，堆排序的基本思想使用普通的insert后remove出的数据为已排序的
 * for(j=0;j<size;j++) theHeap.insert(array[j])
 * for(j=0;j<size;j++)array[j] = theHeap.remove()
 * */
}
/**
 * @author hasee
 * @TIME 2017年5月2日
 * 节点类，作为堆的数据节点
 */
class Node{
	//节点数据
	private int iData;
	public Node(int key){
		this.iData  = key;
	}
	public int getKey(){
		return iData;
	}
	public void setKey(int key){
		iData = key;
	}
}

```
