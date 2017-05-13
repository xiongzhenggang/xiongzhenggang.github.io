
package com.xzg.cn.nio;



import java.io.BufferedInputStream;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.RandomAccessFile;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;



/**

 * @author hasee

 * @TIME 2017年1月3日

 * 注意类的隐藏和实例创建

 */

public class TestNio {



	public static  void  main(String arg[]){

	//method2();

	method1();

	}

	public static void method2(){

	       InputStream in = null;

	       try{

	           in = new BufferedInputStream(new FileInputStream("E:/logs/nio.txt"));

	 

	           byte [] buf = new byte[1024];

	           int bytesRead = in.read(buf);

	           while(bytesRead != -1)

	           {

	               for(int i=0;i<bytesRead;i++)

	                   System.out.print((char)buf[i]);

	               bytesRead = in.read(buf);

	           }

	       }catch (IOException e)

	       {

	           e.printStackTrace();

	       }finally{

	           try{

	               if(in != null){

	                   in.close();

	               }

	           }catch (IOException e){

	               e.printStackTrace();

	           }

	       }

	   }

	public static void method1(){

        RandomAccessFile aFile = null;

        try{

            aFile = new RandomAccessFile("E:/logs/nio.txt","rw");

            FileChannel fileChannel = aFile.getChannel();

            ByteBuffer buf = ByteBuffer.allocate(1024);

 

            int bytesRead = fileChannel.read(buf);

            System.out.println(bytesRead);

 

            while(bytesRead != -1)

            {

                buf.flip();

                while(buf.hasRemaining())

                {

                    System.out.print((char)buf.get());

                }

 

                buf.compact();

                bytesRead = fileChannel.read(buf);

            }

        }catch (IOException e){

            e.printStackTrace();

        }finally{

            try{

                if(aFile != null){

                    aFile.close();

                }

            }catch (IOException e){

                e.printStackTrace();

            }

        }

    }

}
