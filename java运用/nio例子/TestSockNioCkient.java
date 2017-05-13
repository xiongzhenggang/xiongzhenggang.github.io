/**

 * 

 */

package com.xzg.cn.nio;



import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;

import java.util.concurrent.TimeUnit;



/**

 * @author xzg

 * @TIME 2017年1月3日

 * 注意类的隐藏和实例创建

 */

public class TestSockNioCkient {

	public static  void  main(String arg[]){

		client();

		}

	public static void client(){

        SocketChannel socketChannel = null;//定义管道，相当于公交车

        ByteBuffer buffer = ByteBuffer.allocate(1024);//定会有多少的作为

        try

        {

        	//打开SocketChannel：

            socketChannel = SocketChannel.open();//静态方法获取实例

            socketChannel.configureBlocking(false);//设置非阻塞

            socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));//连接地址

            if(socketChannel.finishConnect())//如果连接状态

            {

                int i=0;

                while(true)//保持数据完整

                {

                    TimeUnit.SECONDS.sleep(1);

                    //读取数据：写入数据到Buffer(int bytesRead = fileChannel.read(buf);)

                    String info = "I'm "+i+++"-th information from client";

                    buffer.clear();//先进行清理

                    //从Buffer中读取数据（System.out.print((char)buf.get());）

                    /*从Channel写到Buffer (fileChannel.read(buf))

                    通过Buffer的put()方法 buf.put(…)*/

                    buffer.put(info.getBytes("utf-8"));

                    buffer.flip();

                    while(buffer.hasRemaining()){

                        System.out.println((char)buffer.get());

                        /*注意SocketChannel.write()方法的调用是在一个while循环中的。

                         * Write()方法无法保证能写多少字节到SocketChannel。所以，我们重复调用write()直到Buffer没有要写的字节为止。

                        非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。

                        所以需要关注它的int返回值，它会告诉你读取了多少字节。*/

                        socketChannel.write(buffer);

                    }

                }

            }

        }

        catch (IOException | InterruptedException e)

        {

            e.printStackTrace();

        }

        finally{

            try{

                if(socketChannel!=null){

                	//关闭：

                    socketChannel.close();

                }

            }catch(IOException e){

                e.printStackTrace();

            }

        }

    }

}
