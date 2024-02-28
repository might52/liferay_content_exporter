package org.might.exporter.api;

import com.liferay.portal.kernel.model.Company;
import org.might.exporter.model.Root;

public interface ContentElementCreator {
    Root getRootContent(Company company);
}
