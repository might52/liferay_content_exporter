package org.might.exporter.api;

import java.io.ByteArrayOutputStream;

public interface Uploader {
    void uploadFile(byte[] content);

    void uploadFile(String content);

    void uploadFile(ByteArrayOutputStream content);
}
