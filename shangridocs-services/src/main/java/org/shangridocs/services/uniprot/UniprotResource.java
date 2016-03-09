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

package org.shangridocs.services.uniprot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.oodt.commons.xml.XMLUtils;

@Path("/uniprot")
public class UniprotResource {

  @PUT
  @Path("/search")
  @Produces("application/json")
  public Response createBody(InputStream is)
      throws IOException, SAXException,
      ParserConfigurationException {
    String query = IOUtils.toString(is, "UTF-8");
    return Response.ok(uniprot(query), MediaType.APPLICATION_JSON).build();

  }

  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is,
          Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = (JSONObject)JSONValue.parse(jsonText);
      return json;
    } finally {
      is.close();
    }
  }

  public static String createStandardJson(String Search_link,
      String Num_results, ArrayList<HashMap<String, String>> entries) {

    String json = "";
    json += "{";

    json += "\"Num_results\":\"" + Num_results + "\",";
    json += "\"Search_link\":\"" + Search_link + "\",";
    json += "\"Response\":[";
    for (HashMap<String, String> map : entries) {
      json += "{";
      json += "\"Title\":\"" + map.get("Title") + "\",";
      map.remove("Title");
      json += "\"ID\":\"" + map.get("ID") + "\",";
      map.remove("ID");
      json += "\"Description\":\"" + map.get("Description") + "\",";
      map.remove("Description");
      json += "\"Detail_link\":\"" + map.get("Detail_link") + "\",";
      map.remove("Detail_link");
      json += "\"Properties\":{";

      for (String key : map.keySet()) {
        json += "\"" + key + "\":\"" + map.get(key) + "\",";
      }
      // remove the last comma
      if (String.valueOf(json.charAt(json.length() - 1)).equals(","))
        json = json.substring(0, json.length() - 1);

      json += "}";

      json += "},";
    }
    // remove the last comma
    if (String.valueOf(json.charAt(json.length() - 1)).equals(","))
      json = json.substring(0, json.length() - 1);

    json += "]";

    json += "}";

    return json.replace("\n", " ");
  }

  public static Document readXMLFromUrl(String url) throws IOException,
      SAXException, ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(new URL(url).openStream());
  }

 public static JSONArray readJsonArrayFromUrl(String url) throws IOException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is,
          Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONArray json = (JSONArray)JSONValue.parse(jsonText);
      return json;
    } finally {
      is.close();
    }
  }

  public static String uniprot(String query) throws IOException,
      SAXException, ParserConfigurationException {
    JSONArray queryResults = readJsonArrayFromUrl("http://www.uniprot.org/uniprot/?query="
        + query + "&columns=id&start=0&limit=5&format=json");

    String Search_link = "";
    String Num_results = "" + String.valueOf(queryResults != null ? queryResults.size():"0");;
    ArrayList<HashMap<String, String>> entries = new ArrayList<HashMap<String, String>>();

    for (int i = 0; i < queryResults.size(); i++) {
      HashMap<String, String> map = new HashMap<String, String>();
      JSONObject result = (JSONObject) queryResults.get(i);
      String id = (String) result.get("id");
      map.put("ID", id);

      Document doc = readXMLFromUrl("http://www.uniprot.org/uniprot/" + id 
          + ".xml");
      Element docElem = doc.getDocumentElement();

      map.put("Detail_link", "http://www.uniprot.org/uniprot/" + id);
      NodeList organismList = doc.getElementsByTagName("organism");

      Element firstOrganism = (Element) organismList.item(0);
      NodeList nameElementList = firstOrganism.getElementsByTagName("name");

      for (int indx = 0; indx < nameElementList.getLength(); indx++) {
        Element nameElement = (Element) nameElementList.item(indx);

        if (nameElement.getAttribute("type").equals("scientific")) {
          map.put("Organism", nameElement.getTextContent());
        }
      }

      Element proteinElem = XMLUtils.getFirstElement("protein", docElem);
      Element recommendedNameElem = XMLUtils.getFirstElement("recommendedName", proteinElem);
      Element fullNameElem = XMLUtils.getFirstElement("fullName", recommendedNameElem);
      map.put("Title", XMLUtils.getSimpleElementText(fullNameElem));
      map.put("Gene", XMLUtils.getElementText("gene", docElem));

      NodeList commentList = doc.getElementsByTagName("comment");
      for (int indx = 0; indx < commentList.getLength(); indx++) {
        Element commentElement = (Element) commentList.item(indx);
        if (commentElement.getAttribute("type").equals("function")) {
          map.put("Description", commentElement.getTextContent());
        }
      }

      entries.add(map);

    }

    Search_link = "http://www.uniprot.org/uniprot/?query=" + query;
    return createStandardJson(Search_link, Num_results, entries);
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response.ok(
        "<h1>This is uniprot json service running correctly</h1>").build();
  }

}