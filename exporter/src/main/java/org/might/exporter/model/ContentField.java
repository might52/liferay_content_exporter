/*
 * MIT License
 *
 * Copyright (c) 2024 Andrei F._
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.might.exporter.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;

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