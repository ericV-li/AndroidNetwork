package com.eric.core.core;

import java.io.File;

/**
 * @author li
 * @Package com.eric.core.core
 * @Title: FileEntity
 * @Description: Copyright (c)
 * Create DateTime: 2017/10/25
 * 文件实体
 */
public class FileEntity {
    private String name;

    private String fileName;

    private File file;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
