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

package org.shangridocs.services.wikipedia.response;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** .... 
 * 
 * Sub class of SubSection Response :
 *  
 * @param title: title of the Wikipedia Pages
 * @param pageid : Unique identifier of each WIKI Page
 * @param sections : List of Subsections in the page
 * 
 * */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResponse {

	private String title;
	private String pageid;
	private List<SectionInfo> sections;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPageid() {
		return pageid;
	}
	public void setPageid(String pageid) {
		this.pageid = pageid;
	}
	public List<SectionInfo> getSections() {
		return sections;
	}
	public void setSections(List<SectionInfo> sections) {
		this.sections = sections;
	}
	
}
