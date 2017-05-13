## ¿¿callable
```java
class TaskWithResult implements Callable<String>{
    private int id;
    private static int count =10;
    private final int time =count--;
    public TaskWithResult(int id){
        this.id = id;
    }
     
    @Override
    public String call() throws Exception {
        TimeUnit.MILLISECONDS.sleep(100);
        return "Result of TaskWithResult : "+ id+", Time= "+time;
    }
     
}
```
¿¿¿¿
```java
public class CallableDemo {
 
    public static void main(String[] args) throws InterruptedException, ExecutionException {
         ExecutorService exec = Executors.newCachedThreadPool();
         ArrayList<Future<String>> results =new ArrayList<Future<String>>();
         for(int i=0;i<10;i++){
             results.add(exec.submit(new TaskWithResult(i)ËÑË÷));
         }
          
         for(Future<String> fs : results){
             System.out.println(fs.get());
         }
    }
```
