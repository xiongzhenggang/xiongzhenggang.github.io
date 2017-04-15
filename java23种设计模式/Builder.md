# 第四种 建造者模式
### 工厂类模式提供的是创建单个类的模式，而建造者模式则是将各种产品集中起来进行管理，用来创建复合对象，所谓复合对象就是指某个类具有不同的属性，其实建造者模式就是前面抽象工厂模式和最后的Test结合起来得到的。
* 和前面一样，一个Sender接口，两个实现类MailSender和SmsSender。最后，建造者类如下
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
建造者和工厂模式的区别主要是下面的类：
```java
 public class Builder {  
       
     private List<Sender> list = new ArrayList<Sender>();  
       
     public void produceMailSender(int count){  
         for(int i=0; i<count; i++){  
             list.add(new MailSender());  
         }  
     }  
       
     public void produceSmsSender(int count){  
         for(int i=0; i<count; i++){  
            list.add(new SmsSender());  
         }  
     }  
    public List<Sender> getList(){
	return list;
   
   }
 }  

```
* 其实建造者模式只是将工厂模式进行了批量生产的扩展，当然上面是最简单的实现，因为这里返回的实例没有区分放到了一起。

