/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.shangridocs.services.omim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;




@Path("/omim")
public class OmimResource {

	public static String omimkey;

	public OmimResource(@Context ServletContext sc) {
		omimkey=sc.getInitParameter("org.shangridocs.omim.apikey");
	}

	@GET
	@Path("/search")
	@Produces("application/json")
	public Response createColumnArray(@QueryParam("query") String query) throws JSONException, IOException  {




		return Response.ok(omim(query), MediaType.APPLICATION_JSON).build();

	}

	

	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
	
	
	public static String createStandardJson( String Search_link, String Num_results,ArrayList< HashMap<String,String>> entries){
		
		String json="";
		json+="{";
		
		json+="\"Num_results\":\""+Num_results+"\",";
		json+="\"Search_link\":\""+Search_link+"\",";
		json+="\"Response\":[";
		for(HashMap<String, String> map:entries){
			json+="{";
			json+="\"Title\":\""+map.get("Title")+"\",";
			map.remove("Title");
			json+="\"ID\":\""+map.get("ID")+"\",";
			map.remove("ID");
			json+="\"Description\":\""+map.get("Description")+"\",";
			//System.out.println("DEBUG"+map.get("Description"));
			map.remove("Description");
			json+="\"Detail_link\":\""+map.get("Detail_link")+"\",";
			map.remove("Detail_link");	
			json+="\"Properties\":{";
			
			for(String key:map.keySet()){
				json+="\""+key+"\":\""+map.get(key)+"\",";
			}
			//remove the last comma
			if(String.valueOf(json.charAt(json.length()-1)).equals(","))
			json = json.substring(0, json.length()-1);
			
			json+="}";
			
			json+="},";
		}
		//remove the last comma
		if(String.valueOf(json.charAt(json.length()-1)).equals(","))
		json = json.substring(0, json.length()-1);
				
		json+="]";
		
		json+="}";
		
		
		
		
		return json.replace("\n", " ");
	}
	public static Document readXMLFromUrl(String url) throws IOException, JSONException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new URL(url).openStream());	   
	  }

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONArray json = new JSONArray(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
	
	public static String omim(String query) throws JSONException, IOException{
		
		
		
		String Search_link="";
		String Num_results="";
		ArrayList< HashMap<String,String>> entries=new ArrayList< HashMap<String,String>>();
		
		
		
		JSONObject json=readJsonFromUrl("http://api.omim.org/api/entry/search?search="+query+"&start=0&limit=5&apiKey="+omimkey+"&format=json");

		JSONObject temp1=(JSONObject)json.get("omim");
		JSONObject temp2=(JSONObject)temp1.get("searchResponse");
		Integer totalresults=(Integer)temp2.get("totalResults");
		JSONArray ar1=(JSONArray)temp2.get("entryList");
		
		for(int i=0;i<ar1.length();i++){
		
			HashMap<String,String> map=new HashMap<String, String>();
			JSONObject temp4=(JSONObject)ar1.get(i);
			JSONObject temp5=(JSONObject)temp4.get("entry");
			JSONObject temp6=(JSONObject)temp5.get("titles");
			String t2=(String)temp6.get("preferredTitle");
			String t1=(String)temp5.get("mimNumber");
			String t3="";
			
			JSONObject jsontemp=readJsonFromUrl("http://api.omim.org/api/entry?mimNumber="+t1+"&include=text:description&apiKey="+omimkey+"&format=json");
			
			JSONObject temp7=(JSONObject)jsontemp.get("omim");
			JSONArray ar2=(JSONArray)temp7.get("entryList");
			JSONObject temp9=(JSONObject)ar2.get(0);
			JSONObject temp11=(JSONObject)temp9.get("entry");
			try{
			JSONArray ar3=(JSONArray)temp11.get("textSectionList");
			JSONObject temp8=(JSONObject)ar3.get(0);
			JSONObject temp10=(JSONObject)temp8.get("textSection");
			t3=(String)temp10.get("textSectionContent");
			
			
			
			}
			catch(Exception e){
				
			}
			
			//System.out.println("DEBUG"+"Title:"+t2);
			//System.out.println("DEBUG"+"ID:"+t1);
			//System.out.println("DEBUG"+"Description:"+t3);
			//System.out.println("DEBUG"+"Detail_link:http://omim.org/entry/"+t1);
			//System.out.println("DEBUG"+"\n\n\n\n");
			map.put("Title",t2);
			map.put("ID", t1);
			map.put("Description",t3);
			map.put("Detail_link","http://omim.org/entry/"+t1);
			
			entries.add(map);
			
			
			
		}
		//System.out.println("DEBUG"+"Num_results:"+totalresults);
		//System.out.println("DEBUG"+"Search_link:http://www.ncbi.nlm.nih.gov/omim/?term="+query);
		
		Search_link="http://www.ncbi.nlm.nih.gov/omim/?term="+query;
		Num_results=String.valueOf(totalresults);
		
		
		
		return createStandardJson(Search_link, Num_results, entries);
	}
	
	
	
	@GET
	  @Path("/status")
	  @Produces("text/html")
	  public Response status() {
	    return Response
	        .ok("<h1>This is omim json service running correctly</h1>").build();
	  }
	

}