package lock;

import com.github.ixtf.japp.core.J;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.Optional;

/**
 * @author jzb 2019-03-03
 */
public class LockTest {
    static IndexWriter indexWriter;

    @SneakyThrows
    public static void main(String[] args) {
        final Path indexPath = Paths.get("/home/jzb/logs/SilkBarcode");
        indexWriter = new IndexWriter(FSDirectory.open(indexPath), new IndexWriterConfig(new SmartChineseAnalyzer()));
        testdd();
    }

    // line: 5c8782bba3f0a0602365d799
// lineMachine 1: 5c883c78a3f0a03522f18e9e
    @SneakyThrows
    private static void testdd() {
        final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        final LocalDate ld = LocalDate.of(2019, Month.MARCH, 28);
        final long startL = Optional.ofNullable(ld)
                .map(J::date)
                .map(Date::getTime)
                .orElse(-1l);
        final long endL = Optional.ofNullable(ld)
                .map(J::date)
                .map(Date::getTime)
                .orElse(-1l);
        bqBuilder.add(LongPoint.newRangeQuery("codeDate", startL, endL), BooleanClause.Occur.MUST);
        bqBuilder.add(new TermQuery(new Term("lineMachine", "5c883c78a3f0a03522f18e9e")), BooleanClause.Occur.MUST);
        bqBuilder.add(new TermQuery(new Term("doffingNum", "A1")), BooleanClause.Occur.MUST);

        @Cleanup final IndexReader indexReader = DirectoryReader.open(indexWriter);
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(bqBuilder.build(), Integer.MAX_VALUE);
        System.out.println(topDocs);
    }


}
