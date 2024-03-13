package org.might.exporter.menu;

import java.util.Map;

public class PortletData {
    private String portletName;
    private Map<String, Object> templateParametersMap;
    private String baseUrl;

    public PortletData(String portletName, Map<String, Object> templateParametersMap, String baseUrl) {
        this.portletName = portletName;
        this.templateParametersMap = templateParametersMap;
        this.baseUrl = baseUrl;
    }

    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    public Map<String, Object> getTemplateParametersMap() {
        return templateParametersMap;
    }

    public void setTemplateParametersMap(Map<String, Object> templateParametersMap) {
        this.templateParametersMap = templateParametersMap;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
