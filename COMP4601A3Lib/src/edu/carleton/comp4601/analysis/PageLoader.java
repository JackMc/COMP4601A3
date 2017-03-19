package edu.carleton.comp4601.analysis;

import edu.carleton.comp4601.database.Database;

public class PageLoader {
	public PageLoader(String context) {
		Database.getInstance(context);
	}
	
	public void load() {
		
	}
}
