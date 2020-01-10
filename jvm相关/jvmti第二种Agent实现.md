### Agent第二种实现方式
上一部分[第一种](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/jvm%E7%9B%B8%E5%85%B3/jvmti%E4%B9%8B%E5%88%9D%E8%A7%81.md)和本节基本相同，具体如下：

### 编写java动态执行代码
```java
package abc;
import java.io.IOException;
import com.sun.tools.attach.VirtualMachine;

public class VMAttacher {

    public static void main(String[] args) throws Exception {
	 // args[0]为java进程id
         System.out.println(args[0]);
         VirtualMachine virtualMachine = com.sun.tools.attach.VirtualMachine.attach(args[0]);
         System.out.println(virtualMachine.getClass());
         System.out.println("==========contintue");
         // args[1]为共享库路径
         virtualMachine.loadAgentPath(args[1], null);
         virtualMachine.detach();
    }

}

```
编译java程序生成class文件，指定额外的依赖
>javac -Djava.ext.dirs=C:\jdk1.8.0_101\lib ./abc/VMAttacher.java
### 编写cpp代码打印加载的类信息
```cpp
//如果引入失败可以直接指定文件地址例如 "./jvm/jvmti.h"
#include <jvmti.h>
#include <string>
#include <cstring>
#include <iostream>
#include <list>
#include <map>
#include <set>
#include <stdlib.h>
#include <jni_md.h>
using namespace std;
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* jvm, char* options,
	void* reserved) {
	jvmtiEnv* jvmti;
	jint result = jvm->GetEnv((void**)&jvmti, JVMTI_VERSION_1_1);
	if (result != JNI_OK) {
		printf("ERROR: Unable to access JVMTI!\n");
	}
	jvmtiError err = (jvmtiError)0;
	jclass* classes;
	jint count;

	err = jvmti->GetLoadedClasses(&count, &classes);
	if (err) {
		printf("ERROR: JVMTI GetLoadedClasses failed!\n");
	}
	for (int i = 0; i < count; i++) {
		char* sig;
		jvmti->GetClassSignature(classes[i], &sig, NULL);
		printf("cls sig=%s\n", sig);
	}
	return err;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
	cout << "Agent_OnUnload(" << vm << ")" << endl;
}
```
### 生成动态链接库：
>cl /EHsc -I${JAVA_HOME}/include/ -I${JAVA_HOME}/include/win32 -LD  Agent2.cpp -FeAgent2.dll
```
用于 x64 的 Microsoft (R) C/C++ 优化编译器 19.24.28314 版
版权所有(C) Microsoft Corporation。保留所有权利。

Agent2.cpp
Microsoft (R) Incremental Linker Version 14.24.28314.0
Copyright (C) Microsoft Corporation.  All rights reserved.

/dll
/implib:Agent2.lib
/out:Agent2.dll
Agent2.obj
  正在创建库 Agent2.lib 和对象 Agent2.exp
```
完成，后动态启动加载该链接库到对应的jvm中

### 执行
>java -Djava.ext.dirs=./lib -cp ./ abc.VMAttacher 12345 /xx/Agent2.dll
* lib文件下是拷贝jdk lib下的tools.jar

执行成功后，可以在对应的进程12345下看到下面jvmti打印的日志
```
class signature=Lorg/springframework/core/ReactiveAdapterRegistry$ReactorRegistrar$$Lambda$517/1273048940;
class signature=Lorg/springframework/core/ReactiveAdapterRegistry$ReactorRegistrar$$Lambda$516/375201108;
....
...
```

* 以上就是两种实现方式了，具体可以根据需求实现具体的业务逻辑
