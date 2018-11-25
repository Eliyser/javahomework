package com.haien.myowntest;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @Author haien
 * @Description nio写大文件几种方法比较
 * @Date 2018/11/24
 **/
public class WriteBigFile {
    //待写入内容总大小
    private static final long LEN=2L*1024*1024*1024; //2G
    //每次写多少进去
    private static final int DATA_CHUNK=128*1024*1024; //128M;chunk：块

    public static void writeWithFileChannel() throws IOException {
        File file=new File("e:/fc.dat");
        if(file.exists()){
            file.delete();
        }else{
            file.createNewFile(); //上级目录必须存在
        }

        //用任意访问的方式打开文件，指定要来读写它
        RandomAccessFile raf=new RandomAccessFile(file,"rw");
        FileChannel fileChannel=raf.getChannel();

        //存储待写入内容
        byte[] data=null;
        //缓冲数组
        ByteBuffer buf=ByteBuffer.allocate(DATA_CHUNK);
        //单位转换，128*1024*1024B->128M
        int dataChunk=DATA_CHUNK/1024/1024;
        //未写入的内容大小
        long len;
        for(len=LEN;len>DATA_CHUNK;len-=DATA_CHUNK){
            System.out.println("Write a data chunk: "+dataChunk+"MB");
            buf.clear();
            data=new byte[DATA_CHUNK];
            //省略数据准备
            for(int i=0;i<DATA_CHUNK;i++){
                //把数据放到缓冲区里来
                buf.put(data[i]);
            }
            //清空数据存储区
            data=null;
            buf.flip();
            //从缓冲区写入通道
            fileChannel.write(buf);
            //把通道里的内容强制刷出
            fileChannel.force(true);
        }
        //最后一次可能不足DATA_CHUNK
        if(len>0){
            System.out.println("Write rest data chunk: "+len/1024/1024+"MB");
            //剩下的比较少，可以从本地内存中分配
            buf=ByteBuffer.allocateDirect((int)len);
            data=new byte[(int)len];
            for(int i=0;i<len;i++){
                buf.put(data[i]);
            }
            buf.flip();
            fileChannel.write(buf);
            fileChannel.force(true);
            data=null;
        }

        fileChannel.close();
        raf.close();
    }

    public static void wirteWithMappedByteBuffer() throws IOException {
        File file=new File("e:/mb.dat");
        if(file.exists()){
            file.delete();
        }else{
            file.createNewFile();
        }

        RandomAccessFile raf=new RandomAccessFile(file,"rw");
        FileChannel fileChannel=raf.getChannel();
        byte[] data=null;
        int start;
        MappedByteBuffer mbb=null;
        long len;
        int dataChunk=DATA_CHUNK/1024/1024;
        for(start=0,len=LEN;len>=DATA_CHUNK;len-=DATA_CHUNK,start+=DATA_CHUNK){
            System.out.println("Write a data chunk: "+dataChunk+"MB");
            mbb=fileChannel.map(FileChannel.MapMode.READ_WRITE,start,DATA_CHUNK);
            data=new byte[DATA_CHUNK];
            mbb.put(data);
            data=null;
        }
        if(len>0){
            mbb=fileChannel.map(FileChannel.MapMode.READ_WRITE,start,DATA_CHUNK);
            data=new byte[DATA_CHUNK];
            mbb.put(data);
            data=null;
        }
        //release MappedByteBuffer
        unmap(mbb);
        fileChannel.close();
    }

