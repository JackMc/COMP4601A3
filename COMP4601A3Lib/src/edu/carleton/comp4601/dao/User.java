package edu.carleton.comp4601.dao;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import edu.carleton.comp4601.database.Database;

public class User {
	private String userName;
	
	private Set<Item> visitedPages;
	
	private Set<Review> writtenReviews;
	
	public User(String name) {
		this.userName = name;
		this.writtenReviews = new HashSet<>();
		this.visitedPages = new HashSet<>();
	}
	
	public void addReview(Review review) {
		writtenReviews.add(review);
	}
	
	public void addVisited(Item page) {
		visitedPages.add(page);
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + " numReviews = " + writtenReviews.size() + "]";
	}
	
	public void save() {
		Document doc = new Document();
		
		doc.put("_id", userName);
		
		List<String> reviewIds = new ArrayList<>();
		
		for (Review review : writtenReviews) {
			reviewIds.add(review.getId());
		}
		
		doc.put("reviews", reviewIds);
		
		List<String> pages = new ArrayList<>();
		
		for (Item page : visitedPages) {
			pages.add(page.getName());
		}
		
		doc.put("visited", pages);
		
		Database.getInstance().getCollection(COLLECTION).insertOne(doc);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadDoc(Document d) {
		List<String> reviewIds = (List<String>) d.get("reviews");
		
		for (String id : reviewIds) {
			writtenReviews.add(Review.id(id));
		}
		
		List<String> pageIds = (List<String>) d.get("visited");
		
		for (String id : pageIds) {
			visitedPages.add(Item.id(id));
		}
	}

	private static final Map<String, User> users = new HashMap<>();
	private static final String COLLECTION = "users";
	
	public static User id(String id) {
		if (!users.containsKey(id)) {
			Document d = Database.getInstance().getCollection(COLLECTION).find(eq("_id", id)).first();
			
			User u = new User(id);
			users.put(id, u);
			
			u.loadDoc(d);
		}
		return users.get(id);
	}
	
	public static void all() {
		for (Document d : Database.getInstance().getCollection(COLLECTION).find()) {
			User u = new User(d.getString("_id"));
			
			users.put(d.getString("id"), u);
			u.loadDoc(d);
		}
	}
	
	public static Collection<User> getUsers() {
		return users.values();
	}
}
