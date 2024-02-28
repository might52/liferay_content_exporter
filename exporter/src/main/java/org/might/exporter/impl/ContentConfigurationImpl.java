package org.might.exporter.impl;

import com.liferay.portal.kernel.util.PrefsPropsUtil;
import org.might.exporter.api.ContentConfiguration;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class ContentConfigurationImpl implements ContentConfiguration {

    @Override
    public String getExportFolder() {
        return PrefsPropsUtil.getString("");
    }

    @Override
    public String getFilterKeywords() {
        return PrefsPropsUtil.getString("");
    }
}