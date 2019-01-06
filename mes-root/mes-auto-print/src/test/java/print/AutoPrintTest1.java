package print;

import lombok.SneakyThrows;
import my.PrintData;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;

import static my.PrintData.mmToPix;

/**
 * @author liuyuan
 * @create 2018-08-15 23:43
 * @description
 **/
public class AutoPrintTest1 implements Printable {

    public static void main(String[] args) throws Exception {
        new AutoPrintTest1().PrintLabel();
    }

    @SneakyThrows
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex <= 2) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            border(g2d);

            g2d.setColor(Color.black);
            final Font font = new Font(null, Font.PLAIN, 7);
            g2d.setFont(font);

            float x = 1;
            float y = 3;
            drawString("140dtex/72f", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("140dtex/72f", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("140dtex/72f", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("140dtex/72f", g2d, font, mmToPix(x), mmToPix(y));

            x = 1;
            y += 3.5;
            drawString("C8-48/139", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("C8-48/139", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("C8-48/139", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("C8-48/139", g2d, font, mmToPix(x), mmToPix(y));

            x = 1;
            y += 3.5;
            drawString("GB013024", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GB013024", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GB013024", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GB013024", g2d, font, mmToPix(x), mmToPix(y));

            x = 1;
            y += 3.5;
            drawString("GXHY20180919B4", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GXHY20180919B4", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GXHY20180919B4", g2d, font, mmToPix(x), mmToPix(y));
            x += 25;
            drawString("GXHY20180919B4", g2d, font, mmToPix(x), mmToPix(y));

            float imageX = 3;
            x = 1;
            BufferedImage bufferedImage = PrintData.silkCarImage();
            g2d.drawImage(bufferedImage, (int) mmToPix(imageX), (int) mmToPix(15), (int) mmToPix(21), (int) mmToPix(7), null);
            drawString("18091805137E", g2d, font, mmToPix(x), mmToPix(24));

            imageX += 25;
            x += 25;
            g2d.drawImage(bufferedImage, (int) mmToPix(imageX), (int) mmToPix(15), (int) mmToPix(21), (int) mmToPix(7), null);
            drawString("18091805137E", g2d, font, mmToPix(x), mmToPix(24));

            imageX += 25;
            x += 25;
            g2d.drawImage(bufferedImage, (int) mmToPix(imageX), (int) mmToPix(15), (int) mmToPix(21), (int) mmToPix(7), null);
            drawString("18091805137E", g2d, font, mmToPix(x), mmToPix(24));

            imageX += 25;
            x += 25;
            g2d.drawImage(bufferedImage, (int) mmToPix(imageX), (int) mmToPix(15), (int) mmToPix(21), (int) mmToPix(7), null);
            drawString("18091805137E", g2d, font, mmToPix(x), mmToPix(24));
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    private void drawString(String s, Graphics2D g2d, Font font, float x, float codeY) {
        final FontRenderContext frc = g2d.getFontMetrics().getFontRenderContext();
        final Rectangle2D rect = font.getStringBounds(s, frc);
        final double codeX = (mmToPix(25) / 2) - (rect.getWidth() / 2) + x;
        g2d.drawString(s, (float) codeX, codeY);
    }

    private void border(Graphics2D g2d) {
        final float topLeftX = mmToPix(2);
        final float topLeftY = 0;
        final float topRightX = topLeftX + mmToPix(25) * 4;
        final float topRightY = 0;
        final float bottomLeftX = mmToPix(2);
        final float bottomLeftY = mmToPix(25);
        final float bottomRightX = topLeftX + mmToPix(25) * 4;
        final float bottomRightY = mmToPix(25);

        g2d.setColor(Color.red);
        g2d.drawLine((int) topLeftX, (int) topLeftY, (int) topRightX, (int) topRightY);
        g2d.drawLine((int) topLeftX, (int) topLeftY, (int) bottomLeftX, (int) bottomLeftY);
        g2d.drawLine((int) topRightX, (int) topRightY, (int) bottomRightX, (int) bottomRightY);
        g2d.drawLine((int) bottomLeftX, (int) bottomLeftY, (int) bottomRightX, (int) bottomRightY);
    }

    public void PrintLabel() throws Exception {
        Book book = new Book();
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        Paper p = new Paper();
        p.setSize(mmToPix(104.5f), mmToPix(26.8f)); // Paper Size,A4 590, 840
        p.setImageableArea(0, 0, mmToPix(104.5f), mmToPix(26.8f)); // Print Area
        pf.setPaper(p);
        book.append(this, pf, 1);
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(printService);
        job.setPageable(book);
        job.print();
    }
}
