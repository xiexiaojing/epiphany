package com.brmayi.epiphany.business.socket;

import javax.annotation.Resource;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.springframework.stereotype.Service;

@Service
public class ManuallyHttpServer implements Runnable {  
	
	@Resource
	private ManuallyHttpServerInitializer manuallyHttpServerInitializer;
    private int port = 0;  
  
    public void setServer(int port) {  
        this.port = port;  
    }  
  
    @Override
    public void run() {  
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);  
        EventLoopGroup workerGroup = new NioEventLoopGroup();  
        try {  
            ServerBootstrap b = new ServerBootstrap();  
            b.group(bossGroup, workerGroup)
            	.channel(NioServerSocketChannel.class)  
                .childHandler(manuallyHttpServerInitializer);  
  
            Channel ch = b.bind(port).sync().channel();  
            ch.closeFuture().sync();  
        } catch(Exception e) {
        	
    	}finally {  
            bossGroup.shutdownGracefully();  
            workerGroup.shutdownGracefully();  
        }  
    } 
}  

