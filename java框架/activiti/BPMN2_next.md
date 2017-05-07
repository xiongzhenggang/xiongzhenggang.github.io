### BPMN 2.0 任务

<p>
	一个任务表示工作需要被外部实体完成， 比如人工或自动服务。 
重要的是注意BPMN语法的'task'与jPDL语法的区别。 在jPDL中，'task'的概念总是用在人工做一些事情的环境。 的那个流程引擎遇到jPDL中的task，它会创建一个task， 交给一些人的任务列表，然后它会进入等待状态。然而在BPMN 2.0中， 这里有很多任务类型，一些表示等待状态（比如，User Task 一些表示自动活动（比如，Service Task。 所以小心不要混淆了任务的概念，在切换语言的时候。 
	任务被描绘成一个圆角矩形，一般内部包含文字。 任务的类型（用户任务，服务任务，脚本任务，等等）显示在矩形的左上角，用小图标区别。 根据任务的类型， 引擎会执行不同的功能。
</p>

* 任务：人工任务
<p>
 <strong>任务：人工任务</strong>是典型的'人工任务'， 实际中的每个workflow或BPMN软件中都可以找到。当流程执行到达这样一个user task时， 一个新人工任务就会被创建，交给用户的任务列表。 
	和manual task的主要区别是 （也与人工工作对应）是流程引擎了解任务。 引擎可以跟踪竞争，分配，时间，其他，这些不是manual task的情况。 
user task描绘为一个圆角矩形，在左上角是一个小用户图标。 

![图07](/java框架/activiti/img/act08.png)

user task被定义为下面的BPMN 2.0 XML：

```xml
<userTask id="myTask" name="My task" />
```
	根据规范，可以使用多种实现（WebService, WS-humantask，等等）。 通过使用implementation属性。 当前，只有标准的jBPM任务机制才可以用，所以这里（还）没有 定义'implementation'属性的功能。
	BPMN 2.0规范包含了一些方法把任务分配给用户，组，角色等等。 当前的BPMN 2.0 jBPM实现允许使用一个 resourceAssignmentExpression来分配任务， 结合humanPerformer or PotentialOwner结构。 这部分希望在未来的版本里能够进一步演化。
	potentialOwner用来在你希望确定用户，组，角色的时候。 这是一个task的候选人。 参考下面的例子。这里的'My task'任务的候选人组是'management'用户组。 也要注意，需要在流程外部定义一个资源， 这样任务分配器可以引用到这个资源。 实际上，任何活动都可以引用一个或多个资源元素。 目前，只需要定义这个资源就可以了（因为它是规范中的一个必须的元素）， 但是在以后的发布中会进行加强（比如，资源可以拥有运行时参数）。

```xml
<resource id="manager" name="manager" />

<process ...>

...

<userTask id="myTask" name="My task">
  <potentialOwner resourceRef="manager" jbpm:type="group">
    <resourceAssignmentExpression>
      <formalExpression>management</formalExpression>
    </resourceAssignmentExpression>
  </potentialOwner>
</userTask>

```
	注意，我们使用了一个特定的后缀 (jbpm:type="group")，来定义这是一个用户组的分配方式。 如果删除了这个属性，就会默认使用用户组的语法 （在这个例子中也是没问题的）。 现在假设Peter和Mary是management组的成员 (这里使用默认的身份服务)： 

```java
identityService.createGroup("management");

identityService.createUser("peter", "Peter", "Pan");
identityService.createMembership("peter", "management");

identityService.createUser("mary", "Mary", "Littlelamb");
identityService.createMembership("mary", "management");  
```
Peter和Mary都可以在他们的任务列表中看到这条任务 （代码来自实例单元测试）：

```java
// Peter and Mary are both part of management, so they both should see the task
List<Task> tasks = taskService.findGroupTasks("peter");
assertEquals(1, tasks.size());
 tasks = taskService.findGroupTasks("mary");
assertEquals(1, tasks.size());

// Mary claims the task
Task task = tasks.get(0);
taskService.takeTask(task.getId(), "mary");
assertNull(taskService.createTaskQuery().candidate("peter").uniqueResult());

taskService.completeTask(task.getId());
assertProcessInstanceEnded(processInstance);
```
	当分配方式应该是候选用户时， 只需要使用jbpm:type="user"属性。
