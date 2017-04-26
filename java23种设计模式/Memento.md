# 第19种 备忘录模式
### 主要目的是保存一个对象的某个状态，以便在适当的时候恢复对象，个人觉得叫备份模式更形象些，通俗的讲下：假设有原始类A，A中有各种属性，A可以决定需要备份的属性，备忘录类B是用来存储A的一些内部状态，类C呢，就是一个用来存储备忘录的，且只能存储，不能修改等操作。做个图来分析一下：
![备忘录模式](/java23种设计模式/img/memento.png)
### Original类是原始类，里面有需要保存的属性value及创建一个备忘录类，用来保存value值。Memento类是备忘录类，Storage类是存储备忘录的类，持有Memento类的实例，该模式很好理解。直接看源码：
```java  
/**
 * @author xzg
 *作为一个保存类，用于保存原始类的状态，以便恢复
 */
class Memory{
	private String value;
	
	public Memory(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
```
```java
/**
 * @author xzg
 * 初始类
 */
class Original{
	public Original(String value){
		this.value = value;
	}
	//原始类的数值
	private String value;
	/**
	 * @return 持有一个保存类，保存初始值
	 */
	public Memory createMemory(){
		return new Memory(value);
	}
	/**
	 * @param memory
	 * 恢复修改前的状态
	 */
	public void restoreMemory(Memory memory){
		this.value = memory.getValue();
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
} 
```
```java
/**
 * @author xzg
 *	保存memory，这里为原始类初始化的值
 */
class Storage{
	private Memory memory;

	public Storage(Memory memory){
		this.memory = memory;
	}
	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}
}
 
```
### 测试类：
```java
public class MementoTest {
	public static void main(String[] args){
		//创建初始类
		Original original = new Original("我是初始值！！");
		//创建存储类实例保存初始类的需要保存的状态即备忘录
		Storage storage = new Storage(original.createMemory());
		System.out.println("初始类初始值为："+original.getValue());
		original.setValue("我是修改后的值");
		System.out.println("修改后的值为："+original.getValue());
		//恢复初始值
		original.restoreMemory(storage.getMemory());
		System.out.println("恢复后的值为："+original.getValue());
	}
}
```
### 新建原始类时，value被初始化为我是初始值！！，后经过修改，将value的值置为我是修改后的值，最后倒数进行恢复状态，结果成功恢复了。其实这个模式叫“备份-恢复”模式最形象。
