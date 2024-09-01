package com.aviatorrob06.disx;

import com.grack.nanojson.JsonParser;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DisxModInfo {
    private static final String VERSION = "b0.1.0";
    private static String LATEST_VERSION = "N/A - NO INTERNET";
    private static final String DISCORD_URL = "http://discord.aviatorrob06.com";
    private static final String MODRINTH_URL = "https://modrinth.com/mod/disx";
    private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/disx";
    private static final String GITHUB_URL = "https://github.com/AviatorRob/disx";
    private static final String ROADMAP_URL = "https://trello.com/b/JwbWrPbE";
    private static final String BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/aviatorrob06";
    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/AviatorRob/disx/master/LATEST_VERSION.json";
    private static Boolean isUpToDate = true;
    private static int versionsOutdated = 0;

    public static void pullLatestVersion(){
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(LATEST_VERSION_URL))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the JSON response
                String responseBody = response.body();
                JSONObject json = new JSONObject(responseBody);
                JSONObject subJson = (JSONObject) json.get(Platform.getMinecraftVersion());
                JSONArray versionsJson = subJson.getJSONArray("versions");
                LATEST_VERSION = versionsJson.getString(0).toString();
                if (!LATEST_VERSION.equals(VERSION)){
                    isUpToDate = false;
                    int versionOutdateCounter = 1;
                    for (int i = 1; i < versionsJson.length(); i++){
                        String next = versionsJson.getString(i);
                        if (next.equals(VERSION)) {
                            break;
                        } else {
                            versionOutdateCounter++;
                            DisxMain.LOGGER.info("+1 VERSION OUTDATED");
                        }
                    }
                    versionsOutdated = versionOutdateCounter;
                    DisxMain.LOGGER.info(Integer.toString(versionsOutdated));
                }
            } else {
                System.out.println("Version Fetch Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            if (e instanceof ConnectException){
                DisxMain.LOGGER.info(e.getCause() + ": Failed to get latest Disx version. Is there an internet connection readily available?");
            } else {
                e.printStackTrace();
            }

        }
    }

    public static String getBuyMeACoffeeUrl() {
        return BUY_ME_A_COFFEE_URL;
    }

    public static String getLatestVersion() {
        return LATEST_VERSION;
    }

    public static String getCurseforgeUrl() {
        return CURSEFORGE_URL;
    }

    public static String getDiscordUrl() {
        return DISCORD_URL;
    }

    public static String getVERSION() {
        return VERSION;
    }

    public static String getGithubUrl() {
        return GITHUB_URL;
    }

    public static String getModrinthUrl() {
        return MODRINTH_URL;
    }

    public static String getRoadmapUrl() {
        return ROADMAP_URL;
    }

    public static Boolean getIsUpToDate() {
        return isUpToDate;
    }

    public static int getVersionsOutdated() {
        return versionsOutdated;
    }
}
