package org.shangridocs.services.wikipedia.Response;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikipediaSearchResponse {
	
	Map<String,WikiPages> wikiPages;

	public Map<String, WikiPages> getWikiPages() {
		return wikiPages;
	}

	public void setWikiPages(Map<String, WikiPages> wikiPages) {
		this.wikiPages = wikiPages;
	}
	
	
}