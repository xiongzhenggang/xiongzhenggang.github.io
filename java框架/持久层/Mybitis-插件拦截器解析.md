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
        //反射生成实例对象
        Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
        //设置属性
        interceptorInstance.setProperties(properties);
        //将实现的拦截器添加的连接器链上
        configuration.addInterceptor(interceptorInstance);
      }
    }
  }
  //............省略其他
```

