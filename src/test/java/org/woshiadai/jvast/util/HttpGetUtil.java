package org.woshiadai.jvast.util;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpGetUtil {
  private static final Logger LOGGER = LogManager.getLogger(HttpGetUtil.class);
  private static final HttpClient client = HttpClient.newBuilder()
      .followRedirects(Redirect.ALWAYS)
      .connectTimeout(Duration.ofSeconds(10))
      .build();

  /**
   * Send an HTTP GET request and get response.
   * @param url String
   * @return String
   */
  public static String get(String url) {
    try {
      HttpRequest req = HttpRequest.newBuilder()
          .uri(new URI(url))
          .GET()
          .timeout(Duration.ofSeconds(10))
          .build();

      HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());

      final int statusCode = resp.statusCode();
      if (statusCode == HTTP_OK) {
        return resp.body();
      } else {
        LOGGER.error("Non-200 response for GET request for url: {}", url);
        LOGGER.error("status code: {}", statusCode);
        LOGGER.error("response body: {}", resp.body());
        return StringUtil.EMPTY;
      }
    } catch (URISyntaxException e) {
      LOGGER.error("Invalid url: {}", url);
      return StringUtil.EMPTY;
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error getting HTTP response: {}", e);
      return StringUtil.EMPTY;
    }
  }

  public static void main(String[] args) {
    final String respBody = get("https://postman-echo.com");
    System.out.println(respBody);
  }
}
