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

import javax.servlet.ServletContext;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.shangridocs.webcomponents.ctakes.CTakesPanel;
import org.shangridocs.webcomponents.search.SearchServicesPanel;
import org.shangridocs.webcomponents.tika.TikaTabbedPanel;

public class HomePage extends WebPage {

	public static final String SHANGRIDOCS_SKIN = "org.shangridocs.skin";

	public HomePage() {
		
//		v2 is the revamped version of shangridocs and it has different panels in it in comparison to the older versions.
		if("v2".equalsIgnoreCase(getServletContext().getInitParameter(SHANGRIDOCS_SKIN))){
			add(new TikaTabbedPanel("tika_panel", false));
		}else{
			add(new TikaTabbedPanel("tika_panel", false));
			add(new CTakesPanel("ctakes_panel", false, false));
			add(new SearchServicesPanel("search_panel", false, false));
		}	
	}

	private ServletContext getServletContext() {

		ServletContext servletContext = WebApplication.get().getServletContext();
		return servletContext;
	}

}
