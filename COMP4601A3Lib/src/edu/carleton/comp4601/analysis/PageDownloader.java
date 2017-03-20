package edu.carleton.comp4601.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.carleton.comp4601.dao.Review;
import edu.carleton.comp4601.dao.Item;
import edu.carleton.comp4601.dao.User;
import edu.carleton.comp4601.database.Database;

public class PageDownloader {
	private static final String DATA_PREFIX = "http://sikaman.dyndns.org:8888/courses/4601/assignments/";
	private static final String USERS_DIR = "users";
	private static final String PAGES_DIR = "pages";
	
	private String context;
	
	public PageDownloader(String context) {
		this.context = context;
		Database.getInstance(context);
	}
	
	public void setContext(String context) {
		Database.getInstance(context);
	}
	
	public void downloadPages() {
		String prefixWithContext = DATA_PREFIX + context + "/";
		String pagesUrl = prefixWithContext + PAGES_DIR;
		String usersUrl = prefixWithContext + USERS_DIR + "/";
		
		try {
			Document jsoupDoc = Jsoup.connect(pagesUrl).get();
			
			Elements links = jsoupDoc.select("a");
			
			Set<Review> reviews = new HashSet<Review>();
			Map<String, User> users = new HashMap<>();
			Map<String, Item> pages = new HashMap<>();
			
			int total = links.size();
			int soFar = 0;
			int freq = 5;
			
			System.out.println("Downloading pages");
			
			for (Element link : links) {
				if (link.text().equals("Parent Directory") || link.attr("href").startsWith("?"))
					continue;
				String href = link.attr("abs:href");
				
				Document pageDoc = Jsoup.connect(href).get();
				
				Elements bodyEles = pageDoc.body().children();
				
				Iterator<Element> it = bodyEles.iterator();
				
				Item item = new Item(pageDoc.title());
				
				pages.put(item.getName(), item);

				while (it.hasNext()) {
					Element userLink = it.next();
					assert userLink.tag().getName().equalsIgnoreCase("a");
					Element temp = it.next(); // Skip the br
					assert temp.tag().getName().equalsIgnoreCase("br");
					StringBuilder reviewTextBuilder = new StringBuilder();
					Element paragraph = null;
					// This code shouldn't work, but it absorbs the last "br" (which has no text) and terminates, which is good enough for me.
					do {
						paragraph = it.next();

						reviewTextBuilder.append(paragraph.text()).append("\n\n");
					} while(it.hasNext() && paragraph.tag().getName().equals("p"));
					String userName = userLink.text();
					
					Review review = new Review(userName + ":" + item.getName());
					User user = null;
					if (users.containsKey(userName)) {
						user = users.get(userName);
					}
					else {
						user = new User(userName);
						users.put(userName, user);
					}
					
					review.setAuthor(user.getUserName());
					review.setText(reviewTextBuilder.toString());
					review.setItemId(item.getName());
					user.addReview(review);
					item.addReview(review);
					reviews.add(review);
				}
				
				soFar += 1;
				
				if (soFar % freq == 0) {
					System.out.printf("%.2f%% pages downloaded so far\n", ((double)soFar/total)*100);
				}
			}
			
			System.out.println("Done downloading pages");
			
			soFar = 0;
			total = users.size();
			System.out.println("Downloading users");
			for (User u : users.values()) {
				String url = usersUrl + u.getUserName() + ".html";
				Document userPage = Jsoup.connect(url).get();
				
				Elements eles = userPage.select("a");
				
				for (Element pageLink : eles) {
					u.addVisited(pages.get(pageLink.text()));
				}
				
				soFar += 1;
				if (soFar % freq == 0) {
					System.out.printf("%.2f%% users downloaded so far\n", ((double)soFar/total)*100);
				}
			}
			
			System.out.println("Done downloading all users.");
			
			System.out.println("Saving reviews to database.");
			for (Review r : reviews) {
				r.save();
			}
			System.out.println("Done saving reviews.");
			
			System.out.println("Saving users to database.");
			for (User u : users.values()) {
				u.save();
			}
			System.out.println("Done saving users.");
			
			System.out.println("Saving items to database.");
			for (Item i : pages.values()) {
				i.save();
			}
			System.out.println("Done saving items");
			
			System.out.println("stats: users=" + users.size() + ", pages=" + pages.size() + ", reviews=" + reviews.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		new PageDownloader("training").downloadPages();
	}
}
