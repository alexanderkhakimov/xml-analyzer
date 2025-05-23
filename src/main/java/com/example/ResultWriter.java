package com.example;

import java.io.IOException;
import java.util.Map;

public interface ResultWriter {
    void writeResults(Map<String, Integer> totalWordCounts, Map<String, Integer> changeMarkWordCounts, Map<String, Map<String, Integer>> wordStats, String outputPath) throws IOException;
}
