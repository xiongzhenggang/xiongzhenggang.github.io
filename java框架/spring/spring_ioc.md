#### spring IOC核心详解
* <p>原理其实就是通过反射解析类及其类的各种信息，包括构造器、方法及其参数，属性。然后将其封装成bean定义信息类、constructor信息类、method信息类、property信息类，最终放在一个map里，也就是所谓的container、池等等，其实就是个map。当写好配置文件，启动项目后，框架会先按照你的配置文件找到那个要scan的包，然后解析包里面的所有类，找到所有含有@bean，@service等注解的类，利用反射解析它们，包括解析构造器，方法，属性等等，然后封装成各种信息类放到一个map里。每当你需要一个bean的时候，框架就会从container找是不是有这个类的定义啊？如果找到则通过构造器new出来（这就是控制反转，不用你new,框架帮你new），再在这个类找是不是有要注入的属性或者方法，比如标有@autowired的属性，如果有则还是到container找对应的解析类，new出对象，并通过之前解析出来的信息类找到setter方法，然后用该方法注入对象（这就是依赖注入）。如果其中有一个类container里没找到，则抛出异常，比如常见的spring无法找到该类定义，无法wire的异常。还有就是嵌套bean则用了一下递归，container会放到servletcontext里面，每次reQuest从servletcontext找这个container即可，不用多次解析类定义。如果bean的scope是singleton，则会重用这个bean不再重新创建，将这个bean放到一个map里，每次用都先从这个map里面找
</p>
	BeanFactory和BeanDefinition，一个是IOC的核心工厂接口，一个是IOC的bean定义接口，无法让BeanFactory持有一个Map<String,Object>来完成bean工厂的功能，是因为spring的初始化是可以控制的，可以到用的时候才将bean实例化供开发者使用，除非我们将bean的lazy-init属性设置为true，初始化bean工厂时采用延迟加载
	
* 最基本的IOC容器接口BeanFactory

```java
public interface BeanFactory {    
     
     //对FactoryBean的转义定义，因为如果使用bean的名字检索FactoryBean得到的对象是工厂生成的对象，    
     //如果需要得到工厂本身，需要转义           
     String FACTORY_BEAN_PREFIX = "&"; 
        
     //根据bean的名字，获取在IOC容器中得到bean实例    
     Object getBean(String name) throws BeansException;    
   
    //根据bean的名字和Class类型来得到bean实例，增加了类型安全验证机制。    
     Object getBean(String name, Class requiredType) throws BeansException;    
    
    //提供对bean的检索，看看是否在IOC容器有这个名字的bean    
     boolean containsBean(String name);    
    
    //根据bean名字得到bean实例，并同时判断这个bean是不是单例    
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;    
    
    //得到bean实例的Class类型    
    Class getType(String name) throws NoSuchBeanDefinitionException;    
    
    //得到bean的别名，如果根据别名检索，那么其原名也会被检索出来    
   String[] getAliases(String name);    
    
 }
```
* BeanDefinition的源码
```java
/**
 * BeanDefinition描述了一个bean实例，它具有属性值，构造函数参数值以及具体实现提供的更多信息。
 *
 * <p>这只是一个最小的接口：其主要目的是允许像{@link PropertyPlaceholderConfigurer}和{@link BeanFactoryPostProcessor}
 * 反射修改属性值和其他bean元数据。
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	//作用域标识符
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;
	//作用域标识符
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;
	/**
	 * Role hint indicating that a {@code BeanDefinition} is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 */
	int ROLE_APPLICATION = 0;
	/**
	 * Role hint indicating that a {@code BeanDefinition} is a supporting
	 * part of some larger configuration, typically an outer
	 */
	int ROLE_SUPPORT = 1;
	/**
	 * Role hint indicating that a {@code BeanDefinition} is providing an
	 * entirely background role and has no relevance to the end-user. This hint is
	 * used when registering beans that are completely part of the internal workings
	 * of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 */
	int ROLE_INFRASTRUCTURE = 2;
	// Modifiable attributes

	//设置这个bean定义的父定义的名字（如果有的话）
	void setParentName(String parentName);

	//返回这个bean定义的父定义的名字，如果有的话。 
	String getParentName();

	/**
	 * 指定此bean定义的bean类名称。
	 * <p>The class name can be modified during bean factory post-processing,
	 * typically replacing the original class name with a parsed variant of it.
	 */
	void setBeanClassName(String beanClassName);

	//Return the current bean class name of this bean definition. 
	String getBeanClassName();

	/**
	 * Override the target scope of this bean, specifying a new scope name.
	 */
	void setScope(String scope);

	// 返回此bean的当前目标作用域的名称， 
	String getScope();

	//设置是否延迟加载
	void setLazyInit(boolean lazyInit);

	//返回是否延迟加载
	boolean isLazyInit();

	/**
	 *设置这个bean依赖于被初始化的bean的名字。
 	* bean工厂将保证这些bean首先被初始化。
	 */
	void setDependsOn(String... dependsOn);

	/**
	 * 返回依赖的bean
	 */
	String[] getDependsOn();

	/**
	 * 设置这个bean是否可以让自动装配成为其他bean。
	 * <p>Note that this flag is designed to only affect type-based autowiring.
	 * It does not affect explicit references by name, which will get resolved even
	 * if the specified bean is not marked as an autowire candidate. As a consequence,
	 * autowiring by name will nevertheless inject a bean if the name matches.
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 *返回这个bean是否是一个获得自动装入其他bean的候选者。
	 */
	boolean isAutowireCandidate();

	/**
	 * 设置这个bean是否是主要的autowire候选者。
	 * <p>If this value is {@code true} for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	void setPrimary(boolean primary);

	//返回这个bean是否是主要的autowire候选者。
	boolean isPrimary();

	/**
	 *指定要使用的工厂bean（如果有）。
         *这是调用指定工厂方法的bean的名称。
	 * @see #setFactoryMethodName
	 */
	void setFactoryBeanName(String factoryBeanName);
	
	 // Return the factory bean name, if any.
	String getFactoryBeanName();

	/**
	 * 指定工厂方法（如果有的话）。 如果没有指定任何参数，将使用构造函数参数调用此方法，或者不使用参数。
	 *方法将在指定的工厂bean上调用，如果有的话，
	* 或者作为本地bean类的静态方法。
	 */
	void setFactoryMethodName(String factoryMethodName);

	// Return a factory method, if any.
	String getFactoryMethodName();

	//返回此bean的构造函数参数值
	ConstructorArgumentValues getConstructorArgumentValues();

	//返回要应用于bean的新实例的属性值。
	MutablePropertyValues getPropertyValues();

	// Read-only attributes
	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	//Return whether this bean is "abstract", that is, not meant to be instantiated.
	boolean isAbstract();
	/**
	 * Get the role hint for this {@code BeanDefinition}. The role hint
	 * provides the frameworks as well as tools with an indication of
	 * the role and importance of a particular {@code BeanDefinition}.
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();
	//返回这个bean定义的可读描述。
	String getDescription();
	//返回这个bean定义的资源的描述
	String getResourceDescription();
	//返回原始BeanDefinition，否则返回{null}。
	BeanDefinition getOriginatingBeanDefinition();

}
```
* 简单的基础的ioc分析
XmlBeanFactory(IOC)的整个流程:

