package p3;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.awt.*;
import java.awt.print.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author liuyuan
 * @create 2018-08-15 23:43
 * @description
 **/
public class P3Test implements Printable {

    /**
     * * @param Graphic指明打印的图形环境   * @param
     * PageFormat指明打印页格式（页面大小以点为计量单位，1点为1英寸的1/72，1英寸为25.4毫米。A4纸大致为595×842点）   * @param
     * pageIndex指明页号
     **/
    int PageNumbers;
    String StartSN;
    String Model;
    String Format;

    public P3Test(int PageNumbers, String StartSN, String Model, String Format) {
        this.PageNumbers = PageNumbers;
        this.StartSN = StartSN;
        this.Model = Model;
        this.Format = Format;
    }

    public static void main(String[] args) {

        (new P3Test(1, "2132300001", "FTH-18", "0")).PrintLabel("\\\\SHA1APFPSW02\\sha1aplj5sIT");
    }

    public void SetPageNum(int PageNumbers) {
        this.PageNumbers = PageNumbers;
    }

    public int getPageNum() {
        return PageNumbers;
    }

    public void SetStartSN(String StartSN) {
        this.StartSN = StartSN;
    }

    public String getStartSN() {
        return StartSN;
    }

    public void SetModel(String Model) {
        this.Model = Model;
    }

    public String getModel() {
        return Model;
    }

    public void SetFormat(String Format) {
        this.Format = Format;
    }

    public String getFormat() {
        return Format;
    }

    public int print(Graphics gra, PageFormat pf, int pageIndex) throws PrinterException {
        Graphics2D g2 = (Graphics2D) gra;
        // 设置打印颜色为黑色
        g2.setColor(Color.black);
        Font font = new Font("MS PGothic", Font.PLAIN, 13);
        g2.setFont(font); // 设置字体
        if (pageIndex >= PageNumbers) {
            return Printable.NO_SUCH_PAGE;
        }
        String SN = StartSN.substring(6, 10);
        int intSN = Integer.parseInt(SN);
        intSN = intSN + pageIndex;
        NumberFormat formattern = new DecimalFormat("0000");
        SN = formattern.format(intSN);
        System.out.println(SN);

        g2.drawString(Model, (float) 134, (float) 145); //第二排
        g2.drawString(StartSN.substring(0, 6) + SN, (float) 266, (float) 145); //第三排

        return Printable.PAGE_EXISTS;
    }

    public void PrintLabel(String PrintQ) {
        //ReadData();
        // 通俗理解就是书、文档
        Book book = new Book();
        // 设置成竖打
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT); // LANDSCAPE表示竖打;PORTRAIT表示横打;REVERSE_LANDSCAPE表示打印空白
        // 通过Paper设置页面的空白边距和可打印区域。必须与实际打印纸张大小相符。
        Paper p = new Paper();
//        p.setSize(567, 252); // Warranty Paper Size,A4 590, 840
//        p.setImageableArea(10, 10, 590, 840); // Print Area
        p.setSize(200, 100); // Warranty Paper Size,A4 590, 840
        p.setImageableArea(10, 10, 200, 100); // Print Area
        pf.setPaper(p);
        // 把 PageFormat 和 Printable 添加到书中，组成一个页面
        book.append(this, pf, this.getPageNum());
        String printerName = PrintQ;
        HashAttributeSet hs = new HashAttributeSet();
        hs.add(new PrinterName(printerName, null));
        // 获取打印服务对象
        PrintService[] printService = PrintServiceLookup.lookupPrintServices(null, hs);
        PrinterJob job = PrinterJob.getPrinterJob();
        try {
            if (printService.length > 0) {
                job.setPrintService(printService[0]);
            }
            //  设置打印类
            job.setPageable(book);
            //  Print
            job.print();


        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }
}
