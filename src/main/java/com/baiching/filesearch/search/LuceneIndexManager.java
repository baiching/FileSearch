package com.baiching.filesearch.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class LuceneIndexManager {
    /*
    * createIndex(): Initial setup/rebuild of the index from SQLite.
    * addDocument(String path): Adds a single file path to the index.
    * updateDocument(String oldPath, String newPath): Updates a path in the index.
    * deleteDocument(String path): Removes a path from the index.
    * It will manage the IndexWriter and Analyzer instances.
     */
    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;

    /**
     * Initializes the Lucene index manager with specified index directory
     * @param indexDirPath File system path for the Lucene index
     * @throws IOException If there's an error opening the index directory
     */

    public LuceneIndexManager(String indexDirPath) throws IOException {
        this.directory = FSDirectory.open(Paths.get(indexDirPath));
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        config.setRAMBufferSizeMB(256);
        this.writer = new IndexWriter(directory, config);
    }

    /**
     * Initial setup/rebuild of the index from SQLite
     * @throws IOException If there's an error resetting the index
     */
    public void createIndex(List<String> paths) throws IOException {
        //clearing existing index
        writer.deleteAll();
        for (String path: paths) {
            Document doc = new Document();
            doc.add(new StringField("path", path, Field.Store.YES));
            writer.addDocument(doc);
        }

        //optimizeing commit for builk operations
        writer.commit();
    }

    /**
     * Adds a single file path to the index
     * @param paths File path to be added
     * @throws IOException If there's an error adding the document
     */
    public void addDocument(List<String> paths) throws IOException {
        for (String path : paths) {
            Document doc = new Document();
            doc.add(new StringField("path", path, Field.Store.YES));
            writer.addDocument(doc);
        }
    }

    /**
     * Updates a path in the index
     * @param oldPath Original file path to be updated
     * @param newPath New file path to replace the old path
     * @throws IOException If there's an error updating the document
     */
    public void updateDocument(String oldPath, String newPath) throws IOException {
        Term term = new Term("path", oldPath);
        Document doc = new Document();
        doc.add(new StringField("path", newPath, Field.Store.YES));
        writer.updateDocument(term, doc);
    }

    /**
     * Removes a path from the index
     * @param path File path to be removed
     * @throws IOException If there's an error deleting the document
     */
    public void deleteDocument(String path) throws IOException {
        Term term = new Term("path", path);
        writer.deleteDocuments(term);
    }

    /**
     * Removes multiple paths in bulk
     * @param paths List of paths to remove
     * @throws IOException If there's an error deleting documents
     */
    public void deleteDocuments(List<String> paths) throws IOException {
        for (String path : paths) {
            Term term = new Term("path", path);
            writer.deleteDocuments(term);
        }
    }

    /**
     * Commits pending changes and optimizes the index
     * @throws IOException If there's an error committing
     */
    public void commit() throws IOException {
        writer.commit();
    }

    /**
     * Closes resources and releases locks
     * @throws IOException If there's an error closing resources
     */
    public void close() throws IOException {
        writer.close();
        directory.close();
        analyzer.close();
    }
}
