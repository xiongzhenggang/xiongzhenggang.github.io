## 问题描述；在同一个浏览器中当多个用户同时登陆在线，可以确定的是他们的sessionId相同

* 创建：
<p>
当同一个浏览器访问同一个应用，服务端在第一次加载jsp页面会创建一个session对象，每次客户端向服务器端
发送请求时，都会将此sessionId携带过去，服务器会对此sessionId进行校验。
</p>
* 活动：
<p>

       某次会话当中通过超链接打开的新页面属于同一次会话。

       只要当前会话页面没有全部关闭，重新打开新的浏览器窗口访问同一项目资源时属于同一次会话。

       除非本次会话的所有页面都关闭后再重新访问某个JSP或者Servlet将会创建新的会话。

       注意事项：注意原有会话还存在，只是这个旧的sessionId仍然存在于服务端，只不过再也没有客户端会携带它然

后交予服务端校验。
</p>
* 销毁：
<p>
       session的销毁有三种方式：

       1调用了session.invalidate()方法

       2session过期(超时)

       3服务器重新启动

</p>
       实例：
	
<p>我们在session_page1.jsp页面中设置最大的生命周期时间，过了期限那么跳转到session_page.jsp页面接受到的.信息就不会保存。
    所以解决该问题，不能讲sessionId作为判断条件而是，将该用户对象保存到application内置对象中，
并且在服务端利用session监听，1. 使用HttpSessionListener监听session的销毁。 
<p>

2. 使用HttpSessionBindingListener监听session的销毁。

检测到session失效时清楚application中的user用户对象，除此之外为了避免用户直接关闭页面，可以在js中调用函数处理。
