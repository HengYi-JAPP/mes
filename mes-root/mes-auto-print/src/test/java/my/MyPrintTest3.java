package my;

import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.SneakyThrows;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.IOException;
import java.util.Map;

/**
 * @author liuyuan
 * @create 2018-08-15 19:52
 * @description
 **/
public class MyPrintTest3 implements Printable {
    public static void main(String[] args) throws Exception {
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        DocAttributeSet das = new HashDocAttributeSet();
//        final MediaPrintableArea printableArea = new MediaPrintableArea(0, 0, 104, 50, Size2DSyntax.MM);
//        das.add(OrientationRequested.REVERSE_PORTRAIT);
//        das.add(printableArea);

        final MyMediaSize myMediaSize = new MyMediaSize();
        das.add(myMediaSize);

        DocPrintJob pj = printService.createPrintJob();
        Doc doc = new SimpleDoc(new MyPrintTest3(), DocFlavor.SERVICE_FORMATTED.PRINTABLE, das);

        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(myMediaSize);
//        pras.add(printableArea);
//        pras.add(new JobName("My job", Locale.CHINA));

        pj.print(doc, pras);
    }

    public static BitMatrix silkBitMatrix() throws WriterException, IOException {
        //配置参数
        Map<EncodeHintType, Object> hints = Maps.newHashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错级别 这里选择最高H级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        MultiFormatWriter writer = new MultiFormatWriter();
        return writer.encode("01000000001A", BarcodeFormat.CODE_128, 500, 200, hints);
    }

    public static BitMatrix silkCarBitMatrix() throws WriterException, IOException {
        //配置参数
        Map<EncodeHintType, Object> hints = Maps.newHashMap();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错级别 这里选择最高H级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        MultiFormatWriter writer = new MultiFormatWriter();
        return writer.encode("6000P36012", BarcodeFormat.CODE_128, 500, 300, hints);
    }

    public static BufferedImage silkCarImage() throws WriterException, IOException {
        return MatrixToImageWriter.toBufferedImage(silkCarBitMatrix());
    }

    @SneakyThrows
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex == 0) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setColor(Color.black);
            g2d.drawString("P36012", 0, 10);
            g2d.drawString("P36012", 50, 10);

            System.out.println("getImageableWidth=" + pf.getImageableWidth());
            System.out.println("getImageableHeight=" + pf.getImageableHeight());

            BufferedImage bufferedImage = MyPrintTest3.silkCarImage();
            g2d.drawImage(bufferedImage, 10, 30, 40, 20, null);

            g2d.drawImage(bufferedImage, 60, 30, 40, 20, null);

//            g2d.fillRect(0, 0, 200, 200);
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }
}
