## linux 中so库问题
问题一
1. error while loading shared libraries: libGL.so.1

如下解决方案如下：
```xml
I am trying to run Chamsys MagicQ on Ubuntu 12.10 with 64bit AMD processor.

This is what it tells me when I try to run the program.


./magicq: error while loading shared libraries: libGLU.so.1: cannot open shared object >file: No such file or directory



austin@ubuntu:~/magicq$ ldd ./magicq

linux-gate.so.1 => (0xf7799000)

libGLU.so.1 => not found

libusb-0.1.so.4 => not found

libQt5PrintSupport.so.5 => not found

libQt5OpenGL.so.5 => not found

libQt5Widgets.so.5 => not found

libQt5Network.so.5 => not found

libQt5Gui.so.5 => not found

libQt5Core.so.5 => not found

libGL.so.1 => /usr/lib32/fglrx/libGL.so.1 (0xf7694000)

libpthread.so.0 => /lib/i386-linux-gnu/libpthread.so.0 (0xf7679000)

libstdc++.so.6 => not found

libm.so.6 => /lib/i386-linux-gnu/libm.so.6 (0xf764d000)

libgcc_s.so.1 => /lib/i386-linux-gnu/libgcc_s.so.1 (0xf762e000)

libc.so.6 => /lib/i386-linux-gnu/libc.so.6 (0xf7483000)

libXext.so.6 => not found

libatiuki.so.1 => /usr/lib32/fglrx/libatiuki.so.1 (0xf746c000)

libdl.so.2 => /lib/i386-linux-gnu/libdl.so.2 (0xf7467000)

/lib/ld-linux.so.2 (0xf779a000)
```
I know libGLU.so.1 is in /usr/lib/x86_64-linux-gnu as a "link to shared library (application/x-sharedlib)" Link target: "libGLU.so.1.3.1" How do I fix this?

成功解决 
Actually I couldn't find libglu package itself. What helped is:

sudo apt-get install libglu1-mesa:i386

Ubuntu 14.0



问题描述2：
```
The problem that we solved by commenting was that you were using 32-bits libraries in a 64-bits system.
```
解决方案：
wrong ELF class: ELFCLASS32

The ending of the class should have been 64, hence producing this error. The way to go is purging the 32-bits libraries then reinstalling the 64-bits.


sudo apt-get purge libgl1-mesa-glx:i386

sudo apt-get --reinstall install libgl1-mesa-glx

And refreshing our GNU linker:


sudo ldconfig

Once everything is ok, ldconfig -p | grep libGL.so.1 should show:



libGL.so.1 (libc6) => /usr/lib/x86_64-linux-gnu/mesa/libGL.so.1

libGL.so.1 (libc6) => /usr/local/lib/libGL.so.1

If you ever need the 32-bits libraries for running 32-bits applications, you could do so installing the libgl1-mesa-glx:i386 package. But in this case remember to setting your LD_LIBRARY_PATH temporally to where the 32bits libraries are, so it won't mess up your other programs.



 export LD_LIBRARY_PATH="/path/to/library/"

 ./run_some_32_bit_program

