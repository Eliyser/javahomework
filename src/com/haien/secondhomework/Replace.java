package com.haien.secondhomework;

import com.haien.myexception.InvalidArgumentException;
import com.haien.myexception.InvalidFileException;
import com.haien.myexception.IsDirectoryException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @Author haien
 * @Description 删除指定文件的某个字符串
 * @Date 2018/11/21
 **/
public class Replace {
    //如果不考虑超大文件的话直接读取文件到内存，否则要考虑分块读取、临时文件、追加内容、校验、删除原有文件、改名

    public static void main(String[] args) throws InvalidArgumentException {
        //命令行传入参数
        if(args.length==0||null==args[0]||null==args[1]) { //命令行参数为空的话访问args[0]会溢出，所以先判空
            throw new InvalidArgumentException("请指定文件和字符串！");
        }

        //调用删除字符串的方法
        try {
            replaceInFile2(args[0],args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IsDirectoryException e) {
            e.printStackTrace();
        } catch (InvalidFileException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Author haien
     * @Description 新建文件，复制原文件内容，读取新文件内容，删除指定字符串，覆盖原文件内容
     * @Date 2018/11/20
     * @Param [path, target] 指定文件，指定字符串
     * @return void
     **/
    private static void replaceInFile(String path,String target) throws IOException, IsDirectoryException, InvalidFileException {
        //用指定路径创建Path对象
        Path file=Paths.get(path);
        File file1=file.toFile();

        //检测文件是否存在
        if(!Files.exists(file)){
            throw new FileNotFoundException("文件不存在！");
        }
        //检测是否为文件
        if(Files.isDirectory(file)){
            throw new IsDirectoryException("指定路径非文件！");
        }

        //日志
        System.out.println("读取文件："+file.toAbsolutePath()+"\n开始删除字符串："+target);

        //缓存文件
        String filename=file.getFileName().toString().substring(0,file.getFileName().toString().lastIndexOf(".")); //切割文件名
        String suffix=file.getFileName().toString().substring(file.getFileName().toString().lastIndexOf(".")); //文件后缀
        if(!suffix.equals(".txt")){
            throw new InvalidFileException("文件不是文本文件！");
        }
        Path tmpFile=Paths.get(file.getParent()+"/"+filename+".tmp"+suffix);
        Files.copy(file,tmpFile);

        BufferedReader reader=null;
        BufferedWriter writer=null;
        //读取文本文件
        try {
            reader=Files.newBufferedReader(tmpFile);
            writer=Files.newBufferedWriter(file);
            String info=null;
            while ((info=reader.readLine())!=null){ //纯文本大多数有换行符
                //删除指定字符串
                if(info.contains(target)) {
                    info=info.replaceAll(target, "");
                }
                //写入缓存文件（必须手动加换行符且不能漏掉/r）
                writer.write(info+"\r\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new IOException("操作文件失败！");
        }finally {
            //释放资源
            if (null!=writer){
                writer.close();
            }

        }
        //删除缓存文件
        Files.delete(tmpFile);
        //日志
        System.out.println("字符串删除完毕！");
    }

    /**
     * @Author haien
     * @Description 新建缓存文件，读取源文件，删除字符串后写入缓存文件，重命名缓存文件，删除源文件
     * @Date 2018/11/24
     * @Param [path, target]
     * @return void
     **/
    private static void replaceInFile2(String path,String target) throws IOException, IsDirectoryException, InvalidFileException {
        //用指定路径创建Path对象
        Path file=Paths.get(path);
        File file1=file.toFile();

        //缓存文件
        String filename=file.getFileName().toString().substring(0,file.getFileName().toString().lastIndexOf(".")); //切割文件名
        String suffix=file.getFileName().toString().substring(file.getFileName().toString().lastIndexOf(".")); //文件后缀
        Path tmpFile=Paths.get(file.getParent()+"/"+filename+".tmp"+suffix); //试试.tmp结尾

        //读取文本文件
        BufferedReader reader = Files.newBufferedReader(file);
        BufferedWriter writer = Files.newBufferedWriter(tmpFile);
        String info = null;
        while ((info = reader.readLine()) != null) { //纯文本大多数有换行符
            if (info.contains(target)) {
                info = info.replaceAll(target, "");
            }
            writer.write(info + "\r\n");
        }
        writer.flush();
        writer.close();
        reader.close(); //记得关，不然后面move报错FileAlreadyExistsException，因为文件被占用了

        //强制覆盖源文件，相当于重命名
        Files.move(tmpFile,file,StandardCopyOption.REPLACE_EXISTING); //tmp覆盖源文件内容，但源文件名不变
        /*删除缓存文件
        Files.delete(tmpFile); 上面move换成copy的话就需要删除了*/
        //日志
        System.out.println("字符串删除完毕！");
    }
}























