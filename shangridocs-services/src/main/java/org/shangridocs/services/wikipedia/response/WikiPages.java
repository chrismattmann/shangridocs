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

/**
 *
 * Wiki Page gives the link:URL of the page,
 * desc : Heading of the page, 
 * section info: Sub section of that page
 *
 */
public class WikiPages {

	private String link;
	private String desc;
	private ParsedResponse sectionInfo;
	
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public ParsedResponse getSectionInfo() {
		return sectionInfo;
	}
	public void setSectionInfo(ParsedResponse sectionInfo) {
		this.sectionInfo = sectionInfo;
	}
}
