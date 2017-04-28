# 第22种 中介模式
### 中介者模式也是用来降低类类之间的耦合的，因为如果类类之间有依赖关系的话，不利于功能的拓展和维护，因为只要修改一个对象，其它关联的对象都得进行修改。如果使用中介者模式，只需关心和Mediator类的关系，具体类类之间的关系及调度交给Mediator就行，这有点像spring容器的作用。先看看图：
![中介模式](/java23种设计模式/img/mediator.png)
### User类统一接口，User1和User2分别是不同的对象，二者之间有关联，如果不采用中介者模式，则需要二者相互持有引用，这样二者的耦合度很高，为了解耦，引入了Mediator类，提供统一接口，MyMediator为其实现类，里面持有User1和User2的实例，用来实现对User1和User2的控制。这样User1和User2两个对象相互独立，他们只需要保持好和Mediator之间的关系就行，剩下的全由MyMediator类来维护！基本实现：
```java  
/**
 * @author xzg
 *	中介的接口规范
 */	
interface Mediator{
	/**
	 * 创建中介要执行的内容
	 */
	public void createMediator();
	/**
	 * 替用户执行
	 */
	public void workAll();
}
```
```java
/**
 * @author xzg
 *	因为多个实例用户的中介相同，这里使用抽象类，抽象出共同的模式
 */
abstract class User{
	private Mediator mediator;

	public void setMediator(Mediator mediator) {
		this.mediator = mediator;
	}

	public Mediator getMediator() {
		return mediator;
	}
	public User(Mediator mediator){
		this.mediator = mediator;
	}
	/**
	 * 需要不同子类自定义
	 */
	public abstract void work();
}
```
### 俩个实现类
```java
class User01 extends User{

	/**
	 * @param mediator
	 * 指定中介
	 */
	public User01(Mediator mediator) {
		super(mediator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void work() {
		// TODO Auto-generated method stub
		System.out.println("用户01工作！！");
	}
}
class User02 extends User{

	public User02(Mediator mediator) {
		super(mediator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void work() {
		// TODO Auto-generated method stub
		System.out.println("用户02工作！！");
	}
	
}
```
```java
/**
 * @author xzg
 *	中介接口的实现，持有来注册的用户
 */
class MyMediator implements Mediator{
	private User user01;
	private User user02;
	public User getUser01() {
		return user01;
	}

	public void setUser01(User user01) {
		this.user01 = user01;
	}

	public User getUser02() {
		return user02;
	}

	public void setUser02(User user02) {
		this.user02 = user02;
	}

	/**
	 * @see com.xzg.design.Mediator#createMediator()
	 * 这里相当于ioc，由中介替我们创建对象。并且将中介本身注册到每个用户中
	 */
	public void createMediator() {
		// TODO Auto-generated method stub
		user01 = new User01(this);
		user02 = new User02(this);
	}
	/**
	 * @see com.xzg.design.Mediator#workAll()
	 * 由中介替用户调用执行
	 */
	public void workAll() {
		// TODO Auto-generated method stub
		user01.work();
		user02.work();
	}
}
```
### 测试类
```java
public class MediatorTest {
	public static void main(String[] args){
		Mediator mediator = new MyMediator();
		mediator.createMediator();
		mediator.workAll();
	}
}

```
执行结果：
用户01工作！！
用户02工作！！

