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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LuceneSearchService {
    private static final String SEARCH_FIELD = "filename"; // Changed to filename
    private IndexReader reader;
    private IndexSearcher searcher;
    private final Analyzer analyzer;
    private final QueryParser queryParser;

    public LuceneSearchService(String indexDirPath) throws IOException {
        Directory directory = FSDirectory.open(Paths.get(indexDirPath));
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new StandardAnalyzer();
        this.queryParser = new QueryParser(SEARCH_FIELD, analyzer);
    }

    public List<String> search(String queryText, int maxResults) throws Exception {
        Query query = queryParser.parse(queryText); // Removed escaping
        TopDocs topDocs = searcher.search(query, maxResults);
        return getPathsFromHits(topDocs);
    }

    public List<String> fuzzySearch(String queryText, int maxResults, int maxEdits) throws IOException {
        String[] terms = queryText.split("\\s+");
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (String term : terms) {
            Term t = new Term(SEARCH_FIELD, term);
            FuzzyQuery fuzzyQuery = new FuzzyQuery(t, maxEdits);
            builder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery booleanQuery = builder.build();
        TopDocs topDocs = searcher.search(booleanQuery, maxResults);
        return getPathsFromHits(topDocs);
    }

    private List<String> getPathsFromHits(TopDocs topDocs) throws IOException {
        List<String> results = new ArrayList<>();
        StoredFields storedFields = searcher.storedFields();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = storedFields.document(scoreDoc.doc);
            results.add(doc.get("path")); // Return stored "path"
        }
        return results;
    }

    public void refresh() throws IOException {
        IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
        if (newReader != null) {
            reader.close();
            this.reader = newReader;
            this.searcher = new IndexSearcher(newReader);
        }
    }

    public void close() throws IOException {
        reader.close();
        // No need to close analyzer
    }

}
