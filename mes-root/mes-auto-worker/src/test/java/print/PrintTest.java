package print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-11-22
 */
public class PrintTest {
    public static void main(String[] args) throws Exception {

//        final JsonNode jsonNode = MAPPER.readTree(FileUtils.getFile("/tmp/test.json"));
//        final SilkCarRuntimeInitEvent.DTO dto = SilkCarRuntimeInitEvent.DTO.from(jsonNode.get("initEvent").asText());
//        final List<SilkRuntime.DTO> silkRuntimes = dto.getSilkRuntimes().stream().filter(it -> it.getSideType() == SilkCarSideType.B).collect(Collectors.toList());
//        dto.setSilkRuntimes(silkRuntimes);
//
//        final ObjectNode node = MAPPER.createObjectNode().put("initEvent", MAPPER.writeValueAsString(dto));
//
//        System.out.println(node.toString());

        initEvent("[{\"sideType\":\"B\",\"row\":1,\"col\":1,\"silk\":{\"id\":\"003M0003GD23\"}},{\"sideType\":\"B\",\"row\":1,\"col\":2,\"silk\":{\"id\":\"003M0003GD21\"}},{\"sideType\":\"B\",\"row\":1,\"col\":3,\"silk\":{\"id\":\"003M0003GD19\"}},{\"sideType\":\"B\",\"row\":1,\"col\":4,\"silk\":{\"id\":\"003M0003GD17\"}},{\"sideType\":\"B\",\"row\":1,\"col\":5,\"silk\":{\"id\":\"003M0003GD15\"}},{\"sideType\":\"B\",\"row\":1,\"col\":6,\"silk\":{\"id\":\"003M0003GD13\"}},{\"sideType\":\"B\",\"row\":2,\"col\":1,\"silk\":{\"id\":\"003M0003GD01\"}},{\"sideType\":\"B\",\"row\":2,\"col\":2,\"silk\":{\"id\":\"003M0003GD03\"}},{\"sideType\":\"B\",\"row\":2,\"col\":3,\"silk\":{\"id\":\"003M0003GD05\"}},{\"sideType\":\"B\",\"row\":2,\"col\":4,\"silk\":{\"id\":\"003M0003GD07\"}},{\"sideType\":\"B\",\"row\":2,\"col\":5,\"silk\":{\"id\":\"003M0003GD09\"}},{\"sideType\":\"B\",\"row\":2,\"col\":6,\"silk\":{\"id\":\"003M0003GD11\"}},{\"sideType\":\"B\",\"row\":3,\"col\":1,\"silk\":{\"id\":\"003M0003GD24\"}},{\"sideType\":\"B\",\"row\":3,\"col\":2,\"silk\":{\"id\":\"003M0003GD22\"}},{\"sideType\":\"B\",\"row\":3,\"col\":3,\"silk\":{\"id\":\"003M0003GD20\"}},{\"sideType\":\"B\",\"row\":3,\"col\":4,\"silk\":{\"id\":\"003M0003GD18\"}},{\"sideType\":\"B\",\"row\":3,\"col\":5,\"silk\":{\"id\":\"003M0003GD16\"}},{\"sideType\":\"B\",\"row\":3,\"col\":6,\"silk\":{\"id\":\"003M0003GD14\"}},{\"sideType\":\"B\",\"row\":4,\"col\":1,\"silk\":{\"id\":\"003M0003GD02\"}},{\"sideType\":\"B\",\"row\":4,\"col\":2,\"silk\":{\"id\":\"003M0003GD04\"}},{\"sideType\":\"B\",\"row\":4,\"col\":3,\"silk\":{\"id\":\"003M0003GD06\"}},{\"sideType\":\"B\",\"row\":4,\"col\":4,\"silk\":{\"id\":\"003M0003GD08\"}},{\"sideType\":\"B\",\"row\":4,\"col\":5,\"silk\":{\"id\":\"003M0003GD10\"}},{\"sideType\":\"B\",\"row\":4,\"col\":6,\"silk\":{\"id\":\"003M0003GD12\"}}]");

        final String ss = "{ \"_id\" : \"5c0c142d12f79200011b3cf3\", \"doffingNum\" : \"A1\", \"doffingOperator\" : \"5b384b2cd8712064f101e31e\", \"doffingType\" : \"MANUAL\", \"batch\" : \"5bffa63d8857b85a437d1fd4\", \"spindle\" : 1, \"silkCarRecords\" : [ \"5c0c142d12f79200011b3db7\" ], \"lineMachine\" : \"5bffa63d8857b85a437d224f\", \"code\" : \"003L00030H01\" }";

        final String silkBarcode = "003M0003GD";
        final String lineMachine = "5bffa63d8857b85a437d2295";
        final String doffingNum = "A1";
        IntStream.rangeClosed(1, 24).forEach(spindle -> {
            try {
                final String pad = Strings.padStart("" + spindle, 2, '0');
                final ObjectNode node = (ObjectNode) MAPPER.readTree(ss);
                node.put("_id", silkBarcode + pad);
                node.put("code", silkBarcode + pad);
                node.put("spindle", spindle);
                node.put("lineMachine", lineMachine);
                node.put("doffingNum", doffingNum);
                System.out.println(node);
            } catch (IOException e) {
            }
        });

//        TimeUnit.DAYS.sleep(1);
    }

    private static void initEvent(String s) throws IOException {
        final JsonNode jsonNode = MAPPER.readTree(s);
        final String x = MAPPER.writeValueAsString(jsonNode);
        final ObjectNode result = MAPPER.createObjectNode().put("test", x);
        System.out.println(result.toString());
    }
}
