package com.haien.firsthomework;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author haien
 * @Description 输入两个正整数m和n，求其最大公约数和最小公倍数
 * @Date 2018/10/14
 **/
public class Zhanchufa {

    //最大公约数
    private int maxfactor;
    //最小公倍数
    private int minTime;

    /**
     * @Author haien
     * @Description 辗除法求最大公约数
     * @Date 2018/10/14
     * @Param [m, n]
     * @return int
     **/
    public static int gongYueShu(int m,int n){
        if (0 == n) {
            return m;
        }
        int resd=m%n;
        //若初始m<n，则此时两数被调换；若初始m>n，则此时参数为较小数和余数
        return gongYueShu(n,resd);
    }

    /**
     * @Author haien
     * @Description 读取用户的输入，验证是否正整数并转换为整型数据
     * @Date 2018/10/14
     * @Param []
     * @return int
     **/
    public static int scanAndParse() {
        String m1 = null;
        boolean isPositiveInt = false;
        while (!isPositiveInt) {
            //输入
            Scanner in = new Scanner(System.in);
            m1 = in.nextLine();
            m1.trim();
            //正则验证(不能输入负数、0、0开头的数字、小数)
            Pattern pattern = Pattern.compile("^[+]?[1-9]+\\d*$");
            Matcher match = pattern.matcher(m1);
            isPositiveInt = true;
            if (!match.matches()) {
                isPositiveInt = false;
                System.out.println("输入错误，请重新输入：");
            }
        }
        //字符串转为整型
        int m = 0;
        try {
            m=Integer.parseInt(m1);
        }catch (NumberFormatException e){
            throw new NumberFormatException("数字不要超过"+Integer.MAX_VALUE+"哦！");
        }
        return m;
    }

    public static void main(String[] args) {

        System.out.println("欢迎使用本程序！");

        //支持多次测试
        String option="1";
        String m1=null,n1=null;
        Zhanchufa zhanchufa=new Zhanchufa();
        while (option.equals("1")){
            //输入
            System.out.println("请输入正整数m：");
            int m=Zhanchufa.scanAndParse();
            System.out.println("请输入正整数n：");
            int n=Zhanchufa.scanAndParse();

            //最大公约数
            zhanchufa.maxfactor=Zhanchufa.gongYueShu(m,n);
            System.out.println("最大公约数为："+zhanchufa.maxfactor);

            //最小公倍数
            zhanchufa.minTime=m*n/zhanchufa.maxfactor;
            System.out.println("最小公倍数为："+zhanchufa.minTime);

            //菜单
            System.out.println("退出程序请键入任意字符，继续测试请输入1：");
            Scanner in = new Scanner(System.in);
            option = in.nextLine();
        }
    }
}
