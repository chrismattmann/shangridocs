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

package gov.nasa.jpl.celgene.shangrila.tika;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

@Path("/tika")
public class TikaCtakesResource {

  public static final Logger LOG = Logger.getLogger(TikaCtakesResource.class
      .getName());

  public static final String PROXY_URL_TIKA = "http://localhost:9201/rmeta";

  public static final String PROXY_URL_CTAKES = "http://localhost:9202/rmeta";

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response.ok(
        "<h1>This is Tika cTAKES Resource: running correctly</h1>").build();
  }

  @PUT
  @Path("/rmeta")
  @Produces("application/json")
  public Response forwardTika(InputStream is,
      @HeaderParam("Content-Dispotion") String contentDisposition) {
    return forwardProxy(is, PROXY_URL_TIKA, contentDisposition);
  }

  @PUT
  @Path("/ctakes")
  public Response forwardCtakes(InputStream is,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(is, PROXY_URL_CTAKES, contentDisposition);
  }

  private Response forwardProxy(InputStream is, String url,
      String contentDisposition) {
    LOG.info("PUTTING document [" + contentDisposition + "] to Tika at :["
        + url + "]");
    Response response = WebClient.create(url).accept("application/json")
        .header("Content-Disposition", contentDisposition).put(is);
    String json = response.readEntity(String.class);
    LOG.info("Response received: " + json);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

}
