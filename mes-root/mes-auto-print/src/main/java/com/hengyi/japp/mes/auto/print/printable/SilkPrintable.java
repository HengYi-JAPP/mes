package com.hengyi.japp.mes.auto.print.printable;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hengyi.japp.mes.auto.print.command.SilkPrintCommand;
import com.hengyi.japp.mes.auto.print.config.PaperConfig;
import com.hengyi.japp.mes.auto.print.config.SilkPrintConfig;
import com.hengyi.japp.mes.auto.print.config.ZxingConfig;
import lombok.SneakyThrows;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.print.Jprint.mmToPix;

/**
 * @author jzb 2018-08-18
 */
public class SilkPrintable implements Printable {
    private final SilkPrintConfig config;
    private final SilkPrintCommand command;
    private final int numPages;

    public SilkPrintable(SilkPrintConfig config, SilkPrintCommand command) {
        this.config = config;
        this.command = command;

        final double size = command.getSilks().size();
        final double pages = size / 4;
        this.numPages = (int) Math.ceil(pages);
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex < numPages) {
            final List<SilkPrintCommand.Item> items = pickSilks(pageIndex);

            final Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setColor(Color.black);
            final Font font = new Font(null, Font.PLAIN, config.getFontConfig().getSize());
            g2d.setFont(font);

            float x = 1;
            float imageX = 3;
            for (SilkPrintCommand.Item item : items) {
                float y = 3;
                drawString(item.getBatchSpec(), g2d, font, mmToPix(x), mmToPix(y));
                final String lineName = item.getLineName();
                final int spindle = item.getSpindle();
                final int lineMachineItem = item.getLineMachineItem();
                final String s2 = lineName + "-" + spindle + "/" + lineMachineItem;
                y += 3.5;
                drawString(s2, g2d, font, mmToPix(x), mmToPix(y));
                y += 3.5;
                drawString(item.getBatchNo(), g2d, font, mmToPix(x), mmToPix(y));
                final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                final String dateString = df.format(item.getCodeDate());
                final String doffingNum = J.defaultString(item.getDoffingNum());
                final String s4 = String.join("", config.getCorpPrefix(), dateString, doffingNum);
                y += 3.5;
                drawString(s4, g2d, font, mmToPix(x), mmToPix(y));
                final String code = item.getCode();
                final BufferedImage bufferedImage = silkBarCodeImage(code);
                g2d.drawImage(bufferedImage, (int) mmToPix(imageX), (int) mmToPix(15), (int) mmToPix(21), (int) mmToPix(7), null);
                drawString(code, g2d, font, mmToPix(x), mmToPix(24));

                x += 25;
                imageX += 25;
            }

            return Printable.PAGE_EXISTS;
        }
        return Printable.NO_SUCH_PAGE;
    }

    private List<SilkPrintCommand.Item> pickSilks(int numPages) {
        final int skip = numPages * 4;
        return command.getSilks().stream()
                .skip(skip)
                .limit(4)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private BufferedImage silkBarCodeImage(String content) {
        //配置参数
        Map<EncodeHintType, Object> hints = Maps.newHashMap();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错级别 这里选择最高H级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        MultiFormatWriter writer = new MultiFormatWriter();
        @NotNull final ZxingConfig zxingConfig = config.getZxingConfig();
        final BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, zxingConfig.getWidth(), zxingConfig.getHeight(), hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private void drawString(String s, Graphics2D g2d, Font font, float x, float codeY) {
        final FontRenderContext frc = g2d.getFontMetrics().getFontRenderContext();
        final Rectangle2D rect = font.getStringBounds(s, frc);
        final double codeX = (mmToPix(25) / 2) - (rect.getWidth() / 2) + x;
        g2d.drawString(s, (float) codeX, codeY);
    }

    public void PrintLabel() throws Exception {
        Book book = new Book();
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        Paper p = new Paper();
        final PaperConfig paperConfig = config.getPaperConfig();
        final float paperWithPix = paperConfig.getWithPix();
        final float paperHeightPix = paperConfig.getHeightPix();
        p.setSize(paperWithPix, paperHeightPix); // Paper Size,A4 590, 840
        p.setImageableArea(0, 0, paperWithPix, paperHeightPix); // Print Area
        pf.setPaper(p);
        book.append(this, pf, numPages);
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(printService);
        job.setPageable(book);
        job.print();
    }

}
