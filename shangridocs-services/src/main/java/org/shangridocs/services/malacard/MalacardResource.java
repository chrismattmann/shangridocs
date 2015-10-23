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

package org.shangridocs.services.malacard;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.protocol.HttpContext;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

@Path("/malacard")
public class MalacardResource {

    private final static String BASE_URL_KEY = "org.shangridocs.malacard.baseUrl";
    private final static String SEARCH_PATH = "/search/results/";
    
    private String baseUrl;

    private enum MalacardData {
        Index(0), Family(2), MCID(3), Name(4), MIFTS(5), Score(6);

        private int value;

        private MalacardData(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    };

    public MalacardResource(@Context ServletContext sc) {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
                java.util.logging.Level.OFF);
        this.baseUrl = sc.getInitParameter(BASE_URL_KEY);

    }

    @PUT
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("text/plain")
    public Response query(InputStream is) throws IOException {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.setWebConnection(new MyWebConnection(webClient));
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setRedirectEnabled(true);

            String searchText = IOUtils.toString(is, "UTF-8");
            WebRequest request = new WebRequest(new URL(baseUrl + SEARCH_PATH + searchText));
            final HtmlPage page = webClient.getPage(request);
            StringBuilder response = new StringBuilder();
            HtmlTable searchResultsTable = (HtmlTable) page.getElementsByTagName("table").get(0);
            if (searchResultsTable != null) {
                response.append("[");
                HtmlTableBody body = searchResultsTable.getBodies().get(0);
                List<HtmlTableRow> rows = body.getRows();
                // Get rid of the header row as there is no data.
                rows.remove(0);
                if (rows != null && rows.size() > 0) {
                    for (HtmlTableRow row : rows) {
                        List<HtmlTableCell> cells = row.getCells();
                        // Make sure cells are not empty and it is not one of the hidden result
                        // cells (i.e. < 2 cells)
                        if (cells != null && cells.size() > 2) {
                            response.append("{");
                            response.append("\"index\":");
                            response.append(cells.get(MalacardData.Index.getValue()).getTextContent());
                            response.append(",");
                            response.append("\"family\":\"");
                            response.append(cells.get(MalacardData.Family.getValue()).getTextContent());
                            response.append("\",");
                            response.append("\"identifier\":\"");
                            response.append(cells.get(MalacardData.MCID.getValue()).getTextContent());
                            response.append("\",");
                            response.append("\"title\":\"");
                            response.append(cells.get(MalacardData.Name.getValue()).getTextContent());
                            response.append( "\",");
                            response.append("\"link\":\"");
                            response.append(baseUrl);
                            response.append(cells.get(MalacardData.Name.getValue()).getFirstElementChild()
                                    .getAttribute("href"));
                            response.append( "\",");
                            response.append("\"mifts\":");
                            response.append(cells.get(MalacardData.MIFTS.getValue()).getTextContent());
                            response.append(",");
                            response.append("\"score\":");
                            response.append(cells.get(MalacardData.Score.getValue()).getTextContent());
                            response.append("},");
                        }
                    }

                    response.deleteCharAt(response.length() - 1);
                }

                response.append("]");
            }

            return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
        }

    }

    class MyWebConnection extends HttpWebConnection {
        public MyWebConnection(WebClient webClient) {
            super(webClient);
        }

        @Override
        protected HttpClientBuilder getHttpClientBuilder() {
            HttpClientBuilder builder = super.getHttpClientBuilder();

            CookieStore cookieStore = new BasicCookieStore();
            CookieSpecProvider csf = new CookieSpecProvider() {
                @Override
                public CookieSpec create(HttpContext context) {
                    return new BrowserCompatSpec() {
                        @Override
                        public void validate(Cookie cookie, CookieOrigin origin)
                                throws MalformedCookieException {
                            // accept all cookies
                        }

                    };

                }
            };

            RequestConfig requestConfig = RequestConfig.custom()
                    .setCookieSpec("easy").build();

            builder.setDefaultRequestConfig(requestConfig);
            builder.setDefaultCookieStore(cookieStore);
            builder.setDefaultCookieSpecRegistry(RegistryBuilder
                    .<CookieSpecProvider> create().register(CookieSpecs.BEST_MATCH, csf)
                    .register(CookieSpecs.BROWSER_COMPATIBILITY, csf)
                    .register("easy", csf).build());

            return builder;
        }
    }

}