package edu.carleton.comp4601.database;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.carleton.comp4601.dao.Item;

public class Database {
	private MongoClient client;

	private static Database database;
	
	private String context;
	
	private Database(String context) {
		this.context = context;
	}
	
	public String getContext() {
		return context;
	}
	
	public MongoDatabase getDB() {
		if (client == null) {
			client = new MongoClient("localhost", 27017);
		}

		return client.getDatabase("ad_engine_" + context);
	}

	public MongoCollection<Document> getCollection(String name) {
		return getDB().getCollection(name);
	}
	
	public static Database getInstance(String context) {
		if (database == null || !context.equalsIgnoreCase(database.getContext())) {
			database = new Database(context);
		}
		
		return database;
	}
	
	public static Database getInstance() {
		if (database == null) {
			throw new IllegalArgumentException();
		}
		
		return database;
	}
}
