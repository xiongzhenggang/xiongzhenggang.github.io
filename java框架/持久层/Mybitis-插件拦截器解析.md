### Mybitis 源码插件拦截器解析

1. 如何使用，官网提供如下使用方式

1）org.apache.ibatis.plugin包下，MyBatis拦截器的接口定义：
```java
/**
 * @author Clinton Begin
 */
public interface Interceptor {

  Object intercept(Invocation invocation) throws Throwable;

  Object plugin(Object target);

  void setProperties(Properties properties);

}
```
2） MyBatis默认没有一个拦截器接口的实现类，开发者们可以实现符合自己需求的拦截器。下面例子MyBatis官网的一个拦截器实例：
```java
@Intercepts({@Signature(type= Executor.class,method = "update",args = {MappedStatement.class,Object.class})})
public class ExamplePlugin implements Interceptor {
  public Object intercept(Invocation invocation) throws Throwable {
    return invocation.proceed();
  }
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }
  public void setProperties(Properties properties) {
  }
}
```
3) 在mybitis的配置文件中添加插件配置
```xml
<plugins>
    <plugin interceptor="org.format.mybatis.cache.interceptor.ExamplePlugin"></plugin>
</plugins>
```
* 这个拦截器拦截Executor接口的update方法（其实也就是SqlSession的新增，删除，修改操作），所有执行executor的update方法都会被该拦截器拦截到。

2. 总结

MyBatis 允许你在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：

    Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
    ParameterHandler (getParameterObject, setParameters)
    ResultSetHandler (handleResultSets, handleOutputParameters)
    StatementHandler (prepare, parameterize, batch, update, query)

我们看到了可以拦截Executor接口的部分方法，比如update，query，commit，rollback等方法，还有其他接口的一些方法等。

总体概括为：

    拦截执行器的方法
    拦截参数的处理
    拦截结果集的处理
    拦截Sql语法构建的处理
    
2.  源码分析
首先从源头->配置文件开始分析xml文件，解析配置文件类XMLConfigBuilder
```java
//加载配置文件核心方法
private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      propertiesElement(root.evalNode("properties"));
      typeAliasesElement(root.evalNode("typeAliases"));
      //解析加载插件
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectionFactoryElement(root.evalNode("reflectionFactory"));
      settingsElement(root.evalNode("settings"));
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }
  
   private void pluginElement(XNode parent) throws Exception {
    if (parent != null) {
    //解析所有插件的子节点
      for (XNode child : parent.getChildren()) {
        String interceptor = child.getStringAttribute("interceptor");
        //获取name和value
        Properties properties = child.getChildrenAsProperties();
        //反射生成实例对象（ 其实使用Class.forName(name, true, cl)）
        Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
        //设置属性
        interceptorInstance.setProperties(properties);
        //将实现的拦截器添加的连接器链上（configuration为XMLConfigBuilder的父类BaseBuilder中的一个属性）
        configuration.addInterceptor(interceptorInstance);
      }
    }
  }
  //............省略其他
```
上面 configuration 中的拦截器链InterceptorChain 的类如下：
```java
public class InterceptorChain {
//list集合包含所有的用户添加的拦截器
  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }
  
  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
```
3. 为何拦截器会拦截，在Configuration中有下面部分代码（都是关于拦截Executor，ParameterHandler，ResultSetHandler，StatementHandler）：
```java
 public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
      ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
  }

  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }
```
以上4个方法都是Configuration的方法。这些方法在MyBatis的一个操作(新增，删除，修改，查询)中都会被执行到，执行的先后顺序是Executor，ParameterHandler，ResultSetHandler，StatementHandler(其中ParameterHandler和ResultSetHandler的创建是在创建StatementHandler[3个可用的实现类CallableStatementHandler,PreparedStatementHandler,SimpleStatementHandler]的时候，其构造函数调用的[这3个实现类的构造函数其实都调用了父类BaseStatementHandler的构造函数])。

