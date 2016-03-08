package org.shangridocs.services.wikipedia.Response;

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
