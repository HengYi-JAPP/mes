package my;

import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * @author liuyuan
 * @create 2018-08-16 0:14
 * @description
 **/
public class PrintData {
    //页面大小以点为计量单位，1点为1英寸的1/72，1英寸为25.4毫米。A4纸大致为595×842点
    public static float mmToPix(float mm) {
        return (float) (mm / 25.4 * 72);
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
        return writer.encode("18091805137E", BarcodeFormat.CODE_128, 500, 300, hints);
    }

    public static BufferedImage silkCarImage() throws WriterException, IOException {
        return MatrixToImageWriter.toBufferedImage(silkCarBitMatrix());
    }
}
