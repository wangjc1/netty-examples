package com.wjc.java.reference;

import org.junit.Assert;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * @author: wangjc
 * 2017/7/5
 */
public class WeakHashMapTest {

    @Test
    public void test1(){
        WeakHashMap w= new WeakHashMap();
        //三个key-value中的key 都是匿名对象，没有强引用指向该实际对象
        String yw = new String("语文");
        String yx = new String("优秀");
        w.put(yw,yx);
        w.put(new String("数学"), new String("及格"));
        w.put(new String("英语"), new String("中等"));
        //增加一个字符串的强引用
        w.put("java", new String("特别优秀"));
        System.out.println(w);

        //通知垃圾回收机制来进行回收
        yw = null;
        System.gc();
        System.runFinalization();
        //再次输出w
        System.out.println("第二次输出:"+w);
    }

    @Test
    public void test2(){
        String abc = "124";
        Assert.assertEquals(abc.intern(),abc);
    }

    @Test
    public void test3(){
        String a = new String("abc");
        WeakReference<String> ref = new WeakReference<String>(a);
        a = null;

        System.gc();
        System.runFinalization();

        Assert.assertNull(ref.get());
    }
}
