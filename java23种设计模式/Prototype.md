# 第五种 原型模式
### 原型模式虽然是创建型的模式，但是与工程模式没有关系，从名字即可看出，该模式的思想就是将一个对象作为原型，对其进行复制、克隆，产生一个和原对象类似的新对象。本小结会通过对象的复制，进行讲解。在Java中，复制对象是通过clone()实现的，先创建一个原型类：
```java  
 public class Prototype implements Cloneable {  
   
     public Object clone() throws CloneNotSupportedException {  
         Prototype proto = (Prototype) super.clone();  
         return proto;  
     }  
 }  
```

* 很简单，一个原型类，只需要实现Cloneable接口，覆写clone方法，此处clone方法可以改成任意的名称，因为Cloneable接口是个空接口，你可以任意定义实现类的方法名，如cloneA或者cloneB，因为此处的重点是super.clone()这句话，super.clone()调用的是Object的clone()方法，而在Object类中，clone()是native的.
浅复制：将一个对象复制后，基本数据类型的变量都会重新创建，而引用类型，指向的还是原对象所指向的。
深复制：将一个对象复制后，不论是基本数据类型还有引用类型，都是重新创建的。简单来说，就是深复制进行了完全彻底的复制，而浅复制不彻底。

### 这里稍微区分以下new 和clone来创建一个对象的区别
new操作符的本意是分配内存。程序执行到new操作符时， 首先去看new操作符后面的类型，因为知道了类型，才能知道要分配多大的内存空间。分配完内存之后，再调用构造函数，填充对象的各个域，这一步叫做对象的初始化，构造方法返回后，一个对象创建完毕，可以把他的引用（地址）发布到外部，在外部就可以使用这个引用操纵这个对象。而clone在第一步是和new相似的， 都是分配内存，调用clone方法时，分配的内存和源对象（即调用clone方法的对象）相同，然后再使用原对象中对应的各个域，填充新对象的域， 填充完成之后，clone方法返回，一个新的相同的对象被创建，同样可以把这个新对象的引用发布到外部。
### 这里是一个深复制和浅复制写在一起的例子
```java
public class Prototype implements Cloneable,Serializable{
	/**
	 * 要实现深复制，需要采用流的形式读入当前对象的二进制输入，再写出二进制数据对应的对象。
	 */
	private static final long serialVersionUID = 1L;
	
	private String string;
	private SerializableObject object;
	
	/*浅复制*/
	public Object clone() throws CloneNotSupportedException{
		Prototype prototype = (Prototype) super.clone();
		return prototype;
	}
	/* 深复制 */
	public Object deepClone() throws IOException, ClassNotFoundException{
		//以二进制的形式写入输出管道
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		//使用输入管道去链接输出管道接收对象
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		return ois.readObject(); //读取输入管道中的对象
	}
	//这里的get set方法是为了在进行深复制的将相应的对象引用也提供支持
	public String getString() {
		return string;
	}
	public void setString(String string) {
		this.string = string;
	}
	public SerializableObject getObject() {
		return object;
	}
	public void setObject(SerializableObject object) {
		this.object = object;
	}

}
class SerializableObject implements Serializable{

	/**
	 * 作为类的引用属性深复制时也要继承Serializable
	 */
	private static final long serialVersionUID = 1L;
	
}

```
