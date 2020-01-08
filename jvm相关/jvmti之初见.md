### 简介
VMTI（JVM Tool Interface）是 Java 虚拟机所提供的 native 编程接口，是 JVMPI（Java Virtual Machine Profiler Interface）
###Agent 的工作过程

Agent 是在 Java 虚拟机启动之时加载的，这个加载处于虚拟机初始化的早期，在这个时间点上：

    所有的 Java 类都未被初始化；
    所有的对象实例都未被创建；
    因而，没有任何 Java 代码被执行；

但在这个时候，我们已经可以：

    操作 JVMTI 的 Capability 参数；
    使用系统参数；

[官方文档(jdk111)：](https://docs.oracle.com/en/java/javase/11/docs/specs/jvmti.html)

动态库被加载之后，虚拟机会先寻找一个 Agent 入口函数：
```java
JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
```
这个函数中，虚拟机传入了一个 JavaVM 指针，以及命令行的参数。
通过 JavaVM，我们可以获得 JVMTI 的指针，并获得 JVMTI 函数的使用能力，所有的 JVMTI 函数都通过这个 jvmtiEnv 获取，不同的虚拟机实现提供的函数细节可能不一样，但是使用的方式是统一的。
```java
jvmtiEnv *jvmti;
...
(*jvm)->GetEnv(jvm, &jvmti, JVMTI_VERSION_1_0);
```
### JVMTI的启动方式

 JVMTI有两种启动方式，第一种是随java进程启动时，自动载入共享库。另一种方式是，java运行时，通过attach api动态载入。

方式1的实现方式是通过在java启动时传递一个特殊的option:
```sh
    java -agentlib:<agent-lib-name>=<options> Sample
    注意，这里的共享库路径是环境变量路径，例如 java -agentlib:foo=opt1,opt2，java启动时会从linux的LD_LIBRARY_PATH或windows的PATH环境变量定义的路径处装载foo.so或foo.dll，找不到则抛异常
    java -agentpath:<path-to-agent>=<options> Sample
    这是以绝对路径的方式装载共享库，例如 java -agentpath:/home/admin/agentlib/foo.so=opt1,opt2

    windows下：的动态链接生成为dll而不是.so文件，稍后主要使用windows下vs编译共享库文件
 ```

 方式2的实现方式是通过attach api，这是一套纯java的api，它负责动态地将dynamic module attach到指定进程id的java进程内并触发回调：
 ```java
import java.io.IOException;
import com.sun.tools.attach.VirtualMachine;
public class VMAttacher {
    public static void main(String[] args) throws Exception {
	 // args[0]为java进程id
         VirtualMachine virtualMachine = com.sun.tools.attach.VirtualMachine.attach(args[0]);
         // args[1]为共享库路径，args[2]为传递给agent的参数
         virtualMachine.loadAgentPath(args[1], args[2]);
         virtualMachine.detach();
    }
}
 ```
 * Attach API位于$JAVA_HOME/lib/tools.jar，所以在编译时，需要将这个jar放入classpath

 ```
 javac -cp $JAVA_HOME/lib/tools.jar VMAttacher.java
 ```

 ### 简单开发JVMTI Agent

下面一个简单的例子，阐述如何开发一个简单的 Agent 。这个 Agent 是通过 C++ 编写的，通过监听 JVMTI_EVENT_METHOD_ENTRY 事件，
注册对应的回调函数来响应这个事件，来输出所有被调用函数名。

Agent.cpp
```cpp
#include <iostream>

#include "MethodTraceAgent.h"
#include <jvmti.h>

using namespace std;

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
    cout << "Agent_OnLoad(" << vm << ")" << endl;
    try{
        
        MethodTraceAgent* agent = new MethodTraceAgent();
        agent->Init(vm);
        agent->ParseOptions(options);
        agent->AddCapability();
        agent->RegisterEvent();
        
    } catch (AgentException& e) {
        cout << "Error when enter HandleMethodEntry: " << e.what() << " [" << e.ErrCode() << "]";
        return JNI_ERR;
    }
    
    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm)
{
    cout << "Agent_OnUnload(" << vm << ")" << endl;
}
```

MethodTraceAgent.h

```cpp
#include "<jvmti.h>"
#include <string>
using namespace std;
class AgentException 
{
 public:
    AgentException(jvmtiError err) {
        m_error = err;
    }

    string what() const throw() { 
        return "AgentException"; 
    }

    jvmtiError ErrCode() const throw() {
        return m_error;
    }

 private:
    jvmtiError m_error;
};


class MethodTraceAgent 
{
 public:

    MethodTraceAgent() {}

    ~MethodTraceAgent() ;

    void Init(JavaVM *vm) ;
        
    void ParseOptions(const char* str) ;

    void AddCapability() ;
        
    void RegisterEvent() ;
    
    static void JNICALL HandleMethodEntry(jvmtiEnv* jvmti, JNIEnv* jni, jthread thread, jmethodID method);

 private:
    static void CheckException(jvmtiError error) 
    {
        // 可以根据错误类型扩展对应的异常，这里只做简单处理
        if (error != JVMTI_ERROR_NONE) {
            throw AgentException(error);
        }
    }
    
    static jvmtiEnv * m_jvmti;
    static char* m_filter;
};
```

MethodTraceAgent.cpp
```cpp
#include <iostream>
#include <string.h>
#include <stdio.h> 
#include "MethodTraceAgent.h"
#include "<vmti.h>"

using namespace std;

jvmtiEnv* MethodTraceAgent::m_jvmti = 0;
char* MethodTraceAgent::m_filter = 0;

MethodTraceAgent::~MethodTraceAgent()
{
    // 必须释放内存，防止内存泄露
    m_jvmti->Deallocate(reinterpret_cast<unsigned char*>(m_filter));
}

void MethodTraceAgent::Init(JavaVM *vm) {
    jvmtiEnv *jvmti = 0;
    jint ret = (vm)->GetEnv(reinterpret_cast<void**>(&jvmti), JVMTI_VERSION_1_0);
    if (ret != JNI_OK || jvmti == 0) {
        throw AgentException(JVMTI_ERROR_INTERNAL);
    }
    m_jvmti = jvmti;
}

void MethodTraceAgent::ParseOptions(const char* str)
{
    if (str == 0)
        return;
    const size_t len = strlen(str);
    if (len == 0) 
        return;

    // 必须做好内存复制工作
    jvmtiError error;
    error = m_jvmti->Allocate(len + 1,reinterpret_cast<unsigned char**>(&m_filter));
    CheckException(error);
    strcpy(m_filter, str);

    // 可以在这里进行参数解析的工作
    // ...
}

void MethodTraceAgent::AddCapability()
{
    // 创建一个新的环境
    jvmtiCapabilities caps;
    memset(&caps, 0, sizeof(caps));
    caps.can_generate_method_entry_events = 1;
    
    // 设置当前环境
    jvmtiError error = m_jvmti->AddCapabilities(&caps);
    CheckException(error);
}
  
void MethodTraceAgent::RegisterEvent()
{
    // 创建一个新的回调函数
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.MethodEntry = &MethodTraceAgent::HandleMethodEntry;
    
    // 设置回调函数
    jvmtiError error;
    error = m_jvmti->SetEventCallbacks(&callbacks, static_cast<jint>(sizeof(callbacks)));
    CheckException(error);

    // 开启事件监听
    error = m_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, 0);
    CheckException(error);
}

void JNICALL MethodTraceAgent::HandleMethodEntry(jvmtiEnv* jvmti, JNIEnv* jni, jthread thread, jmethodID method)
{
    try {
        jvmtiError error;
        jclass clazz;
        char* name;
        char* signature;
        
        // 获得方法对应的类
        error = m_jvmti->GetMethodDeclaringClass(method, &clazz);
        CheckException(error);
        // 获得类的签名
        error = m_jvmti->GetClassSignature(clazz, &signature, 0);
        CheckException(error);
        // 获得方法名字
        error = m_jvmti->GetMethodName(method, &name, NULL, NULL);
        CheckException(error);
        
        // 根据参数过滤不必要的方法
        if(m_filter != 0){
            if (strcmp(m_filter, name) != 0)
                return;
        }           
        cout << signature<< " -> " << name << "(..)"<< endl;

        // 必须释放内存，避免内存泄露
        error = m_jvmti->Deallocate(reinterpret_cast<unsigned char*>(name));
        CheckException(error);
        error = m_jvmti->Deallocate(reinterpret_cast<unsigned char*>(signature));
        CheckException(error);

    } catch (AgentException& e) {
        cout << "Error when enter HandleMethodEntry: " << e.what() << " [" << e.ErrCode() << "]";
    }
}
```
创建一个java程序
```java
public class MethodTraceTest {

    public static void main(String[] args){
        MethodTraceTest test = new MethodTraceTest();
        test.first();
        test.second();
    }
    
    public void first(){
        System.out.println("=> test first()");
    }
    
    public void second(){
        System.out.println("=> test second()");
    }
}
```
运行时序图：![图片.png](https://upload-images.jianshu.io/upload_images/7779493-004e2f3d1104cb76.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 操作流程

1. 编译java程序生成class文件

>  javac MethodTraceTest.java

2. 编译Agent 动态链接库，需要将 JDK 提供的一些头文件包含进来

Windows: 
```
cl /EHsc -I${JAVA_HOME}/include/ -I${JAVA_HOME}/include/win32 -LD MethodTraceAgent.cpp Agent.cpp -FeAgent.dll
```
Linux: 
```
g++ -I${JAVA_HOME}/include/ -I${JAVA_HOME}/include/linux  MethodTraceAgent.cpp Main.cpp -fPIC -shared -o libagent.so
```
**注意需要有c++环境，如果是windows环境使用vs 可使用cl**

[Windows下vs2019 c++ toolset](https://docs.microsoft.com/en-us/cpp/build/building-on-the-command-line?view=vs-2019)
注意编译动态库操作系统类型，64bit下使用工具"x64 Native Tools Command Prompt for VS 2019"(可在安装vs后windows搜素框搜索)，具体可参考上面官方文档

### 执行
上面编译动态库后，win下为dll文件、linux为so文件，将编译后的class文件拷贝到动态文件下，执行命令
>java  -agentlib:Agent=first -cp ./ MethodTraceTest
结果如下说明成功监听到虚拟机相应事件
```sh
Agent_OnLoad(000000006990F4A0)
LMethodTraceTest; -> first(..)
=> Call first()
=> Call second()
Agent_OnUnload(000000006990F4A0)
```

* 至此算是初步了解jvmti，后续再详解另一种方式以及更多jvmti相关操作
