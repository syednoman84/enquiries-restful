package com.latidude99.util;

import org.springframework.core.io.ByteArrayResource;

public class FileUploadResource  extends ByteArrayResource {

    private final String filename;

        public FileUploadResource(final byte[] byteArray, final String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }
}