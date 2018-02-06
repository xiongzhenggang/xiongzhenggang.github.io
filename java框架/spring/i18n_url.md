## 基于请求URL的国际化实现方式
### 原理：使用spring的request bean保存相应的国际化组件，这样保证同一个请求的国际化相同，也是在微服务处理国际化的一种方式。需要针对每个请求做不同的国际化
### 实现所以需要，相应的拦截器去处理对应请求域中的国际化组件
1. 配置文件：
* spring容器配置applicationContext.xml，中添加取得信息的messageSource，放在spring容器而非springmvc容器加载是因为代码中有Service的注解依赖于他
```xml
<!-- 国际换的service依赖于他，所以从mvc提到前面 -->
	<bean id="messageSource"
		class="org.springframework.context.support.ResourceBundleMessageSource">
		<!-- 国际化信息所在的文件名 -->
		<property name="basename" value="messages/messages" />
		<!-- 如果在国际化资源文件中找不到对应代码的信息，就用这个代码作为名称 -->
		<property name="useCodeAsDefaultMessage" value="true" />
	</bean>
  ```
  2. springmvc容器配置拦截器，注意拦截器的顺序，国际化的拦截器在处理请求的拦截器前面
  ```xml
  <mvc:interceptors>
    	 <mvc:interceptor>  
        <mvc:mapping path="/**"/>  
         <!-- 国际化操作拦截器 如果采用基于（请求/Session/Cookie）则必需配置 --> 
    	<bean  id="localeChangeInterceptor"
    			 class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor">
   	 		<property name="paramName" value="locale"/>
    	</bean>
    	</mvc:interceptor>  
        <mvc:interceptor>
            <!-- 需拦截的地址 -->
            <!--   级目录 -->
            <mvc:mapping path="/*" />
            <mvc:mapping path="/*/*" />
            <!-- 需排除拦截的地址 -->
            <mvc:exclude-mapping path="/*.html"/>
            <bean class="cn.xx.xx.xx.interceptor.ControllerInterceptor" />
        </mvc:interceptor>
    </mvc:interceptors>
	<!-- 基于url的国际化 id必须为localeResolver否则国际化组件无法识别，UrlAcceptHeaderLocaleResolver为自定义实现部分-->
<bean id="localeResolver" class="cn.abcsys.devops.application.service.UrlAcceptHeaderLocaleResolver"/>
  ```
3. UrlAcceptHeaderLocaleResolver作为localeResolver国际urlLocal
 ```java
 /**
 * Copyright: Copyright (c) 2018 LanRu-Caifu
 * @author xzg
 * 2018年2月6日
 * @ClassName: UrlAcceptHeaderLocaleResolver.java
 * @Description: 国际化拦截请求后对请求更改Local
 * @version: v1.0.0
 */
public class UrlAcceptHeaderLocaleResolver extends AcceptHeaderLocaleResolver {

    private Locale urlLocal;

    public Locale resolveLocale(HttpServletRequest request) {
        
    	return urlLocal != null?urlLocal:request.getLocale();
    } 
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    	urlLocal = locale;
    }
}
 ```
 4. spring中的request bean依赖于接口实现，下面是其接口和对应的实现类
  ```java
 public interface I18nSessionService {
	
	public void setRc(RequestContext rc);
	
	public void setRcByRequest(HttpServletRequest request);
	
	public String getMessage(String key); 
	
	public String getMessage(String key,Object info);
	
	public String getMessage(String key,Object...objects);
}
  ```
  * 下面注解主要为设置作用域为request，注入messageSource组件，并提供RequestContext用于切换语言配置国际化
  ```java
 @Component
@RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
public class I18nSessionServiceImpl implements I18nSessionService {

	@Autowired  
	@Qualifier("messageSource")  
	private MessageSource resources;
	//前端设置切换语言是设置
	private RequestContext rc ;
	
	public RequestContext getRc() {
		return rc;
	}
	@Override
	public void setRcByRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		this.rc = new RequestContext(request);
	}
	
	@Override
	public void setRc(RequestContext rc) {
		this.rc = rc;
	}

	@Override
	public String getMessage(String key) {
		// TODO Auto-generated method stub
		if(null != getRc()){
			return getRc().getMessage(key);
		}
		return resources.getMessage(key, null, null);
	}

	@Override
	public String getMessage(String key, Object info) {
		// TODO Auto-generated method stub
		if(null != getRc()){
			return getRc().getMessage(key, new Object[]{info});
		}
		return resources.getMessage(key, new Object[]{info}, null);
	}

	@Override
	public String getMessage(String key, Object... objects) {
				if(null != getRc()){
					return getRc().getMessage(key, objects);
				}
				return resources.getMessage(key, objects, null);
	}
}
  ```
  5. 自定义的拦截器中处理，国际化组件的请求bean
  ```java
  @Component
public class ControllerInterceptor implements HandlerInterceptor {
    /**
     * 在Controller方法前进行拦截
     */
	@Resource
	private I18nSessionService  i18nSessionService;
    private static Logger log = Logger.getLogger("ControllerInterceptor");
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        log.info("RequestURI :"+request.getRequestURI());
        //解决跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","POST");
        response.setHeader("Access-Control-Allow-Headers","x-requested-with,content-type");
        //拦截器中对所有的请求处理，保存到request bean中
        i18nSessionService.setRcByRequest(request);
        return true;
    }

    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }
    /**
     * 在Controller方法后进行拦截
     */
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
```
 5. 使用方式
 ```java
 //发送请求 http://localhost:8080/testI18n.do?locale=en_US 或者http://localhost:8080/testI18n.do?locale=zh_CN
 @Resource
private I18nSessionService is;
@RequestMapping(value = "/testI18n.do", method = { RequestMethod.POST,RequestMethod.GET})
public @ResponseBody
	Result testI18n(){
		return new Result(true, is.getMessage("argument.required"), "");
}
```
* 总结：以上就是基本实现过程，已经可以用了