```xml
<userTask id="myTask" name="My User task">
  <potentialOwner resourceRef="employee" jbpm:type="user">
    <resourceAssignmentExpression>
      <formalExpression>peter</formalExpression>
    </resourceAssignmentExpression>
  </potentialOwner>
</userTask>
```
	在这个例子里，Peter将可以看到任务，因为他是这个任务的候选用户。

```java
List<Task> tasks = taskService.createTaskQuery().candidate("peter").list();
```
<strong>human performer</strong>用来，当你想把一个任务直接分配给一个人， 组，角色时。这个方法的使用方式 看起来和potential owner很像。

```xml
<resource id="employee" name="employee" />

<process ...>

...

<userTask id="myTask" name="My User task">
  <humanPerformer resourceRef="employee">
    <resourceAssignmentExpression>
      <formalExpression>mary</formalExpression>
    </resourceAssignmentExpression>
  </humanPerformer>
</userTask>
```
	在这个例子中，任务会直接分配给Mary。 她可以在自己的任务列表中看到这个任务： 

```java
List<Task> tasks = taskService.findPersonalTasks("mary");
```
	因为任务分配已经完成，通过使用 formalExpression，它也可以定义表达式 在运行期解析。表达式本身需要放在 ${}中，这和jBPM一样。 比如，如果流程变量'user'被定义了，然后，它可以用在表达式中。 当然也可以使用更复杂的表达式。 

```xml
<userTask id="myTask" name="My User task">
  <humanPerformer resourceRef="employee">
    <resourceAssignmentExpression>
      <formalExpression>${user}</formalExpression>
    </resourceAssignmentExpression>
  </humanPerformer>
</userTask>
```
	注意不需要在humanPerformer元素中使用'jbpm:type'，因为只能进行 直接用户分配。如果任务需要被分配给一个角色或一个组， 使用potentialOwner和group类型（当你把任务分配给一个组时， 组中的所有成员都会成为候选用户 - 参考potentialOwner的用法）。
</p>

* 任务：Java服务任务

<p>
<strong>Service Task</strong> 是一个自动活动，它会调用一些服务， 比如web service，java service等等。当前jBPM引擎 只支持调用java service，但是web service的调用 已经在未来的版本中做了计划。 
	定义一个服务任务需要好几行XML（这里就可以看到BPEL的影响力）。 当然，在不久的未来，我们希望有工具可以把这部分大量的简化。 一个服务任务需要如下定义:

```xml
<serviceTask id="MyServiceTask" name="My service task"
  implementation="Other" operationRef="myOperation" />
```
	服务任务需要一个必填的id和一个可选的 name。implementation元素 是用来表示调用服务的类型。可选值是WebService, Other或者Unspecified。 因为我们只实现了Java调用， 现在只能选择Other。 
服务任务将调用一个操作，operation的id 会在operationRef属性中引用。 这样一个操作就是下面实例的 interface的一部分。每个操作都至少有一个 输入信息，并且 最多有一个输出信息。 
```xml
<interface id="myInterface"
    name="org.jbpm.MyJavaServicek">
    <operation id="myOperation2" name="myMethod">
      <inMessageRef>inputMessage</inMessageRef>
      <outMessageRef>outputMessage</outMessageRef>
    </bpmn:operation>
</interface>
```
	对于java服务，接口的名称用来 指定java类的全类名。操作的名称 用来指定将要调用方法名。 输入/输出信息表示着java方法的参数/返回值， 定义如下所示： 

```xml
<message id="inputMessage" name="input message" structureRef="myItemDefinition1" />
```
	BPMN中很多元素叫做'item感知'，包括这个消息结构。 这意味着它们会在流程执行过程中保存或读取item。 负责这些元素的数据结构需要使用ItemDefinition。 在这个环境下，消息指定了它的数据结构， 通过引用 structureRef属性中定义的ItemDefinition。 
