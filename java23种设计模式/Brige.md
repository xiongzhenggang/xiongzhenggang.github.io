# 第10种 桥接模式
### 桥接模式就是把事物和其具体实现分开，使他们可以各自独立的变化。桥接的用意是：将抽象化与实现化解耦，使得二者可以独立变化，像我们常用的JDBC桥DriverManager一样，JDBC进行连接数据库的时候，在各个数据库之间进行切换，基本不需要动太多的代码，甚至丝毫不用动，原因就是JDBC提供统一接口，每个数据库提供各自的实现，用一个叫做数据库驱动的程序来桥接就行了。我们来看看关系图：
![桥接模式](/java23种设计模式/img/brige.png)
* 接下来代码示例
```java  
 interface Source{
	void method();
}  
```
创建两个不同的实现类：
```java
class SourceAble01 implements Source{

	public void method() {
		// TODO Auto-generated method stub
		System.out.println("first method");
	}
} 
```
```java
class SourceAble02 implements Source{
	public void method(){
		System.out.println("second method");
	}
}
```
//定义一个桥，持有Sourceable的一个实例：
```java
abstract class Brige{
	private Source source;

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
	          
	public void method(){
		source.method();
	}
}
```
实现自定义桥
```java
class Mybrige extends Brige{
	public void method(){
		getSource().method();
	}
}
```
测试以下：
```java
public class BrigeDesige {
	public static void  mian(String[] args){
		Brige brige = new Mybrige();
		Source source01 = new SourceAble01();
		Source Source02 =new SourceAble02();
		brige.setSource(source01);
		brige.method();
		brige.setSource(Source02);
		brige.method();
	}
}
```
* 这样，就通过对Bridge类的调用，实现了对接口Sourceable的实现类SourceSub1和SourceSub2的调。下面用这个图是我们JDBC连接的原理，有数据库学习基础的，一结合就都懂了

![jdbc连接模式](/java23种设计模式/img/jdbc.png)
