package edu.carleton.comp4601.dao;

import static com.mongodb.client.model.Filters.eq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import edu.carleton.comp4601.database.Database;

public class Review {
	private String text;
	private String authorId;
	private String itemId;
	private String id;

	public String getItem() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthor(String author) {
		this.authorId = author;
	}
	
	public String getId() {
		return id;
	}

	public Review(String id) {
		this.id = id;
	}

	public int hashCode() {
		return getId().hashCode();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Review)) {
			return false;
		}

		Review r = (Review)o;
		return r.getId().equals(this.getId());
	}

	@Override
	public String toString() {
		return "Review [text=" + text + ", author=" + authorId + "]";
	}

	public void save() {
		Document doc = new Document();

		doc.put("_id", getId());
		doc.put("text", text);
		doc.put("author", authorId);
		doc.put("page", itemId);

		Database.getInstance().getCollection(COLLECTION).insertOne(doc);
	}
	
	protected void loadDoc(Document d) {
		this.text = d.getString("text");
		this.authorId = d.getString("author");
		this.itemId = d.getString("page");
	}

	private static final Map<String, Review> reviews = new HashMap<>();
	private static final String COLLECTION = "reviews";

	public static Review id(String reviewId) {
		if (!reviews.containsKey(reviewId)) {
			Document d = Database.getInstance().getCollection(COLLECTION).find(eq("_id", reviewId)).first();
			
			Review r = new Review(reviewId);

			reviews.put(reviewId, r);
			
			r.loadDoc(d);
			System.out.println("Loaded review " + reviewId);
		}
		
		return reviews.get(reviewId);
	}
	
	public static Collection<Review> getReviews() {
		return reviews.values();
	}
}
