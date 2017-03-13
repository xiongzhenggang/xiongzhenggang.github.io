/**
 * 
 */
package com.xzg.cn.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author hasee
 * @TIME 2017年1月3日
 * 注意类的隐藏和实例创建
 */
public class TestServerIo {
	public static  void  main(String arg[]){
		server();
		}
	public static void server(){
	       ServerSocket serverSocket = null;
	       InputStream in = null;
	       try
	       {
	           serverSocket = new ServerSocket(80);
	           int recvMsgSize = 0;
	           byte[] recvBuf = new byte[1024];
	           while(true){
	               Socket clntSocket = serverSocket.accept();
	               SocketAddress clientAddress = clntSocket.getRemoteSocketAddress();//获取客户端段的地址
	               System.out.println("Handling client at "+clientAddress);
	               in = clntSocket.getInputStream();
	            /*   非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。*/
	               while((recvMsgSize=in.read(recvBuf))!=-1){
	                   byte[] temp = new byte[recvMsgSize];
	                   System.arraycopy(recvBuf, 0, temp, 0, recvMsgSize);
	                   System.out.println(new String(temp));
	               }
	           }
	       }
	       catch (IOException e)
	       {
	           e.printStackTrace();
	       }
	       finally{
	           try{
	               if(serverSocket!=null){
	                   serverSocket.close();
	               }
	               if(in!=null){
	                   in.close();
	               }
	           }catch(IOException e){
	               e.printStackTrace();
	           }
	       }
	   }
}
