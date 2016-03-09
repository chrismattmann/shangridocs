package org.shangridocs;

import org.shangridocs.webcomponents.search.SearchServicesPanel_v2;

public class HomePageV2 extends BaseHomePage {

	public HomePageV2() {

		add(new SearchServicesPanel_v2("search_panel_v2", false, false));

	}

}
