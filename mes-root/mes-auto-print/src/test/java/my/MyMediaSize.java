package my;

import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.Size2DSyntax;

/**
 * @author liuyuan
 * @create 2018-08-15 22:32
 * @description
 **/
public class MyMediaSize extends Size2DSyntax implements PrintRequestAttribute, DocAttribute {
    protected MyMediaSize() {
        super(50, 104, Size2DSyntax.MM);
    }

    @Override
    public Class<? extends Attribute> getCategory() {
        return MyMediaSize.class;
    }

    @Override
    public String getName() {
        return "media-size";
    }
}
