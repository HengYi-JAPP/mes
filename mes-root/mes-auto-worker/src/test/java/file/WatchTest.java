package file;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-03-15
 */
public class WatchTest {
    public static void main(String[] args) {
        final ObjectNode objectNode = MAPPER.createObjectNode().put("test", "1565654400000");
        final long test = objectNode.get("test").asLong();
        System.out.println(test);
    }
}
