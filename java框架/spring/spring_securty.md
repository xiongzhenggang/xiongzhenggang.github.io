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

## xss（跨站脚本）
* 原理：
跨站脚本（Cross site script，简称xss）是一种“HTML注入”，由于攻击的脚本多数时候是跨域的，所以称之为“跨域脚本”。

我们常常听到“注入”（Injection），如SQL注入，那么到底“注入”是什么？注入本质上就是把输入的数据变成可执行的程序语句。SQL注入是如此，XSS也如此，只不过XSS一般注入的是恶意的脚本代码，这些脚本代码可以用来获取合法用户的数据，如Cookie信息。

XSS从攻击原理上，分为三类：

1. 反射型XSS

    将用户输入“反射”回浏览器，即将用户的输入变成HTML传输回客户端。如：
          Response.Write(“<script>alert(/xss/);</script>”)
    就是一个典型的反射型XSS。

2. 存储性XSS

    存储性XSS本质上也是一种反射型XSS，但是它把攻击脚本放置在服务器端，一旦被注入，可被多人多次利用。如，发表博文，就可以引入存储性的XSS。

3. DOM BASED XSS

    如果用户的输入被用于修改原有HTML的DOM内容，就会引入这一类攻击。 

    最典型的是输入的内容用于作为某个节点的innerHTML，如果不对输入作验证，则会被注入攻击代码。 

    如下的一段脚本注入后，就会获取用户的Cookie
```js    
<script language=”javascript”>
          var cockieInfo =window.cockie;
          //send cockieInfo to luminji
    </javascript>
```
* 应对策略:
1. 在服务器段限制输入格式,输入类型，输入长度以及输入字符
要注意避免使用一些有潜在危险的html标签，这些标签很容易嵌入一些恶意网页代码。如
```html
<img> <iframe><script><frameset><embed><object>< style>等。
```
注意，不要仅仅在客户端使用js代码加以验证。因为客户端的js脚本可以被绕过。
2.  格式化输出。将输入的内容通过HttpUtility.HtmlEncode处理，这样就不能直接看出输出的内容。
3. IE本身也有机制阻止跨站脚本
等。
### Spring MVC里面的预防：
方法一：
web.xml加上：
```xml

<context-param>
   <param-name>defaultHtmlEscape</param-name>
   <param-value>true</param-value>
</context-param>
```
forms上加上
```xml
<spring:htmlEscape defaultHtmlEscape="true" />
```
方法二 是手动escape，例如用户可以输入：
```xml
<script>alert()</script> 或者输入<h2>abc<h2>，如果有异常，显然有xss漏洞。
```
首先添加一个jar包：commons-lang-2.5.jar ，然后在后台调用这些函数：StringEscapeUtils.escapeHtml(string); StringEscapeUtils.escapeJavaScript(string); StringEscapeUtils.escapeSql(string);
前台js调用escape函数即可。
方法三
后台加Filter，对每个post请求的参数过滤一些关键字，替换成安全的，例如：< > ' " \ /  # &

方法是实现一个自定义的HttpServletRequestWrapper，然后在Filter里面调用它，替换掉getParameter函数即可。