```java
public class XmlBeanFactory extends DefaultListableBeanFactory{
     private final XmlBeanDefinitionReader reader; 

     public XmlBeanFactory(Resource resource)throws BeansException{
         this(resource, null);
     }  
     public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory)
          throws BeansException{
         super(parentBeanFactory);
         this.reader = new XmlBeanDefinitionReader(this);
         this.reader.loadBeanDefinitions(resource);
    }
 }
```
上面可分解如下：
```java
//根据Xml配置文件创建Resource资源对象，该对象中包含了BeanDefinition的信息
 ClassPathResource resource =new ClassPathResource("application-context.xml");
//创建DefaultListableBeanFactory
 DefaultListableBeanFactory factory =new DefaultListableBeanFactory();
//创建XmlBeanDefinitionReader读取器，用于载入BeanDefinition。之所以需要BeanFactory作为参数，是因为会将读取的信息回调配置给factory
 XmlBeanDefinitionReader reader =new XmlBeanDefinitionReader(factory);
//XmlBeanDefinitionReader执行载入BeanDefinition的方法，最后会完成Bean的载入和注册。完成后Bean就成功的放置到IOC容器当中，以后我们就可以从中取得Bean来使用
 reader.loadBeanDefinitions(resource);
 ```

1. bean的初始化过程
容器的初始化包括BeanDefinition的Resource定位、载入和注册这三个基本的过程。我们以ApplicationContext为例讲解，ApplicationContext系列容器也许是我们最熟悉的，因为web项目中使用的XmlWebApplicationContext就属于这个继承体系，还有ClasspathXmlApplicationContext等

```
afterPropertiesSet与init-method

(1)、init-method方法，初始化bean的时候执行，可以针对某个具体的bean进行配置。init-method需要在applicationContext.xml配置文档中bean的定义里头写明。例如：<bean id="TestBean" class="nju.software.xkxt.util.TestBean" init-method="init"></bean>
这样，当TestBean在初始化的时候会执行TestBean中定义的init方法。  
(2)、afterPropertiesSet方法，初始化bean的时候执行，可以针对某个具体的bean进行配置。afterPropertiesSet 必须实现 InitializingBean接口。实现 InitializingBean接口必须实现afterPropertiesSet方法。 InitializingBean是一个接口，它仅仅包含一个方法：afterPropertiesSet()。Spring要求init-method是一个无参数的方法，如果init-method指定的方法中有参数，那么Spring将会抛出异常init-method指定的方法可以是public、protected以及private的，并且方法也可以是final的。
(3)、BeanPostProcessor，针对所有Spring上下文中所有的bean，可以在配置文档applicationContext.xml中配置一个BeanPostProcessor，然后对所有的bean进行一个初始化方法之前和之后的代理。BeanPostProcessor接口中有两个方法： postProcessBeforeInitialization和postProcessAfterInitialization。前者postProcessBeforeInitialization在实例化及依赖注入完成后、在任何初始化代码（比如配置文件中的init-method）调用之前调用；后者postProcessAfterInitialization在初始化代码调用之后调用
 postProcessBeforeInitialization方法在bean初始化之前执行， postProcessAfterInitialization方法在bean初始化之后执行。
 ```
 
