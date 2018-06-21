## spring IOC 分析
### 1、需要了解spring的主要作用
### 一、 spring简概：[spring简述](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/java项目存档点/spring/spring_ioc原.md) .
### 二、 spring的核心组件，包括beans、core、context。其中是以beans为主体，context作为容器、core则是操作两者的桥梁。
### 三、 以下为spring启动的过程。理解下面的代码片段，就基本上理解spring ioc是如何工作的了。 该方法来源于AbstractApplicationContext类，所在的包为org.springframework.context.support。当然为了便于理解可以下载对应的源码，使用eclipse打上相应的断点运行查看。

手动生成application的容器 ApplicationContext ac =new FileSystemXmlApplicationContext("applicationContext.xml");

```xml
FileSystemXmlApplicationContext继承的为：FileSystemXmlApplicationContext
--->AbstractXmlApplicationContext
--->AbstractRefreshableConfigApplicationContext
--->AbstractRefreshableApplicationContext(implements BeanNameAware, InitializingBean)
---> AbstractApplicationContext
--->DefaultResourceLoader(implements ConfigurableApplicationContext, DisposableBean)
--->implements ResourceLoader
```

FileSystemXmlApplicationContext调用的构造函数如下：
```java
/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * @param configLocation file path
	 * @throws BeansException if context creation failed
	 */
	public FileSystemXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}
	
	public FileSystemXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {
//调用父类容器的构造方法(super(parent)方法)为容器设置好Bean资源加载器。
		super(parent);
//再调用父类AbstractRefreshableConfigApplicationContext的setConfigLocations(configLocations)方法设置Bean定义资源文件的定位路径。
		setConfigLocations(configLocations);
		//是否刷新容器
		if (refresh) {
			refresh();
		}
	}

```
2. 设置资源加载器和资源定位
super(parent);方法最终调用AbstractApplicationContext中的构造方法：
```java
/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		//资源定位
		this();
		//设置当前容器的父容器
		setParent(parent);
	}
```
setConfigLocations(configLocations);方法为AbstractRefreshableConfigApplicationContext类中的方法
```java
/**
	 * Set the config locations for this application context.
	 * <p>If not set, the implementation may use a default as appropriate.
	 */
	public void setConfigLocations(String... locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		}
		else {
			this.configLocations = null;
		}
	}
```
总结： 在创建FileSystemXmlApplicationContext容器时，构造方法做以下两项重要工作：
1） 调用父类容器的构造方法(super(parent)方法)为容器设置好Bean资源加载器。
2）再调用父类AbstractRefreshableConfigApplicationContext的setConfigLocations(configLocations)方法设置Bean定义资源文件的定位路径。

追踪FileSystemXmlApplicationContext的继承体系,其父类的父类AbstractApplicationContext中初始化IoC容器所做的主要源码如下：
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader  
        implements ConfigurableApplicationContext, DisposableBean {  
    //静态初始化块，在整个容器创建过程中只执行一次  
    static {  
        //为了避免应用程序在Weblogic8.1关闭时出现类加载异常加载问题，加载IoC容  
       //器关闭事件(ContextClosedEvent)类  
        ContextClosedEvent.class.getName();  
    }  
    //FileSystemXmlApplicationContext调用父类构造方法super(parent);调用的就是该方法 
	public AbstractApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}
