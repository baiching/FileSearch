package com.baiching.filesearch.tests;

import com.baiching.filesearch.search.LuceneIndexManager;
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
        indexManager = new LuceneIndexManager(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (indexManager != null) {
            indexManager.close();
        }
    }

    @Test
    void createIndex_shouldCreateNewIndexWithDocuments() throws IOException {
        // Arrange
        List<String> paths = Arrays.asList(
                "/path/to/file1.txt",
                "/path/to/file2.jpg"
        );

        // Act
        indexManager.createIndex(paths);

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
        // Arrange - Add initial docs
        indexManager.addDocument(List.of("/existing/file.txt"));
        indexManager.commit();

        List<String> newPaths = List.of("/new/file1.txt", "/new/file2.txt");

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
        List<String> paths = List.of("/path/to/file.txt");

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
            manager.createIndex(List.of("/test/path"));

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
                    () -> manager.createIndex(List.of("/failing/path")));

            assertEquals("Test exception", exception.getMessage());
        }
    }
}
