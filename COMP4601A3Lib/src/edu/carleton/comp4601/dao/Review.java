package edu.carleton.comp4601.dao;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import edu.carleton.comp4601.database.Database;

public class Review {
	private String text;
	private User author;
	private Item item;

	public Review(Document d) {
		this.text = d.getString("text");
	}

	public Item getPage() {
		return item;
	}

	public void setPage(Item page) {
		this.item = page;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}
	
	public String getId() {
		return author.getUserName() + ":" + item.getName();
	}

	public Review() { }

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
		return "Review [text=" + text + ", author=" + author + "]";
	}

	public void save() {
		Document doc = new Document();

		doc.put("_id", getId());
		doc.put("text", text);
		doc.put("author", author.getUserName());
		doc.put("page", item.getName());

		Database.getInstance().getCollection(COLLECTION).insertOne(doc);
	}
	
	protected void loadDoc(Document d) {
		this.text = d.getString("text");
		this.author = User.id(d.getString("author"));
		this.item = Item.id(d.getString("page"));
	}

	private static final Map<String, Review> reviews = new HashMap<>();
	private static final String COLLECTION = "reviews";

	public static Review id(String reviewId) {
		if (!reviews.containsKey(reviewId)) {
			Document d = Database.getInstance().getCollection(COLLECTION).find(eq("_id", reviewId)).first();
			
			Review r = new Review();

			reviews.put(reviewId, r);
			
			r.loadDoc(d);
		}
		
		return reviews.get(reviewId);
	}
}
