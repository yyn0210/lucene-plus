import com.janeluo.luceneplus.realtime.RealTimeReopenUtil;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;

import java.util.List;

/**
 * Created by Janeluo on 2016/8/31 0031.
 */
public class RealTimeReopenUtilTest extends TestCase {

    public  void test1(){

        String fields[] = {"fieldname"};
//        RealTimeReopenUtil.search(fields);
//        RealTimeReopenUtil.search(fields);

        RealTimeReopenUtil instance = RealTimeReopenUtil.getInstance("D:/testindex");
        List<Document> search = instance.search(fields);
        System.out.println(">>>>>>>>>>>>>>>>>>> " + search.size());

    }
}
