package com.baiching.filesearch.search;

/*
 *  search(String queryText, int maxResults): Takes a user's search query and returns a list of matching file paths.
 *  It will manage the IndexReader and IndexSearcher instances, and the QueryParser.
 *  It will internally use the same Analyzer as the LuceneIndexManager for consistency.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LuceneSearchService {
    private static final String SEARCH_FIELD = "path";  // TextField used for searching
    private final IndexReader reader;
    private final IndexSearcher searcher;
    private final Analyzer analyzer;
    private final QueryParser queryParser;

    /**
     * Initializes the search service with the Lucene index directory
     * @param indexDirPath Path to the Lucene index directory
     * @throws IOException If there's an error opening the index
     */
    public LuceneSearchService(String indexDirPath) throws IOException {
        Directory directory = FSDirectory.open(Paths.get(indexDirPath));
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new StandardAnalyzer();  // Same analyzer as used for indexing
        this.queryParser = new QueryParser(SEARCH_FIELD, analyzer);
    }

    /**
     * Searches the index for matching file paths
     * @param queryText User's search query
     * @param maxResults Maximum number of results to return
     * @return List of matching file paths
     * @throws Exception If there's an error parsing the query or searching
     */
    public List<String> search(String queryText, int maxResults) throws Exception {
        Query query = queryParser.parse(QueryParser.escape(queryText));
        TopDocs topDocs = searcher.search(query, maxResults);
        return getPathsFromHits(topDocs);
    }

    /**
     * Converts search hits to file paths
     * @param topDocs Search results
     * @return List of file paths
     * @throws IOException If there's an error accessing documents
     */
    private List<String> getPathsFromHits(TopDocs topDocs) throws IOException {
        List<String> results = new ArrayList<>();
        StoredFields storedFields = searcher.storedFields();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = storedFields.document(scoreDoc.doc);
            results.add(doc.get(SEARCH_FIELD));
        }
        return results;
    }

    /**
     * Closes resources
     * @throws IOException If there's an error closing resources
     */
    public void close() throws IOException {
        reader.close();
        analyzer.close();
    }

}
