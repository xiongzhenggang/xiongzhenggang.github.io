/**
 * 
 */
package com.xzg.cn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author hasee
 * @TIME 2017年1月3日
 * 注意类的隐藏和实例创建
 */
public class TestServiceNio {
	private static final int BUF_SIZE=1024;
    private static final int PORT = 8080;
    private static final int TIMEOUT = 3000;
 
    public static void main(String[] args)
    {
        selector();
    }
 //监听新进来的连接
    public static void handleAccept(SelectionKey key) throws IOException{
        ServerSocketChannel ssChannel = (ServerSocketChannel)key.channel();
        SocketChannel sc = ssChannel.accept();
        //在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。 因此，需要检查返回的SocketChannel是否是null
        sc.configureBlocking(false);//设置非阻塞
        sc.register(key.selector(), SelectionKey.OP_READ,ByteBuffer.allocateDirect(BUF_SIZE));
    }
 
    public static void handleRead(SelectionKey key) throws IOException{//准备读，从SelectionKey访问Channel和Selector很简单
        SocketChannel sc = (SocketChannel)key.channel();
        ByteBuffer buf = (ByteBuffer)key.attachment();
        long bytesRead = sc.read(buf);
        while(bytesRead>0){
            buf.flip();
            while(buf.hasRemaining()){
                System.out.print((char)buf.get());
            }
            System.out.println();
            buf.clear();
            bytesRead = sc.read(buf);
        }
        if(bytesRead == -1){
            sc.close();
        }
    }
 
    public static void handleWrite(SelectionKey key) throws IOException{//准备写
        ByteBuffer buf = (ByteBuffer)key.attachment();
        buf.flip();
        SocketChannel sc = (SocketChannel) key.channel();
        while(buf.hasRemaining()){
            sc.write(buf);
        }
        buf.compact();
    }
 
    public static void selector() {
        Selector selector = null;
        ServerSocketChannel ssc = null;
        try{
        	//Selector的创建：Selector selector = Selector.open();
            selector = Selector.open();
            //打开ServerSocketChannel：
            ssc= ServerSocketChannel.open();
            //绑定地址端口号
            ssc.socket().bind(new InetSocketAddress(PORT));
            ssc.configureBlocking(false);//配置非阻塞
            /*与Selector一起使用时，Channel必须处于非阻塞模式下。这意味着不能将FileChannel与Selector一起使用，
             * 因为FileChannel不能切换到非阻塞模式。而套接字通道都可以。
             * 为了将Channel和Selector配合使用，必须将Channel注册到Selector上，
             * 通过SelectableChannel.register()方法来实现，沿用案例5中的部分代码：*/
            ssc.register(selector, SelectionKey.OP_ACCEPT);//register()方法的第二个参数。这是一个“interest集合”.Selector监听Channel时对什么事件感兴趣。可以监听四种不同类型的事件：
            /*Connect
             * 2. Accept
            3. Read
            4. Write*/
            /*ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，
            如果还没有新进来的连接,返回的将是null。 因此，需要检查返回的SocketChannel是否是null.如：
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null)
            {
                // do something with socketChannel...
            }    */
            while(true){
         /*一旦向Selector注册了一或多个通道，就可以调用几个重载的select()方法。这些方法返回你所感兴趣的事件（如连接、接受、读或写）
         已经准备就绪的那些通道。换句话说，如果你对“读就绪”的通道感兴趣，select()方法会返回读事件已经就绪的那些通道。*/
            	//select()方法返回的int值表示有多少通道已经就绪。然后可以通过调用selector的selectedKeys()方法，访问“已选择键集（selected key set）”中的就绪通道。
                if(selector.select(TIMEOUT) == 0){//select()阻塞到至少有一个通道在你注册的事件上就绪了
                    System.out.println("==");
                  /*selectNow()不会阻塞，不管什么通道就绪都立刻返回（译者注：此方法执行非阻塞的选择操作。
                    如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零。）*/
                    continue;
                }
                /*通道触发了一个事件意思是该事件已经就绪。所以，某个channel成功连接到另一个服务器称为“连接就绪”。
                 * 一个server socket channel准备好接收新进入的连接称为“接收就绪”。
                 * 一个有数据可读的通道可以说是“读就绪”。等待写数据的通道可以说是“写就绪”。 这四种事件用SelectionKey的四个常量来表示*/
                //SelectionKey.OP_CONNECT\SelectionKey.OP_ACCEPT\ SelectionKey.OP_READ\SelectionKey.OP_WRITE
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while(iter.hasNext()){
                	//当向Selector注册Channel时，register()方法会返回一个SelectionKey对象。这个对象包含了一些你感兴趣的属性：
                    SelectionKey key = iter.next();
 // 可以用像检测interest集合那样的方法，来检测channel中什么事件或操作已经就绪。也可使用下四个方法，它们都返回一个布尔类型
                    if(key.isAcceptable()){//检测channel中什么事件或操作已经就绪
                        handleAccept(key);//监听新进来的连接
                    }
                    if(key.isReadable()){
                        handleRead(key);
                    }
                    if(key.isWritable() && key.isValid()){
                        handleWrite(key);
                    }
                    if(key.isConnectable()){
                        System.out.println("isConnectable = true");
                    }
                    iter.remove();
                    /*注意每次迭代末尾的keyIterator.remove()调用。Selector不会自己从已选择键集中移除SelectionKey实例。
                    必须在处理完通道时自己移除。下次该通道变成就绪时，Selector会再次将其放入已选择键集中*/
                }
            }
 
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                if(selector!=null){
                    selector.close();
                }
                if(ssc!=null){
                	//关闭ServerSocketChannel
                    ssc.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
