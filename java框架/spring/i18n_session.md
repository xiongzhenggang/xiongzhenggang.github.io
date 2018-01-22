### 基于session的国际化实现方式
* 原理：使用spring的session bean保存相应的国际化组件。
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
  2. springmvc容器配置
  ```xml
  <mvc:interceptors>
		<!-- 国际化操作拦截器 如果采用基于（请求/Session/Cookie/url）则必需配置 -->
		<bean class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor" />
	</mvc:interceptors>
	<!-- 基于session的国际化 -->
	<bean id="localeResolver"
		class="org.springframework.web.servlet.i18n.SessionLocaleResolver" />
  ```
  3. spring中的session bean依赖于接口实现，下面是其接口和对应的实现类
  ```java
  public interface I18nSessionService {
	
	public void setRc(RequestContext rc);
	
	public String getMessage(String key); 
	
	public String getMessage(String key,Object info);
	
	public String getMessage(String key,Object...objects);
}
  ```
  * 下面注解主要为设置作用域为session，注入messageSource组件，并提供RequestContext用于切换语言配置国际化
  ```java
  @Service
@Scope(value = WebApplicationContext.SCOPE_SESSION,
        proxyMode = ScopedProxyMode.INTERFACES)
public class I18nSessionServiceImpl implements I18nSessionService {

	@Autowired  
	@Qualifier("messageSource")  
	private MessageSource resources;
	//前端设置切换语言是设置
	RequestContext rc ;
	
	public RequestContext getRc() {
		return rc;
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
  4. 切换语言的controller用于切换
  ```java
  @Resource
 	//注入I18nSessionService后在session作用域中更改local
	private I18nSessionService i18nSessionService;

	@RequestMapping(value = "/turnLanguage", method = { RequestMethod.POST, RequestMethod.GET })
	public void TurnLanguage(HttpServletRequest request,
			@RequestParam(value = "langType", defaultValue = "zh") String langType) {
		if (langType.equals("zh")) {
			Locale locale = new Locale("zh", "CN");
			request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
		} else if (langType.equals("en")) {
			Locale locale = new Locale("en", "US");
			request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
		} else {
			request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
					LocaleContextHolder.getLocale());
		}
		RequestContext requestContext = new RequestContext(request);
		i18nSessionService.setRc(requestContext);
	}
```
 5. 使用方式
 ```java
 @Resource
	private I18nSessionService is;
	@RequestMapping(value="/nation",method={RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Result test(HttpServletRequest request){//, @RequestParam(value="langType", defaultValue="zh") String langType
		String msg = is.getMessage("argument.required","userDao");
        return new Result(true, msg, "返回数据");
	}
```
* 总结：以上就是基本实现过程，已经可以用了
