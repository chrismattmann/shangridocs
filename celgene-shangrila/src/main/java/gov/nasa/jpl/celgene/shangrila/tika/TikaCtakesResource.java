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
      @HeaderParam("Content-Dispotion") String contentDispotion) {
    LOG.info("PUTTING document [" + contentDispotion + "] to Tika at :["
        + PROXY_URL_TIKA + "]");
    Response response = WebClient.create(PROXY_URL_TIKA)
        .accept("application/json")
        .header("Content-Disposition", contentDispotion).put(is);
    String json = response.readEntity(String.class);
    LOG.info("Response received: " + json);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

}
