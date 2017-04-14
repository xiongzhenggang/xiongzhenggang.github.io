# 第二种 抽象工厂模式
### 工厂方法模式有一个问题就是，类的创建依赖工厂类，也就是说，如果想要拓展程序，必须对工厂类进行修改，这违背了闭包原则，所以，从设计角度考虑，有一定的问题，如何解决？就用到抽象工厂模式，创建多个工厂类，这样一旦需要增加新的功能，直接增加新的工厂类就可以了，不需要修改之前的代码。因为抽象工厂不太好理解，我们先看看图，然后就和代码，就比较容易理解。
![factory02](/java23种设计模式/img/factory02.png)
* 创建和第一个类似的例子，抽象工厂是为了弥补工厂模式的不足，它的作用在于不同的实例由不同的工厂来完成。而这些不同的工厂之间又必须统一标准（工厂的接口）如下例：

```java  
 public interface Sender {  
     public void Send();  
 }  
```
其次，创建实现类：
```java 
public class MailSender implements Sender {  
     @Override  
     public void Send() {  
         System.out.println("this is mailsender!");  
     }  
 }  
```
```java 
 public class SmsSender implements Sender {  
   
     @Override  
     public void Send() {  
         System.out.println("this is sms sender!");  
     }  
 }  
```
创建制定工厂标准接口
```java
 public interface Provider {  
     public Sender produce();  
 }  
```
创建相应的实现类
```java
public class SendMailFactory implements Provider {  
       
     @Override  
     public Sender produce(){  
         return new MailSender();  
     }  
 }  
```
```java
public class SendSmsFactory implements Provider{  
   
     @Override  
    public Sender produce() {  
         return new SmsSender();  
     }  
 }  
```
最后测试以下：
```java 
public class Test {  
   
     public static void main(String[] args) {  
         Provider provider = new SendMailFactory();  
         Sender sender = provider.produce();  
         sender.Send();  
     }  
 }  
```

