package com.neovaccine.service;

/**
 * ApiService.java — The COMMUNICATION LAYER
 * ==========================================
 * This class is the "phone" our JavaFX app uses to call the Python backend.
 *
 * FLOW:
 *   JavaFX app → ApiService.predict() → HTTP POST to Python → JSON response → Java objects
 *
 * We use Apache HttpClient to make HTTP requests (like a browser does, but in code).
 * We use Jackson to convert JSON strings into Java objects and back.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovaccine.MainApp;
import com.neovaccine.model.PeptidePrediction;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApiService {

    // Jackson ObjectMapper = the JSON translator
    // It converts: Java objects ↔ JSON strings
    private final ObjectMapper jackson = new ObjectMapper();

    // ═══════════════════════════════════════════════════════════════════
    // METHOD 1: Check if the Python backend is running
    // Returns true if the server responds, false if it's offline.
    // ═══════════════════════════════════════════════════════════════════
    public boolean isBackendAlive() {
        // We use try-with-resources so the HttpClient is auto-closed when done.
        // It's like a faucet that automatically turns off — no memory leaks!
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            // Build the GET request to our /api/health endpoint
            HttpGet request = new HttpGet(MainApp.BACKEND_URL + "/api/health");
            
            // Execute the request and check if we get a 200 OK response
            return client.execute(request, response -> {
                return response.getCode() == 200;
            });

        } catch (IOException e) {
            // If we can't connect at all, the server is definitely offline
            System.err.println("Backend is offline: " + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // METHOD 2: Fetch the list of supported HLA alleles from backend
    // Returns a List of strings like ["HLA-A*02:01", "HLA-B*07:02", ...]
    // ═══════════════════════════════════════════════════════════════════
    public List<String> fetchAlleles() throws IOException {
        List<String> alleles = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(MainApp.BACKEND_URL + "/api/alleles");
            
            String responseBody = client.execute(request, response -> {
                // Read the response body as a String
                return new String(
                    response.getEntity().getContent().readAllBytes(),
                    StandardCharsets.UTF_8
                );
            });

            // Parse the JSON response
            // responseBody looks like: {"alleles": ["HLA-A*02:01", ...], "count": 20}
            JsonNode root = jackson.readTree(responseBody);
            JsonNode allelesNode = root.get("alleles");
            
            if (allelesNode != null && allelesNode.isArray()) {
                for (JsonNode alleleNode : allelesNode) {
                    alleles.add(alleleNode.asText());
                }
            }
        }

        return alleles;
    }

    // ═══════════════════════════════════════════════════════════════════
    // METHOD 3: THE BIG ONE — Send peptides to backend for prediction
    //
    // @param peptides — List of peptide sequences, e.g. ["SIINFEKL", "GILGFVFTL"]
    // @param alleles  — List of HLA alleles, e.g. ["HLA-A*02:01", "HLA-B*07:02"]
    // @return List of PeptidePrediction objects (one per peptide-allele combo)
    // ═══════════════════════════════════════════════════════════════════
    public PredictionResult predict(List<String> peptides, List<String> alleles)
            throws IOException {

        // ── STEP A: Build the JSON request body ─────────────────────────
        // We need to send:
        // {
        //   "peptides": ["SIINFEKL", "GILGFVFTL"],
        //   "alleles": ["HLA-A*02:01", "HLA-B*07:02"]
        // }
        ObjectNode requestBody = jackson.createObjectNode();

        ArrayNode peptidesArray = requestBody.putArray("peptides");
        for (String p : peptides) {
            peptidesArray.add(p.trim().toUpperCase());
        }

        ArrayNode allelesArray = requestBody.putArray("alleles");
        for (String a : alleles) {
            allelesArray.add(a);
        }

        String requestJson = jackson.writeValueAsString(requestBody);

        // ── STEP B: Send the POST request ──────────────────────────────
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            
            HttpPost request = new HttpPost(MainApp.BACKEND_URL + "/api/predict");
            
            // Set the request body as JSON
            request.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
            
            String responseBody = client.execute(request, response -> {
                int statusCode = response.getCode();
                String body = new String(
                    response.getEntity().getContent().readAllBytes(),
                    StandardCharsets.UTF_8
                );
                
                if (statusCode != 200) {
                    throw new IOException("Backend returned error " + statusCode + ": " + body);
                }
                return body;
            });

            // ── STEP C: Parse the JSON response ─────────────────────────
            return parseResponse(responseBody);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRIVATE HELPER: Parse the JSON response into Java objects
    //
    // The response looks like:
    // {
    //   "success": true,
    //   "total_peptides": 3,
    //   "summary": {"strong_binders": 1, "weak_binders": 1, "non_binders": 1},
    //   "results": [
    //     {
    //       "peptide": "SIINFEKL",
    //       "length": 8,
    //       "allele_predictions": [
    //         {"allele": "HLA-A*02:01", "percent_rank": 0.23, ...}
    //       ]
    //     }
    //   ]
    // }
    // ═══════════════════════════════════════════════════════════════════
    private PredictionResult parseResponse(String json) throws IOException {
        JsonNode root = jackson.readTree(json);

        // Read summary counts
        JsonNode summaryNode = root.get("summary");
        int strongBinders = summaryNode.get("strong_binders").asInt();
        int weakBinders   = summaryNode.get("weak_binders").asInt();
        int nonBinders    = summaryNode.get("non_binders").asInt();

        // Build list of PeptidePrediction objects
        List<PeptidePrediction> predictions = new ArrayList<>();
        
        JsonNode resultsNode = root.get("results");
        if (resultsNode != null && resultsNode.isArray()) {
            for (JsonNode result : resultsNode) {
                String peptide = result.get("peptide").asText();
                int    length  = result.get("length").asInt();

                JsonNode allelePreds = result.get("allele_predictions");
                if (allelePreds != null && allelePreds.isArray()) {
                    for (JsonNode ap : allelePreds) {
                        String allele    = ap.get("allele").asText();
                        double pctRank   = ap.get("percent_rank").asDouble();
                        double affinity  = ap.get("affinity_nm").asDouble();
                        String bindLevel = ap.get("binder_level").asText();
                        double immScore  = ap.get("immunogenicity_score").asDouble();

                        predictions.add(new PeptidePrediction(
                            peptide, length, allele, pctRank, affinity, bindLevel, immScore
                        ));
                    }
                }
            }
        }

        return new PredictionResult(predictions, strongBinders, weakBinders, nonBinders);
    }

    // ═══════════════════════════════════════════════════════════════════
    // INNER CLASS: PredictionResult
    // Holds everything returned from a prediction call.
    // Like a package that contains the predictions + summary stats.
    // ═══════════════════════════════════════════════════════════════════
    public static class PredictionResult {
        public final List<PeptidePrediction> predictions;
        public final int strongBinders;
        public final int weakBinders;
        public final int nonBinders;

        public PredictionResult(List<PeptidePrediction> predictions,
                                int strongBinders, int weakBinders, int nonBinders) {
            this.predictions   = predictions;
            this.strongBinders = strongBinders;
            this.weakBinders   = weakBinders;
            this.nonBinders    = nonBinders;
        }

        public int total() {
            return predictions.size();
        }
    }
}