```xml
 <itemDefinition id="myItemDefinition1" >
    <jbpm:arg>
      <jbpm:object expr="#{var1}" />
    </jbpm:arg>
  </itemDefinition>

  <itemDefinition id="myItemDefinition2">
    <jbpm:var name="returnVar" />
  </itemDefinition>
```
注意，这写不是标准的BPMN 2.0标准（因此都有'jbpm'的前缀）。 实际上，根据标准，ItemDefinition不应该包含多余一个数据结构定义。 实际在输入参数的映射，使用一个数据结构， 在serviceTask的ioSpecification章节已经完成了。 然而，当前jBPM BPMN 2.0实现还没有实现那个结构。 所以，这意味着当前使用的上面这种方法， 很可能在不久的未来就会出现变化。 
<strong>>重要提醒：接口</strong>，ItemDefinitions和消息需要定义在 <process>外边。参考实例 ServiceTaskTest的实际流程和单元测试. 

</p>

* 任务：脚本任务

<p>
```xml
<scriptTask id="scriptTask" name="Script Task" scriptLanguage="bsh">
  <script><![CDATA[
    for(int i=0; i < input.length; i++){
      System.out.println(input[i] + " x 2 = " + (input[i]*2));
    }]]>
  </script>
</scriptTask>
```
	脚本任务，除了必填id和可选的 name之外，还允许指定 scriptLanguage和script。 因为我们使用了JSR-223（java平台的脚本语言），修改脚本语言就需要： 
	
	把scriptLanguage 属性修改为JSR-223兼容的名称 
	在classpath下添加JSR规范的ScriptEngine实现
	上面的XML对应图形如下所示（添加了空开始和结束事件）。

![图8](/java框架/activiti/img/act09.png)
	像上面例子中显示的那样，可以在脚本中使用流程变量。 我们现在可以启动一个这个例子的流程，也要提供一些随机生成的输入变量

```java
Map<String, Object> variables = new HashMap<String, Object>();
Integer[] values = { 11, 23, 56, 980, 67543, 8762524 };
variables.put("input", values);
executionService.startProcessInstanceBykey("scriptTaskExample", variables);
```
在输出控制台里，我们现在可以看到执行的执行的脚本：

``` xml
11 x 2 = 22
23 x 2 = 46
56 x 2 = 112
980 x 2 = 1960
67543 x 2 = 135086
8762524 x 2 = 17525048
```
</p>

* 任务：手工任务

![图9](/java框架/activiti/img/act10.png)

<p>
	手工任务时一个由外部人员执行的任务，但是没有指定是 一个BPM系统或是一个服务会被调用。在真实世界里，有很多例子： 安装一个电话系统，使用定期邮件发送一封信， 用电话联系客户，等等。

```xml
<manualTask id="myManualTask" name="Call customer" />
```
	手工任务的目标更像 文档/建模提醒的，因为它 对流程引擎的运行没有任何意义，因此，当流程引擎遇到一个手工任务时 会简单略过。

</p>

* 任务：java接收任务
<p>
	receive task是一个任务会等到外部消息的到来。 除了广泛使用的web service用例，规范在其他环境中的使用也是一样的。 web service用例还没有实现， 但是receive task已经可以在java环境中使用了。 
receive task显示为一个圆角矩形（和task图形一样） 在左上角有一个小信封的图标。 

![图10](/java框架/activiti/img/act11.png)

	在java环境中，receive task没有其他属性，除了id和name（可选）， 行为就像是一个等待状态。为了在你的业务流程中使用等待状态， 只需要加入如下几行： 

```xml
<receiveTask id="receiveTask" name="wait" />
```
	流程执行会在这样一个receive task中等待。流程会使用 熟悉的jBPM signal methods来继续执行。 注意，这些可能在未来改变，因为'signal' 在BPMN 2.0中拥有完全不同的含义。

```java
Execution execution = processInstance.findActiveExecutionIn("receiveTask");
executionService.signalExecutionById(execution.getId());
```
### 完全实例

![BPMN2.0完全实例部分链接](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/java框架/activiti/BPMN_2.0All.md).

