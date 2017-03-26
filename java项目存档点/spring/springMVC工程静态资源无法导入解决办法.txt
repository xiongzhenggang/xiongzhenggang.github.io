静态资源不要WEB-INF目录下
办法1（要求是springMVC映射的是*.do而非/）、
直接在web.xml中设置spring mvc的过滤路径
 <servlet-name>spring-mvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>1</load-on-startup><!-- 大于等于0启动时加载此框架 -->
  </servlet>
  <servlet-mapping>
    <servlet-name>spring-mvc</servlet-name>
    <url-pattern>*.do</url-pattern>
  </servlet-mapping>
办法二(*.do）
方法. 修改web.xml文件，增加对静态资源的url映射，要加在org.springframework.web.servlet.DispatcherServlet的前面
如：
<servlet-mapping>
<servlet-name>default</servlet-name>
<url-pattern>*.js</url-pattern>
</servlet-mapping>
<servlet-mapping>
<servlet-name>default</servlet-name>
<url-pattern>*.css</url-pattern>
</servlet-mapping>
在web.xml中添加好配置后，在jsp页面就可以引用这些静态资源了
<script type="text/javascript" src="static/js/1.js"></script>
这里还需要说明的是：这种方法不能访问WEB-INF目录下的静态资源，也就是js目录必须是web根(可能是webapp,webContent等)目录下，否则是不能引用的；
如果放在WEB-INF目录下，即使你使用<c:url value=“/WEB-INF/js/jquery.js”>也是会出现404错误的。WEB-INF是Java的WEB应用的安全目录。所谓安全就是客户端无法访问，只有服务端可以访问的目录。
如果想在页面中直接访问其中的文件，必须通过web.xml文件对要访问的文件进行相应映射才能访问。
web.xml中这样配置就会产生，静态资源（js、css、images等）无法访问的问题。

方法三：当为<url-pattern>/</url-pattern>：可使用
jsp页面中导入静态资源的时候需要用<c:url>标签
例如：
<%@ taglib prefix="c" uri="http://Java.sun.com/jsp/jstl/core" %>
<script type="text/JavaScript" src='<c:url value="/js/jQuery.js"></c:url>'></script>

方法四;使用spring 3.0.4的新特性,在相应的 -servlet.xml中添加配置<mvc:resource>
如：
3.1 <mvc:resources location="/js/" mapping="/js/**" />
或
3.2 <mvc:resources location="/WEB-INF/js/" mapping="/js/**" />
这种方法我写了两个配置，不同的地方只是location的值，一个是“/js/”,
一个是“/WEB-INF/js/”；两种都可以，根据你自己的目录结构来引用。
这就说明使用这种方式可以引用WEB-INF目录下的静态资源；这里的mapping属性的值用了ant的通配符方式，
"/js/**"(两个"*")指location的值所表示的目录以及所有子目录；但是在jsp页面中引用时需要注意：