package org.might.exporter.api;

import com.liferay.headless.delivery.dto.v1_0.Document;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;

import java.util.Collection;

public interface ContentLoader {
    Collection<Document> getDocuments(String locale) throws Exception;

    Collection<StructuredContent> getStructuredContents(String locale) throws Exception;

    void setUpSearchConfiguration(Company company, int pageSize) throws PortalException;
}
