package model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 
// Extend HttpServlet class
public class delRHBook  extends HttpServlet {
	 
  
	private static final long serialVersionUID = 1L;
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	response.setContentType("text/html;charset=utf-8");
    	String account = request.getParameter("account");
    	String pwd = request.getParameter("password");
    	int rsh = Integer.parseInt( request.getParameter("chkBox"));
    	PrintWriter out = response.getWriter();
    	
    	/*****prepare the parameters*****/
    	String urLoginlParameters = "code=" + account + "&pin=" + pwd + "&submit.x=0&submit.y=0&submit=submit";
    	String requestLoginURL = "http://ocean.ntou.edu.tw:1083/patroninfo*cht";
    	URL loginURL = new URL(requestLoginURL);
    	HttpURLConnection loginConnection = (HttpURLConnection) loginURL.openConnection();
    	loginConnection.setDoOutput(true);
    	loginConnection.setDoInput(true);
    	loginConnection.setInstanceFollowRedirects(false); 
    	loginConnection.setRequestMethod("POST"); 
    	loginConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    	loginConnection.setRequestProperty("charset", "iso-8859-1");
    	loginConnection.setRequestProperty("Content-Length", "" + Integer.toString(urLoginlParameters.getBytes().length));
    	loginConnection.setUseCaches (false);
    	
    	
    	/** send login request**/
        DataOutputStream wr = new DataOutputStream ( loginConnection.getOutputStream ());
    		wr.writeBytes (urLoginlParameters);
    		wr.flush ();
    		wr.close ();
    	
    		
    	/**detect session**/
  	      	  String cookie = loginConnection.getHeaderField("Set-Cookie"); 
    	      String[] cookies = cookie.split(";");
    	      String[] sessionString = cookies[ 0 ].split("=");
    	      String session = sessionString[ 1 ];
    	      //out.println(session);
       
    	/*detect individual location*/      
    	      String location = loginConnection.getHeaderField("Location"); 
    	      if (location == null){ out.println("Login failed"); return;}
    	      String[] myLocationTokens = location.split("/");
    	      String myLocation = myLocationTokens[ 2 ];    	    

    	/**start to fetch reading history**/
    	    String requestURL = "http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" +  myLocation + "/readinghistory/rsh&rsh" + rsh +"=1";
    	     Connection.Response cr =Jsoup.connect(requestURL)
    	    		  .cookie("III_SESSION_ID", session)
    	    		  .cookie("III_EXPT_FILE" , "aa17054" )
    	    		  .cookie("SESSION_LANGUAGE","cht")
    	    		  .cookie("SESSION_SCOPE","0")
    	    		  .timeout(15*1000)
    	    		  .execute();
    	     
    	     JSONObject jsonResponse = new JSONObject();
    	     try {
				jsonResponse.put("querySuccess", "true");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	     
    }
    
}