    /**
     * @Author haien
     * @Description 一般用了MappedByteBuffer之后都是这样释放资源的，淡单单channel.close()是不够的
     * @Date 2018/11/24
     * @Param [mappedByteBuffer]
     * @return void
     **/
    public static void unmap(final MappedByteBuffer mappedByteBuffer){
        try {
            if (mappedByteBuffer == null) {
                return;
            }

            //把残留的内容强制刷出
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                @SuppressWarnings("restriction")
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass()
                                .getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod
                                .invoke(mappedByteBuffer, new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @Author haien
     * @Description 中间转换成字节数组再写入文件
     * @Date 2018/11/25
     * @Param []
     * @return void
     **/
    public static void writeWithByteArray() throws IOException {
        File file=new File("e:/ba.dat");
        if(file.exists()){
            file.delete();
        }else{
            file.createNewFile();
        }

        RandomAccessFile raf=new RandomAccessFile(file,"rw");
        FileChannel fileChannel=raf.getChannel();

        //数据准备
        byte[] data=null;
        //把数据包装为字节数组
        ByteArrayInputStream bis=null;
        //把字节数组转化为流
        ReadableByteChannel byteChannel=null;
        long len=0;
        long start=0;
        //单位转换
        int dataChunk=DATA_CHUNK/1024/1024;
        for(len=LEN;len>=DATA_CHUNK;len-=DATA_CHUNK,start+=DATA_CHUNK){
            System.out.println("Write a data chunk: "+dataChunk+"MB");

            //数据准备
            data=new byte[DATA_CHUNK];
            //包装成字节数组输入流
            bis=new ByteArrayInputStream(data);
            //获取通道
            byteChannel =Channels.newChannel(bis);
            //写到目标文件的channel中
            fileChannel.transferFrom(byteChannel,start,DATA_CHUNK); //transferFrom:将其他通道从start字节开始写指定字节数到文件通道中
            data=null;
        }

        if(len>0){
            System.out.println("Write rest data chunk: "+len/1024/1024+"MB");

            data=new byte[(int)len];
            bis=new ByteArrayInputStream(data);
            byteChannel=Channels.newChannel(bis);
            fileChannel.transferFrom(fileChannel,start,len);
            data=null;
        }
        fileChannel.close();
        byteChannel.close();
    }

    public static void main(String[] args) throws IOException {
        long startTime=System.currentTimeMillis();
        //writeWithFileChannel();//30s左右
        wirteWithMappedByteBuffer(); //48s
        //writeWithByteArray(); //19s
        long endTime=System.currentTimeMillis();
        System.out.println("运行时间："+(endTime-startTime)/1000+"s");

    }


    /**
     * @Author haien
     * @Description 读字节数组
     * @Date 2018/11/25
     * @Param []
     * @return void
     **/
    public void readByteArray() throws IOException {
        /**
         * 准备字节数组：把这个想象成是我们要从服务器上读取的内容
         * 那为什么服务器不直接把这句话传给我们我们直接打印不就好了吗？
         * 因为直接传字符串涉及到编码问题，所以中间的传输部分还是用字节数组来的准确
         */
        String meg="操作与文件输入流一致";
        byte[] src=meg.getBytes();
        /**
         * 选择流
         * ByteArrayInputStream: 把字节数组包装成输入流
         * BufferedInputStream：再加个缓冲区调优
         * 这里不会抛异常（FileNotFoundException之类），不是跟外部资源建立联系就不会抛异常
         */
        InputStream is=new BufferedInputStream(new ByteArrayInputStream(src));
        //缓冲数组
        byte[] flush=new byte[1024];
        int len=0;
        while((len=is.read(flush))!=-1){
            System.out.println(new String(flush,0,len));
        }
        //释不释放资源都无所谓了
    }

    /**
     * @Author haien
     * @Description 写字节数组，跟写文件略有不同，有新增方法，不能使用多台
     * @Date 2018/11/25
     * @Param []
     * @return void
     **/
    public byte[] writeToByteArray(){
        //准备字节数组
        String msg="操作与文件输出流不一致";
        byte[] info=msg.getBytes();
        //目的地
        byte[] dest; //不知道多大，先搁着
        ByteArrayOutputStream bos=new ByteArrayOutputStream(); //不能直接把字节数组dest丢进来；不要使用多态，因为等下要使用新增方法toByteArray()
        //写入
        bos.write(info,0,info.length); //只是写到了管道里，没有写到目标字节数组中
        dest=bos.toByteArray();
        return dest;
    }

    /**
     * @Author haien
     * @Description ByteArrayStream的应用:把服务端的文件流保存到字节数组中，以便多次访问
     * @Date 2018/11/25
     * @Param []
     * @return void
     **/
    /*public void byteArrayStreamApp() throws IOException {
        //目的地
        byte[] dest=null;
        //源
        InputStream is=httpconn.getInputStream();
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int len;
        while((len=is.read(buffer))!=-1){
            bos.write(buffer,0,len);
        }
        bos.flush();
        dest=bos.toByteArray();

        //第一次访问
        InputStream is1=new ByteArrayInputStream(dest);
        //do something to print the stream...

        //第二次访问
        InputStream is2=new ByteArrayInputStream(dest);
        //do something to save in local...
    }*/
}


















