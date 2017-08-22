### 和对象池类似这里主要介绍线程池使用模式
* 核心将多个线程交给java线程池去管理
代码示例：
* 抽象任务类，为其他任务的父类
```java
public abstract class Task {
  //所有子类的根据id号来区分
	  private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

	  private final int id;
	  private final int timeMs;

	  public Task(final int timeMs) {
	    this.id = ID_GENERATOR.incrementAndGet();
	    this.timeMs = timeMs;
	  }

	  public int getId() {
	    return id;
	  }

	  public int getTimeMs() {
	    return timeMs;
	  }

	  @Override
	  public String toString() {
	    return String.format("id=%d timeMs=%d", id, timeMs);
	  }
	}
  ```
* 任务一
```java
public class PotatoPeelingTask extends Task {

	  private static final int TIME_PER_POTATO = 200;
	  //这是个为马铃薯去皮的任务，去皮一个需要时间200
	  public PotatoPeelingTask(int numPotatoes) {
	    super(numPotatoes * TIME_PER_POTATO);
	  }

	  @Override
	  public String toString() {
	    return String.format("%s %s", this.getClass().getSimpleName(), super.toString());
	  }
	}
```
* 任务二
```java
  public class CoffeeMakingTask extends Task {

	  private static final int TIME_PER_CUP = 100;
	  //这是一个冲咖啡的任务，完成一杯用时100
	  public CoffeeMakingTask(int numCups) {
	    super(numCups * TIME_PER_CUP);
	  }

	  @Override
	  public String toString() {
	    return String.format("%s %s", this.getClass().getSimpleName(), super.toString());
	  }
	}
  ```
* 执行任务的线程
  ```java
  public class Worker implements Runnable {


	  private final Task task;
	  //每一个线程都对应一个任务
	  public Worker(final Task task) {
	    this.task = task;
	  }
	  
	  /** 
	* @see java.lang.Runnable#run()  
	* @Function: Worker.java
	* @Description:  任务结束对应线程结束（时间由具体的任务*任务数量决定）
	* @version: v1.0.0
	* @author: Administrator
	* @date: 2017年8月22日 下午5:38:07 
	* @Modify:
	*/
	@Override
	  public void run() {
	 System.out.println(Thread.currentThread().getName()+": processing :"+task.toString());
	    try {
	      Thread.sleep(task.getTimeMs());
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	  }
	}
  ```
* 测试执行任务
  ```java
  public class App {
	  
	  /**
	   * Program entry point
	   * 
	   * @param args command line args
	   */
	  public static void main(String[] args) {

	   System.out.println("Program started");

	    // 创建一个任务集合,放入不同的任务种类
	    List<Task> tasks = new ArrayList<>();
	    tasks.add(new PotatoPeelingTask(3));
	    tasks.add(new PotatoPeelingTask(6));
	    tasks.add(new CoffeeMakingTask(2));
	    tasks.add(new CoffeeMakingTask(6));
	    tasks.add(new PotatoPeelingTask(4));
	    tasks.add(new CoffeeMakingTask(2));
	    tasks.add(new PotatoPeelingTask(4));
	    tasks.add(new CoffeeMakingTask(9));
	    tasks.add(new PotatoPeelingTask(3));
	    tasks.add(new CoffeeMakingTask(2));
	    tasks.add(new PotatoPeelingTask(4));
	    tasks.add(new CoffeeMakingTask(2));
	    tasks.add(new CoffeeMakingTask(7));
	    tasks.add(new PotatoPeelingTask(4));
	    tasks.add(new PotatoPeelingTask(5));

	    // 创建一个最大线程数为三个的线程池，保证同一时间最多只有三个线程同时运行，其他线程任务等待执行
	    ExecutorService executor = Executors.newFixedThreadPool(3);

	    // 1、为每一个任务都分配一个线程
	    // 2、将所有的线程都提交到线程池中运行
	    // available in the thread pool
	    for (int i = 0; i < tasks.size(); i++) {
	      Runnable worker = new Worker(tasks.get(i));
	      executor.execute(worker);
	    }
	    //所有线程任务提交后，关闭线程池
	    executor.shutdown();
	    // shutdown、shutdownNow被调用了后，判断是否所有的线程已经运行完  
	    while (!executor.isTerminated()) {
	    //没有执行完的主线程调用yield(),让任务线程继续
	      Thread.yield();
	    }
	    System.out.println("Program finished");
	  }
	}

```
* 测试结果：
```
Program started
pool-1-thread-1: processing :PotatoPeelingTask id=1 timeMs=600
pool-1-thread-2: processing :PotatoPeelingTask id=2 timeMs=1200
pool-1-thread-3: processing :CoffeeMakingTask id=3 timeMs=200
pool-1-thread-3: processing :CoffeeMakingTask id=4 timeMs=600
pool-1-thread-1: processing :PotatoPeelingTask id=5 timeMs=800
pool-1-thread-3: processing :CoffeeMakingTask id=6 timeMs=200
pool-1-thread-3: processing :PotatoPeelingTask id=7 timeMs=800
pool-1-thread-2: processing :CoffeeMakingTask id=8 timeMs=900
pool-1-thread-1: processing :PotatoPeelingTask id=9 timeMs=600
pool-1-thread-3: processing :CoffeeMakingTask id=10 timeMs=200
pool-1-thread-1: processing :CoffeeMakingTask id=12 timeMs=200
pool-1-thread-3: processing :PotatoPeelingTask id=11 timeMs=800
pool-1-thread-2: processing :CoffeeMakingTask id=13 timeMs=700
pool-1-thread-1: processing :PotatoPeelingTask id=14 timeMs=800
pool-1-thread-2: processing :PotatoPeelingTask id=15 timeMs=1000
Program finished
```
