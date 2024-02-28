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
public class ContentField implements Serializable {
    private ContentFieldValue contentFieldValue;
    @XmlElementWrapper(name = "nestedContentFields")
    private ContentField[] nestedContentFields;
    private String dataType;
    private String inputControl;
    private String label;
    private String name;
    private String repeatable;

    public ContentField() {
    }

    public ContentFieldValue getContentFieldValue() {
        return contentFieldValue;
    }

    public void setContentFieldValue(ContentFieldValue contentFieldValue) {
        this.contentFieldValue = contentFieldValue;
    }

    public ContentField[] getNestedContentFields() {
        return nestedContentFields;
    }

    public void setNestedContentFields(ContentField[] nestedContentFields) {
        this.nestedContentFields = nestedContentFields;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getInputControl() {
        return inputControl;
    }

    public void setInputControl(String inputControl) {
        this.inputControl = inputControl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepeatable() {
        return repeatable;
    }

    public void setRepeatable(String repeatable) {
        this.repeatable = repeatable;
    }
}