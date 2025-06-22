package com.baiching.filesearch.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private Directory directory;
    private Analyzer analyzer;
    private IndexWriter writer;
    private String indexPath;

    public LuceneIndexManager() {}

    public LuceneIndexManager(String indexDirPath) throws IOException {
        Path dirPath = Paths.get(indexDirPath);

        // Ensure the directory exists (create if missing)
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // Creates all parent dirs too
        }
        this.directory = FSDirectory.open(dirPath);
        this.indexPath = dirPath.toString();
        this.analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        //config.setRAMBufferSizeMB(256);
        this.writer = new IndexWriter(directory, config);
    }

    public void createIndex(List<String> paths) throws IOException {
        writer.deleteAll();
        for (String path : paths) {
            Document doc = createDocument(path);
            writer.addDocument(doc);
        }
        writer.commit();
    }

    public void addDocument(List<String> paths) throws IOException {
        for (String path : paths) {
            Document doc = createDocument(path);
            writer.addDocument(doc);
        }
    }

    public void updateDocument(String oldPath, String newPath) throws IOException {
        Term term = new Term("path", oldPath);
        Document doc = createDocument(newPath);
        writer.updateDocument(term, doc);
    }

    public void deleteDocument(String path) throws IOException {
        Term term = new Term("path", path);
        writer.deleteDocuments(term);
    }

    public void deleteDocuments(List<String> paths) throws IOException {
        for (String path : paths) {
            Term term = new Term("path", path);
            writer.deleteDocuments(term);
        }
    }

    public void close() throws IOException {
        writer.close();
        directory.close();
    }
    public void commit() throws IOException {
        writer.commit();
    }

    private Document createDocument(String fpath) throws IOException {
        Path path = Paths.get(fpath);
        File file = path.toFile();
        Document document = new Document();

        FileReader fileReader = new FileReader(file);
        document.add(new TextField("contents", fileReader));
        document.add(new StringField("path", file.getPath(), Field.Store.YES));
        document.add(new StringField("filename", file.getName(), Field.Store.YES));

        return document;
    }
}
