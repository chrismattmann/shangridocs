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

package org.shangridocs.services.wikipedia;

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
import org.json.simple.JSONValue;

@Path("/wikipedia")
public class WikipediaResource {

  private static final String WIKIPEDIA_BASE_URL = "org.shangridocs.wikipedia.baseUrl";

  private String wikipediaBaseUrlStr;

  private static final Logger LOG = Logger.getLogger(WikipediaResource.class
      .getName());

  public WikipediaResource(@Context ServletContext sc) {
    wikipediaBaseUrlStr = sc.getInitParameter(WIKIPEDIA_BASE_URL);
  }

  @PUT
  @Path("/search")
  @Produces("application/json")
  @Consumes("text/plain")
  public Response query(InputStream is) throws IOException {
    String query = IOUtils.toString(is, "UTF-8");
    WebClient client = WebClient.create(wikipediaBaseUrlStr)
        .query("action", "opensearch").query("search", query)
        .query("format", "json").accept(MediaType.APPLICATION_JSON);
    LOG.info("Issuing wikipedia query for " + query);
    Response r = client.get();
    String responseJson = r.readEntity(String.class);
    LOG.info("response was " + responseJson);
    // response is
    // [ "searchterm", ["article title1", "article title2"], ["description1",
    // "description2"], ["link1", "link2"]]
    Object jsonObj = JSONValue.parse(responseJson);
    JSONArray objList = (JSONArray) jsonObj;
    JSONArray wikiTitles = (JSONArray) objList.get(1);
    JSONArray wikiDescs = (JSONArray) objList.get(2);
    JSONArray wikiLinks = (JSONArray) objList.get(3);

    StringBuilder jsonStrBuf = new StringBuilder("{");
    for (int i = 0; i < wikiTitles.size(); i++) {
      String title = (String) wikiTitles.get(i);
      String link = (String) wikiLinks.get(i);
      String desc = (String) wikiDescs.get(i);
      jsonStrBuf.append("\"");
      jsonStrBuf.append(title);
      jsonStrBuf.append("\" : { \"link\" : \"");
      jsonStrBuf.append(link);
      jsonStrBuf.append("\", \"desc\" : \"");
      jsonStrBuf.append(desc);
      if (i < wikiTitles.size() - 1) {
        jsonStrBuf.append("\"},");
      } else {
        jsonStrBuf.append("\"}");
      }

    }
    jsonStrBuf.append("}");
    return Response.ok(jsonStrBuf.toString(), MediaType.APPLICATION_JSON).build();

  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Tika Wikipedia Proxy: running correctly</h1><h2>Wikipedia Search Proxy: /search</h2><p>"
            + wikipediaBaseUrlStr + "</p>").build();
  }
}
