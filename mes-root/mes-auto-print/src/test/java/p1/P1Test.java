package p1;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import java.io.*;

/**
 * @author liuyuan
 * @create 2018-08-15 18:54
 * @description
 **/
public class P1Test {
    public static void main(String[] args) throws Exception {
        /* Use the pre-defined flavor for a GIF from an InputStream */
        DocFlavor flavor = DocFlavor.INPUT_STREAM.GIF;

        /* Create a set which specifies how the job is to be printed */
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.NA_LETTER);
        aset.add(new Copies(1));

        /* Locate print services which can print a GIF in the manner specified */
        PrintService[] pservices = PrintServiceLookup.lookupPrintServices(flavor, aset);

        /* Create a Print Job */
        DocPrintJob printJob = pservices[0].createPrintJob();

        /* Create a Doc implementation to pass the print data */
        Doc doc = new InputStreamDoc("D:/download/test.gif", flavor);

        /* Print the doc as specified */
        printJob.print(doc, aset);

        System.out.println(pservices);
    }
}

class InputStreamDoc implements Doc {
    private String filename;
    private DocFlavor docFlavor;
    private InputStream stream;

    public InputStreamDoc(String name, DocFlavor flavor) {
        filename = name;
        docFlavor = flavor;
    }

    public DocFlavor getDocFlavor() {
        return docFlavor;
    }

    /* No attributes attached to this Doc - mainly useful for MultiDoc */
    public DocAttributeSet getAttributes() {
        return null;
    }

    /* Since the data is to be supplied as an InputStream delegate to
     * getStreamForBytes().
     */
    public Object getPrintData() throws IOException {
        return getStreamForBytes();
    }

    /* Not possible to return a GIF as text */
    public Reader getReaderForText()
            throws UnsupportedEncodingException, IOException {
        return null;
    }

    /* Return the print data as an InputStream.
     * Always return the same instance.
     */
    public InputStream getStreamForBytes() throws IOException {
        synchronized (this) {
            if (stream == null) {
                stream = new FileInputStream(filename);
            }
            return stream;
        }
    }
}