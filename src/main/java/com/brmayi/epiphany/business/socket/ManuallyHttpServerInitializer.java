package com.brmayi.epiphany.business.socket;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;  

@Service
public class ManuallyHttpServerInitializer extends ChannelInitializer<SocketChannel> { 
	@Resource
	private ManuallyHttpServerHandler manuallyHttpServerHandler;
    @Override  
    public void initChannel(SocketChannel ch) throws Exception {  
        // Create a default pipeline implementation.  
        ChannelPipeline pipeline = ch.pipeline();  
  
        /** 
         * http-request解码器 
         * http服务器端对request解码 
         */  
        pipeline.addLast("decoder", new HttpRequestDecoder());  
        /** 
         * http-response解码器 
         * http服务器端对response编码 
         */  
        pipeline.addLast("encoder", new HttpResponseEncoder());  
  
        /** 
         * 压缩 
         * Compresses an HttpMessage and an HttpContent in gzip or deflate encoding 
         * while respecting the "Accept-Encoding" header. 
         * If there is no matching encoding, no compression is done. 
         */  
        pipeline.addLast("deflater", new HttpContentCompressor());  
  
        pipeline.addLast("handler", manuallyHttpServerHandler);  
    }  
}  
