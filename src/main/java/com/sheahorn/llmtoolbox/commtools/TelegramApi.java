package com.sheahorn.llmtoolbox.commtools;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@ApplicationScoped
public class TelegramApi {

    private static final Logger LOG = Logger.getLogger(TelegramApi.class.getName());
    public static final int MAX_MESSAGE_LENGTH = 4096;

    @ConfigProperty(name = "llmtoolbox.communication.telegram.token", defaultValue = "none")
    String token;

    @ConfigProperty(name = "llmtoolbox.communication.telegram.chatid", defaultValue = "0")
    String chatId;

    public void send(String message) throws IOException, InterruptedException {
        String url = "https://api.telegram.org/bot" + token + "/sendMessage"
            + "?chat_id=" + chatId
            + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Telegram API returned status " + response.statusCode() + ": " + response.body());
        }
    }

    private String sanitize(String s) {
        if (token != null && !token.isEmpty() && !"none".equals(token) && !"0".equals(token)) {
            s = s.replace(token, "[TOKEN_ID]");
        }
        if (chatId != null && !chatId.isEmpty() && !"0".equals(chatId)) {
            s = s.replace(chatId, "[CHAT_ID]");
        }
        return s;
    }

    public void logError(Exception e) {
        LOG.severe(e.getClass().getName() + ": " + sanitize(e.getMessage()));
    }
}
