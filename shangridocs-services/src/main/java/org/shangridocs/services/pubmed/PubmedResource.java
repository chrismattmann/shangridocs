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

package org.shangridocs.services.pubmed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Path("/pubmed")
public class PubmedResource {

  private static final String PUBMED_BASE_URL = "org.shangridocs.pubmed.baseUrl";

  private static final String PUBMED_ID_BASE_URL = "org.shangridocs.pubmed.ids.baseUrl";

  private static final String PUBMED_BASE = "http://www.ncbi.nlm.nih.gov/pubmed/";

  private String pubMedBaseUrlStr;

  private String pubMedIdBaseUrlStr;

  private static final Logger LOG = Logger.getLogger(PubmedResource.class
      .getName());

  public PubmedResource(@Context ServletContext sc) {
    pubMedBaseUrlStr = sc.getInitParameter(PUBMED_BASE_URL);
    pubMedIdBaseUrlStr = sc.getInitParameter(PUBMED_ID_BASE_URL);
  }

  @PUT
  @Path("/text")
  @Consumes("text/plain")
  @Produces("text/plain")
  public Response getPubIds(InputStream is) throws IOException {
    String searchText = IOUtils.toString(is, "UTF-8");
    String pubMedSearchUrlStr = pubMedBaseUrlStr;
    LOG.info("searching " + pubMedSearchUrlStr + " for terms " + searchText);
    WebClient client = WebClient.create(pubMedSearchUrlStr)
        .query("db", "pubmed").query("retmode", "json")
        .query("term", searchText).accept("application/json");
    Response r = client.get();
    String responseJson = r.readEntity(String.class);
    LOG.info("Call to " + pubMedSearchUrlStr + " returned: " + responseJson);
    Object jsonObj = JSONValue.parse(responseJson);
    JSONObject jObj = (JSONObject) jsonObj;
    JSONObject eSearchObj = (JSONObject) jObj.get("esearchresult");
    JSONArray idArr = (JSONArray) eSearchObj.get("idlist");
    StringBuilder responseBldr = new StringBuilder();
    for (int i = 0; i < idArr.size(); i++) {
      responseBldr.append(idArr.get(i));
      if (i + 1 < idArr.size()) {
        responseBldr.append(",");
      }
    }

    return Response.ok(responseBldr.toString(), MediaType.TEXT_PLAIN).build();

  }

  @PUT
  @Path("/ids")
  @Consumes("text/plain")
  @Produces("application/json")
  public Response getPubMedURLs(InputStream is) throws IOException {
    String ids = IOUtils.toString(is, "UTF-8");
    String pubMedSearchUrlStr = pubMedIdBaseUrlStr;
    LOG.info("searching " + pubMedSearchUrlStr + " for ids " + ids);
    WebClient client = WebClient.create(pubMedSearchUrlStr)
        .query("db", "pubmed").query("retmode", "json")
        .query("rettype", "abstract").query("id", ids)
        .accept("application/json");
    Response r = client.get();
    String responseJson = r.readEntity(String.class);
    LOG.info("Call to " + pubMedSearchUrlStr + " returned: " + responseJson);
    Object jsonObj = JSONValue.parse(responseJson);
    JSONObject jObj = (JSONObject) jsonObj;
    JSONObject resultObj = (JSONObject) jObj.get("result");
    JSONArray idArr = (JSONArray) resultObj.get("uids");
    StringBuilder responseBldr = new StringBuilder();
    responseBldr.append("[");
    for (int i = 0; i < idArr.size(); i++) {
      String id = (String) idArr.get(i);
      JSONObject idObj = (JSONObject) resultObj.get(id);
      String title = (String) idObj.get("title");
      responseBldr.append("{\"id\" : \"");
      responseBldr.append(id);
      responseBldr.append("\",");
      responseBldr.append("\"url\" : \"");
      responseBldr.append(PUBMED_BASE);
      responseBldr.append(idArr.get(i));
      responseBldr.append("\", \"title\" : \"");
      responseBldr.append(title);
      responseBldr.append("\"");
      responseBldr.append("}");
      if (i + 1 < idArr.size()) {
        responseBldr.append(",");
      }
    }

    responseBldr.append("]");

    return Response.ok(responseBldr.toString(), MediaType.APPLICATION_JSON)
        .build();

  }

  @PUT
  @Path("/query")
  @Consumes("text/plain")
  @Produces("application/json")
  public Response query(InputStream is) throws IOException {
    Response r = getPubIds(is);
    String resp = r.readEntity(String.class);
    ByteArrayInputStream idStream = new ByteArrayInputStream(resp.getBytes());
    return getPubMedURLs(idStream);
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Tika PubMed Proxy: running correctly</h1><h2>PubMed Search Proxy: /text</h2><p>"
            + pubMedBaseUrlStr
            + "</p><h2>PubMed ID URL Proxy: /ids</h2><p>"
            + pubMedIdBaseUrlStr + "</p>").build();
  }

}
