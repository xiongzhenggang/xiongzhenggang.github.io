## spring secure相关问题

### Spring REST 配置CSRF防护
<p>
CSRF 攻击简单来说，是多Tab页面浏览器的一个安全漏洞，比如你正在访问A网站，此时如果浏览器有你的cookie，并且session没有过期，此时你去访问B网站，那么B网站可以直接调用A网站的接口，而A网站则认为是你本人进行的操作。
</p>

如图：
![csrf攻击](/java框架/spring/img/csrf.png)

* 如何进行防御
对CSRF进行防御，可以通过加Token.也就是当你访问A网站的时候，A会给你一个token，然后，接下去的post请求，你需要把token带上，不然服务器则拒绝接收这个请求。
- 1. token的产生：spring-security 4.0之后默认开启csrf，可以直接产生csrf token。
- 2. token的存储：这里存储是指服务端的存储，token是存储在session中。
- 3. token的传送：token可以通过cookie，也可以放在header中自定义的属性中。
- 4. token的接收和返回：前段收到http respon 之后，需要把相应的token返回回来。
- 5. token校验：服务器端对自己持有的token和客户端反馈回来的token进行校验，决定是否拒绝服务（拒绝服务可以自定义）。
* REST 的CSRF防御
一般写REST服务（也就是直接@ResponseBody）返回json字符串，则可以把token加在header里头的自定义属性中,为什么不能直接加在header中的cooike里，spring-sercurity官方给出的答案:
```xml
One might ask why the expected CsrfToken isn’t stored in a cookie by default. This is because there are known exploits in which headers (i.e. specify the cookies) can be set by another domain. This is the same reason Ruby on Rails no longer skips CSRF checks when the header X-Requested-With is present. See this webappsec.org thread for details on how to perform the exploit. Another disadvantage is that by removing the state (i.e. the timeout) you lose the ability to forcibly terminate the token if it is compromised.
```

既然如此，那么需要在header中加入token，我们只要注册一个Filter，就可以完成这个功能： 

1.  创建Filter
```java
/**
 *
 * "将CSRF TOKEN加入到header中"
 *
 */
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {
    protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
    protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
    protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
    protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

        if (token != null) {
            response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
            response.setHeader(RESPONSE_PARAM_NAME, token.getParameterName());
            response.setHeader(RESPONSE_TOKEN_NAME , token.getToken());
        }

        filterChain.doFilter(request, response);
    }
}
```

2. 加入到过滤器中
```java
@Configuration
@EnableWebSecurity
public class SecurityConfigure extends WebSecurityConfigurerAdapter {

private static final Logger THIRDPARTY_LOG = LoggerFactory.getLogger("THIRDPARTY_LOGGER");

@Autowired
UserService userService;

protected  void configure(HttpSecurity httpSecurity) throws Exception {
    CsrfTokenResponseHeaderBindingFilter csrfTokenFilter = new CsrfTokenResponseHeaderBindingFilter();
    CustomAccessDeniedHandler accessDeniedHandler=new CustomAccessDeniedHandler();
    httpSecurity.addFilterAfter(csrfTokenFilter,CsrfFilter.class);
}
}
```
### 当然使用spring—security更方便，使用方法如下：
* 本质上原理和上述一样。核心实现类在org.springframework.security.web.csrf.CsrfFilter的doFilterInternal方法：
```java
protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
// 先从tokenRepository中加载token  
   CsrfToken csrfToken = tokenRepository.loadToken(request);  
   final boolean missingToken = csrfToken == null;  
   // 如果为空，则tokenRepository生成新的token，并保存到tokenRepository中  
   if(missingToken) {  
       CsrfToken generatedToken = tokenRepository.generateToken(request);  
       // 默认的SaveOnAccessCsrfToken方法，记录tokenRepository，  
       // tokenRepository，response，获取token时先将token同步保存到tokenRepository中  
       csrfToken = new SaveOnAccessCsrfToken(tokenRepository, request, response, generatedToken);  
   }  
   // 将token写入request的attribute中，方便页面上使用  
   request.setAttribute(CsrfToken.class.getName(), csrfToken);  
   request.setAttribute(csrfToken.getParameterName(), csrfToken);  
  
   // 如果不需要csrf验证的请求，则直接下传请求（requireCsrfProtectionMatcher是默认的对象，对符合^(GET|HEAD|TRACE|OPTIONS)$的请求不验证）  
   if(!requireCsrfProtectionMatcher.matches(request)) {  
       filterChain.doFilter(request, response);  
       return;  
   }  
  
   // 从用户请求中获取token信息  
   String actualToken = request.getHeader(csrfToken.getHeaderName());  
   if(actualToken == null) {  
       actualToken = request.getParameter(csrfToken.getParameterName());  
   }  
   // 验证，如果相同，则下传请求，如果不同，则抛出异常  
   if(!csrfToken.getToken().equals(actualToken)) {  
       if(logger.isDebugEnabled()) {  
           logger.debug("Invalid CSRF token found for " + UrlUtils.buildFullRequestUrl(request));  
       }  
       if(missingToken) {  
           accessDeniedHandler.handle(request, response, new MissingCsrfTokenException(actualToken));  
       } else {  
           accessDeniedHandler.handle(request, response, new InvalidCsrfTokenException(csrfToken, actualToken));  
       }  
       return;  
   }  
  
   filterChain.doFilter(request, response); 
}
```
* 使用步骤
1. 在web.xml增加spring的代理过滤器
```xml
<filter>
		<filter-name>csrfFilter</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
		<async-supported>true</async-supported>
	</filter>
	<filter-mapping>
		<filter-name>csrfFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```
2. 在spring的配置文件servlet-context.xml中增加如下bean
```xml
<!--
		CSRF protection. Here we only include the CsrfFilter instead of all of Spring Security.
		Spring Security通过过滤器对csrf添加token
	-->
	
	<bean id="csrfFilter" class="org.springframework.security.web.csrf.CsrfFilter">
		<constructor-arg>
		<!--HttpSessionCsrfTokenRepository是把token放到session中来存取. 默认headerName= "_csrf" headerName = "X-CSRF-TOKEN" -->
			<bean class="org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository"/>
		</constructor-arg>
	</bean>
	<!--
		Provides automatic CSRF token inclusion when using Spring MVC Form tags or Thymeleaf. See
		如果用的是spring mvc 的form标签，则配置此项时自动将crsf的token放入到一个hidden的input中，而不需要开发人员显式的写入form 
	-->
	<bean id="requestDataValueProcessor" class="org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor"/>
```
 
<strong>至此上面的配置已经可以在使用mvc的form标签中自动使用而无需开发人员请自参与。但是如果是其他非form标签的请求的做法如下:</strong>
首先获取token:
```html
    <meta name="_csrf" content="${_csrf.token}"/>  
    <meta name="_csrf_header" content="${_csrf.headerName}"/>  
```
在使用ajax发送的时候需要提前将token放到header中如下：
```js
// Include CSRF token as header in JQuery AJAX requests
	// See http://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/#csrf-include-csrf-token-ajax
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	//ajaxSend() 方法在 AJAX 请求开始时执行函数。它是一个 Ajax 事件
	$(document).ajaxSend(function(e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
```
* 具体客参考springmvc项目
[springmvc基本功能使用实例](https://github.com/xiongzhenggang/spring-mvc-showcase) .
