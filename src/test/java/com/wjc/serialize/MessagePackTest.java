package com.wjc.serialize;

import com.phei.netty.codec.messagepack.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangjc
 * 2018/4/27
 */
public class MessagePackTest {
    MessagePack messagePack = new MessagePack();
    byte[] buf;

    @Test
    @Before
    public void serialize() throws IOException {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(1);
        userInfo.setName("alex");
        userInfo.setAge(28);
        //序列化
        buf = messagePack.write(userInfo);
    }

    @Test
    public void deserialize() throws IOException {
        System.out.println(buf.length);
        //反序列化
        UserInfo user = messagePack.read(buf, UserInfo.class);
        System.out.println(user);
    }

    @Test
    public void serialize2() throws IOException {
        // Create serialize objects
        List<String> src=new ArrayList<String>();
        src.add("msgpack");
        src.add("kumofs");
        src.add("viver");
        MessagePack msgpack=new MessagePack();
        // Serialize
        byte[] raw = msgpack.write(src);

        // Deserialize directly using a template
        List<String> dst1 = msgpack.read(raw, Templates.tList(Templates.TString));
        System.out.println(dst1.get(0));
        System.out.println(dst1.get(1));
        System.out.println(dst1.get(2));;
    }

}

