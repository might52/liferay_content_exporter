package org.might.exporter.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;

/**
 * Date: 15.02.2021
 * Time: 13:53
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Content implements Serializable {
    @XmlElementWrapper(name = "contentFields")
    private ContentField[] contentFields;
    private String dateCreated;
    private String dateModified;
    private String datePublished;
    private Long contentStructureId;
    private Creator creator;
    private String friendlyUrlPath;
    @XmlElementWrapper(name = "keywords")
    private String[] keywords;
    private Long id;
    private String key;
    private int numberOfComments;
    private Long siteId;
    private String subscribed;
    private String title;
    private String uuid;

    public Content() {
    }

    public ContentField[] getContentFields() {
        return contentFields;
    }

    public void setContentFields(ContentField[] contentFields) {
        this.contentFields = contentFields;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public Long getContentStructureId() {
        return contentStructureId;
    }

    public void setContentStructureId(Long contentStructureId) {
        this.contentStructureId = contentStructureId;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public String getFriendlyUrlPath() {
        return friendlyUrlPath;
    }

    public void setFriendlyUrlPath(String friendlyUrlPath) {
        this.friendlyUrlPath = friendlyUrlPath;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(String subscribed) {
        this.subscribed = subscribed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}