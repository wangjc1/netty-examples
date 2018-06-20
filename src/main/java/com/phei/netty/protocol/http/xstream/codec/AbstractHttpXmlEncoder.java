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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.Charset;


/**
 * @author Administrator
 * @version 1.0
 * @date 2014年3月1日
 */
public abstract class AbstractHttpXmlEncoder<T> extends MessageToMessageEncoder<T> {
    final static String CHARSET_NAME = "UTF-8";
    final static Charset UTF_8 = Charset.forName(CHARSET_NAME);

    protected ByteBuf encode0(ChannelHandlerContext ctx, Object body) throws Exception {
        // 将Order类转换为xml流
        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        // 注册使用了注解的VO
        xStream.processAnnotations(new Class[] { Order.class, Customer.class, Shipping.class, Address.class });
        String xml = xStream.toXML(body);
        ByteBuf encodeBuf = Unpooled.copiedBuffer(xml, UTF_8);
        return encodeBuf;
    }

    @Skip
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("fail to encode");
    }

}
