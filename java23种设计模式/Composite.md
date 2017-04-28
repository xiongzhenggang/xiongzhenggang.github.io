# 第22种 解释器模式
### 一般主要应用在OOP开发中的编译器的开发中，所以适用面比较窄。
![解释器模式](/java23种设计模式/img/itnerpreter.png)
### Context类是一个上下文环境类，Plus和Minus分别是用来计算的实现，代码如下：
```java  
/**
 * @author xzg
 *	持有上下文数据的容器类
 */
class IContext{
	private int num1;
	private int num2;
	public IContext(int num1,int num2){
		this.num1 = num1;
		this.num2 = num2;
	}
	public int getNum1() {
		return num1;
	}
	public void setNum1(int num1) {
		this.num1 = num1;
	}
	public int getNum2() {
		return num2;
	}
	public void setNum2(int num2) {
		this.num2 = num2;
	}
}
```
```java
/**
 * @author xzg
 *	提供一个公有的计算接口
 */
interface Expression{
	public int interpret(IContext context);
}
```
```java
/**
 * @author xzg
 *	加法实现
 */
class IPlus implements Expression{

	public int interpret(IContext context) {
		// TODO Auto-generated method stub
		return context.getNum1()+context.getNum2();
	}
}
/**
 * @author xzg
 *	减法实现
 */
class IMinus implements Expression{

	public int interpret(IContext context) {
		// TODO Auto-generated method stub
		return context.getNum1() - context.getNum2();
	}
}
```

### 测试类
```java
public class InterpreterTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IContext context = new IContext(6, 3);
		IPlus plus = new IPlus();
		IMinus minus = new IMinus();
		System.out.println(plus.interpret(context));
		System.out.println(minus.interpret(context));
	}
}
```
### 解释器模式用来做各种各样的解释器，如正则表达式等的解释器等等。
## 到这里的java 23中设计模式就告一段落了。不过整体来说，每一种设计模式都有其适合的应用场景，所以在开发的过程中，根据合适的场景使用。理解上面的设计模式并不难，难在如何在开发的过程中选择合适的，这就需要对软件开发有深刻的理解和足够的经验。以上 
