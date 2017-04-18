# 第8种 代理模式
### 其实每个模式名称就表明了该模式的作用，代理模式就是多一个代理类出来，替原对象进行一些操作，比如我们在租房子的时候回去找中介，为什么呢？因为你对该地区房屋的信息掌握的不够全面，希望找一个更熟悉的人去帮你做，此处的代理就是这个意思
![代理模式](/java23种设计模式/img/proxy.png)
```java  
  public interface Sourceable {  
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
### 代理类
```java 
 public class Proxy implements Sourceable {  
   
     private Source source;  
     public Proxy(){  
         super();  
         this.source = new Source();  
     }  
     @Override  
     public void method() {  
         before();  
         source.method();  
         atfer();  
     }  
     private void atfer() {  
         System.out.println("after proxy!");  
     }  
    private void before() {  
         System.out.println("before proxy!");  
     }  
 }  
```
### 测试类：
```java
 public class ProxyTest {  
   
     public static void main(String[] args) {  
         Sourceable source = new Proxy();  
         source.method();  
     }  
   
 }  
```
* 代理模式的应用场景：
如果已有的方法在使用的时候需要对原有的方法进行改进，此时有两种办法：
1、修改原有的方法来适应。这样违反了“对扩展开放，对修改关闭”的原则。
2、就是采用一个代理类调用原有的方法，且对产生的结果进行控制。这种方法就是代理模式。使用代理模式，可以将功能划分的更加清晰，有助于后期维护！
