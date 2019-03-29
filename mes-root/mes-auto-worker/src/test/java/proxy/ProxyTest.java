package proxy;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hengyi.japp.mes.auto.application.command.PrintCommand.DOFFING_NUM_PATTERN;

/**
 * @author jzb 2019-02-24
 */
public class ProxyTest {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final List<TestItem> items = Lists.newArrayList(new TestItem("A10"), new TestItem("A2"));
        Collections.sort(items);
        System.out.println(items);


        final Pattern pattern = Pattern.compile("(\\d+$)");
        final Matcher matcher = pattern.matcher("A10");
        if (matcher.find()) {
            final Integer integer = Integer.valueOf(matcher.group(0));
            final String group = matcher.group(0);
            System.out.println(group);
        }
        final int start = matcher.start();
        System.out.println(start);
    }

    @Data
    public static class TestItem implements Comparable<TestItem> {
        private final String doffingNum;

        @Override
        public int compareTo(TestItem o) {
            int i = 0;
            if (J.nonBlank(doffingNum) && J.nonBlank(o.doffingNum)) {
                final Matcher m1 = DOFFING_NUM_PATTERN.matcher(doffingNum);
                final Matcher m2 = DOFFING_NUM_PATTERN.matcher(o.doffingNum);
                if (m1.find() && m2.find()) {
                    final Integer i1 = Integer.valueOf(m1.group(0));
                    final Integer i2 = Integer.valueOf(m2.group(0));
                    i = Integer.compare(i1, i2);
                    if (i != 0) {
                        return i;
                    }
                }
            }
            return i;
        }
    }
}
