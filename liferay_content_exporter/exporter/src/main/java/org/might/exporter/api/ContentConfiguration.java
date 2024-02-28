package org.might.exporter.api;

public interface ContentConfiguration {

    /**
     * Folder where generated file will be uploaded.
     *
     * @return String representation of the path.
     */
    String getExportFolder();

    /**
     * List of the keywords for searching at the libraries splitted by comma.
     */
    String getFilterKeywords();

}