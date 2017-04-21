# 第14种 策略模式
### 策略模式定义了一系列算法，并将每个算法封装起来，使他们可以相互替换，且算法的变化不会影响到使用算法的客户。需要设计一个接口，为一系列实现类提供统一的方法，多个实现类实现该接口，设计一个抽象类（可有可无，属于辅助类），提供辅助函数，关系图如下：
![策略模式](/java23种设计模式/img/strategy.png)

### 图中ICalculator提供同意的方法，AbstractCalculator是辅助类，提供辅助方法，接下来，依次实现下每个类首先统一接口：
```java  
interface ICalcultor{
	public int 	calcultor(String exp);
}   
```
创建辅助类
```java
abstract class CalcultorUtil{
	public int[] split(String exp,String opt){
		String[] arrystr = exp.split(opt);
		int[] arryint = new int[2];
		arryint[0] = Integer.valueOf(arrystr[0]);
		arryint[1] = Integer.valueOf(arrystr[1]);
		return arryint;
	}

}
```

创建实现类1：
```java 
class Plus extends CalcultorUtil implements ICalcultor{
	
	public int calcultor(String exp) {
		// TODO Auto-generated method stub
		int[] arryint = split(exp, "\\+");
		return arryint[0]+arryint[1];
	}
	
}
```
创建实现类2
```java
class Minus extends CalcultorUtil implements ICalcultor{
	public int calcultor(String exp){
		int[] arryint = split(exp, "\\-");
		return arryint[0]- arryint[1];
	}
}
```
### 测试类：
```java
public class Strategy {
	 public static void main(String[] args){
		Plus plus = new Plus();
		int result = plus.calcultor("1+1");
		System.out.println(result);
		Minus minus = new Minus();
		result = minus.calcultor("3-0");
		System.out.println(result);
	}

}

```
* 策略模式的决定权在用户，系统本身提供不同算法的实现，新增或者删除算法，对各种算法做封装。因此，策略模式多用在算法决策系统中，外部用户只需要决定用哪个算法即可。
