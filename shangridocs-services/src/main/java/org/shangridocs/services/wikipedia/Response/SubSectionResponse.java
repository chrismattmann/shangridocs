package org.shangridocs.services.wikipedia.Response;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubSectionResponse {

	ParsedResponse parse;

	public ParsedResponse getParse() {
		return parse;
	}

	public void setParse(ParsedResponse parse) {
		this.parse = parse;
	}
	
	
}
