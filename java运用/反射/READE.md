## 一些反射jdbc相关知识点
* 先来看一个jdbc模板，这是学习持久层框架的基础
```java
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestJdbcBlog {
	public static void main(String[] args) {
        Connection con = null;
        try {
            // 1. 加载驱动（Java6以上版本可以省略）
            Class.forName("com.mysql.jdbc.Driver");
            // 2. 建立连接
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "123456");
            // 3. 创建语句对象
            PreparedStatement ps = con.prepareStatement("insert into tb_user values (default, ?, ?)");
            ps.setString(1, "名字");              // 将SQL语句中第一个占位符换成字符串
            try (InputStream in = new FileInputStream("test.jpg")) {    // Java 7的TWR
                ps.setBinaryStream(2, in);      // 将SQL语句中第二个占位符换成二进制流
                // 4. 发出SQL语句获得受影响行数
                System.out.println(ps.executeUpdate() == 1 ? "插入成功" : "插入失败");
            } catch(IOException e) {
                System.out.println("读取照片失败!");
            }
        } catch (ClassNotFoundException | SQLException e) {     // Java 7的多异常捕获
            e.printStackTrace();
        } finally { // 释放外部资源的代码都应当放在finally中保证其能够得到执行
            try {
                if(con != null && !con.isClosed()) {
                    con.close();    // 5. 释放数据库连接 
                    con = null;     // 指示垃圾回收器可以回收该对象
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
}
}
```
### 下面这是一个反射的应用
```java
import java.lang.reflect.Field;

	class Cat{
		private String name;
		public int age;
		private String color;
	}

	public class TestReflect {
		public static void main(String[] args) throws Exception {
			Class<Cat> clz = Cat.class;
			Field[] f = clz.getDeclaredFields();//获取所用的字段
			
			for (Field field : f) {
				System.out.println(field);
			}
			
			Field fi = clz.getDeclaredField("name");//指定获取字段名
			System.out.println(fi);
			System.out.println("-----------------------");
			System.out.println(fi.getName());//name
			
			//核心开始
			/**
			 *  void set(Object obj, Object value) 
	将指定对象变量上此 Field 对象表示的字段设置为指定的新值。 
			 */
			Cat c = clz.newInstance();
			fi.setAccessible(true);//操作私有属性
			fi.set(c, "刘昭");//赋值成功
			Object o = fi.get(c);
			String s=(String) fi.get(c);
			System.out.println(s);//取出成功 
			
			fi = clz.getDeclaredField("age");
			fi.setAccessible(true);
			fi.set(c, 21);
			int i = fi.getInt(c);//左边的接受类型已经写成了int，右边的返回类型就也必须是int
			System.out.println(i);//获取成功
		}
	}
```
* 关于反射相关应用可以参考
[基于普通biojava动态代理的简单rpc](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/java框架/负载均衡/Rpc_Simple.md) .
