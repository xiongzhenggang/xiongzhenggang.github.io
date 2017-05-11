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
