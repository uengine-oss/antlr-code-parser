package legacymodernizer.parser.api;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** 시험용 — 진짜 Tomcat 에 multipart 를 그대로 보낸다(파일 n 개, 이름의 # 은 번호). */
final class MultipartPost {

    private MultipartPost() {}

    static HttpResponse<String> files(int port, int count, int bytesEach, String namePattern) throws Exception {
        String boundary = "----robo" + System.nanoTime();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[bytesEach];
        Arrays.fill(data, (byte) 'a');
        for (int i = 0; i < count; i++) {
            String fn = namePattern.replace("#", String.valueOf(i));
            out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"files\"; filename=\"" + fn
                    + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(data);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/antlr/fileUpload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
