package com.janeluo.luceneplus.realtime;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实时热搜工具类
 *
 * @author yangyining@janeluo.com
 */
public class RealTimeReopenUtil {


    private SearcherManager mgr;
    private IndexWriter writer = null;
    private TrackingIndexWriter tkWriter = null;
    private ControlledRealTimeReopenThread<IndexSearcher> crtThread = null;

    private final static Map<String, RealTimeReopenUtil> utilMap = new HashMap<String, RealTimeReopenUtil>();

    static {

    }

    /**
     * @param path
     */
    private RealTimeReopenUtil(String path) {
        try {
            Directory fsDir = FSDirectory.open(new File(path));
            //创建writer
            writer = new IndexWriter(fsDir, new IndexWriterConfig(Version.LUCENE_47, new IKAnalyzer(true)));
            //新建SearcherManager
            //true 表示在内存中删除，false可能删可能不删，设为false性能会更好一些
            mgr = new SearcherManager(writer, false, new SearcherFactory());
            //ControlledRealTimeReopenThread 构造是必须有的，主要将writer装，每个方法都没有commit 操作。
            tkWriter = new TrackingIndexWriter(writer);//为writer 包装了一层
            //创建线程，线程安全的，我们不须处理
            crtThread = new ControlledRealTimeReopenThread<IndexSearcher>(tkWriter, mgr, 5.0, 0.025);
            crtThread.setDaemon(true);//设为后台进程
            crtThread.setName("我是好人");
            crtThread.start();//启动线程
//            crtThread.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static RealTimeReopenUtil getInstance(String path) {
        RealTimeReopenUtil util = utilMap.get(path);
        if (null == util) {
            util = new RealTimeReopenUtil(path);
        }

        return util;
    }

    /**
     * 根据
     *
     * @param fields
     * @return 查下结果结合
     */
    public List<Document> search(String fields[]) {
        IndexSearcher searcher = null;
        try {

            //更新看看内存中索引是否有变化如果，有一个更新了，其他线程也会更新

            mgr.maybeRefresh();

            //利用acquire 方法获取search，执行此方法前须执行maybeRefresh
            searcher = mgr.acquire();

//            String fields[] = {"body", "name"};
            QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_47, fields, new IKAnalyzer(true));
            Query query = new TermQuery(new Term("id", "2"));// qp.parse("中国");
            TopDocs tds = searcher.search(query, 5);
            ScoreDoc[] sds = tds.scoreDocs;

            List<Document> resultList = new ArrayList<Document>();
            for (ScoreDoc sd : sds) {
                resultList.add(searcher.doc(sd.doc));
            }

            return resultList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //释放searcher，
                if (searcher != null) {
                    mgr.release(searcher);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("------------------------------------------------------------------------------------------------");
        }
        return null;
    }
}