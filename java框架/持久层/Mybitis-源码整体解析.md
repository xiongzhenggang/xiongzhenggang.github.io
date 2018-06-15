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

执行过程分析：
1. mybitis的session实现类DefaultSqlSession中getMapper方法
```java
 @Override
  public <T> T getMapper(Class<T> type) {
    return configuration.<T>getMapper(type, this);
  }
```
2. SqlSession中getMapper调用的是Configuration中的getMapper进入
```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
```
3. 发现Configtiong中的getMapper调用MapperRegistry中的getMapper方法,这个方法是核心，如下
本质上是使用代理的设计模式，将用户的mapper接口用mybitis实现其代理类执行
```java
 @SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
 
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
  ```
4. 查看mapperProxyFactory中代理类工厂中的创建代理实现类过程如下：

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
5. mybitis调用newInstance(SqlSession sqlSession)，后在调用newInstance(MapperProxy<T> mapperProxy)方法，这个jdk代理中重点在于InvocationHandler这个参数，这里使用的是mapperProxy，然后调用jdk的代理类中的Proxy.newProxyInstance，重点在于mapperProxy的实现，我们进入查看
	
```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;
//构造器中sqlSession重点关注
  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }
//这里是代理类的实现方式
//该方法总结
//1）、method.getDeclaringClass用来判断当前这个方法是哪个类的方法。
//2）、接口创建出的代理对象不仅有实现接口的方法，也有从Object继承过来的方法
//3）、实现的接口的方法method.getDeclaringClass是接口类型，比如com.atguigu.dao.EmpDao从Object类继承过来的方法类型是java.lang.Object类型
//4）、如果是Object类继承来的方法，直接反射调用如果是实现的接口规定的方法，利用Mybatis的MapperMethod调用
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  //如果这个判断的意义
 /*动态代理对象里面的方法都是Interface规定的。但是动态代理对象也能调用比如toString(),hashCode()等这些方法呀，这些方法是所有类从Object继承过来的。
所以这个判断的根本作用就是，如果利用动态代理对象调用的是toString，hashCode,getClass等这些从Object类继承过来的方法，就直接反射调用。如果调用的是接口规定的方法。我们就用MapperMethod来执行。*/
    if (Object.class.equals(method.getDeclaringClass())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
    //cachedMapperMethod在下面。意思上面如果是目标类的代理方法则执行mapperMethod.execute来实现
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }
  
// methodCache在代理类工厂MapperProxyFactory定义为ConcurrentHashMap<Method, MapperMethod>()
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
    //如果在缓存方法中没有找到，则实现这个之心方法的对象
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }

}	
```
6. 接下来就是另一个核心，为整个mybitis执行sql的核心方法,下面是整个类的部分（重点在于execute方法）
这里的Configuration 为SqlsessionFactory创建SqlSession，时构造器注入到SqlSession。SqlsessionFactory中的
Configuration为容器启动时通过配置文件mybitis-config文件创建。具体加载配置文件以及生成配置类暂不叙述了
```java
public class MapperMethod {
//SqlCommand为封装mybitis执行接口中的方法名称以及执行类型（执行type通过xml映射文件比如<select><update>等）
  private final SqlCommand command;
  //MethodSignature收集方法中的参数，核心方法为convertArgsToSqlCommandParam
  private final MethodSignature method;
  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, method);
  }

  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    if (SqlCommandType.INSERT == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
    } else if (SqlCommandType.UPDATE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
    } else if (SqlCommandType.DELETE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
    } else if (SqlCommandType.SELECT == command.getType()) {
      if (method.returnsVoid() && method.hasResultHandler()) {
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
        result = executeForMap(sqlSession, args);
      } else {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = sqlSession.selectOne(command.getName(), param);
      }
    } else if (SqlCommandType.FLUSH == command.getType()) {
        result = sqlSession.flushStatements();
    } else {
      throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
  
//....其他方法



//两个内部类
public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      String statementName = mapperInterface.getName() + "." + method.getName();
      MappedStatement ms = null;
      if (configuration.hasStatement(statementName)) {
        ms = configuration.getMappedStatement(statementName);
      } else if (!mapperInterface.equals(method.getDeclaringClass())) { // issue #35
        String parentStatementName = method.getDeclaringClass().getName() + "." + method.getName();
        if (configuration.hasStatement(parentStatementName)) {
          ms = configuration.getMappedStatement(parentStatementName);
        }
      }
      if (ms == null) {
        if(method.getAnnotation(Flush.class) != null){
          name = null;
          type = SqlCommandType.FLUSH;
        } else {
          throw new BindingException("Invalid bound statement (not found): " + statementName);
        }
      } else {
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }
  }
  //MethodSignature内部类
public static class MethodSignature {
//
    public Object convertArgsToSqlCommandParam(Object[] args) {
      final int paramCount = params.size();
      if (args == null || paramCount == 0) {
        return null;
      } else if (!hasNamedParameters && paramCount == 1) {
        return args[params.keySet().iterator().next().intValue()];
      } else {
        final Map<String, Object> param = new ParamMap<Object>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : params.entrySet()) {
          param.put(entry.getValue(), args[entry.getKey().intValue()]);
          // issue #71, add param names as param1, param2...but ensure backward compatibility
          final String genericParamName = "param" + String.valueOf(i + 1);
          if (!param.containsKey(genericParamName)) {
            param.put(genericParamName, args[entry.getKey()]);
          }
          i++;
        }
        return param;
      }
    }
    
    //省略其他

  }

}
```

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
代理执行sql的基本顺序是

MapperMethod.execute() --> DefaultSqlSession.selectOne  -->  BaseExecutor.query  -->  SimpleExecutor.doQuery  --> SimpleStatementHandler.query -->  DefaultResultSetHandler.handleResultSets(Statement stmt)  
