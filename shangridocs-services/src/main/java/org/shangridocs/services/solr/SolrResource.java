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

package org.shangridocs.services.solr;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.oodt.cas.metadata.util.PathUtils;

@Path("/solr")
public class SolrResource {

  private static final String SOLR_SELECT_ENDPOINT = "select";

  private static final Logger LOG = Logger.getLogger(SolrResource.class
      .getName());

  private String baseUrl;

  private String username;

  private String password;

  private String resultUrlPrefix;

  public SolrResource(@Context ServletContext sc) {
    this.baseUrl = getContextParam(sc, "org.shangridocs.solr.url");
    if (this.baseUrl != null) {
      this.baseUrl = cleanseSlash(this.baseUrl);
    }
    this.username = getContextParam(sc, "org.shangridocs.solr.username");
    this.password = getContextParam(sc, "org.shangridocs.solr.password");
    this.resultUrlPrefix = getContextParam(sc, "org.shangridocs.solr.resultURL");
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Tika Solr Resource: running correctly</h1><h2>Solr Proxy: /query</h2><p>"
            + (this.baseUrl != null ? this.baseUrl : "Not Configured") + "</p>")
        .build();
  }

  @GET
  @Path("/config")
  @Produces("application/json")
  public Response config() {
    String json = "{\"resultUrlPrefix\" : \"" + this.resultUrlPrefix + "\"}";
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

  @GET
  @Path("/query")
  @Produces("application/json")
  public Response forwardSearch(@QueryParam("q") String query) {
    if (this.baseUrl == null)
      return Response.serverError().build();
    String queryUrl = this.baseUrl;
    queryUrl += SOLR_SELECT_ENDPOINT;
    return forwardProxy(query, queryUrl, this.username, this.password);
  }

  private Response forwardProxy(String query, String url, final String user,
      final String pass) {
    LOG.info("Issuing query [" + query + "] to Solr at :[" + url + "]");
    WebClient client = null;

    if (isCredentialed()) {
      client = WebClient.create(url, user, pass, null);
    } else {
      client = WebClient.create(url);
    }

    client = client.accept("application/json");
    Response response = client.query("wt", "json").query("q", query).get();
    String json = response.readEntity(String.class);
    LOG.info("Response received: " + json);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

  private String getContextParam(ServletContext sc, String name) {
    if (sc.getInitParameter(name) != null
        && !sc.getInitParameter(name).equals("")) {
      return PathUtils.replaceEnvVariables(sc.getInitParameter(name));
    } else
      return null;
  }

  private String cleanseSlash(String url) {
    String cleanseUrl = "";
    if (!url.endsWith("/")) {
      cleanseUrl = url + "/";
    } else {
      cleanseUrl = url;
    }
    return cleanseUrl;
  }

  private boolean isCredentialed() {
    return this.username != null && !this.username.equals("")
        && this.password != null && !this.password.equals("");
  }
}