2. ApplicationContext允许上下文嵌套，通过保持父上下文可以维持一个上下文体系。对于bean的查找可以在这个上下文体系中发生，首先检查当前上下文，其次是父上下文，逐级向上，这样为不同的Spring应用提供了一个共享的bean定义环境。
使用手动加载spring bean的方式：

[applicationContext的典型的Ioc分析](https://github.com/xiongzhenggang/xiongzhenggang.github.io/edit/master/java%E6%A1%86%E6%9E%B6/spring/spring_AppConIoc.md)

3 Spring bean作用域与生命周期
实例化。Spring通过new关键字将一个Bean进行实例化，JavaBean都有默认的构造函数，因此不需要提供构造参数。填入属性。Spring根据xml文件中的配置通过调用Bean中的setXXX方法填入对应的属性。事件通知。Spring依次检查Bean是否实现了BeanNameAware、BeanFactoryAware、ApplicationContextAware、BeanPostProcessor、InitializingBean接口，如果有的话，依次调用这些接口。使用。应用程序可以正常使用这个Bean了。销毁。如果Bean实现了DisposableBean接口，就调用其destroy方法。

注意:如果bean的scope设为prototype时，当ctx.close时，destroy方法不会被调用.

原因：对于prototype作用域的bean，有一点非常重要，那就是Spring不能对一个prototype bean的整个生命周期负责：容器在初始化、配置、装饰或者是装配完一个prototype实例后，将它交给客户端，随后就对该prototype实例不闻不问了。不管何种作用域，容器都会调用所有对象的初始化生命周期回调方法。但对prototype而言，任何配置好的析构生命周期回调方法都将不会 被调用。清除prototype作用域的对象并释放任何prototype bean所持有的昂贵资源，都是客户端代码的职责。（让Spring容器释放被prototype作用域bean占用资源的一种可行方式是，通过使用bean的后置处理器，该处理器持有要被清除的bean的引用。）谈及prototype作用域的bean时，在某些方面你可以将Spring容器的角色看作是Java new 操作的替代者。任何迟于该时间点的生命周期事宜都得交由客户端来处理。

4 BeanDefinition的载入和解析

对IoC容器来说，这个载入过程，相当于把定义的BeanDefinition在IoC容器中转化成一个Spring内部表示的数据结构的过程。IoC容器对Bean的管理和依赖注入功能的实现，是通过对其持有的BeanDefinition进行各种相关操作来完成的。这些BeanDefinition数据在IoC容器中通过一个HashMap来保持和维护。当然这只是一种比较简单的维护方式，如果需要提高IoC容器的性能和容量，完全可以自己做一些扩展。

IoC容器的初始化入口，也就是看一下refresh方法。这个方法的最初是在FileSystemXmlApplicationContext的构造函数中被调用的，它的调用标志着容器初始化的开始，
这些初始化对象就是Bean. 

5 Spring容器初始化过程

spring的IoC容器初始化包括：Bean定义资源文件的定位、载入和注册3个基本过程。当 BeanDefinition 注册完毕以后， Spring Bean 工厂就可以随时根据需要进行实例化了。对于 XmlBeanFactory 来说，实例化默认是延迟进行的，也就是说在 getBean 的时候才会；而对于 ApplicationContext 来说，实例化会在容器启动后通过AbstractApplicationContext 中 reflash 方法自动进行，主要经过方法链： reflesh() finishBeanFactoryInitialization (factory) DefaultListableBeanFactory.preInstantiateSingletons (), 在这里会根据注册的 BeanDefinition 信息依此调用 getBean(beanName) 。而真正实例化的逻辑和 BeanFactory 是“殊途同归”的，所有有关 Bean 实例化都可以从 getBean(beanName) 入手。IoC容器和上下文初始化一般不包含Bean依赖注入的实现。一般而言，依赖注入发送在应用第一次通过getBean方法向容器获取Bean时。但是有个特例是：IoC容器预实例化配置的lazyinit属性，如果某个Bean设置了lazyinit属性，则该Bean的依赖注入在IoC容器初始化时就预先完成了
5 如何启动spring容器:在Web项目中，启动Spring容器的方式有三种，ContextLoaderListener、ContextLoadServlet、ContextLoaderPlugin。

5 ApplicationContext和beanfactory区别:BeanFacotry是spring中比较原始的Factory。如XMLBeanFactory就是一种典型的BeanFactory。原始的BeanFactory无法支持spring的许多插件，如AOP功能、Web应用等。 ApplicationContext接口,它由BeanFactory接口派生而来，因而提供BeanFactory所有的功能
