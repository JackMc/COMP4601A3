package edu.carleton.comp4601.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.carleton.comp4601.dao.Item;
import edu.carleton.comp4601.dao.Review;
import edu.carleton.comp4601.dao.User;
import edu.carleton.comp4601.database.Database;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class PageLoader {
	public PageLoader(String context) {
		Database.getInstance(context);
	}
	
	public void load() {
		long start = System.currentTimeMillis();
		Item.all();
		User.all();
		
		Properties props = new Properties();
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		props.setProperty("annotators", "sentiment");
		
		int total = Review.getReviews().size();
		AtomicInteger soFar = new AtomicInteger(0);
		
		ExecutorService executors = Executors.newFixedThreadPool(8);
		
		List<Callable<Void>> todo = new ArrayList<>();

		Runnable task = new Runnable() {
			
			@Override
			public void run() {
				System.out.printf("%.2f%% done\n", ((double)soFar.get()/total)*100);				
			}
		};
		
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		
		ses.scheduleAtFixedRate(task, 5000, 5000, TimeUnit.MILLISECONDS);
		
		
		for (Review r : Review.getReviews()) {
			todo.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Annotation a = new Annotation(r.getText());
					pipeline.annotate(a);
					// Increment the number of processed reviews
					soFar.incrementAndGet();
					return null;
				}
			});
		}
		
		try {
			executors.invokeAll(todo);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ses.shutdown();
		
		System.out.println("Took " + (System.currentTimeMillis() - start));
	}
	
	public static void main(String[] args) {
		new PageLoader("training").load();
	}
}
