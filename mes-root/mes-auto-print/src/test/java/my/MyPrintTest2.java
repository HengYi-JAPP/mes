package my;

import lombok.SneakyThrows;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

/**
 * @author liuyuan
 * @create 2018-08-15 19:52
 * @description
 **/
public class MyPrintTest2 implements Printable {
    public static void main(String[] args) throws Exception {
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        final MyMediaSize myMediaSize = new MyMediaSize();
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet(myMediaSize);
        pras.add(myMediaSize);

        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintService(printService);
        pj.setPrintable(new MyPrintTest2(), pj.pageDialog(new PageFormat()));
        pj.print(pras);
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

            BufferedImage bufferedImage = PrintData.silkCarImage();
            g2d.drawImage(bufferedImage, 10, 30, 40, 20, null);

            g2d.drawImage(bufferedImage, 60, 30, 40, 20, null);

//            g2d.fillRect(0, 0, 200, 200);
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }
}
