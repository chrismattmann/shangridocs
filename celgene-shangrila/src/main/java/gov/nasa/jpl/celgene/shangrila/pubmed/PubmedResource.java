package gov.nasa.jpl.celgene.shangrila.pubmed;

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

  private static final String PUBMED_BASE_URL = "gov.nasa.jpl.celgene.shangrila.pubmed.baseUrl";

  private static final String PUBMED_ID_BASE_URL = "gov.nasa.jpl.celgene.shangrila.pubmed.ids.baseUrl";

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
    LOG.info("searching "+pubMedSearchUrlStr+" for terms "+searchText);
    WebClient client = WebClient.create(pubMedSearchUrlStr)
        .query("db", "pubmed")
        .query("retmode", "json")
        .query("term", searchText)
        .accept(
        "application/json");
    Response r = client.get();
    String responseJson = r.readEntity(String.class);
    LOG.info("Call to "+pubMedSearchUrlStr+" returned: "+responseJson);
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
