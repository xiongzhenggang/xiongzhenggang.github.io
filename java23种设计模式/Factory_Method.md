# 第一种 工厂方法模式
### 1.1简单工厂模式就是建立一个工厂类，对实现了同一接口的一些类进行实例的创建 如图：

![factory01](/java23种设计模式/img/factory01.png)

举一个发送邮件和短信的例子）
首先，创建二者的共同接口：
[java] view plaincopy
```java  
1. public interface Sender {  
2.     public void Send();  
3. }  
```
其次，创建实现类：
```java 
1. public class MailSender implements Sender {  
2.     @Override  
3.     public void Send() {  
4.         System.out.println("this is mailsender!");  
5.     }  
6. }  
```
```java 
1. public class SmsSender implements Sender {  
2.   
3.     @Override  
4.     public void Send() {  
5.         System.out.println("this is sms sender!");  
6.     }  
7. }  
```
最后，建工厂类：
```java 
1. public class SendFactory {  
2.   
3.     public Sender produce(String type) {  
4.         if ("mail".equals(type)) {  
5.             return new MailSender();  
6.         } else if ("sms".equals(type)) {  
7.             return new SmsSender();  
8.         } else {  
9.             System.out.println("请输入正确的类型!");  
10.             return null;  
11.         }  
12.     }  
13. }  
```
测试下：
```java 
1. public class FactoryTest {  
2.   
3.     public static void main(String[] args) {  
4.         SendFactory factory = new SendFactory();  
5.         Sender sender = factory.produce("sms");  
6.         sender.Send();  
7.     }  
8. } 
```
输出：this is sms sender!

### 1.2多个工厂方法模式，是对普通工厂方法模式的改进，在普通工厂方法模式中，如果传递的字符串出错，则不能正确创建对象，而多个工厂方法模式是提供多个工厂方法，分别创建对象。关系图：

![factory01](/java23种设计模式/img/factory02.png)
将上面的代码做下修改，改动下SendFactory类就行，如下：
```java 
   public Sender produceMail(){  
         return new MailSender();  
     }  
            
  public Sender produceSms(){  
         return new SmsSender();  
     }  
 }  
```
测试类如下：
```java 
 public class FactoryTest {  
 
     public static void main(String[] args) {  
         SendFactory factory = new SendFactory();  
         Sender sender = factory.produceMail();  
         sender.Send();  
     }  
 } 
```
输出：this is mailsender!
