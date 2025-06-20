package com.baiching.filesearch.service;

import com.baiching.filesearch.search.LuceneIndexManager;
import com.baiching.filesearch.utils.DBOperations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IndexSyncService {
    private final DBOperations dbOperations;
    private final LuceneIndexManager indexManager;
    private Set<String> lastPathSet = new HashSet<>();

    public IndexSyncService(DBOperations dbOperations, LuceneIndexManager indexManager) {
        this.dbOperations = dbOperations;
        this.indexManager = indexManager;
    }

    public void initialSync() throws IOException {
        List<String> paths = dbOperations.getAllPaths();
        lastPathSet = new HashSet<>(paths);
        indexManager.createIndex(paths);

    }

    public void incrementalSync() {
        try {
            List<String> currentPaths = dbOperations.getAllPaths();
            Set<String> currentPathSet = new HashSet<>(currentPaths);

            // Find new paths
            Set<String> added = new HashSet<>(currentPathSet);
            added.removeAll(lastPathSet);

            Set<String> removed = new HashSet<>(lastPathSet);
            removed.removeAll(currentPathSet);

            // Update index
            if (!removed.isEmpty()) {
                indexManager.deleteDocuments(new ArrayList<>(removed));
            }
            if (!added.isEmpty()) {
                indexManager.addDocument(new ArrayList<>(added));
            }
            indexManager.commit();

            lastPathSet = currentPathSet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void startPeriodicSync(long interval, TimeUnit unit) {
        try {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::incrementalSync, interval, interval, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
