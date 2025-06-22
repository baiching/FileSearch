package com.baiching.filesearch.tests;

import com.baiching.filesearch.search.LuceneIndexManager;
import com.baiching.filesearch.utils.DBOperations;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LuceneIndexManagerTest {

    @TempDir
    Path tempDir;
    private LuceneIndexManager indexManager;

    @BeforeEach
    void setUp() throws IOException {
        System.out.println(tempDir.toString());
        indexManager = new LuceneIndexManager(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (indexManager != null) {
            indexManager.close();
        }
    }

    @Test
    void createIndex_shouldCreateNewIndexWithDocuments(@TempDir Path testDir) throws IOException {
        // 1. Create dedicated index directory inside temp test space
        Path indexPath = testDir.resolve("lucene-index");
        Files.createDirectories(indexPath);

        DBOperations dbOps = new DBOperations();
        List<String> paths = dbOps.getAllPaths();

        // 2. Initialize index manager with test directory
        indexManager = new LuceneIndexManager(indexPath.toString());

        // Assert
        try (Directory directory = FSDirectory.open(tempDir);
             IndexReader reader = DirectoryReader.open(directory)) {
            StoredFields storedFields = reader.storedFields(); // Modern Lucene API
            assertEquals(2, reader.numDocs());

            Document doc1 = storedFields.document(0);
            Document doc2 = storedFields.document(1);

            assertAll("Document fields",
                    () -> assertEquals(paths.get(0), doc1.get("path")),
                    () -> assertEquals(paths.get(1), doc2.get("path"))
            );
        }

    }

    @Test
    void createIndex_shouldClearExistingIndex() throws IOException {
        DBOperations dbOps = new DBOperations();
        // Arrange - Add initial docs
        indexManager.addDocument(dbOps.getAllPaths());
        indexManager.commit();

        List<String> newPaths = List.of("src/main/resources/test/file1.txt", "src/main/resources/test/file2.txt");

        // Act
        indexManager.createIndex(newPaths);

        // Assert
        try (Directory directory = FSDirectory.open(tempDir);
             IndexReader reader = DirectoryReader.open(directory)) {
            StoredFields storedFields = reader.storedFields();
            assertEquals(2, reader.numDocs());
            assertEquals(newPaths.get(0), storedFields.document(0).get("path"));
            assertEquals(newPaths.get(1), storedFields.document(1).get("path"));
        }
    }


    @Test
    void createIndex_shouldHandleEmptyList() throws IOException {
        // Arrange
        List<String> paths = List.of();

        // Act
        indexManager.createIndex(paths);

        // Assert
        try (Directory directory = FSDirectory.open(tempDir);
             IndexReader reader = DirectoryReader.open(directory)) {
            assertEquals(0, reader.numDocs(), "Index should be empty");
        }
    }

    @Test
    void createIndex_shouldCommitChanges() throws IOException {
        // Arrange
        List<String> paths = List.of("src/main/resources/test/file1.txt");

        // Act
        indexManager.createIndex(paths);

        // Assert - If commit didn't happen, reader wouldn't see docs
        try (Directory directory = FSDirectory.open(tempDir);
             IndexReader reader = DirectoryReader.open(directory)) {
            assertEquals(1, reader.numDocs());
        }
    }

    @Test
    void createIndex_shouldCallDeleteAll() throws IOException {
        // Arrange - Mock IndexWriter
        try (MockedConstruction<IndexWriter> mockedWriter = Mockito.mockConstruction(IndexWriter.class,
                (mock, context) -> {
                    // Configure mock behavior
                    when(mock.deleteAll()).thenReturn(123L);
                })) {

            // Recreate manager with mock dependencies
            LuceneIndexManager manager = new LuceneIndexManager(tempDir.toString()) {
                //@Override
                protected Document createDocument(String path) {
                    return new Document(); // Simplified doc
                }
            };

            // Act
            manager.createIndex(List.of("src/main/resources/test/"));

            // Assert
            IndexWriter writer = mockedWriter.constructed().get(0);
            verify(writer, times(1)).deleteAll();
            verify(writer, atLeastOnce()).addDocument(any(Document.class));
            verify(writer, times(1)).commit();
        }
    }

    @Test
    void createIndex_shouldHandleIOException() throws IOException {
        // Arrange - Inject faulty IndexWriter
        try (MockedConstruction<IndexWriter> ignored = Mockito.mockConstruction(IndexWriter.class,
                (mock, context) -> {
                    doThrow(new IOException("Test exception")).when(mock).deleteAll();
                })) {

            LuceneIndexManager manager = new LuceneIndexManager(tempDir.toString());

            // Act & Assert
            IOException exception = assertThrows(IOException.class,
                    () -> manager.createIndex(List.of("src/main/resources/test/")));

            assertEquals("Test exception", exception.getMessage());
        }
    }
}
