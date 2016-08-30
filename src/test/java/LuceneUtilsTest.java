import com.janeluo.luceneplus.page.Page;
import com.janeluo.luceneplus.utils.LuceneUtils;
import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.util.List;

/**
 * Created by Janeluo on 2016/8/31 0031.
 */
public class LuceneUtilsTest extends TestCase {
    public void test1() throws IOException, ParseException {
        Analyzer analyzer = new IKAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        IndexWriter indexWrtier = LuceneUtils.getIndexWrtier("D:/testindex", config);

        // 创建索引
        Document doc = new Document();
        String text = "我是分词器.";
        doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
        LuceneUtils.addIndex(indexWrtier, doc);

        //无分页 查询
        FSDirectory directory = LuceneUtils.openFSDirectory("D:/testindex");
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(Version.LUCENE_47, "fieldname", analyzer);
        Query query = parser.parse("分词");
        List<Document> documentList = LuceneUtils.query(isearcher, query);
        System.out.println(">>>>>>>>>>>>>>>" + documentList.size());

        Page<Document> page = new Page<Document>(1, 10);

        LuceneUtils.pageQuery(isearcher,query,page);

        System.out.println(">>>>>>>>>>>>>>>" + page.getItems().size());


    }
}
