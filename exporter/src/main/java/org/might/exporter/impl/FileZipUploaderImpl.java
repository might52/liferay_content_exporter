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
package org.might.exporter.impl;

import com.liferay.portal.kernel.util.PropsUtil;
import org.might.exporter.api.ContentConfiguration;
import org.might.exporter.api.Uploader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Optional.ofNullable;

@Component(immediate = true, service = Uploader.class)
public class FileZipUploaderImpl implements Uploader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileZipUploaderImpl.class);
    private static final String NOTHING_TO_UPLOAD = "Content is empty. There are nothing to upload";
    private static final String START_UPLOAD_FILE = "Start preparation and upload marketing file to folder: {}";
    private static final String STOP_UPLOAD_FILE = "Finish preparation and upload marketing file to folder: {}";
    private static final String TEMP_FILE_CREATED = "Temp file: {} created at temp directory: {}";
    private static final String ZIP_ARCHIVE_CREATED = "Zip archive: {} successfully made at temp directory: {}";
    private static final String ZIP_ARCHIVE_MOVED = "Zip archive: {} moved to directory: {}";
    private static final String ZIP_ARCHIVE_REMOVE_DOT = "Zip archive renamed, removed dot: {}, folder: {}";
    private static final String NAME_TIME_FORMAT = "YYYY-MM-dd'T'HH-mm-ss'Z'";
    private static final String FILE_SUFFIX_NAME = "content.xml";
    private static final String ARCHIVE_SUFFIX_NAME = "LiferayContent.zip";
    private static final String LIFERAY_HOME = PropsUtil.get("liferay.home");
    private static final String FOLDER_NOT_AVAILABLE = "Folder: {} doesn't exist or not available for user. " +
            "Will be used default, based on the liferay.home: {} directory.";

    @Reference
    private ContentConfiguration contentConfiguration;

    /**
     * Perform upload file by String source.
     *
     * @param content {@link String}
     */
    @Override
    public void uploadFile(String content) {
        uploadFile(content.getBytes());
    }

    @Override
    public void uploadFile(ByteArrayOutputStream content) {
        uploadFile(ofNullable(content).map(ByteArrayOutputStream::toByteArray).orElse(new byte[0]));
    }

    /**
     * Perform upload file by byte array source.
     *
     * @param content {@link byte[]}
     */
    @Override
    public void uploadFile(byte[] content) {
        final String exportFolder = getExportFolder();
        if (content.length < 1) {
            LOGGER.error(NOTHING_TO_UPLOAD);
            return;
        }

        LOGGER.info(START_UPLOAD_FILE, exportFolder);
        try {
            String prefixName = getPrefixName();
            Path tempFile = getTempFilePath(prefixName);
            LOGGER.info(TEMP_FILE_CREATED, tempFile.getFileName(), tempFile.getParent().getFileName());
            ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempFile));
            zipOutputStream.putNextEntry(new ZipEntry(getFileName(prefixName, FILE_SUFFIX_NAME)));
            zipOutputStream.write(content, 0, content.length);
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            LOGGER.info(ZIP_ARCHIVE_CREATED, tempFile.getFileName(), tempFile.getParent().getFileName());
            tempFile = Files.move(tempFile, Paths.get(exportFolder,
                    getFileNameWithDot(prefixName, ARCHIVE_SUFFIX_NAME)));
            LOGGER.info(ZIP_ARCHIVE_MOVED, tempFile.getFileName(),
                    Paths.get(exportFolder).getFileName());
            tempFile = Files.move(tempFile, Paths.get(exportFolder,
                    getFileName(prefixName, ARCHIVE_SUFFIX_NAME)));
            LOGGER.info(ZIP_ARCHIVE_REMOVE_DOT, tempFile.getFileName(),
                    Paths.get(exportFolder).getFileName());
        } catch (UnsupportedOperationException | IOException ex) {
            LOGGER.error("During uploading file something went wrong.", ex);
        }
        LOGGER.info(STOP_UPLOAD_FILE, exportFolder);
    }

    /**
     * Return temp file path.
     *
     * @param prefixName prefix file name.
     * @return {@link Path} representation of the temp file.
     * @throws IOException, UnsupportedOperationException
     */
    private Path getTempFilePath(String prefixName) throws IOException, UnsupportedOperationException {
        return System.getProperty("os.name").toLowerCase().contains("win") ?
                Files.createTempFile(getFileName(
                                prefixName,
                                ARCHIVE_SUFFIX_NAME),
                        null) :
                Files.createTempFile(getFileName(
                                prefixName,
                                ARCHIVE_SUFFIX_NAME),
                        null, PosixFilePermissions.asFileAttribute(EnumSet.of(
                                OWNER_READ, OWNER_WRITE,
                                GROUP_READ, GROUP_WRITE,
                                OTHERS_READ)));
    }

    /**
     * Return the folder for export content.
     *
     * @return {@link String} representation of the path to the folder.
     */
    private String getExportFolder() {
        if (!isFolderExist(contentConfiguration.getExportFolder())) {
            LOGGER.error(FOLDER_NOT_AVAILABLE, contentConfiguration.getExportFolder(), LIFERAY_HOME);
            return LIFERAY_HOME;
        }
        return contentConfiguration.getExportFolder();
    }

    /**
     * Get combined file name with lead dot.
     *
     * @param prefixName prefix time name.
     * @param suffixName suffix name.
     * @return {@link String}
     */
    private String getFileNameWithDot(String prefixName, String suffixName) {
        return String.format(".%s", getFileName(prefixName, suffixName));
    }

    /**
     * Get combined file name.
     *
     * @param prefixName prefix time name.
     * @param suffixName suffix name.
     * @return {@link String}
     */
    private String getFileName(String prefixName, String suffixName) {
        return String.format("%s_%s", prefixName, suffixName);
    }

    /**
     * Prepare the time prefix file name based on mask.
     *
     * @return {@link String}
     */
    private String getPrefixName() {
        Calendar calendar = GregorianCalendar.getInstance();
        DateFormat formatter = new SimpleDateFormat(NAME_TIME_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(calendar.getTime());
    }

    /**
     * Perform validation that folder exists.
     *
     * @param folderPath {@link String} path of the checked folder.
     * @return TRUE if folder exists another FALSE.
     */
    private boolean isFolderExist(String folderPath) {
        Path checkDir = Paths.get(folderPath);
        return Files.exists(checkDir) && Files.isDirectory(checkDir);
    }
}
