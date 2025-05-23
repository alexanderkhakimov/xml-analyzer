package com.example;

import java.io.IOException;
import java.util.List;

public interface TagReader {
    List<String> readTags(String tagsPath) throws IOException;
}
