package com.phei.netty.codec.messagepack;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class EchoClientHandler extends ChannelHandlerAdapter {

    // sendNumber为写入发送缓冲区的对象数量
    private int sendNumber;

    public EchoClientHandler(int sendNumber) {
        this.sendNumber = sendNumber;
    }

    /**
     * 构建长度为userNum的User对象数组
     * @param userNum
     * @return
     */
    private UserInfo[] getUserArray(int userNum) {
        UserInfo[] users = new UserInfo[userNum];
        UserInfo user = null;
        for(int i = 0; i < userNum; i++) {
            user = new UserInfo();
            user.setName("ABCDEFG --->" + i);
            user.setAge(i);
            users[i] = user;
        }
        return users;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        UserInfo[] users = getUserArray(sendNumber);
        for (UserInfo user : users) {
            ctx.writeAndFlush(user);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client receive the msgpack message : " + msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}