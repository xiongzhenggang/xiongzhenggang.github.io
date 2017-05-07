##  BPMN 2.0
### BPMN 2.0是什么呢？
<p>
业务流程模型注解（Business Process Modeling Notation - BPMN）是 业务流程模型的一种标准图形注解。这个标准 是由对象管理组（Object Management Group - OMG）维护的。 
基本上，BPMN规范定义了任务看起来怎样的，哪些结构可以 与其他进行连接，等等。这就意味着 意思不会被误解。 
标准的早期版本（1.2版以及之前）仅仅限制在模型上， 目标是在所有的利益相关者之间形成通用的理解， 在文档，讨论和实现业务流程之上。 BPMN标准证明了它自己，现在市场上许多建模工具 都使用了BPMN标准中的元素和结构。 实际上，现在的jPDL设计器也使用了 BPMN元素。 
BPMN规范的2.0版本，当前已经处于最终阶段了， 已经计划不就就会完成，允许添加精确的技术细节 在BPMN的图形和元素中， 同时制定BPMN元素的执行语法。 通过使用XML语言来指定业务流程的可执行语法， BPMN规范已经演变为业务流程的语言， 可以执行在任何兼容BPMN2的流程引擎中， 同时依然可以使用强大的图形注解。 
</p>

### 历史和目标
<p>
jBPM BPMN2的实现是在jBPM 4.0发布之后 在2009年8月，在与社区进行了紧密协作之后启动的。 而后，我们决定了第一个发布版（比如，文档/QA） 涉及一部分BPMN2规范，将在jBPM 4.3发布。 
我们的目标是建立一个原生BPMN2运行引擎 （或者说实现'可执行的BPMN2'）基于流程虚拟机 （Process Virtual Machine - PVM）。 注意，这个版本的主要目标是原生可执行， 不是图形注解 - 但是我们清楚 对于未来的版本是很重要的。 
如果用户已经了解了jBPM，就会发现 
配置结构保持不变 
API与已经存在的完全一样或者很类似 
测试BPMN2流程也可以使用常用的java测试框架 
数据库表结构保持不变
所以，总体来说，我们的主要目标是保持所有在jBPM上好的事情， 加强它们，使用一个标准的流程语言。 
</p>

### JPDL vs BPMN 2.0
<p>
第一个问题可能是，很正当的，映入脑海的是， 为什么已经有了jPDL还要实现BPMN2。它们两个语言 的目标都是定义可执行的业务流程。从高层次来看， 两个语言是等效的。主要的区别是 BPMN2是“厂商中立”的，你可以使用标准， 而jPDL是绑定在jBPM上的（虽然会有一些争论 绑定在开源语言厂商比如jPDL 和绑定在闭源产品）。 
在jBPM中，两个语言实现都是建立在jBPM流程虚拟机上的 （PVM）。这意味着两个语言共享通用功能 （持久化，事务，配置，也有基本流程结构，等等）。 结果就是，对jBPM核心的优化 会对两个语言有益。依靠PVM，BPMN2实现 建立在基础上，已经在过去证明了它自己， 并拥有了很大的最终用户社区。 
当执行语言，把它们相互比较的时候， 下面几点必须纳入考虑： 
BPMN2是基于被BPM工业接受的一个标准。 
BPMN2是与实现无关的。这一点的缺点是集成java技术 jPDL总会更早。 所以，从java开发者的角度，jPDL更简单，感觉更自然 （一些BPEL/WSDL的“层次”也在BPMN中）。 
jPDL的一个目标是XML可读，BPMN2流程在 一定程度上也是可读的，但是工具和更多规范的细节 会要求实现同等级的 生产力。 
java开发者可以很快学会jPDL，因为他们很了解jPDL语言， 会发现实用工具有时候很麻烦， 语言本身也过于复杂了。 
BPMN2包含一个很大的描述结构的集合，在规范中。 然而，对接口代码的绑定在规范中是开放的 （与XPDL相比），即使WSDL通常会被默认使用。 这意味着流程的可移植性丧失了， 当我们把流程移植到一个引擎上，而这个引擎不支持同样的绑定机制。 比如，调用java类通常是jBPM的默认实现 的绑定方式。
很自然的，因为政治原因，BPMN2规范发展的会比较慢。 jPDL就可以快速变化，和新技术进行集成， 当他们发布的时候， 与BPMN2相比可以加快步伐进行演化。 当然，因为两个都建立在同一个PVM上，jPDL中的逻辑 也可以一直到BPMN2上， 作为一个扩展，不会出现很多麻烦。 
</P>
### Bpmn 2.0 执行

