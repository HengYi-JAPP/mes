package mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-20
 */
public class MongoTest {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.DAYS).readTimeout(1, TimeUnit.DAYS).build();
    private static final String baseUrl = "http://localhost:9998";
    private static final String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g";

    public static void main(String[] args) {
        IntStream.range(0, 10).parallel().forEach(it -> {
            final boolean result = test(it);
            System.out.println(result);
        });
    }

    @SneakyThrows
    private static boolean test(int i) {
        final ObjectNode lineMachine = MAPPER.createObjectNode().put("id", "5bfd4b87716bb151bd059ded");
        final JsonNode command = MAPPER.createObjectNode()
                .put("codeDate", "2020-10-01")
                .put("doffingNum", "A1")
                .set("lineMachine", lineMachine);
        final ArrayNode arrayNode = MAPPER.createArrayNode().add(command);
        final JsonNode node = MAPPER.createObjectNode().set("commands", arrayNode);
        final RequestBody body = RequestBody.create(JSON, node.toString());
        final Request request = new Request.Builder()
                .url(baseUrl + "/api/batchSilkBarcodes")
                .addHeader("Authorization", token)
                .post(body).build();
        final Response response = client.newCall(request).execute();
        System.out.println(i + ": " + response.body().string());
        return response.isSuccessful();
    }

}
