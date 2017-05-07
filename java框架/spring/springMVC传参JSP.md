## spring接收请求参数:

1. 使用HttpServletRequest获取
```java
@RequestMapping("/login.do")  

public String login(HttpServletRequest request){  

    String name = request.getParameter("name")  

    String pass = request.getParameter("pass")  

} 
```
 2. Spring会自动将表单参数注入到方法参数，和表单的name属性保持一致。和Struts2一样
```java
@RequestMapping("/login.do")  

public String login(HttpServletRequest request,  

                                String name,  

 @RequestParam("pass")String password) // 表单属性是pass,用变量password接收  

{  

   syso(name);  

   syso(password)  

} 
```
3. 自动注入Bean属性

```html
<form action="login.do">  

用户名：<input name="name"/>  

密码：<input name="pass"/>  

<input type="submit" value="登陆">  

</form>  
```
```java
//封装的User类  

public class User{  

  private String name;  

  private String pass;  

} 

@RequestMapping("/login.do")  

public String login(User user)  

{  

   syso(user.getName());  

   syso(user.getPass());  

} 
```
### 向页面传值：

当Controller组件处理后，向jsp页面传值，

1. 使用HttpServletRequest 和 Session  然后setAttribute()，就和Servlet中一样

2. 使用ModelAndView对象

3. 使用ModelMap对象

4. 使用@ModelAttribute注解

### Model数据会利用HttpServletRequest的Attribute传值到success.jsp中
```java
@RequestMapping("/login.do")  

public ModelAndView  login(String name,String pass){  

    User user = userService.login(name,pwd);  

    Map<String,Object> data = new HashMap<String,Object>();  

    data.put("user",user);  

    return new ModelAndView("success",data);  

}
```
###使用ModelMap参数对象示例:

ModelMap数据会利用HttpServletRequest的Attribute传值到success.jsp中
```java
@RequestMapping("/login.do")  

public　String login(String name,String pass ,ModelMap model){  

    User user  = userService.login(name,pwd);  

    model.addAttribute("user",user);  

    model.put("name",name);  

    return "success";  

} 
```

* 使用@ModelAttribute示例

在Controller方法的参数部分或Bean属性方法上使用
@ModelAttribute数据会利用HttpServletRequest的Attribute传值到success.jsp中

```java
@RequestMapping("/login.do")  

public String login(@ModelAttribute("user") User user){  

    //TODO  

   return "success";  

}  

@ModelAttribute("name")  

public String getName(){  

    return name;  

}  
```
* Session存储：

可以利用HttpServletReequest的getSession()方法
```java
@RequestMapping("/login.do")  

public String login(String name,String pwd  

                            ModelMap model,HttpServletRequest request){  

     User user = serService.login(name,pwd);  

     HttpSession session = request.getSession();  

     session.setAttribute("user",user);  

     model.addAttribute("user",user);  

     return "success";  

}  
```
### Spring MVC 默认采用的是转发来定位视图，如果要使用重定向，可以如下操作

1. 使用RedirectView

2. 使用redirect:前缀
```java
public ModelAndView login(){  

   RedirectView view = new RedirectView("regirst.do");  

   return new ModelAndView(view);  

}  
```
* 或者用如下方法，工作中常用的方法：

```java
public String login(){  

    //TODO  

    return "redirect:regirst.do";  

}  
```
