## 完全的实例（包括控制台任务表单)
<strong>前提条件：</strong>为了运行实例，我们假设 已经在JBoss server中安装了jBPM控制台。如果没有， 请先执行'demo.setup.jboss'安装脚本。
	我们实现的业务流程实现起来像下面这样：

![图11](/java框架/activiti/img/act12.png)

<p>
	你可能已经看过这个例子了，因为我们也在发布包中的 实例中使用jPDL实现过它了。 
	业务流程很简单：一个员工可以启动一个新流程， 申请一定时间的假期。在请求任务完成之后， 经理会在任务列表中看到审核任务。 经理可以决定批准或驳回这个申请。 根据outcome（那是外向顺序流上的小菱形 - 这意味着在顺序流上有条件表达式）， 会发送一个驳回信息或者流程结束。注意，实际上我们这里使用了简写： 不是在'verify request'任务的外向顺序流上设置表达式， 我们可以在用户任务之后使用一个唯一网关来控制流程的流向。 也要注意，因为我们还没有实现泳道（可能在下一个版本会实现）， 所以很难看到谁在业务流程中。 
	流程的XML版本看起来像下面这样：

```xml
<process id="vacationRequestProcess" name="BPMN2 Example process using task forms">

    <startEvent id="start" />

    <sequenceFlow id="flow1" name="fromStartToRequestVacation"
      sourceRef="start" targetRef="requestVacation" />

    <userTask id="requestVacation" name="Request Vacation"
      implementation="other">
     <potentialOwner resourceRef="user" jbpm:type="group">
        <resourceAssignmentExpression>
          <formalExpression>user</formalExpression>
        </resourceAssignmentExpression>
      </potentialOwner>
      <rendering id="requestForm">
        <jbpm:form>org/jbpm/examples/bpmn/usertask/taskform/request_vacation.ftl</jbpm:form>
      </rendering>
    </userTask>

    <sequenceFlow id="flow2"
      name="fromRequestVacationToVerifyRequest" sourceRef="requestVacation"
      targetRef="verifyRequest" />

    <userTask id="verifyRequest" name="Verify Request"
      implementation="other">
      <potentialOwner resourceRef="user" jbpm:type="group">
        <resourceAssignmentExpression>
          <formalExpression>manager</formalExpression>
        </resourceAssignmentExpression>
      </potentialOwner>
      <rendering id="verifyForm">
        <jbpm:form>org/jbpm/examples/bpmn/usertask/taskform/verify_request.ftl</jbpm:form>
      </rendering>
    </userTask>

    <sequenceFlow id="flow3" name="fromVerifyRequestToEnd"
      sourceRef="verifyRequest" targetRef="theEnd">
      <conditionExpression xsi:type="tFormalExpression">
        ${verificationResult == 'OK'}
      </conditionExpression>
    </sequenceFlow>

    <sequenceFlow id="flow4"
      name="fromVerifyRequestToSendRejectionMessage" sourceRef="verifyRequest"
      targetRef="sendRejectionMessage">
      <conditionExpression xsi:type="tFormalExpression">
        ${verificationResult == 'Not OK'}
      </conditionExpression>
    </sequenceFlow>

    <scriptTask id="sendRejectionMessage" name="Send rejection Message"
      scriptLanguage="bsh">
      <script>
        <![CDATA[System.out.println("Vacation request refused!");]]>
      </script>
    </scriptTask>

    <sequenceFlow id="flow5"
      name="fromSendRejectionMessageToEnd" sourceRef="sendRejectionMessage"
      targetRef="theEnd" />

    <endEvent id="theEnd" name="End" />
</process>

```
<strong>注意：</strong>当你在安装demo时，自己都已经安装了。 也要注意，我们这里使用了脚本任务，为了快速的编写一些输出， 而不是发送真实的信息（图形显示了一个service task）。 也要注意，我们这里在任务分配中做了一些简略 （会在下一个版本进行修复）。 
	在这个实现使用的结构中覆盖了之前章节中的所有内容。 也要注意我们这里使用了任务表单功能， 这是一个自定义jBPM扩展， 可以为用户任务渲染元素.

