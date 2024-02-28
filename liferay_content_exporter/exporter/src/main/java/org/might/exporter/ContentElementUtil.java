package org.might.exporter;

import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.headless.delivery.dto.v1_0.ContentDocument;
import com.liferay.headless.delivery.dto.v1_0.Document;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import org.apache.commons.io.IOUtils;
import org.might.exporter.model.Content;
import org.might.exporter.model.ContentField;
import org.might.exporter.model.ContentFieldValue;
import org.might.exporter.model.Creator;
import org.might.exporter.model.DocumentType;
import org.might.exporter.model.Documents;
import org.might.exporter.model.Geo;
import org.might.exporter.model.ImageDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

public class ContentElementUtil {
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_FORMAT_PATTERN));
    private static final String COULD_NOT_LOAD_FILE = "Couldn't load file: {}. Base64 content skipped at output.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentElementUtil.class);

    public static Documents getDocument(Document documentDTO) throws PortalException, IOException {
        Documents document = new Documents();
        document.setContentUrl(documentDTO.getContentUrl());
        try {
            FileEntry fileEntry = DLAppServiceUtil.getFileEntry(documentDTO.getId());
            try (InputStream finput = fileEntry.getContentStream()) {
                byte[] imageBytes = IOUtils.toByteArray(finput);
                String base64content = Base64.getEncoder().encodeToString(imageBytes);
                document.setContentValue(base64content);
            }
        } catch (NoSuchFileException ex) {
            LOGGER.error(COULD_NOT_LOAD_FILE, documentDTO, ex);
        }
        document.setDateCreated(DATE_FORMATTER.get().format(documentDTO.getDateCreated()));
        document.setDateModified(DATE_FORMATTER.get().format(documentDTO.getDateModified()));
        ofNullable(documentDTO.getDocumentType()).ifPresent(documentType -> {
            document.setDocumentType(new DocumentType(documentType.getName(), documentType.getDescription()));
        });
        document.setDocumentFolderId(documentDTO.getDocumentFolderId());
        document.setEncodingFormat(documentDTO.getEncodingFormat());
        document.setFileExtension(documentDTO.getFileExtension());
        document.setKeywords(documentDTO.getKeywords());
        document.setId(documentDTO.getId());
        document.setNumberOfComments(documentDTO.getNumberOfComments());
        document.setSizeInBytes(documentDTO.getSizeInBytes());
        document.setTitle(documentDTO.getTitle());
        ofNullable(documentDTO.getCreator()).ifPresent(creator ->
                document.setCreator(fillCreator(creator))
        );
        return document;
    }

    public static Content getContent(StructuredContent contentDTO) {
        Content content = new Content();
        content.setDateCreated(DATE_FORMATTER.get().format(contentDTO.getDateCreated()));
        content.setDateModified(DATE_FORMATTER.get().format(contentDTO.getDateModified()));
        content.setDatePublished(DATE_FORMATTER.get().format(contentDTO.getDatePublished()));
        content.setContentStructureId(contentDTO.getContentStructureId());
        content.setCreator(fillCreator(contentDTO.getCreator()));
        content.setFriendlyUrlPath(contentDTO.getFriendlyUrlPath());
        content.setKeywords(contentDTO.getKeywords());
        content.setId(contentDTO.getId());
        content.setKey(contentDTO.getKey());
        content.setNumberOfComments(contentDTO.getNumberOfComments());
        content.setSiteId(contentDTO.getSiteId());
        content.setSubscribed(contentDTO.getSubscribed().toString());
        content.setTitle(contentDTO.getTitle());
        content.setUuid(contentDTO.getUuid());
        content.setContentFields(fillContentFields(contentDTO.getContentFields()));
        return content;
    }

    private static ContentField[] fillContentFields(com.liferay.headless.delivery.dto.v1_0.ContentField[] contentFieldsDTO) {
        if (contentFieldsDTO == null || contentFieldsDTO.length == 0) {
            return null;
        }
        List<ContentField> result = new ArrayList<>();
        for (com.liferay.headless.delivery.dto.v1_0.ContentField contentFieldElementDTO : contentFieldsDTO) {
            ContentField resultElem = new ContentField();
            if (Objects.isNull(contentFieldElementDTO.getNestedContentFields()) || contentFieldElementDTO.getNestedContentFields().length > 0) {
                resultElem.setNestedContentFields(fillContentFields(contentFieldElementDTO.getNestedContentFields()));
            }
            resultElem.setDataType(contentFieldElementDTO.getDataType());
            resultElem.setInputControl(contentFieldElementDTO.getInputControl());
            resultElem.setLabel(contentFieldElementDTO.getLabel());
            resultElem.setName(contentFieldElementDTO.getName());
            resultElem.setRepeatable(contentFieldElementDTO.getRepeatable().toString());
            resultElem.setContentFieldValue(fillContentFieldValue(contentFieldElementDTO.getContentFieldValue()));
            result.add(resultElem);
        }
        return result.toArray(new ContentField[0]);
    }

    private static ContentFieldValue fillContentFieldValue(com.liferay.headless.delivery.dto.v1_0.ContentFieldValue contentFieldValuesDTO) {
        ContentFieldValue contentFieldValue = new ContentFieldValue();
        ofNullable(contentFieldValuesDTO).ifPresent(dto -> {
            ofNullable(dto.getData()).ifPresent(contentFieldValue::setData);
            ofNullable(dto.getGeo()).ifPresent(geo ->
                    contentFieldValue.setGeo(new Geo(geo.getLatitude(), geo.getLongitude()))
            );
            ofNullable(dto.getImage()).ifPresent(image ->
                    contentFieldValue.setImage(fillImageDocument(image))
            );
            ofNullable(dto.getDocument()).ifPresent(documentImage ->
                    contentFieldValue.setDocument(fillImageDocument(documentImage))
            );
        });
        return contentFieldValue;
    }

    private static ImageDocument fillImageDocument(ContentDocument contentDocumentDTO) {
        ImageDocument result = new ImageDocument();
        result.setContentType(contentDocumentDTO.getContentType());
        result.setContentUrl(contentDocumentDTO.getContentUrl());
        result.setEncodingFormat(contentDocumentDTO.getEncodingFormat());
        result.setFileExtension(contentDocumentDTO.getFileExtension());
        result.setId(contentDocumentDTO.getId());
        result.setSizeInBytes(contentDocumentDTO.getSizeInBytes());
        result.setTitle(contentDocumentDTO.getTitle());
        return result;
    }

    private static Creator fillCreator(com.liferay.headless.delivery.dto.v1_0.Creator creatorDTO) {
        Creator creator = new Creator();
        creator.setAdditionalName(creatorDTO.getAdditionalName());
        creator.setContentType(creatorDTO.getContentType());
        creator.setFamilyName(creatorDTO.getFamilyName());
        creator.setGivenName(creatorDTO.getGivenName());
        creator.setId(creatorDTO.getId());
        creator.setImage(creatorDTO.getImage());
        creator.setName(creatorDTO.getName());
        creator.setProfileURL(creatorDTO.getProfileURL());
        return creator;
    }

}
