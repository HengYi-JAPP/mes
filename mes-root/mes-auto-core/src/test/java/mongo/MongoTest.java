package mongo;

import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jzb 2018-06-20
 */
public class MongoTest {
    @SneakyThrows
    @Test
    public void testObjectId() throws ParseException {
        final ObjectId objectId = new ObjectId();
        System.out.println(objectId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date()));

//        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
////         Date date = sdf.parse("2013-09-29T18:46:19Z");
//        Date date = sdf.parse("2018-11-12T06:50:27.000Z");
//        System.out.println(date);
//
//        System.out.println(Math.ceil(1.1f));
//
//        System.out.println(Long.valueOf("ZZZZ", Character.MAX_RADIX) / 365);
//
//        Maybe.empty().flatMapCompletable(it -> {
//            System.out.println("test");
//            return Completable.complete();
//        }).subscribe(() -> {
//            System.out.println("ddd");
//        });
//
//        Maybe.empty().flatMapPublisher(it -> {
//            System.out.println("single");
//            return Flowable.just("single");
//        }).subscribe();
//
//        final HashMap<String, String> map = Maps.newHashMap();
//        final String sql = J.strTpl(SQL_TPL, map);
//
////        TimeUnit.DAYS.sleep(1);
    }
}
