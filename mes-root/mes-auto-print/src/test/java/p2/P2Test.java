package p2;

import lombok.SneakyThrows;
import my.MyPrintTest3;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Locale;

/**
 * @author liuyuan
 * @create 2018-08-15 19:34
 * @description
 **/
public class P2Test implements Printable {
    public P2Test() throws Exception {

        /* Construct the print request specification.
         * The print data is a Printable object.
         * the request additonally specifies a job name, 2 copies, and
         * landscape orientation of the media.
         */
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
//        aset.add(OrientationRequested.LANDSCAPE);
//        aset.add(new Copies(2));
        final MediaPrintableArea printableArea = new MediaPrintableArea(0, 0, 104, 50, Size2DSyntax.MM);
        aset.add(printableArea);
        final MediaSizeName mediaSizeName = MediaSize.findMedia(104, 50, Size2DSyntax.MM);
        aset.add(mediaSizeName);
        aset.add(new JobName("My job", Locale.CHINA));

        /* Create a print job */
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        /* locate a print service that can handle the request */
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        pj.setPrintService(printService);
//        pj.pageDialog(aset);
        pj.print(aset);
    }

    public static void main(String arg[]) throws Exception {
        P2Test sp = new P2Test();
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


//    public static void main(String[] args) throws Exception {
//        // Step 1: Set up initial print settings.
//        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
//        // Step 2: Obtain a print job.
//        PrinterJob pj = PrinterJob.getPrinterJob();
//        // Step 3: Find print services.
//        PrintService[] services = PrinterJob.lookupPrintServices();
//        pj.setPrintService(services[0]);
//        // Step 2: Pass the settings to a page dialog and print dialog.
//        pj.pageDialog(aset);
//        if (pj.printDialog(aset)) {
//            // Step 4: Update the settings made by the user in the dialogs.
//            // Step 5: Pass the final settings into the print request.
//            pj.print(aset);
//        }
//    }
}
