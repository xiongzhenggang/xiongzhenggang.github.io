## 使用Gradle构建Java项目
首先关于安装jdk、gradle等不做额外叙述。下面开始
 [spring官方讲解gradle初步使用](https://spring.io/guides/gs/gradle/)
1. 首先创建最简单的java项目如下：

在项目主目录下，创建以下子目录;在系统下可以使用命令：mkdir -p src/main/java/hello

在src/main/java/hello目录中，你可以创建任何Java类。为简单起见并且为了与指南的其余部分保持一致，创建两个类HelloWorld.java和Greeter.java

src/main/java/hello/HelloWorld.java的源代码
```java
package hello;
 
public class HelloWorld {
  public static void main(String[] args) {
    Greeter greeter = new Greeter();
    System.out.println(greeter.sayHello());
  }
}
```
src/main/java/hello/Greeter.java的源代码：
```java
package hello;
 
public class Greeter {
  public String sayHello() {
    return "Hello world!";
  }
}
```
2. 安装gradle后开始构建java代码

创建一个最简单的只有一行的build.gradle文件：
```gradle
apply plugin: 'java'
```
执行一下gradle task，我们可以看到任务列表中增加了一些内容，比如：用来编译java项目的任务、用来创建JavaDoc的任务、用来执行单元测试的任务。

* 我们经常使用的任务是gradle build，这个任务执行以下操作：编译、执行单元测试、组装Jar文件：
```gradle
gradle build
```
看到”BUILD SUCCESSFUL”输出，说明构建已经完成了,然后就可以到build目录下查看gradle做了那些工作
* classes: 保存被编译后的.class文件
* reports: 构建报告（如：测试报告）
* lib: 组装好的项目包（通常为：.jar或者.war文件）
以上就是简单的使用gradle构建了以下java项目，接下啦考虑以下有依赖的情况是如何做的
3. 申明依赖
现在使用Joda Time jar包增加日期时间的控制
将HellowWorld.java修改如下：
```java
package hello;
 
import org.joda.time.LocalTime;
 
public class HelloWorld {
  public static void main(String[] args) {
    LocalTime currentTime = new LocalTime();
    System.out.println("The current local time is: " + currentTime);
 
    Greeter greeter = new Greeter();
    System.out.println(greeter.sayHello());
  }
}
```




