package ir.hanzodev1375.ghostide.ai.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.hanzodev1375.ghostide.ai.model.AttachedFile;
import ir.hanzodev1375.ghostide.ai.model.ChatMessage;
import ir.hanzodev1375.ghostide.ai.utils.AiConstants;

public class ClaudeClient implements AiClient {

    private static final String TAG = "ClaudeClient";

    private final String          apiKey;
    private final String          model;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    public ClaudeClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model  = model;
    }

    @Override
    public void sendMessage(
            List<ChatMessage>  history,
            String             userMessage,
            List<AttachedFile> attachments,
            Callback           callback) {

        executor.execute(() -> {
            try {
                JSONArray messages = new JSONArray();

                // ── History (text only) ────────────────────────────────────────
                for (ChatMessage msg : history) {
                    if (msg.getType() == ChatMessage.TYPE_USER
                            || msg.getType() == ChatMessage.TYPE_AI) {
                        JSONObject m = new JSONObject();
                        m.put("role", msg.getType() == ChatMessage.TYPE_USER ? "user" : "assistant");
                        m.put("content", msg.getContent());
                        messages.put(m);
                    }
                }

                // ── Current user message (multi-part content) ──────────────────
                JSONArray contentParts = new JSONArray();

                // 1. Attached images (Claude Vision)
                for (AttachedFile file : attachments) {
                    if (file.isImage() && file.getBase64Data() != null) {
                        JSONObject imageSource = new JSONObject();
                        imageSource.put("type",       "base64");
                        imageSource.put("media_type", "image/jpeg");
                        imageSource.put("data",       file.getBase64Data());

                        JSONObject imagePart = new JSONObject();
                        imagePart.put("type",   "image");
                        imagePart.put("source", imageSource);

                        contentParts.put(imagePart);
                    }
                }

                // 2. Text file contents (prepended as context blocks)
                for (AttachedFile file : attachments) {
                    if (!file.isImage() && file.getTextContent() != null) {
                        String fileBlock = "```\n[File: " + file.getName() + "]\n"
                                + file.getTextContent() + "\n```";

                        JSONObject textPart = new JSONObject();
                        textPart.put("type", "text");
                        textPart.put("text", fileBlock);
                        contentParts.put(textPart);
                    }
                }

                // 3. User's own text
                if (userMessage != null && !userMessage.isEmpty()) {
                    JSONObject textPart = new JSONObject();
                    textPart.put("type", "text");
                    textPart.put("text", userMessage);
                    contentParts.put(textPart);
                }

                // Use simple string content when there are no attachments
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                if (attachments.isEmpty()) {
                    userMsg.put("content", userMessage);
                } else {
                    userMsg.put("content", contentParts);
                }
                messages.put(userMsg);

                // ── Build request body ─────────────────────────────────────────
                JSONObject body = new JSONObject();
                body.put("model",      model);
                body.put("max_tokens", 4096);
                body.put("messages",   messages);

                // ── HTTP call ──────────────────────────────────────────────────
                URL               url  = new URL(AiConstants.ApiEndpoints.CLAUDE_BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",      "application/json");
                conn.setRequestProperty("x-api-key",         apiKey);
                conn.setRequestProperty("anthropic-version", "2023-06-01");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30_000);
                conn.setReadTimeout(60_000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int           code   = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                    code == 200 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                if (code == 200) {
                    JSONObject resp = new JSONObject(sb.toString());
                    String text = resp.getJSONArray("content")
                                      .getJSONObject(0)
                                      .getString("text");
                    mainHandler.post(() -> callback.onSuccess(text));
                } else {
                    final String err = "Error " + code + ": " + sb;
                    mainHandler.post(() -> callback.onError(err));
                }

            } catch (Exception e) {
                Log.e(TAG, "Claude API error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public String getProviderName() {
        return AiConstants.AiProvider.CLAUDE;
    }
}
