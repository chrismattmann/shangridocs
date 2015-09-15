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

package gov.nasa.jpl.celgene.shangrila.services.tika;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.transport.http.HTTPConduit;

@Path("/tika")
public class TikaCtakesResource {

  public static final Logger LOG = Logger.getLogger(TikaCtakesResource.class
      .getName());

  public static final long DEFAULT_TIMEOUT = 1000000L;

  private URL tikaCtakesURL;

  private URL tikaURL;

  private static final String TIKA_URL_PROPERTY = "gov.nasa.jpl.celgene.shangrila.tika.url";

  private static final String CTAKES_URL_PROPERTY = "gov.nasa.jpl.celgene.shangrila.tika.ctakes.url";

  public TikaCtakesResource(@Context ServletContext sc)
      throws MalformedURLException {
    tikaURL = new URL(sc.getInitParameter(TIKA_URL_PROPERTY));
    tikaCtakesURL = new URL(sc.getInitParameter(CTAKES_URL_PROPERTY));
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Tika cTAKES Resource: running correctly</h1><h2>Tika Proxy: /rmeta</h2><p>"
            + tikaURL.toString()
            + "</p><h2>cTAKES Proxy: /ctakes</h2><p>"
            + tikaCtakesURL.toString()
            + "</p> <h2>Tika Form Proxy: /rmeta/form</h3><p>"
            + tikaURL.toString() + "</p>").build();
  }

  @PUT
  @Consumes("multipart/form-data")
  @Produces({ "text/csv", "application/json" })
  @Path("/rmeta/form")
  public Response forwardTikaMultiPart(Attachment att,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(att.getObject(InputStream.class), tikaURL.toString(),
        contentDisposition);
  }

  @PUT
  @Path("/rmeta")
  @Produces("application/json")
  public Response forwardTika(InputStream is,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(is, tikaURL.toString(), contentDisposition);
  }

  @PUT
  @Path("/ctakes")
  public Response forwardCtakes(InputStream is,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(is, tikaCtakesURL.toString(), contentDisposition);
  }

  private Response forwardProxy(InputStream is, String url,
      String contentDisposition) {
    LOG.info("PUTTING document [" + contentDisposition + "] to Tika at :["
        + url + "]");
    WebClient client = WebClient.create(url).accept("application/json")
        .header("Content-Disposition", contentDisposition);
    HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
    conduit.getClient().setConnectionTimeout(DEFAULT_TIMEOUT);
    conduit.getClient().setReceiveTimeout(DEFAULT_TIMEOUT);
    Response response = client.put(is);
    String json = response.readEntity(String.class);
    LOG.info("Response received: " + json);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

}
