package org.shangridocs.services.wikipedia.Response;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
