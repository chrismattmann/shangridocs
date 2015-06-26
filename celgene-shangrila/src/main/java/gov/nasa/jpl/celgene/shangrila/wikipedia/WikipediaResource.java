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

package gov.nasa.jpl.celgene.shangrila.wikipedia;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path("/wikipedia")
public class WikipediaResource {
  
  private static final String WIKIPEDIA_BASE_URL = "gov.nasa.jpl.celgene.shangrila.wikipedia.baseUrl";

  private String wikipediaBaseUrlStr;
  
  private static final Logger LOG = Logger.getLogger(WikipediaResource.class
      .getName());

  public WikipediaResource(@Context ServletContext sc) {
    wikipediaBaseUrlStr = sc.getInitParameter(WIKIPEDIA_BASE_URL);
  }

  @GET
  @Path("/status")
  @Produces("text/html")
  public Response status() {
    return Response
        .ok("<h1>This is Tika Wikipedia Proxy: running correctly</h1><h2>Wikipedia Search Proxy: /search</h2><p>"
            + wikipediaBaseUrlStr
            + "</p>").build();
  }
}
