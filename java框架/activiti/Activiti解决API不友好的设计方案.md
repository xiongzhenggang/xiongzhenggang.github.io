## Activiti中彻底解决待办事项列表查询复杂、API不友好的设计方案
使用工作流引擎，一个非常重要的功能就是获取待办事项列表，在Activiti中，我们可以通过TaskService的相关API进行查询，这些API设计优雅，但是实际使用中往往不够方便，也缺乏灵活性，达不到技术解决方案的要求，主要有如下几个问题：
* 1.多数情况无法通过调用一个API满足需求，这时一个现实问题就是需要对结果集进行合并然后排序，这样就显得比较麻烦；
* 2.和项目业务表关联困难；
* 3.Activiti中相关查询返回的是Activiti定义的实体，这些实体包含的信息可能不够；
* 4.Activiti中的实体，可能和项目中的对象关系映射（ORM）冲突；
   
	鉴于上述原因，在一些大规模的项目中，Activiti提供的查询API，实际使用价值不大，我们需要另外寻找解决方案。在Activiti的查询API中，也提供原始SQL的查询接口，但是大量使用后，会发现代码不够优雅，维护困难。这个问题其实从开发者角度，查询时用用户的id，用最简单的SQL查询出来所有想要的信息是最理想的。分析上述缺点和需求后，我们认为通过API方式进行查询的话，总是有各种缺陷，因此把目标放在数据库上，如果能通过定义视图的方式解决问题，那么将彻底解决查询的方便性、灵活性、通用性问题。
        
	经过分析Activiti的数据库表，我们发现并不复杂，和待办事项有关系的表，包括ACT_RU_TASK、ACT_RU_IDENTITYLINK，ACT_RU_TASK中存储了任务相关信息，ACT_RU_IDENTITYLINK中存储了候选组和候选人信息，这里面一个比较重要的问题就是，Activiti中的候选组、候选人如何跟系统中的用户、组织、角色对应的问题，本文提供的解决方案，假定系统中有一张名为SYS_ROLE_USER的表，该表中存储了角色和用户的对应关系，并且Activiti中的候选组和角色是同一个概念，开发者的系统中具体是什么情况，需要开发者举一反三，本文仅提供一个设计思路。
        
	在Activiti中，对于一个节点，可分为受托人，候选人和候选组三种情况，后两种可以设置多个，用逗号分隔，对应到数据库中，会被拆分为ACT_RU_IDENTITYLINK的多条记录，这些我们都需要考虑，细节上可以通过UNION实现，下面是样例代码，该代码基于Oracle数据库，其他数据库的版本，稍后会说明。
```sql
CREATE VIEW V_TASKLIST AS
SELECT A.ID_ AS TASK_ID,
       A.PROC_INST_ID_ PROC_INST_ID,
       A.TASK_DEF_KEY_ AS ACT_ID,
       A.NAME_ AS ACT_NAME,
       A.ASSIGNEE_ AS ASSIGNEE,
       A.DELEGATION_ AS DELEGATION_ID,
       A.DESCRIPTION_ AS DESCRIPTION,
       TO_CHAR(A.CREATE_TIME_, 'YYYY-MM-DD HH24:MI:SS') AS CREATE_TIME,
       TO_CHAR(A.DUE_DATE_,'YYYY-MM-DD HH24:MI:SS') AS DUE_DATE,
       I.USER_ID CANDIDATE
  FROM ACT_RU_TASK A
  LEFT JOIN (SELECT DISTINCT * FROM (SELECT TASK_ID_, TO_CHAR(USER_ID_) USER_ID
                    FROM ACT_RU_IDENTITYLINK I, ACT_RU_TASK T
                      WHERE TASK_ID_ IS NOT NULL
                        AND USER_ID_ IS NOT NULL
                        AND I.TASK_ID_ = T.ID_
                        AND T.ASSIGNEE_ IS NULL
                        AND TYPE_ = 'candidate'
                     UNION
                     SELECT TASK_ID_, R.USER_ID
                       FROM ACT_RU_IDENTITYLINK I,SYS_ROLE_USER R,ACT_RU_TASK T
                      WHERE I.TASK_ID_ IS NOT NULL
                        AND I.GROUP_ID_ IS NOT NULL
                        AND I.TASK_ID_ = T.ID_
                        AND T.ASSIGNEE_ IS NULL
                        AND TYPE_ = 'candidate'
                        AND I.GROUP_ID_ = R.ROLE_ID)U) I--候选组和业务上的角色用户表关联
    ON A.ID_ = I.TASK_ID_
```
这个视图比较简单，主要查询了任务信息，如果还需要其他信息，比如和流程实例、流程定义等，可以自行增加其他的表关联，比如要和业务表关联需要一个很重要的字段就是BUSINESS_KEY_,这个和ACT_RU_EXECUTION表关联即可。
        这个视图定义好之后，代办查询可以用如下的更简洁的SQL实现：
```sql
SELECT * FROM V_TASKLIST WHERE ASSIGNEE = ：userId OR CANDIDATE = ：userId
```
这样的话，和业务表关联也非常的方便，也不会受到API的限制，也不涉及和系统的ORM兼容的问题，基本上想查询什么信息就能用一个简单的SQL查询到什么信息，基本可以作为一个通用的解决方案了。
        上述例子仅提供了Oracle的代码，对于兼容多数据库的设计，比较麻烦，各种数据库都对视图的创建做了较多的限制，比如SQLServer不能在SQL中写ORDER BY，MySQL中FROM字句不能嵌套子查询，以及不同数据库字段类型定义不同等，在我们的解决方案中，基本上就是把上述SQL做了拆分，定义了若干非常小的视图，然后V_TASKLIST视图再查询这些视图。具体上开发者可以灵活处理，本文不再展开。
