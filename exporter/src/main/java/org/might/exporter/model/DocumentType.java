package org.might.exporter.model;

import java.io.Serializable;

/**
 * Date: 15.02.2021
 * Time: 13:58
 */
public class DocumentType implements Serializable {
    private String name;
    private String description;

    public DocumentType() {
    }

    public DocumentType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
