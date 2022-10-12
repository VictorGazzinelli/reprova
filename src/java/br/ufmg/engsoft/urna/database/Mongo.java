package br.ufmg.engsoft.urna.database;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mongodb instance.
 */
public class Mongo {
  /**
   * Singleton instance.
   */
  private static Mongo instance;
  
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(Mongo.class);
  
  /**
   * The mongodb driver instance.
   */
  protected final MongoDatabase db;
  
  /**
   * Instantiate for access in the application database.
   */
  private Mongo() {
    /**
     * Full connection string, obtained from 'urna_MONGO' environment variable.
     */
    String endpoint = System.getenv("urna_MONGO");
    this.db = MongoClients
    .create(endpoint)
    .getDatabase("urna");
    
    logger.info("connected to db 'urna'");
  }
    
  /**
   * Returns the application's mongo instance.
   */
  public static Mongo getInstance() {
    if(instance == null) 
      instance = new Mongo();
    return instance;
  }

  /**
   * Gets the given collection in the database.
   */
  public MongoCollection<Document> getCollection(String name) {
    return db.getCollection(name);
  }
}
