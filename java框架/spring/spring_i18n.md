## spring mvc 国际化的几种方案
1. 一.基于浏览器请求的国际化实现：
首先配置我们项目的springservlet-config.xml文件添加的内容如下：
```
<bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
    <!-- 国际化信息所在的文件名,根据ResourceBundleMessageSource类加载资源文件.\src\main\resources\messages\messages_en_US.properties -->                     
    <property name="basename" value="messages/messages" />   
    <!-- 如果在国际化资源文件中找不到对应代码的信息，就用这个代码作为名称  -->               
    <property name="useCodeAsDefaultMessage" value="true" />           
</bean>
```


