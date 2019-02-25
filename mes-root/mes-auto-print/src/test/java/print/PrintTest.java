package print;

import com.hengyi.japp.mes.auto.print.command.SilkPrintCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jzb 2018-08-14
 */
@Slf4j
public class PrintTest {

    public static void main(String[] args) throws Exception {
//        final SilkPrintCommand command = command(10);
//        command.toPrintable().PrintLabel();
    }

    public static SilkPrintCommand command(int count) {
        final SilkPrintCommand command = new SilkPrintCommand();
        final List<SilkPrintCommand.Item> items = IntStream.range(0, count)
                .mapToObj(PrintTest::item)
                .collect(Collectors.toList());
        command.setSilks(items);
        return command;
    }

    public static SilkPrintCommand.Item item(int i) {
        final SilkPrintCommand.Item item = new SilkPrintCommand.Item();
        item.setCodeDate(new Date());
        item.setLineName("B1");
        item.setLineMachineItem(i);
        item.setSpindle(i);
        item.setDoffingNum("A1");
        item.setCode("123456789011");
        item.setBatchNo("setBatchNo");
        item.setBatchSpec("batchSpec");
        return item;
    }
}