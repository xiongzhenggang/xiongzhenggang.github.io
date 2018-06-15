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
//MethodSignature收集方法中的参数，核心方法为convertArgsToSqlCommandParam（如果执行接口方法参数为null返回null，如果一个返回object，否则返回map）
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
      //查询操作的返回结果类型比较多，所以处理结果方法不同，稍后分析
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
    
    //省略其他
}
```
7. 我们以上面的INSERT类型分析，其他除select返回类型比较多使用的处理封装返回结果的方法也比较多，其他删除，更新和新增相同
```java
if (SqlCommandType.INSERT == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
    } 
```
8. 通过上面的源码可最终执行还是在SQLSession中执行，可查看mybitis默认实现类defaultSqlSession
```java
public class DefaultSqlSession implements SqlSession {

  private Configuration configuration;
  private Executor executor;

  private boolean autoCommit;
  private boolean dirty;

  public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
    this.configuration = configuration;
    this.executor = executor;
    this.dirty = false;
    this.autoCommit = autoCommit;
  }

  public DefaultSqlSession(Configuration configuration, Executor executor) {
    this(configuration, executor, false);
  }

  @Override
  public int insert(String statement, Object parameter) {
    return update(statement, parameter);
  }
  @Override
  public int update(String statement, Object parameter) {
    try {
      dirty = true;
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.update(ms, wrapCollection(parameter));
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
//省略其他方法
}
```
9. insert方法最终调用的update方法中的executor.update(ms, wrapCollection(parameter))，接下来我们分析mybitis的执行器executor，下面为其实现类BaseExecutor
```java
  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    //新增更新操作清除本地缓存
    clearLocalCache();
    //最终执行，BaseExecutor里的方法为抽象方法，可以查看具体实现SimpleExecutor
    return doUpdate(ms, parameter);
  }
  
  //省略其他方法
  }
```
SimpleExecutor实现类查看doUpdate方法的具体实现如下：
```java
@Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
    //获取配置
      Configuration configuration = ms.getConfiguration();
      //configuration中的方法newStatementHandler，并且根据MappedStatement ms的类型生成不同的StatementHandler（SimpleStatementHandler、PreparedStatementHandler、CallableStatementHandler）处理
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
      //做相关的准备工作获取连接、事务、软解析等
      newStatementHandler stmt = prepareStatement(handler, ms.getStatementLog());
      //最终执行
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }
  //Statement是jdbc最终执行器
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    Connection connection = getConnection(statementLog);
    stmt = handler.prepare(connection);
    handler.parameterize(stmt);
    return stmt;
  }
```
11. 继续分析handler.update(stmt)的方法具体实现，StatementHandler实现类之一的SimpleStatementHandler类中的方法如下：
```java
@Override
  public int update(Statement statement) throws SQLException {
  //获取封装后的sql语句
    String sql = boundSql.getSql();
   //获取执行的参数
    Object parameterObject = boundSql.getParameterObject();
    //获取主键的生成策略，主键生成策略mybitis主要两种这里暂不赘述
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    int rows;
    if (keyGenerator instanceof Jdbc3KeyGenerator) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
      //返回更新结果的数量
      rows = statement.getUpdateCount();
      //原理就是获得数据库的记录条数，然后加1，作为返回参数的值返回
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else if (keyGenerator instanceof SelectKeyGenerator) {
      statement.execute(sql);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else {
      statement.execute(sql);
      rows = statement.getUpdateCount();
    }
    return rows;
  }
 ```
 最终执行为实现java中sql包下statement接口的实现类（根据当前的数据源）， statement.execute()方法

12. 上面是通过执行结果后返回，xml文件的标签<insert>获取执行convertArgsToSqlCommandParam取得接口方法的参数，最终回到第6步骤，中的方法rowCountResult方法封装好对应的，查询的实现方法多种不再赘述。
sql后执行，封装放回结果。下面是rowCountResult的实现如下，查询的返回封装可自行查看。
	
```java
 private Object rowCountResult(int rowCount) {
    final Object result;
    if (method.returnsVoid()) {
      result = null;
    } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
      result = Integer.valueOf(rowCount);
    } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
      result = Long.valueOf(rowCount);
    } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
      result = Boolean.valueOf(rowCount > 0);
    } else {
      throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
    }
    return result;
  }
  //其他略
```

总结：代理执行查询sql的基本顺序是
MapperMethod.execute() --> DefaultSqlSession.update  -->  BaseExecutor.update  -->  SimpleExecutor.update  --> SimpleStatementHandler.update -->  Statement.execute()
其中 Statement.execute()的Statement为java定义jdbc的接口具体实现根据不同的数据源不同，例如我使用阿里的DruidPooledStatement来执行，方法源码如下：
```java
  @Override
    public final boolean execute(String sql, String columnNames[]) throws SQLException {
        checkOpen();

        incrementExecuteCount();
        transactionRecord(sql);

        conn.beforeExecute();
        try {
            return stmt.execute(sql, columnNames);
        } catch (Throwable t) {
            throw checkException(t);
        } finally {
            conn.afterExecute();
        }
    }
```


