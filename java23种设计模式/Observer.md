# 第15种 观察者模式
### 包括这个模式在内的接下来的四个模式，都是类和类之间的关系，不涉及到继承，学的时候应该 记得归纳，记得本文最开始的那个图。观察者模式很好理解，类似于邮件订阅和RSS订阅，当我们浏览一些博客或wiki时，经常会看到RSS图标，就这的意思是，当你订阅了该文章，如果后续有更新，会及时通知你。其实，简单来讲就一句话：当一个对象变化时，其它依赖该对象的对象都会收到通知，并且随着变化！对象之间是一种一对多的关系。先来看看关系图：
![观察者模式](/java23种设计模式/img/observer.png)

### MySubject类就是我们的主对象，Observer1和Observer2是依赖于MySubject的对象，当MySubject变化时，Observer1和Observer2必然变化。AbstractSubject类中定义着需要监控的对象列表，可以对其进行修改：增加或删除被监控对象，且当MySubject变化时，负责通知在列表内存在的对象。我们看实现代码：

```java  
/**
 * @author xzg
 *被观察者的接口定义
 */
interface Observer{
	public void update();
}
```
观察者实现类
```java
class Observer01 implements Observer{
	public void update() {
		// TODO Auto-generated method stub
		System.out.println(this.getClass()+"检测到更新了！！");
	}
}
```
```java 
class Observer02 implements Observer{
	public void update() {
		// TODO Auto-generated method stub
		System.out.println(this.getClass()+"检测到更新了！！");
	}
}
```
Subject接口及实现类：
```java
interface Subject{
	/** 增加观察者
	 * @param oberver
	 */
	public void add(Observer observer);
	/**
	 * 移除观察者
	 * @param oberver
	 */
	public void del(Observer observer);
	/**
	 * 通知所有的观察者
	 */
	public void notifyObserver();
	/**
	 * 自身的操作
	 */
	public void operation();
}
```
```java
/**
 * @author xzg
 *抽象类的作用是对接口实现的灵活处理，省略某些不必要的处理
 */
abstract class AbstractSubject implements Subject{
	private Vector<Observer> vector = new Vector<Observer>();
	/* (non-Javadoc)
	 * @see com.xzg.design.Subject#add(com.xzg.design.Oberver)
	 */
	public void add(Observer observer){
		vector.add(observer);
	}
	/* (non-Javadoc)
	 * @see com.xzg.design.Subject#del(com.xzg.design.Oberver)
	 */
	public void del(Observer observer){
		vector.remove(observer);
	}
	/**(non-Javadoc)
	 * @see com.xzg.design.Subject#notifyObserver()
	 * Enumeration（枚举）接口的作用和Iterator类似，
	 * 只提供了遍历Vector和HashTable类型集合元素的功能，不支持元素的移除操作。
	 */
	public void notifyObserver(){
		Enumeration<Observer> enumo = vector.elements();
		//通知所有注册了的观察者
		while(enumo.hasMoreElements()){
			enumo.nextElement().update();
		}
	}
}
```
```java
/**
 * @author xzg
 *实现父类没有重写的方法
 */
class MySubject extends AbstractSubject{

	public void operation() {
		// TODO Auto-generated method stub
		System.out.println("更新了！！");
		//调用父类通知方法，通知所有注册的观察者
		notifyObserver();
	}
}
```
### 测试类
```java
public class ObserverTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Subject mySubject = new MySubject();
		mySubject.add(new Observer01());
		mySubject.add(new Observer02());
		mySubject.operation();
	}
}
```
### 对于观察者模式而言核心的地方为AbstractSubject 中的notifyObservers方法，该方法持有所有注册了的观察者对象。所以在执行该方法间接对所有观察者对象调用相应的方法。 