//上面方法调用如下所示：
    /**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
		this.resourcePatternResolver = getResourcePatternResolver();
	}
    //上面的getResourcePatternResolver方法获取一个Spring Source的加载器用于读入Spring Bean定义资源文件  
    protected ResourcePatternResolver getResourcePatternResolver() {  
        // AbstractApplicationContext继承DefaultResourceLoader，也是一个
        //Spring资源加载器，其getResource(String location)方法用于载入资源  
        return new PathMatchingResourcePatternResolver(this);  
    }   
//setParent(parent);方法如下：
//Set the parent of this application context.
	@Override
	public void setParent(ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}
//............
}
```
AbstractApplicationContext构造方法中调用PathMatchingResourcePatternResolver的构造方法创建Spring资源加载器：
```java
public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {  
        Assert.notNull(resourceLoader, "ResourceLoader must not be null");  
        //设置Spring的资源加载器  
        this.resourceLoader = resourceLoader;  
} 
```
设置容器的资源加载器之后，接下来FileSystemXmlApplicationContet执行setConfigLocations方法通过调用其父类AbstractRefreshableConfigApplicationContext的方法进行对Bean定义资源文件的定位，该方法的源码如下：
```java
	 //处理单个资源文件路径为一个字符串的情况  
    public void setConfigLocation(String location) {  
       //String CONFIG_LOCATION_DELIMITERS = ",; /t/n";  
       //即多个资源文件路径之间用” ,; /t/n”分隔，解析成数组形式  
        setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));  
    }  

    //解析Bean定义资源文件的路径，处理多个资源文件字符串数组  
     public void setConfigLocations(String[] locations) {  
        if (locations != null) {  
            Assert.noNullElements(locations, "Config locations must not be null");  
            this.configLocations = new String[locations.length];  
            for (int i = 0; i < locations.length; i++) {  
                // resolvePath为同一个类中将字符串解析为路径的方法  
                this.configLocations[i] = resolvePath(locations[i]).trim();  
            }  
        }  
        else {  
            this.configLocations = null;  
        }  
    } 
```
通过这两个方法的源码我们可以看出，我们既可以使用一个字符串来配置多个Spring Bean定义资源文件，也可以使用字符串数组，即下面两种方式都是可以的：

a.    ClasspathResource res = new ClasspathResource(“a.xml,b.xml,……”);

多个资源文件路径之间可以是用” ,; /t/n”等分隔。

b.    ClasspathResource res = new ClasspathResource(newString[]{“a.xml”,”b.xml”,……});

至此，Spring IoC容器在初始化时将配置的Bean定义资源文件定位为Spring封装的Resource。
3. AbstractApplicationContext的refresh函数载入Bean定义过程：
Spring IoC容器对Bean定义资源的载入是从refresh()函数开始的，refresh()是一个模板方法，refresh()方法的作用是：在创建IoC容器前，如果已经有容器存在，则需要把已有的容器销毁和关闭，以保证在refresh之后使用的是新建立起来的IoC容器。refresh的作用类似于对IoC容器的重启，在新建立好的容器中对容器进行初始化，对Bean定义资源进行载入

* refresh()方法为AbstractApplicationContext类中的方法，为IOC容器加载的核心方法
FileSystemXmlApplicationContext通过调用其父类AbstractApplicationContext的refresh()函数启动整个IoC容器对Bean定义的载入过程：

``` java
public void refresh() throws BeansException, IllegalStateException {  
       synchronized (this.startupShutdownMonitor) {  
           //调用容器准备刷新的方法，获取容器的当前时间，同时给容器设置同步标识  
           prepareRefresh();  
           //告诉子类启动refreshBeanFactory()方法，Bean定义资源文件的载入从  
          //子类的refreshBeanFactory()方法启动  
           ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();  
           //为BeanFactory配置容器特性，例如类加载器、事件处理器等  
           prepareBeanFactory(beanFactory);  
           try {  
               //为容器的某些子类指定特殊的BeanPost事件处理器  
               postProcessBeanFactory(beanFactory);  
               //调用所有注册的BeanFactoryPostProcessor的Bean  
               invokeBeanFactoryPostProcessors(beanFactory);  
               //为BeanFactory注册BeanPost事件处理器.  
               //BeanPostProcessor是Bean后置处理器，用于监听容器触发的事件  
               registerBeanPostProcessors(beanFactory);  
               //初始化信息源，和国际化相关.  
               initMessageSource();  
               //初始化容器事件传播器.  
               initApplicationEventMulticaster();  
               //调用子类的某些特殊Bean初始化方法  
               onRefresh();  
               //为事件传播器注册事件监听器.  
               registerListeners();  
               //初始化所有剩余的单态Bean.  
               finishBeanFactoryInitialization(beanFactory);  
               //初始化容器的生命周期事件处理器，并发布容器的生命周期事件  
               finishRefresh();  
           }  
           catch (BeansException ex) {  
               //销毁以创建的单态Bean  
               destroyBeans();  
               //取消refresh操作，重置容器的同步标识.  
               cancelRefresh(ex);  
               throw ex;  
           }  
       }  
   }
```
refresh()方法主要为IoC容器Bean的生命周期管理提供条件，Spring IoC容器载入Bean定义资源文件从其子类容器的refreshBeanFactory()方法启动，所以整个refresh()中“ConfigurableListableBeanFactory beanFactory =obtainFreshBeanFactory();”这句以后代码的都是注册容器的信息源和生命周期事件，载入过程就是从这句代码启动。

refresh()方法的作用是：在创建IoC容器前，如果已经有容器存在，则需要把已有的容器销毁和关闭，以保证在refresh之后使用的是新建立起来的IoC容器。refresh的作用类似于对IoC容器的重启，在新建立好的容器中对容器进行初始化，对Bean定义资源进行载入

 AbstractApplicationContext的obtainFreshBeanFactory()方法调用子类容器的refreshBeanFactory()方法，启动容器载入Bean定义资源文件的过程，代码如下：
 ```java
  protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {  
        //这里使用了委派设计模式，父类定义了抽象的refreshBeanFactory()方法，具体实现调用子类容器的refreshBeanFactory()方法
         refreshBeanFactory();  
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();  
        if (logger.isDebugEnabled()) {  
            logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);  
        }  
        return beanFactory;  
    } 
 ```
 AbstractApplicationContext子类的refreshBeanFactory()方法：
AbstractApplicationContext类中只抽象定义了refreshBeanFactory()方法，容器真正调用的是其子类AbstractRefreshableApplicationContext实现的    refreshBeanFactory()方法，方法的源码如下：
```java
protected final void refreshBeanFactory() throws BeansException {  
       if (hasBeanFactory()) {//如果已经有容器，销毁容器中的bean，关闭容器  
           destroyBeans();  
           closeBeanFactory();  
       }  
       try {  
            //创建IoC容器  
            DefaultListableBeanFactory beanFactory = createBeanFactory();  
            beanFactory.setSerializationId(getId());  
           //对IoC容器进行定制化，如设置启动参数，开启注解的自动装配等  
           customizeBeanFactory(beanFactory);  
           //调用载入Bean定义的方法，主要这里又使用了一个委派模式，在当前类中只定义了抽象的loadBeanDefinitions方法，具体的实现调用子类容器  
           loadBeanDefinitions(beanFactory);  
           synchronized (this.beanFactoryMonitor) {  
               this.beanFactory = beanFactory;  
           }  
       }  
       catch (IOException ex) {  
           throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);  
       }  
   }
```
