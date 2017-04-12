如图：
![image](https://github.com/xiongzhenggang/xiongzhenggang.github.io/edit/master/java23种设计模式/img/factory01.png)
![Alt text](https://github.com/xiongzhenggang/xiongzhenggang.github.io/edit/master/java23种设计模式/img/factory01.png)

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
10.             return null;  
11.         }  
12.     }  
13. }  
```
我们来测试下：
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
