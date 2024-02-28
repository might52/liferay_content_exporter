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
import org.might.exporter.api.ContentElementCreator;
import org.might.exporter.api.Uploader;
import org.might.exporter.api.XMLSerializer;
import org.might.exporter.model.Root;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.ofNullable;


@Component(
        immediate = true,
        service = {ExportImportLifecycleListener.class}
)
public class ExportImportProcessListener implements ProcessAwareExportImportLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportProcessListener.class);
    private static final String SUCCESS_IMPORT_LAR = "Lar successfully {}. GroupId from event: {}, from company: {}";
    private static final String GLOBAL_SUCCESS_IMPORT_LAR = "Global " + SUCCESS_IMPORT_LAR;
    private static final String START_EXPORT = "Start export process";
    private static final String STOP_EXPORT = "Stop export process";
    private static final String SKIP_EXPORT = "Skip export process based on the build installation";
    private static final String MAP_FIELD = "cmd";
    private static final String IMPORT_TYPE_CMD = "[import]";
    private static final String IMPORT_TYPE = "imported";
    private static final String EXPORT_TYPE = "exported";

    @Reference
    private Uploader fileUploader;

    @Reference
    private ContentElementCreator contentElementCreator;

    @Reference
    private XMLSerializer xmlSerializer;

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
                    CompletableFuture.runAsync(() -> {
                        LOGGER.info(START_EXPORT);
                        Root rootContent = contentElementCreator.getRootContent(company);
                        ByteArrayOutputStream stream = null;
                        if (Objects.nonNull(rootContent)) {
                            stream = xmlSerializer.getXmlStream(rootContent);
                        }

                        fileUploader.uploadFile(stream);
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (Exception ignored) {
                        }
                        LOGGER.info(STOP_EXPORT);
                    });
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
