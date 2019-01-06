package hotfix;

import com.google.common.base.Strings;

/**
 * @author jzb 2018-11-30
 */
public class SilkBarcodeFix {
    public static void main(String[] args) {
//        final String code = "003K001BK02B";
//        System.out.println(SilkBarcodeService.silkCodeToSilkBarCode(code));

        final String s = Long.toString(34, Character.MAX_RADIX);
        System.out.println(Strings.padStart(s, 5, '0').toUpperCase());
//        System.out.println(s);

        System.out.println(Long.parseLong("ZZZZz", Character.MAX_RADIX));
    }
}