### 方案二、调用IdentifyService接口完成同步
参考 ![链接地址](http://www.kafeitu.me/activiti/2012/04/23/synchronize-or-redesign-user-and-role-for-activiti.html)
该方案任然使用的act_id_*表，不过同步数据到用户自定表
### 方案三：自定义SessionFactory
* mybits作为持久层。
* 第一步：在application.xml中导入:<import resource="applicationContext-activiti.xml" />的activiti的配置文件，其中activiti的关键配置如下：
```xml
<bean id="processEngineConfiguration" class="org.activiti.spring.SpringProcessEngineConfiguration">
        <property name="dataSource" ref="dataSource" />
        <property name="transactionManager" ref="transactionManager" />
        <property name="databaseSchemaUpdate" value="true" />
        <property name="jobExecutorActivate" value="true" />
。。。
<!-- 自定义表单字段类型 ,自己的项目里加上这个类然后指向他,这里先使用activiti中自己的用户角色表 --> 
    <property name="customSessionFactories">
<list>
<bean class="com.xzg.workFlow.util.CustomUserEntityManagerFactory"/>
     <bean class="com.xzg.workFlow.util.CustomGroupEntityManagerFactory"/>
</list> </property> 
</bean>
```
其中这两个类就是覆盖activiti调用用户角的接口，通过重写他们来使用自定义的用户角色。

目录如下图：
![自定义的用户角色](/java框架/activiti/img/act01.png)
其中两个工程类是用来覆盖原来activiti自己的manager管理类
主要代码如下：
```java
//用户管理工厂类
public class CustomUserEntityManagerFactory implements SessionFactory {
	@Resource
	private CustomUserEntityManager customUserEntityManager;
	public Class<?> getSessionType() {
		return UserIdentityManager.class;
	}
	public Session openSession() {
		return customUserEntityManager;
	}
```
```java
//组管理工厂类
public class CustomGroupEntityManagerFactory implements SessionFactory {
	@Resource
	private CustomGroupEntityManager customGroupEntityManager; 
	public Class<?> getSessionType() {
		// TODO Auto-generated method stub
		return GroupIdentityManager.class;
	}
	public Session openSession() {
		return customGroupEntityManager;
	}
```
接下来就是使用自定义的管理类，但是自定义的管理类必须要继承activiti相应的接口，这是为了避免activiti工作流在调用api的时候和自定义不同。
主要代码如下：
```java
//使用自定义用户角色一般只需覆盖findUserById和findGroupsByUser方法
@Service
public class CustomUserEntityManager  extends UserEntityManager{
	@Resource(name="userManager")
 	private UserMapper	 mapper;
    @Override
	  public UserEntity findUserById(final String userCode) {
	        if (userCode == null)
	            return null;
	        try {
	            UserEntity userEntity = null;
	            com.xzg.domain.User bUser = mapper.getUserById(Long.valueOf(userCode));
	            userEntity = ActivitiUtils.toActivitiUser(bUser);
	            return userEntity;
	        } catch ( Exception e) {
	           e.printStackTrace();
	        }
	       return null ;
	    }
```
其他方法视情况自定。其中UserMapper是dao层。 
	     userEntity = ActivitiUtils.toActivitiUser(bUser);
这里的ActivitiUtils是一个共有转化类，目的是为了将activiti自身的用户和自定义的用户转化。同理：
```java
//使用自定义用户角色一般只需覆盖findGroupById和findGroupsByUser方法
@Service
public class CustomGroupEntityManager extends GroupEntityManager 
。。。
```
以下是ActivitiUtils的主要方法：
```java
/**
 * @author hasee
 * @TIME 2016年12月27日
 * 注意类的隐藏和实例创建
 */
public class ActivitiUtils {
    public static UserEntity  toActivitiUser(User bUser){ 
        UserEntity userEntity = new UserEntity();  
        userEntity.setId(bUser.getUserId().toString());  
        userEntity.setFirstName(bUser.getUserName());  
        userEntity.setLastName(bUser.getUserName());  
        userEntity.setPassword(bUser.getPassword());  
        userEntity.setEmail(bUser.getEmail());  
        userEntity.setRevision(1);  
        return userEntity;  
    }  
    public static GroupEntity  toActivitiGroup(Group bGroup){  
        GroupEntity groupEntity = new GroupEntity();  
        groupEntity.setRevision(1);  
        groupEntity.setType("assignment"); 
        groupEntity.setId(bGroup.getRoleId());  
        groupEntity.setName(bGroup.getRoleName() );  
        return groupEntity;  
    }  
      
    public static List<org.activiti.engine.identity.Group> toActivitiGroups(List<Group> bGroups){  
          
        List<org.activiti.engine.identity.Group> groupEntitys = new ArrayList<org.activiti.engine.identity.Group>();  
        for (Group bGroup : bGroups) {  
            GroupEntity groupEntity = toActivitiGroup(bGroup);  
            groupEntitys.add(groupEntity);  
        }  
        return groupEntitys;  
    }  
}
```

