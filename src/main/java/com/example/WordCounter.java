package com.example;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

public interface WordCounter {
    int[] countWords(Node node, boolean isTemporaryRevision, List<String> tagsToProcess, Map<String, Integer> wordCountMap);
}
