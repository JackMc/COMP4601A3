package edu.carleton.comp4601.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import static com.mongodb.client.model.Filters.*;

import edu.carleton.comp4601.database.Database;

public class Item {
	private String name;

	private Set<Review> reviews;

	public String getName() {
		return name;
	}

	public Item(String name) {
		this.name = name;
		this.reviews = new HashSet<Review>();
	}
	
	@SuppressWarnings("unchecked")
	protected void loadDoc(Document d) {
		this.reviews = new HashSet<>();
		List<String> reviewIds = (List<String>)d.get("reviews");
		
		for (String reviewId : reviewIds) {
			this.reviews.add(Review.id(reviewId));
		}
	}
	
	public void addReview(Review review) {
		reviews.add(review);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Item other = (Item) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public void save() {
		Document doc = new Document();
		
		doc.put("_id", name);
		
		List<String> reviewIds = new ArrayList<>();
		
		for (Review r : reviews) {
			reviewIds.add(r.getId());
		}
		
		doc.put("reviews", reviewIds);
		
		Database.getInstance().getCollection(COLLECTION).insertOne(doc);
	}
	
	private static Map<String, Item> items = new HashMap<>();
	private static final String COLLECTION = "items";
	
	public static Item id(String id) {
		if (!items.containsKey(id)) {
			// Get it from the database
			Document d = Database.getInstance().getCollection(COLLECTION).find(eq("_id", id)).first();
			Item i = new Item(id);
			items.put(id, i);
			i.loadDoc(d);
		}

		return items.get(id);
	}

	public static void all() {
		for (Document d : Database.getInstance().getCollection(COLLECTION).find()) {
			Item i = new Item(d.getString("_id"));
			
			items.put(d.getString("id"), i);
			i.loadDoc(d);
		}
	}
}
