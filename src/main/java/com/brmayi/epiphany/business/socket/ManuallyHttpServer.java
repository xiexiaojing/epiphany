package com.brmayi.epiphany.business.socket;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.brmayi.epiphany.service.DataService;

public class ManuallyHttpServer implements Runnable {  

	private ManuallyHttpServerInitializer manuallyHttpServerInitializer = new ManuallyHttpServerInitializer();
	
	private ManuallyHttpServerHandler manuallyHttpServerHandler = new ManuallyHttpServerHandler();
	
	private DataService dataService;
	
    private int port = 0;  
  
    public void setServer(int port) {  
        this.port = port;  
    }  
  
    public void setDataService(DataService dataService) {
    	this.dataService = dataService;
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
    
    
    private class ManuallyHttpServerInitializer extends ChannelInitializer<SocketChannel> { 
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
    
    @Sharable
    private class ManuallyHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {  
        @Override  
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {  
       
        }  
      
        public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {  
            /** 
             * msg的类型 
             * {@link DefaultHttpRequest} 
             * {@link LastHttpContent} 
             */  
            if (msg instanceof HttpRequest) {  
                HttpRequest request = (HttpRequest) msg;  
                URI uri = new URI(request.getUri());  
                String data = uri.getQuery();
                data = data.replace("ids=", "");
    			String[] sids = data.split(",");
    			List<Long> ids = new ArrayList<Long>();
    			for(String sid : sids) {
    				ids.add(NumberUtils.toLong(sid));
    			}
    			dataService.dealDataByIds(ids, null);
    			String advice="重发了"+ids.toString(); 
                ByteBuf buf = copiedBuffer(advice, CharsetUtil.UTF_8);  
                // Build the response object.  
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);  
          
                response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");  
                response.headers().set(CONTENT_LENGTH, buf.readableBytes());  
          
                // Write the response.  
                ctx.channel().writeAndFlush(response); 
            }  
        }  
     
        @Override  
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {  
            ctx.channel().close();  
        }  
      
        @Override  
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {  
            messageReceived(ctx, msg);  
        }  
    }
}  