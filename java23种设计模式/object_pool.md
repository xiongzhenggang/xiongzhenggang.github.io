## 除了经典的设计模式外，额外添加其他设计模式
* 对象池设计模式，使用例子数据库连接池线程池等
原理也很简单：
```
考虑到普通的数据库连接中，由于每次创建连接都消耗很大的系统资源,所以就出现池的概念。
事先创建好一定数量的连接后放入容器（池）中。需要的时候只需要去容器中取就可以了这样就节省了每次连接创建的消耗，
使用完成后在放入连接池以供其他用户使用。这就是池的基本概念了
```
下面直接使用代码展示：
* 首先定义基本的对象池
```java
public abstract class ObjectPool<T> {
  //空闲池，使用时取其中的对象
	  private Set<T> available = new HashSet<>();
  //使用池，保存从池中取出的对象，方便释放到空闲池中
	  private Set<T> inUse = new HashSet<>();
  //定义的抽象方法，不同的继承者自行定义池中存放的对象
	  protected abstract T create();

	  /**
	   * 同步方法，从池中取出对象
	   */
	  public synchronized T checkOut() {
	    if (available.isEmpty()) {
	      available.add(create());
	    }
	    T instance = available.iterator().next();
	    available.remove(instance);
	    inUse.add(instance);
	    return instance;
	  }
   /**
	   * 同步方法，释放已使用后的对象入池
	   */
	  public synchronized void checkIn(T instance) {
	    inUse.remove(instance);
	    available.add(instance);
	  }

	  @Override
	  public String toString() {
	    return String.format("Pool available=%d inUse=%d", available.size(), inUse.size());
	  }
}
```
* 定义池中存放的具体对象
```java
public class Oliphaunt {
	private static int counter = 1;
	 private final int id;
	  /**
	   * 构造方法，id区分创建的对象
	   */
	  public Oliphaunt() {
	    id = counter++;
	    try {
	      Thread.sleep(1000);
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	  }

	  public int getId() {
	    return id;
	  }

	  @Override
	  public String toString() {
	    return String.format("Oliphaunt id=%d", id);
	  }
}
```
* 用户自己定义的对象池
```java
public class OliphauntPool extends ObjectPool<Oliphaunt> {

	  @Override
	  protected Oliphaunt create() {
	    return new Oliphaunt();
	  }
	}
```
* 测试使用结果
```java
public class App {
	  /**
	   * Program entry point
	   * 
	   * @param args command line args
	   */
	  public static void main(String[] args) {
	    OliphauntPool pool = new OliphauntPool();
	    System.out.println(pool.toString());
	    Oliphaunt oliphaunt1 = pool.checkOut();
	    System.out.println("Checked out:"+oliphaunt1);
	    System.out.println(pool.toString());
	    Oliphaunt oliphaunt2 = pool.checkOut();
	    System.out.println("Checked out:"+oliphaunt2);
	    Oliphaunt oliphaunt3 = pool.checkOut();
	    System.out.println("Checked out:"+oliphaunt3);
	    System.out.println(pool.toString());
	    System.out.println("Checked out:"+oliphaunt1);
	    pool.checkIn(oliphaunt1);
	    System.out.println("Checked out:"+oliphaunt2);
	    pool.checkIn(oliphaunt2);
	    System.out.println(pool.toString());
	    Oliphaunt oliphaunt4 = pool.checkOut();
	    System.out.println("Checked out:"+oliphaunt4);
	    Oliphaunt oliphaunt5 = pool.checkOut();
	    System.out.println("Checked out:"+oliphaunt5);
	    System.out.println(pool.toString());
	  }
}
```
* 结果如下：
```
Pool available=0 inUse=0
Checked out:Oliphaunt id=1
Pool available=0 inUse=1
Checked out:Oliphaunt id=2
Checked out:Oliphaunt id=3
Pool available=0 inUse=3
Checked out:Oliphaunt id=1
Checked out:Oliphaunt id=2
Pool available=2 inUse=1
Checked out:Oliphaunt id=1
Checked out:Oliphaunt id=2
Pool available=0 inUse=3

```
[改自:https://github.com/iluwatar/java-design-patterns](https://github.com/iluwatar/java-design-patterns).
