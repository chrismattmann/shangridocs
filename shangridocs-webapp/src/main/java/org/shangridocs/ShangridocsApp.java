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

package org.shangridocs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.File;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.shangridocs.webcomponents.search.SearchServicesPanel;

public class ShangridocsApp extends WebApplication {

  private static final Logger LOG = Logger.getLogger(ShangridocsApp.class
      .getName());

  public static final String SHANGRIDOCS_HOMEPAGE = "shangridocs.homepage";

  public static final String SHANGRIDOCS_SKIN = "org.shangridocs.skin";
  
  private boolean loggedSkinMessage = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();

    doImageMounts(getImageFiles(ShangridocsApp.class.getPackage().getName()),
        (Class<?>) HomePage.class);

    mountSharedResource("/config/search-config.json", new ResourceReference(
        SearchServicesPanel.class, "search-config.json").getSharedResourceKey());
  }

  @Override
  public Class<? extends Page> getHomePage() {
    try {
      return (Class<? extends Page>) Class.forName(getHomePageClass());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return HomePage.class;
    }
  }

  public String getHomePageClass() {
    return getServletContext().getInitParameter(SHANGRIDOCS_HOMEPAGE);
  }

  public String getSkin() {
    return getServletContext().getInitParameter(SHANGRIDOCS_SKIN);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket
   * .Request, org.apache.wicket.Response)
   */
  @Override
  public Session newSession(Request request, Response response) {
    ShangridocsSession session = new ShangridocsSession(request);
    String skin = getSkin();
    if (skin != null && !skin.equals("")) {
      if (!loggedSkinMessage){
        LOG.log(Level.INFO, "Setting skin to: [" + skin + "]");
        loggedSkinMessage = true;
      }
      session.setStyle(skin);
    }
    return session;
  }

  private void doImageMounts(Set<String> resources, Class<?> clazz) {
    if (resources != null) {
      for (String resource : resources) {
        String resName = new File(resource).getName();
        String resPath = "/images/" + resName;
        LOG.log(Level.INFO, "Mounting: [" + resPath + "] origName: [" + resName
            + "]: resource: [" + resource + "]");
        mountSharedResource(resPath,
            new ResourceReference(clazz, resName).getSharedResourceKey());
      }
    }
  }

  private Set<String> getImageFiles(String packageName) {
    Pattern pattern = Pattern.compile(".*\\.(png|gif|jpg|jpeg|jp2)");
    Set<String> resources = new Reflections(packageName, new ResourcesScanner())
        .getResources(pattern);
    Set<String> filteredResources = new HashSet<String>();
    Map<String, Boolean> resMap = new HashMap<String, Boolean>();
    for (String res : resources) {
      String resName = new File(res).getName();
      if (!resMap.containsKey(resName)) {
        resMap.put(resName, true);
        filteredResources.add(resName);
      }
    }

    return filteredResources;
  }
}