```xml
<userTask id="verifyRequest" name="Verify Request"
       implementation="other">
  <potentialOwner resourceRef="user" jbpm:type="group">
    <resourceAssignmentExpression>
      <formalExpression>user</formalExpression>
    </resourceAssignmentExpression>
  </potentialOwner>
  <rendering id="verifyForm">
    <jbpm:form>org/jbpm/examples/bpmn/usertask/taskform/verify_request.ftl</jbpm:form>
  </rendering>
</userTask>
```
	BPMN 2.0里任务表单的机制与jPDL里完全一样。 表单自身是一个Freemarker模板文件， 需要放在发布中。比如，这个 'verify_request.ftl' 看起来像下面这样：

</p>

```xml
<html>
  <body>

    <form action="${form.action}" method="POST" enctype="multipart/form-data">

      <h3>Your employee, ${employee_name} would like to go on vacation</h3>
      Number of days: ${number_of_days}<br/>

      <hr>

      In case you reject, please provide a reason:<br/>
      <input type="textarea" name="reason"/><br/>

      <input type="submit" name="verificationResult" value="OK">
      <input type="submit" name="verificationResult" value="Not OK">

    </form>
  </body>
</html>
```
<p>
	注意，流程变量可以使用 ${my_process_variable}来访问。也要注意输入控件的名称。 （比如，输入文本框，提交表单）可以用来 定义新流程变量。 比如，下面的输入文本会被保存为 流程变量'reason'。

```xml
<input type="textarea" name="reason"/>
```
	注意这里有两个提交按钮（这是当然的，如果你看到'OK'和'Not OK'两个顺序流 从'request vacation'任务里出去了。通过点击其中一个按钮， 流程变量'verificationResult'会被保存起来。 它可以用来执行外出的顺序流： 

```xml
<sequenceFlow id="flow3" name="fromVerifyRequestToEnd"
      sourceRef="verifyRequest" targetRef="theEnd">
  <conditionExpression xsi:type="tFormalExpression">
    ${verificationResult == 'OK'}
  </conditionExpression>
</sequenceFlow>
```
</p>
<p>
流程现在可以发布了。你可以使用ant的发布任务来做这些事情（参考实例）， 或者你可以指定你的jBPM配置到控制台的数据库。 为了用编程的方式发布你的流程，你需要把任务表单添加到你的发布中：

```java
NewDeployment deployment = repositoryService.createDeployment();
deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/usertask/taskform/vacationrequest.bpmn.xml");
deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/usertask/taskform/request_vacation.ftl");
deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/usertask/taskform/verify_request.ftl");
deployment.deploy();
```
</p>
<p>
	你现在可以嵌入（或在单独的服务器中）这个业务流程，使用熟悉的jBPM API操作。 比如，流程实例现在可以使用 key来启动（比如，BPMN 2.0的流程id）：

```java
ProcessInstance pi = executionService.startProcessInstanceByKey("vacationRequestProcess");
```

任务列表可以这样获得：

```java
Task requestTasktask = taskService.createTaskQuery().candidate("peter").uniqueResult();
```
	当像jBPM控制器数据库发布时，你应该看到我们的新业务流程了。
</p>

![图12](/java框架/activiti/img/act13.png)

	在你启动一个新流程后，一个新任务应该在员工的任务列表中了。 当点击'view'以后，任务表单会被显示出来， 在这里填写未来会在流程中使用的变量。 

![图13](/java框架/activiti/img/act14.png)

	在任务结束之后，经理会在他的任务列表中看到新的审核任务。 他现在可以通过或驳回请假申请，基于员工的输入。 

![图14](/java框架/activiti/img/act15.png)

	因为数据库表结构没有变化，我们只是把BPMN 2.0添加到了jBPM PVM上面， 所有已存的报表都可以用于我们的新BPMN 2.0流程中。

![图16](/java框架/activiti/img/act16.png)


