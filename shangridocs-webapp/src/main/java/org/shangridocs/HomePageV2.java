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

import org.shangridocs.webcomponents.search.SearchServicesPanel_v2;

/**
 * HomePage V2 : is the new version of shangridocs skin created as of date : 11th March 2016
 *
 */
public class HomePageV2 extends BaseHomePage {

	public HomePageV2() {

		add(new SearchServicesPanel_v2("search_panel_v2", false, false));

	}

}
