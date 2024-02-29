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
package org.might.exporter;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lifecycle.ExportImportLifecycleEvent;
import com.liferay.exportimport.kernel.lifecycle.ExportImportLifecycleListener;
import com.liferay.exportimport.kernel.lifecycle.ProcessAwareExportImportLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.might.exporter.api.ContentExporter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

@Component(
        immediate = true,
        service = {ExportImportLifecycleListener.class}
)
public class ExportImportProcessListener implements ProcessAwareExportImportLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportProcessListener.class);
    private static final String SUCCESS_IMPORT_LAR = "Lar successfully {}. GroupId from event: {}, from company: {}";
    private static final String GLOBAL_SUCCESS_IMPORT_LAR = "Global " + SUCCESS_IMPORT_LAR;
    private static final String MAP_FIELD = "cmd";
    private static final String IMPORT_TYPE_CMD = "[import]";
    private static final String IMPORT_TYPE = "imported";
    private static final String EXPORT_TYPE = "exported";

    @Reference
    private ContentExporter contentExporter;

    public ExportImportProcessListener() {
    }

    @Override
    public void onProcessFailed(ExportImportLifecycleEvent exportImportLifecycleEvent) throws Exception {
    }

    @Override
    public void onProcessStarted(ExportImportLifecycleEvent exportImportLifecycleEvent) throws Exception {
    }

    @Override
    public void onProcessSucceeded(ExportImportLifecycleEvent exportImportLifecycleEvent) throws Exception {
        Company company = CompanyLocalServiceUtil.getCompanyByMx(PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID));
        long companyGroupId = company.getGroup().getGroupId();
        ofNullable(exportImportLifecycleEvent.getAttributes())
                .filter(CollectionUtils::isNotEmpty)
                .map(collection -> collection.iterator().next())
                .map(context -> (PortletDataContext) context)
                .ifPresent(context -> {
                    long eventGroupId = context.getGroupId();
                    boolean isImport = isImportType(context);
                    if (!isImport || companyGroupId != eventGroupId) {
                        LOGGER.info(
                                SUCCESS_IMPORT_LAR,
                                isImport ? IMPORT_TYPE : EXPORT_TYPE,
                                eventGroupId,
                                companyGroupId);
                        return;
                    }
                    LOGGER.info(
                            GLOBAL_SUCCESS_IMPORT_LAR,
                            isImport ? IMPORT_TYPE : EXPORT_TYPE,
                            eventGroupId,
                            companyGroupId);
                    contentExporter.export(company);
                });
    }

    private boolean isImportType(PortletDataContext context) {
        return ofNullable(context.getParameterMap())
                .map(map -> map.get(MAP_FIELD))
                .map(Arrays::toString)
                .map(val -> StringUtil.equals(val, IMPORT_TYPE_CMD))
                .orElse(false);
    }

    @Override
    public boolean isParallel() {
        return false;
    }

}
