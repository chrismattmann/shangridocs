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

package org.shangridocs.services.stringdb;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Path("stringdb")
public class StringDBResource {

    private static final Logger LOG = Logger.getLogger(StringDBResource.class
            .getName());

    private static final String BASE_URL_KEY = "org.shangridocs.stringdb.baseUrl";
    private static final String RESOLVE_API_PATH = "/api/json/resolve";
    private static final String DETAILS_PATH = "/newstring_cgi/show_network_section.pl?identifier=";

    private String baseUrl;

    public StringDBResource(@Context ServletContext sc) {
        this.baseUrl = sc.getInitParameter(BASE_URL_KEY);
    }

    @PUT
    @Path("query")
    @Produces("application/json")
    @Consumes("text/plain")
    public Response query(InputStream is) throws IOException {
        String query = IOUtils.toString(is, "UTF-8");
        WebClient client = WebClient.create(baseUrl + RESOLVE_API_PATH)
                .query("identifier", query).accept(MediaType.APPLICATION_JSON);
        LOG.info("Issuing stringdb query for " + query);
        Response r = client.get();
        String responseJson = r.readEntity(String.class);
        LOG.info("response was " + responseJson);

        // Example response is  [{"annotation":"apoptosis antagonizing transcription factor",
        //"preferredName":"AATF","taxonName":"Takifugu rubripes","ncbiTaxonId":31033,
        //"stringId":"31033.ENSTRUP00000000315","queryIndex":-1}

        JSONArray resultList = (JSONArray) JSONValue.parse(responseJson);

        StringBuilder jsonStrBuf = new StringBuilder("[");
        for (int i = 0; i < resultList.size(); i++) {
            JSONObject result = (JSONObject) resultList.get(i);

            String title = (String) result.get("preferredName");
            String identifier = (String) result.get("stringId");
            String link = baseUrl + DETAILS_PATH + identifier;
            String description = (String) result.get("annotation");
            String taxonName = (String) result.get("taxonName");
            Long ncbiTaxonId = (Long) result.get("ncbiTaxonId");

            jsonStrBuf.append("{");
            jsonStrBuf.append("\"link\":\"");
            jsonStrBuf.append(link);
            jsonStrBuf.append("\"");

            jsonStrBuf.append(",");
            jsonStrBuf.append("\"title\":\"");
            jsonStrBuf.append(title);
            jsonStrBuf.append("\"");

            jsonStrBuf.append(",");
            jsonStrBuf.append("\"description\":\"");
            jsonStrBuf.append(description);
            jsonStrBuf.append("\"");

            jsonStrBuf.append(",");
            jsonStrBuf.append("\"taxonName\":\"");
            jsonStrBuf.append(taxonName);
            jsonStrBuf.append("\"");

            jsonStrBuf.append(",");
            jsonStrBuf.append("\"ncbiTaxonId\":");
            jsonStrBuf.append(ncbiTaxonId);
            jsonStrBuf.append("");

            jsonStrBuf.append("}");

            if (i < (resultList.size() - 1)) {
                jsonStrBuf.append(",");
            }
        }

        jsonStrBuf.append("]");

        return Response.ok(jsonStrBuf.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/status")
    @Produces("text/html")
    public Response status() {
        return Response
                .ok("<h1>This is StringDB Resource: running correctly</h1><h2>StrindDB Proxy: /query</h2><p>"
                        + (this.baseUrl != null ? this.baseUrl : "Not Configured") + "</p>")
                .build();
    }
}
