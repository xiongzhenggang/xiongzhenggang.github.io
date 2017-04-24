# 第14种 模板模式
### 一个抽象类中，有一个主方法，再定义1...n个方法，可以是抽象的，也可以是实际的方法，定义一个类，继承该抽象类，重写抽象方法，通过调用抽象类，实现对子类的调用，先看个关系图：
![模板模式](/java23种设计模式/img/template.png)

### 在AbstractCalculator类中定义一个主方法calculate，calculate()调用spilt()等，Plus和Minus分别继承AbstractCalculator类，通过对AbstractCalculator的调用实现对子类的调用，看下面的例子：
```java  
abstract class AbstractCalculator{
	/*主方法，实现对本类其它方法的调用*/
	public final int calculate(String exp,String opt){
		int arry[] = split(exp, opt);
		return calculate(arry[0], arry[1]);
	} 
	
	/**
	 * 需要子类重写的方法
	 * @param i1
	 * @param i2
	 * @return
	 */
	abstract public int calculate(int i1,int i2);
	//抽象类中需要的辅助
	public int[] split(String exp,String opt){
		String[] arrystr = exp.split(opt);
		int arryint[] = new int[2];
		arryint[0] = Integer.valueOf(arrystr[0]);
		arryint[1] = Integer.valueOf(arrystr[1]);
		return arryint;
	}
}

}   
```
加法的实现
```java
/**
 * @author xzg
 * 加法
 */
class Plus01 extends AbstractCalculator{
	@Override
	public int calculate(int num1,int num2){
		return num1+num2;
	}
}
```

减法的实现
```java 
/**
 * @author xzg
 *减法
 */
class Mnus01 extends AbstractCalculator{
	@Override
	public int calculate(int num1,int num2){
		return num1-num2;
	}
}
```
### 测试类
```java
class StrategyTest {
	/**
	 * @param args
	 * 测试方法
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String exp = "6+6";
		AbstractCalculator abstractCalculator = new Plus01();
		int result = abstractCalculator.calculate(exp, "\\+");
		System.out.println(result);
		exp = "6-3";
		abstractCalculator = new Mnus01();
		result = abstractCalculator.calculate(exp, "\\-");
		System.out.println(result);
	}

}
```
### 执行过程：首先将exp和"\\+"做参数，调用AbstractCalculator类里的calculate(String,String)方法，在calculate(String,String)里调用同类的split()，之后再调用calculate(int ,int)方法，从这个方法进入到子类中，执行完return num1 + num2后，将值返回到AbstractCalculator类，赋给result，打印出来。正好验证了我们开头的思路。

### 其实在之前的策略模式中和模板模式是很相近的，他们之间的区别仅仅是策略模式将模板模式中的抽象类的需要子类覆写的方法进剥离到接口中。这样做会更灵活，但是他们之间的耦合度也会下降，所以使用的时候要考虑不同的情况使用。
