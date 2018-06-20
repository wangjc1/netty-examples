/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.protocol.http.xstream.codec;

import com.phei.netty.protocol.http.xstream.pojo.Address;
import com.phei.netty.protocol.http.xstream.pojo.Customer;
import com.phei.netty.protocol.http.xstream.pojo.Order;
import com.phei.netty.protocol.http.xstream.pojo.Shipping;
import com.thoughtworks.xstream.XStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月1日
 */

public abstract class AbstractHttpXmlDecoder<T> extends MessageToMessageDecoder<T> {

    private Class<?> clazz;
    // 是否输出码流的标志，默认为false
    private boolean isPrint;
    private final static String CHARSET_NAME = "UTF-8";
    private final static Charset UTF_8 = Charset.forName(CHARSET_NAME);

    // 当调用这个构造方法是，默认设置isPrint为false
    protected AbstractHttpXmlDecoder(Class<?> clazz) {
        this(clazz, false);
    }

    protected AbstractHttpXmlDecoder(Class<?> clazz, boolean isPrint) {
        this.clazz = clazz;
        this.isPrint = isPrint;
    }

    protected Object decode0(ChannelHandlerContext arg0, ByteBuf body) throws Exception {
        String content = body.toString(UTF_8);
        if (isPrint)
            System.out.println("The body is : " + content);
        XStream xs = new XStream();
        xs.setMode(XStream.NO_REFERENCES);
        // 注册使用了注解的VO
        xs.processAnnotations(new Class[] { Order.class, Customer.class, Shipping.class, Address.class });
        Object result = xs.fromXML(content);
        return result;
    }

    @Skip
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }
}