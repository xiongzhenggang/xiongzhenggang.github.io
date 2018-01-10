## spring mvc 国际化的几种方案

首先配置我们项目的service-servlet.xml文件添加的内容如下：
```xml
<bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
    <!-- 国际化信息所在的文件名,根据ResourceBundleMessageSource类加载资源文件.\src\main\resources\messages\messages_en_US.properties -->                     
    <property name="basename" value="messages/messages" />   
    <!-- 如果在国际化资源文件中找不到对应代码的信息，就用这个代码作为名称  -->               
    <property name="useCodeAsDefaultMessage" value="true" />           
</bean>
1. 一.基于浏览器请求的国际化实现：
```
使用Controller测试，
```java
@RequestMapping(value="/test",method={RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Result test(HttpServletRequest request){
 public Result test(HttpServletRequest request,Model model){           
            //从后台代码获取国际化信息
        RequestContext requestContext = new RequestContext(request);
        String msg = requestContext.getMessage("msg");
        return new Result(true, msg, "返回数据");
	}
```
通过设置浏览器请求测试：http://localhost:8080/xxx/nation/test
* 注意： 上述基于浏览器设置，根据浏览器的本地来确定message
2. 基于session的国际化
在项目中的源文件夹resources/messages中添加messages.properties、messages_zh_CN.properties、messages_en_US.properties三个文件，其中messages.properties、messages_zh_CN.properties里面添加msg="\u662F\u4E0D\u662F"为中文，messages_en_US.properties里面的为msg="ok"。
在项目的service-servlet.xml文件添加的内容如下,(之前ResourceBundleMessageSource的配置任然保留)
```xml
<mvc:interceptors>  
    <!-- 国际化操作拦截器 如果采用基于（请求/Session/Cookie）则必需配置 --> 
    <bean class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor" />  
</mvc:interceptors>  
<bean id="localeResolver" class="org.springframework.web.servlet.i18n.SessionLocaleResolver" />
```
使用controller测试
```java
@RequestMapping(value="/test",method={RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Result test(HttpServletRequest request, @RequestParam(value="langType", defaultValue="zh") String langType){

        if(langType.equals("zh")){
                Locale locale = new Locale("zh", "CN"); 
                request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale); 
            }
            else if(langType.equals("en")){
                Locale locale = new Locale("en", "US"); 
                request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale);
            }else{
	    request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,LocaleContextHolder.getLocale());
	    }            		
      //从后台代码获取国际化信息
        RequestContext requestContext = new RequestContext(request);
        String msg = requestContext.getMessage("msg");
        return new Result(true, msg, "返回数据");
	}
```
通过设置浏览器请求测试：http://localhost:8080/xxx/nation/test?langType=zh 或者 http://localhost:8080/xxx/nation/test?langType=en
3. 基于cookie，与session类似
移除session国际化的设置
```xml
<bean id="localeResolver" class="org.springframework.web.servlet.i18n.SessionLocaleResolver" /> 
```
添加cookie设置
```xml
<bean id="localeResolver" class="org.springframework.web.servlet.i18n.CookieLocaleResolver">
	 <!-- 设置cookieName名称，可以根据名称通过js来修改设置，也可以像上面演示的那样修改设置，默认的名称为 类名+LOCALE（即：org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE-->
    <property name="cookieName" value="lang"/>
    <!-- 设置最大有效时间，如果是-1，则不存储，浏览器关闭后即失效，默认为Integer.MAX_INT-->
    <property name="cookieMaxAge" value="100000" />
    <!-- 设置cookie可见的地址，默认是“/”即对网站所有地址都是可见的，如果设为其它地址，则只有该地址或其后的地址才可见-->
    <property name="cookiePath" value="/" />
</bean>
```
使用Controller测试
```java
@RequestMapping(value="/test",method={RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Result test(HttpServletRequest request, HttpServletResponse response,@RequestParam(value="langType", defaultValue="zh") String langType){

        if(langType.equals("zh")){
            Locale locale = new Locale("zh", "CN"); 
            (new CookieLocaleResolver()).setLocale (request, response, locale);
        }else if(langType.equals("en")){
            Locale locale = new Locale("en", "US"); 
            (new CookieLocaleResolver()).setLocale (request, response, locale);
        }else 
            (new CookieLocaleResolver()).setLocale (request, response, LocaleContextHolder.getLocale());
      //从后台代码获取国际化信息
        RequestContext requestContext = new RequestContext(request);
        String msg = requestContext.getMessage("msg");
        return new Result(true, msg, "返回数据");
	}
```
4. 基于url国际化方式
配置如下,移除上述localeResolver的bean改为下面的：

```xml
<bean id="localeResolver" class="xx.xxx.xxx.UrlAcceptHeaderLocaleResolver"/>

```

UrlAcceptHeaderLocaleResolver为自定义实现，具体代码如下：

```java
public class UrlAcceptHeaderLocaleResolver extends AcceptHeaderLocaleResolver {

    private Locale urlLocal;

    public Locale resolveLocale(HttpServletRequest request) {
        
    	return urlLocal!=null?urlLocal:request.getLocale();
    } 

    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    	urlLocal = locale;
    }
  
}
```
* 之后就可以在请求的URL后附上 locale=zh_CN 或 locale=en_US 如 http://xxxxxxxx?locale=zh_CN 来改变语言了
