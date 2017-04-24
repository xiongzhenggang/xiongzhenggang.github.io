# 第16种 迭代子模式
### 顾名思义，迭代器模式就是顺序访问聚集中的对象，一般来说，集合中非常常见，如果对集合类比较熟悉的话，理解本模式会十分轻松。这句话包含两层意思：一是需要遍历的对象，即聚集对象，二是迭代器对象，用于对聚集对象进行遍历访问。我们看下关系图：
![迭代子模式](/java23种设计模式/img/iterator.png)

### 这个思路和我们常用的一模一样，MyCollection中定义了集合的一些操作，MyIterator中定义了一系列迭代操作，且持有Collection实例，我们来看看实现代码：
首先定义迭代器和集合的接口
```java 
interface Iterator{
	//前移
	public Object previous();
	//后移
	public Object next();
	//判断集合是否为空
	public boolean hasNext();
	//取得第一个元素
	public Object first();
}
```
```java
interface Collection{
	//集合中包含的迭代器
	public Iterator iterator();
	/*取得集合中的元素*/
	public Object get(int i);
	/* 取得集合的大小*/
	public int size();
}
```
两者的实现
```java 
class MyIterator implements Iterator{
	//迭代器持有相应集合的对象
	private Collection collection;
	//作为迭代器的指针
	private int pos = -1;
	//绑定集合对象
	public MyIterator(Collection collection){
		this.collection = collection;
	}
	/* 前移
	 * @see com.xzg.design.Iterator#prebious()
	 */
	public Object previous() {
		// TODO Auto-generated method stub
		if(pos <= 0)
			return null;
		return collection.get(--pos);
	}

	public Object next() {
		// TODO Auto-generated method stub
		if(pos>=collection.size())
			return null;
		return collection.get(++pos);
	}

	public boolean hasNext() {
		// TODO Auto-generated method stub
		if(pos<collection.size()-1)
			return true;
		return false;
	}

	public Object first() {
		// TODO Auto-generated method stub
		return collection.get(0);
	}
}
```
```java
class MyCollection implements Collection{

	//作为操作的实际集合
	String string[] = {"abc","bcd","def","fgh"};
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return new MyIterator(this);
	}

	public Object get(int i) {
		// TODO Auto-generated method stub
		return string[i];
	}

	public int size() {
		// TODO Auto-generated method stub
		return string.length;
	}
}

```
### 测试类
```java
public class TestIterator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Collection collection = new MyCollection();
		Iterator iterator = collection.iterator();
		//初始前移
		System.out.println(iterator.previous());
		//迭代循环
		while(iterator.hasNext()){
			System.out.println(iterator.next());
		}
		//第一个值
		System.out.println(iterator.first());
		//前一个
		System.out.println(iterator.previous());
	}
}
```
### 这中模式在jdk中的很多集合比如 set、list 、arraylist、entryset等都有用到，核心思想就是利用迭代器中持有集合的对象，来操作集合对象。
