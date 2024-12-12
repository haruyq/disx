package xyz.ar06.disx.utils;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.util.UriEncoder;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Deprecated
public class DisxYoutubeAudioURLScraper {
    private static String apiURL = "http://disxytsourceapi.ar06.xyz/get_url/";
    public static String scrapeURL(String videoId){
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiURL + videoId))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                JSONObject jsonObject = new JSONObject(response.body());
                String url = jsonObject.getString("url");
                return url;
            } else {
                DisxLogger.error("Disx Error: YT-SRC API response failed. Status Code: " + response.statusCode());
                return "ERROR";
            }

        } catch (Exception e){
            e.printStackTrace();
            return "ERROR";
        }
    }
}
