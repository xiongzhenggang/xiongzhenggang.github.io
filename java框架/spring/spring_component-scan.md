### 在使用<context:component-scan> 有时候会出现加载不到bean、或者bean被加载多次、甚至有些事务不起作用。下面分析一下原因
1. 一般使用spring中会用到spring主容器和springmvc子容器，这是出现上述的主要原因
* 下面以spring的配置文件为application.xml springmvc配置文件为service-servlet.xml依次分析出现上述的原因
application.xml：
```xml
<context:component-scan base-package="cn.abcsys.cloud.devops.web">
	</context:component-scan>
```
service-servlet.xml：
```xml
<context:component-scan base-package="cn.abcsys.cloud.devops.web">
	</context:component-scan>
```
上面这样配置很显然会导致bean加载多次
---
下面添加过滤条件exclude-filter、include-filter来过滤掉相关的条件
application.xml：
```xml
<context:component-scan base-package="cn.abcsys.cloud.devops.web">
	<!-- exclude-filter：主容器中applicationContext.xml中，将Controller的注解排除掉 -->
		<context:exclude-filter type="annotation"
			expression="org.springframework.stereotype.Controller" />
	</context:component-scan>
```
service-servlet.xml：
```xml
<context:component-scan  base-package="cn.abcsys.cloud.devops.web"> 
		<context:include-filter type="annotation" expression="org.springframework.stereotype.Controller" /> 
	</context:component-scan>
```
但是仍然会多次加载bean，原因在于spring component-scan默认的加载过滤条件 use-default-filters="true"是开启的，所以
service等bean会再次被加载
---
更改如下
application.xml：
```xml
<context:component-scan base-package="cn.abcsys.cloud.devops.web">
	<!-- exclude-filter：主容器中applicationContext.xml中，将Controller的注解排除掉 -->
		<context:exclude-filter type="annotation"
			expression="org.springframework.stereotype.Controller" />
	</context:component-scan>
```
service-servlet.xml：
```xml
<!-- use-default-filters="false" 默认true导致会扫描@Component、@Repository、@Service和@Controller -->
	<context:component-scan  use-default-filters="false" base-package="cn.abcsys.cloud.devops.web"> 
		<context:include-filter type="annotation" expression="org.springframework.stereotype.Controller" /> 
	</context:component-scan>
```
这样springmvc子容器就只加载controller，而不会加载其他bean了
---

  
