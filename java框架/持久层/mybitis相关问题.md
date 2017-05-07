## mybits相关问题记录
1、在执行sql中的

 org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'leave' in 'class org.activiti.web.simple.webapp.model.Leave'



这个问题在于在做insert操作时在mapper.xml文件中的属性parameterType="org.activiti.web.simple.webapp.model.Leave"

，而mybitis会通过反射直接调用注入的类，所以insert中的values中的#{userId},调用的就时类leave的get方法得到，并不是

leave.userId这种属性的调用的方式，所以之前#{leave.userId}的这种方式就是mybitis无法得在leave类中的知leave.userId的get方法



mybits映射的时候要保持mapper和dao方法参数保持一致（参数和实体类也需要保持一致）。例如如下问题：

Error querying database.  Cause: org.apache.ibatis.reflection.ReflectionException

导致该问题一：

这里出现的问题是在DAO方法中定义的参数 与 实体中定义的属性不一致 导致的。

解决方案：



dao层加@Param("userId")注解即可



public List<DictItem> selectKeyByUserId(@Param("userId") long userId);



问题二、

使用dao接口和mapper映射的过程中出现错误如下：

### Error querying database.  Cause: org.apache.ibatis.reflection.ReflectionException: Could not get property 'candidateGroups' from class org.activiti.engine.impl.TaskQueryImpl.  Cause: java.lang.StackOverflowError

### Cause: org.apache.ibatis.reflection.ReflectionException: Could not get property 'candidateGroups' from class org.activiti.engine.impl.TaskQueryImpl.  Cause: java.lang.StackOverflowError

。。。。。。。

Caused by: java.lang.StackOverflowError

	at com.xzg.managers.GroupManager.getUserById(GroupManager.java:52)

	at com.xzg.managers.GroupManager.getUserById(GroupManager.java:52)

	at com.xzg.managers.GroupManager.getUserById(GroupManager.java:52)

	at com.xzg.managers.GroupManager.getUserById(GroupManager.java:52)

	at com.xzg.managers.GroupManager.getUserById(GroupManager.java:52)

。。。。。。。。

。。。

分析解决方法：

出现这种问题的原因很多种，就我遇到的应该是属于mybitis链接数据库后connect没有释放导致。具体原因：

首先使用了UserMapper接口类和mapper的xml映射，让后分别使用UserManger和GroupManger继承UserMapper接口，并实现其方法。而后在activiti的自定义实现

调用中，两者均建立链接而后互相等待释放导致死锁现象。

解决办法：每一个接口对应不同的映射文件。
