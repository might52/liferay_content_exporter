package org.might.exporter.model;

import java.io.Serializable;

public class ContentFieldValue implements Serializable {
    private String data;
    private ImageDocument image;
    private ImageDocument document;
    private Geo geo;

    public ContentFieldValue() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ImageDocument getImage() {
        return image;
    }

    public void setImage(ImageDocument image) {
        this.image = image;
    }

    public ImageDocument getDocument() {
        return document;
    }

    public void setDocument(ImageDocument document) {
        this.document = document;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }
}
