package netty_file.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpFileServerHanlder extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final String url;
	
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	
	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	
	public HttpFileServerHanlder(String url){
		this.url = url;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		System.out.println("请求出现.....");
		//请求不存在
		if(!request.getDecoderResult().isSuccess()){
			sendError(ctx,HttpResponseStatus.BAD_REQUEST);
			return;
		}
		//请求不是get
		if(request.getMethod() != HttpMethod.GET){
			sendError(ctx,HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		final String uri = request.getUri();
		final String path = sanitizeUri(uri);
		if(path == null){
			sendError(ctx,HttpResponseStatus.FORBIDDEN);
			return;
		}
		
		File file = new File(path);
		if(file.isHidden() || !file.exists()){
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
		}
		//判断是不是文件夹
		if(file.isDirectory()){
			if(uri.endsWith("/"))
            {
                sendListing(ctx, file);
            }else{
                sendRedirect(ctx, uri + "/");
            }
            return;
		}
		//判断是否是文件
		if(!file.isFile()){
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
		}
		
		//处理文件
		RandomAccessFile raAccessFile = null;
		try {
			raAccessFile = new RandomAccessFile(file, "r");
		} catch (Exception e) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
		}
		
		long length = raAccessFile.length();
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		
		HttpHeaders.setContentLength(response, length);
		setContentTypeHeader(response, file);
		//判断连接是不是keep alive
		if(HttpHeaders.isKeepAlive(request)){
			response.headers().set(HttpHeaders.Names.LOCATION,HttpHeaders.Values.KEEP_ALIVE);
		}
		ctx.write(response);
		
		ChannelFuture sendFileFuture = null;
		sendFileFuture = ctx.write(new ChunkedFile(raAccessFile,0,length,8192), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			
			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				System.out.println("Transfer complete.");
			}
			
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
				if(total < 0){
					System.err.println("Transfer progress: " + progress);
				} 
                else{
                	System.err.println("Transfer progress: " + progress + "/" + total);
                }
			}
		});
		//如果是chunked
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
	    if (!HttpHeaders.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
		
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if(ctx.channel().isActive())
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
	
	private String sanitizeUri(String uri){
		try {
			uri = URLDecoder.decode(uri,"UTF-8");
		} catch (Exception e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (Exception e2) {
				throw new Error();
			}
		}
		if(!uri.startsWith(url))
            return null;
        if(!uri.startsWith("/"))
            return null;
        uri = uri.replace('/', File.separatorChar);
        if(uri.contains(File.separator + '.') 
        		|| uri.contains('.' + File.separator)
        		|| uri.startsWith(".") 
        		|| uri.endsWith(".") 
                || INSECURE_URI.matcher(uri).matches()){
            return null;
        }
        return System.getProperty("user.dir") + File.separator + uri;
	}
	
	private static void sendListing(ChannelHandlerContext ctx, File dir){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        response.headers().set("CONNECT_TYPE", "text/html;charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html;charset=UTF-8");
        
        String dirPath = dir.getPath();
        StringBuilder buf = new StringBuilder();
        
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append("目录:");
        buf.append("</title></head><body>\r\n");
        
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\" ../\")..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if(f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        
        buf.append("</ul></body></html>\r\n");
        
        ByteBuf buffer = Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);  
        response.content().writeBytes(buffer);  
        buffer.release();  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); 
    }
	
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
//        response.headers().set("LOCATIN", newUri);
        response.headers().set(HttpHeaders.Names.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
	
	
	private void sendError(ChannelHandlerContext ctx,HttpResponseStatus status){
		//组装http response
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/html;charset=UTF-8");
		//发送response
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	
   private static void setContentTypeHeader(HttpResponse response, File file) {

        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
            mimeTypesMap.getContentType(file.getPath()));
    }

	
}
