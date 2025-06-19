package com.baiching.filesearch.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SearchService {
    /*
    * Search service
     */

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;
    private final QueryParser queryParser;

    public SearchService(String indexDirPath) throws IOException {
        this.directory = FSDirectory.open(Paths.get(indexDirPath));
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setRAMBufferSizeMB(256);
        this.writer = new IndexWriter(directory, config);
        this.reader = DirectoryReader.open(writer);
        this.searcher = new IndexSearcher(reader);
        this.queryParser = new QueryParser("filename", analyzer);
    }

    // start index management methods
    public void createIndex(List<String> paths) throws IOException {
        writer.deleteAll();

        for (String path: paths) {
            Document doc = createDocument(path);
            writer.addDocument(doc);
        }
        writer.commit();
        refreshReader();
    }

    public void addDocument(List<String> paths) throws IOException {
        for (String path : paths) {
            Document doc = createDocument(path);
            writer.addDocument(doc);
        }
        writer.commit();
        refreshReader();
    }

    public void updateDocument(String oldPath, String newPath) throws IOException {
        Term term = new Term("path", oldPath);
        Document doc = createDocument(newPath);
        writer.updateDocument(term, doc);
        writer.commit();
        refreshReader();
    }

    public void deleteDocument(String path) throws IOException {
        Term term = new Term("path", path);
        writer.deleteDocuments(term);
        writer.commit();
        refreshReader();
    }
    // end index management methods

    // Start of helper method
    private void refreshReader() throws IOException {
        IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader, writer);
        if (newReader != null) {
            reader.close();
            reader = newReader;
            searcher = new IndexSearcher(reader);
        }
    }

    // helper method
    private Document createDocument(String path) {
        Document doc = new Document();
        doc.add(new StringField("path", path, Field.Store.YES));
        String filename = Paths.get(path).getFileName().toString();
        doc.add(new TextField("filename", filename, Field.Store.NO));
        return doc;
    }
}
