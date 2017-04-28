package xzg.Reflect.com;

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