这4个方法实例化了对应的对象之后，都会调用interceptorChain的pluginAll方法，InterceptorChain的pluginAll刚才已经介绍过了，就是遍历所有的拦截器，然后调用各个拦截器的plugin方法。注意：拦截器的plugin方法的返回值会直接被赋值给原先的对象

由于可以拦截StatementHandler，这个接口主要处理sql语法的构建，因此比如分页的功能，可以用拦截器实现，只需要在拦截器的plugin方法中处理StatementHandler接口实现类中的sql即可，可使用反射实现。

MyBatis还提供了 @Intercepts和 @Signature关于拦截器的注解。官网的例子就是使用了这2个注解，还包括了Plugin类的使用(一个分页插件的源码)
```java
@Override
public Object plugin(Object target) {
    return Plugin.wrap(target, this);
}
```

下面我们就分析这3个 "新组合" 的源码，首先Plugin类（Plugin类实现了InvocationHandler接口，这里返回了一个JDK自身提供的动态代理类）的wrap方法：
```java
  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }
  
/* 首先会拿到拦截器这个类的 @Interceptors注解，然后拿到这个注解的属性 @Signature注解集合，然后遍历这个集合，遍历的时候拿出 @Signature注解的type属性(Class类型)，然后根据这个type得到带有method属性和args属性的Method。由于 @Interceptors注解的 @Signature属性是一个属性，所以最终会返回一个以type为key，value为Set<Method>的Map。*/
   private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());      
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<Class<?>, Set<Method>>();
    for (Signature sig : sigs) {
      Set<Method> methods = signatureMap.get(sig.type());
      if (methods == null) {
        methods = new HashSet<Method>();
        signatureMap.put(sig.type(), methods);
      }
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }
```
getSignatureMap方法： 首先会拿到拦截器这个类的 @Interceptors注解，然后拿到这个注解的属性 @Signature注解集合，然后遍历这个集合，遍历的时候拿出 @Signature注解的type属性(Class类型)，然后根据这个type得到带有method属性和args属性的Method。由于 @Interceptors注解的 @Signature属性是一个属性，所以最终会返回一个以type为key，value为Set<Method>的Map.
下面为一个分页插件的源码
  
