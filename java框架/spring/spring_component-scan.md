### 在使用<context:component-scan> 有时候会出现加载不到bean、或者bean被加载多次、甚至有些事务不起作用。下面分析一线原因
1. 一般使用spring中会用到spring主容器和springmvc子容器，这是出现上述的主要原因
