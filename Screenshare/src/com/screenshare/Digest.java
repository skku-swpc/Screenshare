package com.screenshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class Digest {	
	String username = null;
	String passwd = null;
	String serverAddress="example.net";	
	String url;
	String uri;
	boolean loggedin=false;
	int rCode=99;
	
	DefaultHttpClient client;
	
	public Digest(){
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
		HttpConnectionParams.setSoTimeout(httpParameters, 30000);
		client = new DefaultHttpClient(httpParameters);		
	}
	
    public InputStream downloadImg() throws ClientProtocolException, IOException, IllegalStateException, URISyntaxException, Exception{
    	HttpGet get = new HttpGet(url);
    	HttpResponse response = client.execute(get);
    	if(response.getStatusLine().getStatusCode()!=200){
    		
    		rCode=response.getStatusLine().getStatusCode();
    		throw new Exception();
    	}else{
    		if(loggedin){
    			return response.getEntity().getContent();
    		}else{    		
    			tmp(response);
    			return downloadImg();
    		}
    	}
    }
    
    public void executPost() throws ClientProtocolException, IOException, IllegalStateException, URISyntaxException{
    	HttpPost post = new HttpPost(url);
    	HttpResponse response = client.execute(post);
    	if(loggedin)    		
    		response.getEntity().getContent().close();
    	else{
    		tmp(response);
    		executPost();
    	}
    }
    
    public void executPost(String data) throws ClientProtocolException, IOException, IllegalStateException, URISyntaxException{
    	StringEntity postData = new StringEntity(data);
    	HttpPost post = new HttpPost(url);
    	post.setEntity(postData);
    	HttpResponse response = client.execute(post);
    	if(loggedin){    	
    		response.getEntity().getContent().close();
    	}else{
    		tmp(response);
    		executPost(data);
    	}
    		
    }    
    
    private boolean tmp(HttpResponse response) throws IllegalStateException, IOException{
    	final Pattern HTTP_REFRES_PATTERN = Pattern.compile(
		".*<meta\\s+http-equiv\\s*=\"REFRESH\"\\s+content\\s*=\"[0-9]+;url=http://(.+?)/\"");		
    	byte data[] = new byte[1000];
    	InputStream is = response.getEntity().getContent();
    	int bytesRead = is.read(data);
    	String metaString = new String(data, 0, bytesRead);
    	Matcher m = HTTP_REFRES_PATTERN.matcher(metaString);
    	if (m.find()) {	
    	    setAddress(m.group(1).toString(), false);
    	    setUri(uri);
    	    return true;
    	}else{
    		loggedin=true;
    		is.close();
    	    return false;
    	}        	
    	
    }
    
    public void setUri(String uri){
  		url="http://"+serverAddress+uri;    	
  		this.uri=uri;    
	}
        
    public void changedAddress(){
    	loggedin=false;
    }
	
	public void setAddress(String phoneAddress, boolean proxy){
		if(proxy){
			this.serverAddress="example.net"+"/"+phoneAddress;		
    	}else{
    		this.serverAddress=phoneAddress;
    	}
	}
	
	public void setUserPass(String username, String passwd){
		this.username = username;		
		this.passwd = passwd;
		client.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "Screenshare"),
				new UsernamePasswordCredentials(username, passwd));
	}
	
	public int getRCode(){
		return rCode;
	}	
}
