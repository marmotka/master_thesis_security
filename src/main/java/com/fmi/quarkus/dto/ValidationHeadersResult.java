package com.fmi.quarkus.dto;

import java.io.BufferedReader;
import java.util.List;

public record ValidationHeadersResult(BufferedReader reader, List<String> headers) implements AutoCloseable{
    @Override
    public void close() throws Exception {
        reader.close();
    }
}