```java
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    //sql
    private SqlUtil sqlUtil;
    //
    private Properties properties;
    //
    private Boolean autoDialect;

    public static Page startPage(int pageNum, int pageSize) {
        return startPage(pageNum, pageSize, true);
    }

   
    public static Page startPage(int pageNum, int pageSize, boolean count) {
        return startPage(pageNum, pageSize, count, null);
    }


    public static Page startPage(int pageNum, int pageSize, String orderBy) {
        orderBy(orderBy);
        return startPage(pageNum, pageSize);
    }

    public static Page startPage(int pageNum, int pageSize, boolean count, Boolean reasonable) {
        return startPage(pageNum, pageSize, count, reasonable, null);
    }

    public static Page startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page page = new Page(pageNum, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        SqlUtil.setLocalPage(page);
        return page;
    }
    public static Page startPage(Object params) {
        Page page = SqlUtil.getPageFromObject(params);
        SqlUtil.setLocalPage(page);
        return page;
    }

    public static void orderBy(String orderBy) {
        OrderByHelper.orderBy(orderBy);
    }

 
    public Object intercept(Invocation invocation) throws Throwable {
        if (autoDialect) {
            initSqlUtil(invocation);
        }
        return sqlUtil.processPage(invocation);
    }

    public synchronized void initSqlUtil(Invocation invocation) {
        if (sqlUtil == null) {
            String url = null;
            try {
                MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
                MetaObject msObject = SystemMetaObject.forObject(ms);
                DataSource dataSource = (DataSource) msObject.getValue("configuration.environment.dataSource");
                url = dataSource.getConnection().getMetaData().getURL();
            } catch (SQLException e) {
                throw new RuntimeException("鍒嗛〉鎻掍欢鍒濆鍖栧紓甯�:" + e.getMessage());
            }
            if (url == null || url.length() == 0) {
                throw new RuntimeException("鏃犳硶鑷姩鑾峰彇jdbcUrl锛岃鍦ㄥ垎椤垫彃浠朵腑閰嶇疆dialect鍙傛暟!");
            }
            String dialect = Dialect.fromJdbcUrl(url);
            if (dialect == null) {
                throw new RuntimeException("鏃犳硶鑷姩鑾峰彇鏁版嵁搴撶被鍨嬶紝璇烽�氳繃dialect鍙傛暟鎸囧畾!");
            }
            sqlUtil = new SqlUtil(dialect);
            sqlUtil.setProperties(properties);
            properties = null;
            autoDialect = false;
        }
    }

   
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    public void setProperties(Properties p) {
        //MyBatis3.2.0鐗堟湰鏍￠獙
        try {
            Class.forName("org.apache.ibatis.scripting.xmltags.SqlNode");//SqlNode鏄�3.2.0涔嬪悗鏂板鐨勭被
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("鎮ㄤ娇鐢ㄧ殑MyBatis鐗堟湰澶綆锛孧yBatis鍒嗛〉鎻掍欢PageHelper鏀寔MyBatis3.2.0鍙婁互涓婄増鏈�!");
        }
        
        String dialect = p.getProperty("dialect");
        if (dialect == null || dialect.length() == 0) {
            autoDialect = true;
            this.properties = p;
        } else {
            autoDialect = false;
            sqlUtil = new SqlUtil(dialect);
            sqlUtil.setProperties(p);
        }
    }
}
```
上面的注解@Interceptors注解会返回一个key为Executor，value为集合(这个集合只有一个元素，也就是Method实例，这个Method实例就是Executor接口的query方法，且这个方法带有MappedStatement和Object类型的参数)。这个Method实例是根据 @Signature的method和args属性得到的。如果args参数跟type类型的method方法对应不上，那么将会抛出异常。

getAllInterfaces方法
```java
 private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

```
getAllInterfaces方法解释：根据目标实例target(这个target就是之前所说的MyBatis拦截器可以拦截的类，Executor,ParameterHandler,ResultSetHandler,StatementHandler)和它的父类们，返回signatureMap中含有target实现的接口数组。

所以Plugin这个类的作用就是根据 @Interceptors注解，得到这个注解的属性 @Signature数组，然后根据每个 @Signature注解的type，method，args属性使用反射找到对应的Method。最终根据调用的target对象实现的接口决定是否返回一个代理对象替代原先的target对象。

比如MyBatis官网的例子，当Configuration调用newExecutor方法的时候，由于Executor接口的update(MappedStatement ms, Object parameter)方法被拦截器被截获。因此最终返回的是一个代理类Plugin，而不是Executor。这样调用方法的时候，如果是个代理类，那么会执行：

```java
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
}
```
如果找到对应的方法被代理之后，那么会执行Interceptor接口的interceptor方法。个Invocation类如下:
```java
public class Invocation {

  private Object target;
  private Method method;
  private Object[] args;

  public Invocation(Object target, Method method, Object[] args) {
    this.target = target;
    this.method = method;
    this.args = args;
  }

  public Object getTarget() {
    return target;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArgs() {
    return args;
  }

  public Object proceed() throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, args);
  }

}
```
它的proceed方法也就是调用原先方法(不走代理)。

4. 总结

MyBatis拦截器接口提供的3个方法中，plugin方法用于某些处理器(Handler)的构建过程。interceptor方法用于处理代理类的执行。setProperties方法用于拦截器属性的设置。

其实MyBatis官网提供的使用 @Interceptors和 @Signature注解以及Plugin类这样处理拦截器的方法，我们不一定要直接这样使用。我们也可以抛弃这3个类，直接在plugin方法内部根据target实例的类型做相应的操作。

总体来说MyBatis拦截器还是很简单的，拦截器本身不需要太多的知识点，但是学习拦截器需要对MyBatis中的各个接口很熟悉，因为拦截器涉及到了各个接口的知识点。
