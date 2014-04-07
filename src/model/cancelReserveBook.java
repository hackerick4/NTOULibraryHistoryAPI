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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONException;
import org.json.JSONObject;


public class cancelReserveBook  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
    	String account = request.getParameter("account");
    	String pwd = request.getParameter("password");
    	String radioVal = request.getParameter("radioValue");
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
        /*detect individual location*/      
    	      String location = loginConnection.getHeaderField("Location"); 
    	      if (location == null){ out.println("Login failed"); return;}
    	      String[] myLocationTokens = location.split("/");
    	      String myLocation = myLocationTokens[ 2 ];    	      
    
       /**do the unReserve**/
    	      Document chk_doc = Jsoup.connect("http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" + myLocation +"/holds")
  	    	              .data("currentsortorder" , "current_pickup")
  	    	              .data("cancel" +radioVal, "on") //cancel +radioVal
  	    	              .data("currentsortorder" , "current_pickup")
  	    	              .data("requestUpdateHoldsSome" , "取消已選取館藏")
  	       	    		  .cookie("III_SESSION_ID", session)
  	       	    		  .cookie("III_EXPT_FILE" , "aa17054" )
  	       	    		  .cookie("SESSION_LANGUAGE","cht")
  	       	    		  .cookie("SESSION_SCOPE","0")
  	       	    		  .post();
  	 
  	    	Jsoup.connect("http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" + myLocation +"/holds")
              .data("currentsortorder" , "current_pickup")
              .data("loc" +radioVal, "")  //loc + radioVal + x00
              .data("cancel" +radioVal , "on") //cancel +radioVal + x00
              .data("currentsortorder" , "current_pickup")
              .data("updateholdssome" , "是")
	    		  .cookie("III_SESSION_ID", session)
	    		  .cookie("III_EXPT_FILE" , "aa17054" )
	    		  .cookie("SESSION_LANGUAGE","cht")
	    		  .cookie("SESSION_SCOPE","0")
	    		  .post();
    	
  	    	//out.println(chk_doc); 
  	    	Elements cancelReserveBook = chk_doc.select("html > body > span > form > table > tbody > tr > td > label > a");
  
  	    	if (cancelReserveBook.hasText()){
  	    		JSONObject jsonResponse = new JSONObject();
  	    		try {
					jsonResponse.put("querySuccess", "true");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  	    		out.println(jsonResponse);  	    		
  	    	}
  	    	else{
  	    		JSONObject jsonResponse = new JSONObject();
  	    		try {
					jsonResponse.put("querySuccess", "false");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  	    		out.println(jsonResponse);  	
  	    		
  	    	}
		
	}
	
}