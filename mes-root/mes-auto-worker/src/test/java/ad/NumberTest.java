package ad;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberTest {
    public static void main(String[] args) {
        double d = 0.345 * 89;
//        double d = 0.05 + 0.01;
//        System.out.println(Double.toString(d));

        final BigDecimal bigDecimal = BigDecimal.valueOf(d);
//        final BigDecimal divide = bigDecimal.divide(BigDecimal.valueOf(13),3, RoundingMode.HALF_UP);
        final BigDecimal divide = bigDecimal.divide(BigDecimal.valueOf(13), 3, RoundingMode.HALF_DOWN);
        System.out.println(divide);


        System.out.println(0.05 + 0.01);
    }
}
