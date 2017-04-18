# 第7种 装饰模式
* 顾名思义，装饰模式就是给一个对象增加一些新的功能，而且是动态的，要求装饰对象和被装饰对象实现同一个接口，装饰对象持有被装饰对象的实例，关系图如下：
![装饰模式](/java23种设计模式/img/decorator.png)
### Source类是被装饰类，Decorator类是一个装饰类，可以为Source类动态的添加一些功能，代码如下：
```java  
 public interface Sender {  1. public interface Sourceable {  
     public void method();  
 }  
   
```
创建实现类：
```java 
 public class Source implements Sourceable {  
   
     @Override  
     public void method() {  
         System.out.println("the original method!");  
     }  
 }  
```
### 装饰类
```java 
 public class Decorator implements Sourceable {  
   
     private Sourceable source;  
      
     public Decorator(Sourceable source){  
         super();  
         this.source = source;  
     }  
     @Override  
    public void method() {  
         System.out.println("before decorator!");  
        source.method();  
         System.out.println("after decorator!");  
     }  
 }  
```
### 测试类：
```java
 public class DecoratorTest {  
   
     public static void main(String[] args) {  
         Sourceable source = new Source();  
        Sourceable obj = new Decorator(source);  
         obj.method();  
     }  
 }
```
### 其实装饰模式和代理模式很像，代理模式是通过代理类来执行目标类  
