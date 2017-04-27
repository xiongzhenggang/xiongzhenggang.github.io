# 第21种 访问者模式
### 访问者模式把数据结构和作用于结构上的操作解耦合，使得操作集合可相对自由地演化。访问者模式适用于数据结构相对稳定算法又易变化的系统。因为访问者模式使得算法操作增加变得容易。若系统数据结构对象易于变化，经常有新的数据对象增加进来，则不适合使用访问者模式。访问者模式的优点是增加操作很容易，因为增加操作意味着增加新的访问者。访问者模式将有关行为集中到一个访问者对象中，其改变不影响系统数据结构。其缺点就是增加新的数据结构很困难。简单来说，访问者模式就是一种分离对象数据结构与行为的方法，通过这种分离，可达到为一个被访问者动态添加新的操作而无需做其它的修改的效果。简单关系图： 
![访问者模式](/java23种设计模式/img/visitor.png)
### java实现
```java  
/**
 * @author xzg
 *	访问者接口
 */
interface Vistor{
	/**
	 * 访问的对象
	 */
	public void vist(Subject01 subject);
}
```
```java
interface Subject01{
	/**
	 * @param vistor
	 * 监听来的访问者
	 */
	public void accept(Vistor vistor);
	/**
	 * @return
	 * 返回访问后的数据
	 */
	public String getSubject();
}
```
```java
/**
 * @author xzg
 *	访问实现的类
 */
class MyVistory implements Vistor{

	public void vist(Subject01 subject) {
		// TODO Auto-generated method stub
		System.out.println("这里去访问："+subject.getSubject());
	}
}
```
```java
/**
 * @author xzg
 * 被访问的实现类方法
 */
class MySubject01 implements Subject01{

	public void accept(Vistor vistor) {
		// 被访问的对象监听访问到来后，将自己本身返回给访问者，接受将要访问它的对象
		vistor.vist(this);
	}

	/**
	 *  获取将要被访问的属性，
	 */
	public String getSubject() {
		// TODO Auto-generated method stub
		return "五脏六腑";
	}
}
```
### 测试类
```java
public class VisitorTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Vistor vistor = new MyVistory();
		Subject01 subject = new MySubject01();
		//作为被访问者，监听到访问者到来后，返回给自己的数据
		subject.accept(vistor);
	}
}
```
### 该模式适用场景：如果我们想为一个现有的类增加新功能，不得不考虑几个事情：1、新功能会不会与现有功能出现兼容性问题？2、以后会不会再需要添加？3、如果类不允许修改代码怎么办？面对这些问题，最好的解决方法就是使用访问者模式，访问者模式适用于数据结构相对稳定的系统，把数据结构和算法解耦
