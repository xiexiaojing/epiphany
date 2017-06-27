package com.brmayi.epiphany.business.socket;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import com.brmayi.epiphany.service.DataService;

@Service
@Sharable
public class ManuallyHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {  
	
	@Resource
   	private DataService dataService;
    
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
			dataService.dealData(ids);
			String advice="重发了"+ids.size()+"个ID的数据"; 
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