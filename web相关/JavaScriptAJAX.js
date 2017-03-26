XMLHttpRequest 对象是 AJAX 的关键。
创建 XMLHttpRequest 对象
不同的浏览器使用不同的方法来创建 XMLHttpRequest 对象。
Internet Explorer 使用 ActiveXObject。
其他浏览器使用名为 XMLHttpRequest 的 JavaScript 内建对象。
要克服这个问题，可以使用这段简单的代码：
var XMLHttp=null
if (window.XMLHttpRequest)
  {
  XMLHttp=new XMLHttpRequest()
  }
else if (window.ActiveXObject)
  {
  XMLHttp=new ActiveXObject("Microsoft.XMLHTTP")
  }
代码解释：
1.	首先创建一个作为 XMLHttpRequest 对象使用的 XMLHttp 变量。把它的值设置为 null。
2.	然后测试 window.XMLHttpRequest 对象是否可用。在新版本的 Firefox, Mozilla, Opera 以及 Safari 浏览器中，该对象是可用的。
3.	如果可用，则用它创建一个新对象：XMLHttp=new XMLHttpRequest()
4.	如果不可用，则检测 window.ActiveXObject 是否可用。在 Internet Explorer version 5.5 及更高的版本中，该对象是可用的。
5.	如果可用，使用它来创建一个新对象：XMLHttp=new ActiveXObject()

