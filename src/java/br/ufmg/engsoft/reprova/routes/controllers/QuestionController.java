package br.ufmg.engsoft.reprova.routes.controllers;

import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.services.QuestionService;
import br.ufmg.engsoft.reprova.mime.json.Json;


/**
 * Questions route.
 */
public class QuestionController {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

  /**
   * Access token.
   */
  protected static final String token = System.getenv("REPROVA_TOKEN");

  /**
   * Messages.
   */
  protected static final String unauthorized = "\"Unauthorized\"";
  protected static final String invalid = "\"Invalid request\"";
  protected static final String ok = "\"Ok\"";

  /**
   * Json formatter.
   */
  protected final Json json;

  public QuestionController() {
    json = new Json();
  }

  /**
   * Install the endpoint in Spark.
   * Methods:
   * - GET
   * - POST
   * - PUT
   * - DELETE
   */
  public void setup() {
    Spark.get("/api/questions", this::get);
    Spark.post("/api/questions", this::post);
    Spark.put("/api/questions", this::put);
    Spark.delete("/api/questions", this::delete);

    logger.info("Setup /api/questions.");
  }


  /**
   * Check if the given token is authorized.
   */
  protected static boolean authorized(String token) {
    return QuestionController.token.equals(token);
  }


  /**
   * Get endpoint: lists all questions, or a single question if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    logger.info("Received questions get:");

    var id = request.queryParams("id");
    var auth = authorized(request.queryParams("token"));

    return id == null
      ? this.get(request, response, auth)
      : this.get(request, response, id, auth);
  }

  /**
   * Get id endpoint: fetch the specified question from the database.
   * If not authorized, and the given question is private, returns an error message.
   */
  protected Object get(Request request, Response response, String id, boolean auth) {
    if (id == null)
      throw new IllegalArgumentException("id mustn't be null");

    response.type("application/json");

    logger.info("Fetching question " + id);

    var question = QuestionService.getByID(id);

    if (question == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    if (question.pvt && !auth) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(question);
  }

  /**
   * Get all endpoint: fetch all questions from the database.
   * If not authorized, fetches only public questions.
   */
  protected Object get(Request request, Response response, boolean auth) {
    response.type("application/json");

    logger.info("Fetching questions.");

    var questions = QuestionService.getAll(auth);

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(questions);
  }


  /**
   * Post endpoint: add a question in the database.
   * The question must be supplied in the request's body.
   * The given question is added as a new question in the database.
   * This endpoint is for authorized access only.
   */
  protected Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received questions post:" + body);

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    boolean success = false;
    try {
      success = QuestionService.create(body);
      logger.info("Parsed question");
      logger.info("Adding question.");
    } catch(Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return success ? ok : invalid;
  }

  /**
   * Put endpoint: update a question in the database.
   * The question must be supplied in the request's body.
   * The given question is put as a the question with the given id.
   * This endpoint is for authorized access only.
   */
  protected Object put(Request request, Response response) {
    String body = request.body();

    var id = request.queryParams("id");
    var token = request.queryParams("token");

    logger.info("Received questions put:" + body);

    response.type("application/json");


    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    boolean success = false;
    try {
      success = QuestionService.update(id, body);
      logger.info("Parsed question");
      logger.info("Adding question.");
    } catch(Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return success ? ok : invalid;
  }


  /**
   * Delete endpoint: remove a question from the database.
   * The question's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    logger.info("Received questions delete:");

    response.type("application/json");

    var id = request.queryParams("id");
    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    if (id == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    logger.info("Deleting question " + id);

    boolean success  = QuestionService.deleteByID(id);
    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return success ? ok : invalid;
  }
}