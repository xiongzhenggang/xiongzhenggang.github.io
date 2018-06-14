### mybitis 整体执行分析
1. Mybatis的整个执行过程
```
理解起来分为如下几个过程：

1. 加载配置文件

2. 解析配置文件，从配置文件中解析出来 datasource、mapper文件、事务配置等等。将配置信息保存在对象内

3. 调用相关语句，执行sql。在执行的方法中分别完成JDBC的一系列操作。
```
2. 使用
1） 第一种直接将使用mybitis的api接口实现如下：
原理：Java动态代理，Mybatis通过这种方式实现了我们通过getMapper方式得到的Dao接口，可以直接通过接口的没有实现的方法来执行sql。getMapper方法到底做了什么。
```java
@Autowired
	private SqlSessionFactory sqlSessionFactory;
 //使用 SqlSession openSession(boolean autoCommit);设置不自动提交
 SqlSession session = sqlSessionFactory.openSession(false); 
  UserMapper mapper = session.getMapper(UserMapper.class);
  session.commit();
```
2） 第二种，最常用，平时我们使用mapper接口以及对应mapper的sql xml文件后注入相应的mapper就可以直接使用，本质上使用的也是第一种方式

在mybitis使用session.getMapper(UserMapper.class);跟踪getMapper方法，进入到 MapperProxyFactory 类的 newInstance(SqlSession sqlSession) 方法。
方法：
```java
  @SuppressWarnings("unchecked")

  protected T newInstance(MapperProxy<T> mapperProxy) {

    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);

  }
  public T newInstance(SqlSession sqlSession) {

    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);

    return newInstance(mapperProxy);

  }
  ````
可以发现，mybitis一个mapper接口之所以和xml中的id对应就是在mybitis动态代理时，基于mapper接口实现代理类，代理类的具体实现了xml的sql的执行
上面Proxy.newProxyInstance 来实现切入sql的。他的实现在MapperProxy的 invoke 方法里面。看下invoke方法：
```java
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }
```

