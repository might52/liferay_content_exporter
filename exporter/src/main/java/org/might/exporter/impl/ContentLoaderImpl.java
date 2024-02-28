package org.might.exporter.impl;

import com.liferay.headless.delivery.dto.v1_0.Document;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.headless.delivery.resource.v1_0.DocumentResource;
import com.liferay.headless.delivery.resource.v1_0.StructuredContentResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.might.exporter.CommonConstant;
import org.might.exporter.api.ContentConfiguration;
import org.might.exporter.api.ContentLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;

@Component(immediate = true, service = {ContentLoader.class})
public class ContentLoaderImpl implements ContentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentLoaderImpl.class);
    private static final String START_LOADING = "Start loading %s from LF for location: {}";
    private static final String COMPLETED_LOADING = "Loading %s from LF for location: {} completed";
    private static final String REQUESTED_PAGE = "Requested page: {}, last page: {}, items amount: {}, totalAmount: {}  pageSize: {}.";

    private BooleanFilter filter;
    private long siteId;
    private int pageSize;

    @Reference
    private DocumentResource documentResource;
    @Reference
    private StructuredContentResource structuredContentResource;
    private final BiFunction<Long, Class<?>, Page<?>> getContent = (page, type) -> {
        try {
            return (type.equals(Document.class)) ? documentResource.getSiteDocumentsPage(
                    siteId, true, null, null,
                    filter, Pagination.of(page.intValue(), pageSize), null) :
                    structuredContentResource.getSiteStructuredContentsPage(
                            siteId, true, null, null,
                            filter, Pagination.of(page.intValue(), pageSize), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
    @Reference
    private ContentConfiguration contentConfiguration;

    @Override
    public Collection<Document> getDocuments(String locale) {
        setSearchLocale(locale);
        return getContentForLocaleByType(locale, Document.class);
    }

    @Override
    public Collection<StructuredContent> getStructuredContents(String locale) {
        setSearchLocale(locale);
        return getContentForLocaleByType(locale, StructuredContent.class);
    }

    public void setUpSearchConfiguration(Company company, int pageSize) throws PortalException {
        this.siteId = company.getGroup().getGroupId();
        this.pageSize = pageSize;
        User user = UserLocalServiceUtil.getUserByEmailAddress(company.getCompanyId(),
                PrefsPropsUtil.getString(PropsKeys.ADMIN_EMAIL_FROM_ADDRESS)
        );
        PrincipalThreadLocal.setName(user.getUserId());
        PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(user);
        PermissionThreadLocal.setPermissionChecker(permissionChecker);
        documentResource.setContextCompany(company);
        documentResource.setContextUser(user);
        structuredContentResource.setContextCompany(company);
        structuredContentResource.setContextUser(user);
        fillFilter(contentConfiguration.getFilterKeywords());
    }

    private void setSearchLocale(String locale) {
        documentResource.setContextAcceptLanguage(new AcceptLanguageImpl(locale));
        structuredContentResource.setContextAcceptLanguage(new AcceptLanguageImpl(locale));
    }

    private void fillFilter(String keywords) {
        filter = null;
        if (StringUtils.isNotEmpty(keywords)) {
            filter = new BooleanFilter();
            String[] words = keywords.split(",");
            for (String word : words) {
                filter.addTerm(CommonConstant.KEYWORD, word, BooleanClauseOccur.SHOULD);
            }
        }
    }

    private <T> Collection<T> getContentForLocaleByType(String locale, Class<T> type) {
        LOGGER.info(String.format(START_LOADING, type.getSimpleName()), locale);
        Page<T> page = getContentPageByType(1L, type);
        Collection<T> elements = new ArrayList<>((int) page.getTotalCount());
        elements.addAll(page.getItems());
        for (long pageCount = 2L; pageCount <= page.getLastPage(); pageCount++) {
            elements.addAll(getContentPageByType(pageCount, type).getItems());
        }
        LOGGER.info(String.format(COMPLETED_LOADING, type.getSimpleName()), locale);
        return elements;
    }

    private <T> Page<T> getContentPageByType(long page, Class<T> type) {
        Page<T> pageContent = (Page<T>) getContent.apply(page, type);
        logRequest(pageContent);
        return pageContent;
    }

    private void logRequest(Page<?> pageContent) {
        LOGGER.info(REQUESTED_PAGE,
                pageContent.getPage(),
                pageContent.getLastPage(),
                pageContent.getItems().size(),
                pageContent.getTotalCount(),
                pageSize
        );
    }

}
