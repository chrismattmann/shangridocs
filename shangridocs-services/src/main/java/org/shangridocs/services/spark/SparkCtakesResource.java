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

package org.shangridocs.services.spark;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

@Path("/spark")
public class SparkCtakesResource {

  public static final Logger LOG = Logger.getLogger(SparkCtakesResource.class
      .getName());

  public static final long DEFAULT_TIMEOUT = 1000000L;

  private URL sparkDataURL;

  private URL sparkJobsURL;

  /* 
   * Uploads a new file to spark-jobserver, the full path of the file on the server 
   * is returned, the prefix is the prefix of the actual filename used on the 
   * server (a timestamp is added to ensure uniqueness) 
   * This has to be a POST request e.g. POST /data/<prefix>  
   */
  private static final String SPARK_JOBSERVER_DATA = "org.shangridocs.spark.data.url";

  /* 
   * Starts a new job on spark-jobserver, use ?sync=true to wait for results 
   * This has to be a POST request e.g POST /jobs 
   */
  private static final String SPARK_JOBSERVER_JOBS = "org.shangridocs.spark.jobs.url";

  public SparkCtakesResource(@Context ServletContext sc)
      throws MalformedURLException {
    sparkDataURL = new URL(sc.getInitParameter(SPARK_JOBSERVER_DATA));
    sparkJobsURL = new URL(sc.getInitParameter(SPARK_JOBSERVER_JOBS));
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Spark cTAKES Resource: running correctly.</h1>"
            + "<h2>Spark REST JobServer Data Upload: /data</h2><p>"
            + sparkDataURL.toString()
            + "</p><h2>Spark REST JobServer Jobs Execution: /jobs</h2><p>"
            + sparkJobsURL.toString() + "</p>").build();
  }

  @POST
  @Path("/data")
  public Response forwardSparkData(InputStream is,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(is, sparkDataURL.toString(), contentDisposition);
  }

  @POST
  @Path("/jobs")
  public Response forwardSparkJobs(InputStream is,
      @HeaderParam("Content-Disposition") String contentDisposition) {
    return forwardProxy(is, sparkJobsURL.toString(), contentDisposition);
  }

  private Response forwardProxy(InputStream is, String url,
      String contentDisposition) {
    if (url.equals(sparkDataURL.toString())) {
      LOG.info("POSTING document [" + contentDisposition + "] to Spark JobServer at :[" + url + "]");
    } else if (url.equals(sparkJobsURL.toString())) {
      LOG.info("POSTING job initiation to Spark JobServer at :[" + url + "]");
    } else {
      LOG.severe("Error POSTING anything... JobServer URL at :[" + url + "] is invalid");
      throw new RuntimeException();
    }
    
    WebClient client = WebClient.create(url).accept("application/json")
        .header("Content-Disposition", contentDisposition);
    HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
    conduit.getClient().setConnectionTimeout(DEFAULT_TIMEOUT);
    conduit.getClient().setReceiveTimeout(DEFAULT_TIMEOUT);
    Response response = client.post(is);
    String json = response.readEntity(String.class);
    LOG.info("Response received: " + json);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

}
