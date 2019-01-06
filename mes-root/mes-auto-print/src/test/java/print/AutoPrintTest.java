package print;

import lombok.SneakyThrows;
import my.PrintData;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

import static my.PrintData.mmToPix;

/**
 * @author liuyuan
 * @create 2018-08-15 23:43
 * @description
 **/
public class AutoPrintTest implements Printable {

    public static void main(String[] args) throws Exception {
        new AutoPrintTest().PrintLabel();
    }

    @SneakyThrows
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex <= 50) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setColor(Color.black);
            g2d.drawString("P36012", mmToPix(10), mmToPix(10));
            g2d.drawString("P36012", mmToPix(60), mmToPix(10));

            BufferedImage bufferedImage = PrintData.silkCarImage();
            g2d.drawImage(bufferedImage, (int) mmToPix(2), (int) mmToPix(20), (int) mmToPix(40), (int) mmToPix(15), null);
            g2d.drawString("6000P36012", mmToPix(18), mmToPix(40));
            g2d.drawImage(bufferedImage, (int) mmToPix(55), (int) mmToPix(20), (int) mmToPix(40), (int) mmToPix(15), null);
            g2d.drawString("6000P36012", mmToPix(73), mmToPix(40));
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    public void PrintLabel() throws Exception {
        Book book = new Book();
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        Paper p = new Paper();
        p.setSize(mmToPix(104), mmToPix(51.5f)); // Paper Size,A4 590, 840
        p.setImageableArea(0, 0, mmToPix(104), mmToPix(50)); // Print Area
        pf.setPaper(p);
        book.append(this, pf, 50);
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(printService);
        job.setPageable(book);
        job.print();
    }
}
