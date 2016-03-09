package org.shangridocs;

import org.apache.wicket.markup.html.WebPage;
import org.shangridocs.webcomponents.tika.TikaTabbedPanel;

public abstract class BaseHomePage extends WebPage {

	public BaseHomePage() {

		add(new TikaTabbedPanel("tika_panel", false));
	}
}