<p>
BPMN2规范定义了非常丰富的语言，为建模和执行业务流程。 然而，也意味着它非常困难总览BPMN2可能是怎样 为了简化这种情况，我们决定把 BPMN2结构分为三个等级。 区分的方式主要基于Bruce Silver写的 'BPMN method and Style'这本书(http://www.bpmnstyle.com/)， Dr. Jim Arlow的培训资料( http://www.slideshare.net/jimarlow/introductiontobpmn005)， 'How much BPMN do you need'( http://www.bpm-research.com/2008/03/03/how-much-bpmn-do-you-need/)， 和我们自己的经验。 
我们定义了三种BPMN2结构分类： 
基本：这个分类的结构很直接 并且容易了解。这个分类的结构可以用来为 简单的业务流程建模。 
高级：包含更强大或更复杂的结构， 这些都提高了建模和执行语法的学习曲线。 业务流程的主要目标是使用这个 和之前的分类来实现结构。 
复杂：这个分类的结构用来实现罕见的情况， 或者它们的语法难以理解。
</P>

### 配置
<p>
在你的应用中使用BPMN 2.0是很简单的：只要把下面一行 加入jbpm.cfg.xml文件.

```xml
<import resource="jbpm.bpmn.cfg.xml" />
```
这里的引用会启用BPMN 2.0的流程发布，通过把BPMN 2.0发布器安装到流程引擎中。 注意流程引擎可以同时使用jPDL和BPMN 2.0流程。 这意味着在你的应用里，一些流程可能是jPDL， 其他的可能是BPMN 2.0。 
* 流程引擎是根据定义文件的后缀来区分流程定义的。 对于BPMN 2.0，使用*.bpmn.xml后缀 （jPDL使用*.jpdl.xml后缀）。 
</p>
### 实例

<p>
	发布中包含的例子也包含了下面章节中 讨论的每个结构的实例。查看BPMN 2.0的流程实例 和测试用例， 在org.jbpm.examples.bpmn.* 包下。 
参考用户指南，第二章（安装），研究一下如何导入实例。 查看章节'导入实例
</P>

### 流程根元素
<p>
一个BPMN 2.0 XML流程的根是definitions元素。 在命名状态，子元素会包含真正的业务流程定义。 每个process子元素 可以拥有一个id和 name。一个空的BPMN 2.0业务流程 看起来像下面这样。也要注意把BPMN2.xsd放在classpath下， 来启用XML自动补全。 
</p>
```xml
<definitions id="myProcesses"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://schema.omg.org/spec/BPMN/2.0 BPMN20.xsd"
  xmlns="http://schema.omg.org/spec/BPMN/2.0"
  typeLanguage="http://www.w3.org/2001/XMLSchema"
  expressionLanguage="http://www.w3.org/1999/XPath"
  targetNamespace="http://jbpm.org/example/bpmn2">

  <process id="myBusinessProcess" name="My business processs">

      ...

  </process>
<definitions>
```
<p>
如果一个process元素定义了id，它会作为业务流程的key使用 （比如，启动流程可以通过调用executionService.startProcessInstanceByKey("myBusinessProcess")， 否则jBPM引擎会创建一个唯一流程key（与jPDL相同）。 
</p>
### 基本结构

* 事件
<p>
与活动和网关一起，事件用来在实际的每个业务流程中。 事件让业务建模工具用很自然的方式描述业务流程，比如 '当我接收到客户的订单，这个流程就启动'， '如果两天内任务没结束，就终止流程' 或者'当我收到一封取消邮件，当流程在运行时， 使用子流程处理邮件'。注意典型的业务 通常使用这种事件驱动的方式。人们不会硬编码顺序创建， 但是他们倾向于使用在他们的环境中发生的事情（比如，事件）。 在BPMN规范中，描述了很多事件类型，为了覆盖可能的事情， 在业务环境中可能出现的情况。
</p>
* 事件：空启动事件
<p>
一个启动事件说明了流程的开始（或子流程）。图形形式，它看起来 是一个圆（可能）内部有一个小图标。图标指定了事件的实际类型 会在流程实例创建时被触发。 
空启动事件画出来是一个圆，内部没有图标，意思是 这个触发器是未知或者未指定的。jPDL的开始活动基本是一样的语法。 流程实例的流程定义包含一个空启动事件， 可以使用executionService的API调用创建。 
一个空开始事件像下面这样定义。id是必填的，name是可选的。
```xml
<startEvent id="start"  name="myStart" />
```
</p>

* 事件：空结束事件
<p>
结束事件指定了流程实例中一个流程路径的结束。 图形上，它看起来就是一个圆 拥有厚边框（可能） 内部有小图标。 图标指定了结束的时候 会执行哪种操作。 
空结束事件画出来是一个圆，拥有厚边框，内部没有图标， 这意味着当流程到达事件时，不会抛出任何信号。 jPDL中的结束事件与空结束事件语义相同。 
空结束事件可以像下面一样定义，id是必填的，name是可选的.
```xml
<endEvent id="end" name="myEnd" />
```
</p>
下面的例子显示了只使用空开始和结束事件的流程:

![图01](/java框架/activiti/img/act02.png)

这个流程对应的可执行XML像这样 （忽略声明用的definitions根元素）
```xml
<process id="noneStartEndEvent" name="BPMN2 Example none start and end event">

    <startEvent id="start" />

    <sequenceFlow id="flow1" name="fromStartToEnd"
      sourceRef="start" targetRef="end" />

    <endEvent id="end" name="End" />

  </process>
```
现在可以通过调用startProcessInstanceXXX操作， 创建一个流程实例。
```java
ProcessInstance processInstance = executionService.startProcessInstanceByKey("noneStartEndEvent");
```
* 事件：终止结束事件
<p>
终止和空结束事件的区别是 实际中流程的路径是如何处理的（或者使用BPMN 2.0的术语叫做token）。 终止结束事件会结束整个流程实例，而空结束事件只会结束当前流程路径。 他们都不会抛出任何事情 当到达结束事件的时候。 
一个终止结束事件可以像下面定义。id是必填的，name是可选的。
```xml
<endEvent id="terminateEnd" name="myTerminateEnd">
  <terminateEventDefinition/>
</endEvent>
```
</p>
<p>
终止结束事件被描绘成结束事件一样（圆，厚边框）， 内部图标时一个完整的圆。在下面的例子中，完成task1 会结束流程实例，当完成task2时只会结束到达结束事件 的流程路径，只剩下task1打开</p>

![图02](/java框架/activiti/img/act03.png)
参考jBPM发布包中的实例， 单元测试和业务流程对应XML。 

* 顺序流
<p>
顺序流是事件，活动和网关之间的连线，显示为一条实线 带有箭头，在BPMN图形中（jPDL中等效的是transition）。 每个顺序流都有一个源头和一个 目标引用，包含了 活动，事件或网关的id。 

```xml
<sequenceFlow id="myFlow" name="My Flow"
        sourceRef="sourceId" targetRef="targetId" />
```

与jPDL的一个重要区别是多外向顺序流的行为。 在jPDL中，只有一个转移会成为外向转移，除非活动是fork （或自定义活动拥有fork行为）。然而，在BPMN中， 多外向顺序流的默认行为是切分进入的token（jBPM中术语叫做execution） 分成token集合，每个顺序流一个。在下面情况中， 在完成第一个任务，就会激活三个任务。</p>

![图03](/java框架/activiti/img/act04.png)

<p>
为了避免使用一个顺序流，必须添加condition条件到顺序流中。 在运行时，只有当condition条件结果为true， 顺序流才会被执行。 
为了给顺序流添加condition条件，添加一个conditionExpression 元素到顺序流中。条件可以放在 ${}中。

```xml
<sequenceFlow id=....>
  <conditionExpression xsi:type="tFormalExpression">${amount >= 500}</conditionExpression>
</sequenceFlow>
```
注意，当前必须把 xsi:type="tFormalExpression"添加到 conditionExpression中。在未来的版本中可能会修改。 
活动（比如用户任务）和网关（比如唯一网关）可以用户默认顺序流。 默认顺序流只会在活动或网关的 所有其他外向顺序流的condition条件为false时才会使用。 默认顺序流图形像是顺序流多了一个斜线标记。 

![图04](/java框架/activiti/img/act05.png)
	默认顺序流通过指定活动或网关的 'default' 属性 来使用。 
也要注意，默认顺序流上的表达式会被忽略。</p>

* 网关

<p>
BPMN中的网关是用来控制流程中的流向的。更确切的是， 当一个token（BPMN 2.0中execution的概念注解）到达一个网关， 它会根据网关的类型进行合并或切分。 
网关描绘成一个菱形，使用一个内部图标来指定类型 （唯一，广泛，其他）。 
所有网关类型，都可以设置gatewayDirection。 下面的值可以使用： 
unspecificed (默认)：网关可能拥有多个 进入和外出顺序流。 
mixed：网关必须拥有多个 进入和外出顺序流。 
converging：网关必须拥有多个进入顺序流， 但是只能有一个外出顺序流。 
diverging：网关必须拥有一个进入顺序流， 和多个外出顺序流。 
比如下面的例子：并行网关的gatewayDirection属性为'converging'， 会拥有json行为。 

```xml
<parallelGateway id="myJoin" name="My synchronizing join" gatewayDirection="converging" />
```
<strong>注意：</strong>gatewayDirection属性根据规范是可选的。 这意味着我们不能通过这个属性来 在运行时知道一个网关的行为（比如，一个并行网关， 如果我们用够切分和合并行为）。然而，gatewayDirection属性用在解析时 作为约束条件对进入、外出顺序流。所以使用这个属性 会减低出错的机会，当引用顺序流时， 但不是必填的。 
</p>

* 网关：唯一网关

<p>
	唯一网关表达了一个流程中的唯一决策。 会有一个外向顺序流被使用，根据定义在 顺序流中的条件。 对应的jPDL结构，相同的语法是 decision活动。唯一网关的 完全技术名称是'基于数据的唯一网关'， 但是也经常称为XOR 网关。 XOR网关被描绘为一个菱形，内部有一个'X'， 一个空的菱形，没有网关也象征着唯一网关。 
	下面图形显示了唯一网关的用法：根据amount变量的值， 会选择唯一网关外向的三个外向顺序流 中的一个。 

![图05](/java框架/activiti/img/act06.png)

</br>
	这个流程对应的可执行XML看起来像下面这样。 注意定义在顺序流中的条件。唯一网关会选择一个顺序流， 如果条件执行为true。如果多个条件 执行为true，第一个遇到的就会被使用 （日志信息会显示这种情况.

```xml
<process id="exclusiveGateway" name="BPMN2 Example exclusive gateway">

    <startEvent id="start" />

   <sequenceFlow id="flow1" name="fromStartToExclusiveGateway"
      sourceRef="start" targetRef="decideBasedOnAmountGateway" />

   <exclusiveGateway id="decideBasedOnAmountGateway" name="decideBasedOnAmount" />

   <sequenceFlow id="flow2" name="fromGatewayToEndNotEnough"
      sourceRef="decideBasedOnAmountGateway" targetRef="endNotEnough">
      <conditionExpression xsi:type="tFormalExpression">
        ${amount < 100}
      </conditionExpression>
   </sequenceFlow>

   <sequenceFlow id="flow3" name="fromGatewayToEnEnough"
      sourceRef="decideBasedOnAmountGateway" targetRef="endEnough">
      <conditionExpression xsi:type="tFormalExpression">
        ${amount <= 500 && amount >= 100}
        </conditionExpression>
   </sequenceFlow>

   <sequenceFlow id="flow4" name="fromGatewayToMoreThanEnough"
      sourceRef="decideBasedOnAmountGateway" targetRef="endMoreThanEnough">
      <conditionExpression xsi:type="tFormalExpression">
        ${amount > 500}
      </conditionExpression>
   </sequenceFlow>

   <endEvent id="endNotEnough" name="not enough" />

   <endEvent id="endEnough" name="enough" />

   <endEvent id="endMoreThanEnough" name="more than enough" />

  </process>
```
	这个流程需要一个变量，这样表达式就可以在运行期间执行。 变量可以被提供，当流程实例执行的时候（类似jPDL）。

```java
Map<String, Object> vars = new HashMap<String, Object>();
vars.put("amount", amount);
ProcessInstance processInstance = executionService.startProcessInstanceByKey("exclusiveGateway", vars);
```
	唯一网关需要所有外向顺序流上都定义条件。 对这种规则一种例外是默认顺序流。 使用default 属性来引用一个已存在的 顺序流的id。这个顺序流会被使用 当其他外向顺序流的条件都执行为false时.

```xml
<exclusiveGateway id="decision" name="decideBasedOnAmountAndBankType" default="myFlow"/>

<sequenceFlow id="myFlow" name="fromGatewayToStandard"
    sourceRef="decision" targetRef="standard">
</sequenceFlow>
```
</p> 

* 网关：并行网关

<p>
	并行网关用来切分或同步相关的进入或外出 顺序流。 
并行网关拥有一个进入顺序流的和多于一个的外出顺序流 叫做'并行切分或 'AND-split'。所有外出顺序流都会 被并行使用。注意：像规范中定义的那样， 外出顺序流中的条件都会被忽略。 
	并行网关拥有多个进入顺序流和一个外出顺序流 叫做'并行归并'或 AND-join。所有进入顺序流需要 到达这个并行归并，在外向顺序流使用之前。 
并行网关像下面这样定义:

```xml
<parallelGateway id="myParallelGateway" name="My Parallel Gateway" />
```
	注意，gatewayDirection属性可以被使用， 已获得建模错误，在解析阶段（参考上面）。 
下面的图形显示了一个并行网关可以如何使用。在流程启动后， 'prepare shipment' 和 'bill customer'用户任务都会被激活。 并行网关被描绘为一个菱形，内部图标是一个十字， 对切分和归并行为都是一样。

![图06](/java框架/activiti/img/act07.png)

图形对应的XML如下所示：

```xml
<process id="parallelGateway" name="BPMN2 example parallel gatewar">

    <startEvent id="Start" />

    <sequenceFlow id="flow1" name="fromStartToSplit"
      sourceRef="Start"
      targetRef="parallelGatewaySplit"  />

    <parallelGateway id="parallelGatewaySplit" name="Split"
      gatewayDirection="diverging"/>

    <sequenceFlow id="flow2a" name="Leg 1"
      sourceRef="parallelGatewaySplit"
      targetRef="prepareShipment" />

    <userTask id="prepareShipment" name="Prepare shipment"
      implementation="other" />

    <sequenceFlow id="flow2b" name="fromPrepareShipmentToJoin"
      sourceRef="prepareShipment"
      targetRef="parallelGatewayJoin"  />

    <sequenceFlow id="flow3a" name="Leg 2"
      sourceRef="parallelGatewaySplit"
      targetRef="billCustomer" />

    <userTask id="billCustomer" name="Bill customer"
      implementation="other" />

    <sequenceFlow id="flow3b" name="fromLeg2ToJoin"
      sourceRef="billCustomer"
      targetRef="parallelGatewayJoin"  />

    <parallelGateway id="parallelGatewayJoin" name="Join"
      gatewayDirection="converging"/>

    <sequenceFlow id="flow4"
      sourceRef="parallelGatewayJoin"
      targetRef="End">
    </sequenceFlow>

    <endEvent id="End" name="End" />

  </process>
```
</p>
### 任务见下一节


