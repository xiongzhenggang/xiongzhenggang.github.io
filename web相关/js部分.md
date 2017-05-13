## js使用
Dom 操作表
```xml
<script type="text/javascript">

        window.onload=function(){
            var tb1=document.getElementById("table1");
            tb1.border="5px";
            tb1.cellPadding="5px";
            var img=document.getElementById("img1");
            img.vspace="10";
            //为每一个属性添加一个表格行
            var row1=tb1.insertRow(-1);
            //创建连个表格的单元格
            var cel1=row1.insertCell(0);
            var cel2=row1.insertCell(1);
            //创建文本内容
            var textArr1=document.createTextNode("src");
            var textArr2=document.createTextNode("img.src");
            cel1.appendChild(textArr1);
            cel2.appendChild(textArr2);
        }
    </script>
</head>
<body>
<p><img src="../images/01.jpg" class="testImage" alt="test image" id="img1"></p>
<table id="table1" >
    <tr><th>Attry</th><th>value</th></tr>
</table>
```
### 二、关于h5的本地存储
1. localStorage的存储容量比cookie更大；

（2）cookie作为http规范的一部分，它的主要作用是与服务器进行交互，使http保持连接状态。也就是你每次请求一个新的页面的时候，cookie都会被发送过去，这样无形中就浪费了宽带。

（3)cookie保存是能指定可以访问该cookie的范围；localStorage的访问范围就是当前整个网站，不存在访问范围这个概念。且，两者都不支持跨域调用。

html5中的Web Storage包括了两种存储方式： sessionStorage和localStorage；

sessionStorage用于本地存储一个会话的数据，当会话结束时，存储的数据也会自动销毁（即当页面关闭的同时也销毁数据），因此，sessionStorage不是一个持久化的本地存储，仅仅是会话级别的存储。

localStorage用于持久化的本地存储，除非手动删除数据，否则会一直保存。

### 获取事件的通用写法
```js
function catchEvent(eventObj,event,eventHandler){
    if(eventObj.addEventListener){
        eventObj.addEventListener(event,eventHandler,false);
    }else if(eventObj.attachEvent){
        event="on"+ event;
        eventHandler.attachEvent(event,eventHandler);
    }
}
```
### ajax技术跨浏览器：
```js
var xmlHttpobj;
function getXmlHttp(){
     var xmlHttp=null;
     if(window.XMLHttpRequest) {
         xmlHttp = new XMLHttpRequest()
     }
         else{
             try{
                 XMLHttp=new ActiveXObject("Msxm12.XMLHTTP");
             }catch (e){
                 try{
                     XMLHttp=new ActiveXObject("Microsoft.XMLHTTP");
                 }catch (e){
                     return xmlHttp;
                 }
             }
         }
     return xmlHttp;
 }
```
<p>
open方法语法open(methon,url[,async,username,password])
 可选参数async表示异步默认true，其他表示指定服务用户名和密码
 setRequestHeather（labbel，value）该方法为请求的头添加标记
 sed（content）xmlHttp核心方法，发送请求同时附上相应的数据
 getAllResponseHeader():它将以字符串的形式返回所有的http应答的头，
 其中包含激活超时值，内容类型服务相关信息和日期信息
 getResponseHeader（label）：他将返回应答头指定的信息
 abort（）：用来取消当前的请求
</p>
### Xmlhttp除了以上的6中方法还有6中属性：
1.	onreadystatechange：用来保存当前请求ready状态改变时调用的函数
2.	readyState：有五个可选值，0表示请求未初始化，1表示请求open阶段，2表示请求已发送，3表示正在接受应答，4表示应答已接受。
3.	ResPonseText：文本格式信息
4.	ResponseXML:xml格式的应答信息，可以将其视为有效的xml信息处理
5.	Status：返回请求状态404,200.。
6.	statusText：以文字表示请求状态

### 下面对xmlHttp使用
```js
function populateList(){
    //作用：可把字符串作为URI 组件进行编码。
    var state=encodeURIComponent(document.getElementById("stateList").value);
    var url='chl4-02.php?state='+state;
    if(!xmlHttpobj){
        xmlHttpobj=getXmlHttp();
    }
    if(!xmlHttpobj) return;
    xmlHttpobj.open('GET',url,true);
    xmlHttpobj.onreadystatechange=getCities;
    xmlHttpobj.send(null);
}
```
escape()除了 ASCII 字母、数字和特定的符号外，对传进来的字符串全部进行转义编码，因此如果想对URL编码，最好不要使用此方法。
而encodeURI() 用于编码整个URI,因为URI中的合法字符都不会被编码转换。
encodeURIComponent方法在编码单个URIComponent（指请求参 数）应当是最常用的，
它可以讲参数中的中文、特殊字符进行转义，而不会影响整个URL。

