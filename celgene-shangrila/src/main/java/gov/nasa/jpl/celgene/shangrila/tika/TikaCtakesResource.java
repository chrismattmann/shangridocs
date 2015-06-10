package gov.nasa.jpl.celgene.shangrila.tika;

import java.io.InputStream;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

@Path("/tika")
public class TikaCtakesResource {
  
  public static final String PROXY_URL_TIKA = "http://localhost:9201";
  
  public static final String PROXY_URL_CTAKES = "http://localhost:9202";
  
  @PUT
  @Path("/rmeta")
  public Response forwardTika(InputStream is, @HeaderParam("Content-Dispotion") String contentDispotion){
      Response response = 
          WebClient.create(PROXY_URL_TIKA)
          .accept("application/json")
          .header("Content-Disposition", contentDispotion)
          .put(is);
      return response;    
  }

}
