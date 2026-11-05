package com.backend.modulo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Cloudinary strategy implemented over the plain REST API — no Cloudinary SDK.
 *
 * Activated with {@code app.media.strategy=cloudinary}. Credentials come from
 * the standard {@code CLOUDINARY_URL} environment variable:
 * {@code cloudinary://<api_key>:<api_secret>@<cloud_name>}.
 */
@Service
@ConditionalOnProperty(name = "app.media.strategy", havingValue = "cloudinary")
public class CloudinaryMediaStorageService implements MediaStorage {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryMediaStorageService.class);
    private static final Pattern CLOUDINARY_URL =
            Pattern.compile("^cloudinary://(?<key>[^:]+):(?<secret>[^@]+)@(?<cloud>[^/?]+)$");
    private static final String UPLOAD_FOLDER = "calistenic";
    private static final String RES_UPLOAD_MARKER = "/image/upload/";

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CloudinaryMediaStorageService(@Value("${CLOUDINARY_URL:}") String cloudinaryUrl) {
        Matcher matcher = cloudinaryUrl == null ? null : CLOUDINARY_URL.matcher(cloudinaryUrl.trim());
        if (matcher == null || !matcher.matches()) {
            throw new IllegalStateException(
                    "CLOUDINARY_URL inválida o ausente. Formato esperado: cloudinary://<api_key>:<api_secret>@<cloud_name>");
        }
        this.apiKey = matcher.group("key");
        this.apiSecret = matcher.group("secret");
        this.cloudName = matcher.group("cloud");
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("folder", UPLOAD_FOLDER);
        params.put("timestamp", timestamp);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", uploadResource(file));
        body.add("api_key", apiKey);
        try {
            body.addAll(signedParams(params));
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 no disponible en este JVM.", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            throw new IOException("Cloudinary rechazó la subida: " + e.getMessage(), e);
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IOException("Respuesta inválida de Cloudinary.", e);
        }
        String secureUrl = json.path("secure_url").asText(null);
        if (secureUrl == null || secureUrl.isBlank()) {
            throw new IOException("Cloudinary no devolvió una URL de media.");
        }
        return secureUrl;
    }

    @Override
    public boolean delete(String mediaUrl) throws IOException {
        String publicId = publicIdFromUrl(mediaUrl);
        if (publicId == null) return false;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("public_id", publicId);
        params.put("timestamp", timestamp);
        // invalidate=true purges the CDN copy, not just the asset repository.
        params.put("invalidate", "true");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.addAll(signedParams(params));
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 no disponible en este JVM.", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            // The caller (post/asset deletion) must remain successful; keep the
            // diagnostic for an orphan cleanup later.
            log.warn("Cloudinary no permitió eliminar el asset {}: {}", publicId, e.getMessage());
            return false;
        }
        String result = response.getBody() == null
                ? ""
                : objectMapper.readTree(response.getBody()).path("result").asText("");
        return "ok".equalsIgnoreCase(result);
    }

    /** Builds {@code key=value} pairs plus api_key, timestamp and signature. */
    private MultiValueMap<String, Object> signedParams(Map<String, String> params)
            throws NoSuchAlgorithmException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String signature = signature(params);
        params.forEach(map::add);
        map.add("api_key", apiKey);
        map.add("signature", signature);
        return map;
    }

    /** Cloudinary signature: SHA-1 hex of {@code k1=v1&k2=v2<apiSecret>} with params sorted. */
    private String signature(Map<String, String> params) throws NoSuchAlgorithmException {
        String toSign = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest((toSign + apiSecret).getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private ByteArrayResource uploadResource(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String name = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return name;
            }
        };
    }

    /**
     * Extracts the Cloudinary {@code public_id} from a CDN URL
     * ({@code .../image/upload/v123/<public_id>.<ext>}), or null for foreign URLs.
     */
    static String publicIdFromUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) return null;
        int markerIndex = mediaUrl.indexOf(RES_UPLOAD_MARKER);
        if (markerIndex < 0) return null;
        String tail = mediaUrl.substring(markerIndex + RES_UPLOAD_MARKER.length());
        int query = tail.indexOf('?');
        if (query >= 0) tail = tail.substring(0, query);
        String[] segments = tail.split("/", -1);
        // Drop an optional leading version folder such as v1234.
        int start = (segments.length > 0 && segments[0].matches("^v\\d+$")) ? 1 : 0;
        StringBuilder publicId = new StringBuilder();
        for (int i = start; i < segments.length; i++) {
            if (segments[i].isBlank()) continue;
            if (!publicId.isEmpty()) publicId.append('/');
            publicId.append(segments[i]);
        }
        String id = publicId.toString();
        if (id.isBlank()) return null;
        int dot = id.lastIndexOf('.');
        // Strip the delivery extension only from the last segment.
        if (dot > id.lastIndexOf('/') + 1) {
            id = id.substring(0, dot);
        }
        return id.isBlank() ? null : id;
    }
}
