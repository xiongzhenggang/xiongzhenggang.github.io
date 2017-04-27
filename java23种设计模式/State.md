# 第20种 状态模式
### 核心思想就是：当对象的状态改变时，同时改变其行为，很好理解！就拿QQ来说，有几种状态，在线、隐身、忙碌等，每个状态对应不同的操作，而且你的好友也能看到你的状态，所以，状态模式就两点：1、可以通过改变状态来获得不同的行为。2、你的好友能同时看到你的变化
![状态模式](/java23种设计模式/img/state.png)
### State类是个状态类，Context类可以实现切换，我们来看看代码：
```java  
/**
 * @author xzg
 * 状态类，包含不同情况的执行方法
 */
class State{
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void method01(){
		setValue("第一种状态对应的方法1");
		System.out.println(getValue());
	}
	public void method02(){
		setValue("第二种方法对应的方法2");
		System.out.println(getValue());
	}
}
```
```java
/**
 * @author xzg
 *	用于切换状态的类，通过它来达到不同的状态执行不同的方法
 */
class Context{
	
	/**
	 * @param state
	 *  初始状态
	 */
	public Context(State state){
		this.state = state;
	}
	/**
	 * 保存状态
	 */
	private State state;
	
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	/**
	 * 根据不同的状态执行不同的操作，当然这里可以定义通知方法
	 */
	public void turnState(){
		if("state01".equals(state.getValue())){
			state.method01();
		}else if("state02".equals(state.getValue())){
			state.method02();
		}else{
			System.out.println("请先设置相应的状态");
		}
	}
}
```
### 测试类
```java
public class StateTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		State state = new State();
		Context context = new Context(state);
		context.turnState();
		//设置第一种状态
		state.setValue("state01");
		context.turnState();
		//切换第二种状态
		state.setValue("state02");
		context.turnState();
	}
}
```
### 根据这个特性，状态模式在日常开发中用的挺多的，尤其是做网站的时候，我们有时希望根据对象的某一属性，区别开他们的一些功能，比如说简单的权限控制等
