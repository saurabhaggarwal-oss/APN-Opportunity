package com.ttn.ck.apn.utils;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;

/**
 * @author Amit Raturi
 * OctetStreamUtils convert file to octet stream and add this stream to ResponseEntity, which then can be used by clients to download the resource.
 */
public final class OctetStreamUtils {
    private OctetStreamUtils() {
    }

    /**
     * This method convert file resource to InputStreamResource , which then can be use by client to download the stream. this method add all essential headers along with InputStreamResource.
     *
     * @param file            java File object.
     * @param fileName        name of the file , It's a non-mandatory field if not provided then it picks file name.
     * @param responseBuilder ResponseEntity builder.
     * @return ResponseEntity with InputStreamResource as body and other essential response headers.
     * @throws IOException throws IoExceptions , in case there is some issue while reading or converting file to stream.
     */
    public static ResponseEntity<InputStreamResource> download(File file, String fileName, ResponseEntity.BodyBuilder responseBuilder) throws IOException {
        if (Objects.nonNull(file) && file.exists()) {
            InputStream fileInputStream = new AutoClosableInputStream(file);
            InputStreamResource resource = new InputStreamResource(fileInputStream);
            return addHeaders(responseBuilder, getFileName(file, fileName.replace(" ", "_")), file).body(resource);
        } else {
            responseBuilder.contentLength(0);
        }
        return responseBuilder.build();
    }

    /**
     * This method convert file resource to InputStreamResource , which then can be use by client to download the stream and delete file from folder. this method add all essential headers along with InputStreamResource.
     *
     * @param file            java File object.
     * @param fileName        name of the file , It's a non-mandatory field if not provided then it picks file name.
     * @param responseBuilder ResponseEntity builder.
     * @return ResponseEntity with InputStreamResource as body and other essential response headers.
     * @throws IOException throws IoExceptions , in case there is some issue while reading or converting file to stream.
     */
    public static ResponseEntity<InputStreamResource> downloadAndRemoveFile(File file, String fileName, ResponseEntity.BodyBuilder responseBuilder) throws IOException {
        ResponseEntity<InputStreamResource> response = download(file, fileName, responseBuilder);
        Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
        return response;
    }

    public static ResponseEntity<InputStreamResource> download(String fileName, ResponseEntity.BodyBuilder responseBuilder, InputStream stream) {
        InputStreamResource resource = new InputStreamResource(new AutoClosableInputStream(stream));
        return addHeaders(responseBuilder, fileName).body(resource);
    }

    /**
     * This method convert file resource to InputStreamResource , which then can be use by client to download the stream. this method add all essential headers along with InputStreamResource.
     *
     * @param file            java File object.
     * @param responseBuilder ResponseEntity builder.
     * @return ResponseEntity with InputStreamResource as body and other essential response headers.
     * @throws IOException throws IoExceptions , in case there is some issue while reading or converting file to stream.
     */
    public static ResponseEntity<InputStreamResource> download(File file, ResponseEntity.BodyBuilder responseBuilder) throws IOException {
        return download(file, null, responseBuilder);
    }

    private static String getFileName(File file, String fileName) {

        return StringUtils.hasText(fileName) ? fileName : file.getName();
    }

    private static ResponseEntity.BodyBuilder addHeaders(ResponseEntity.BodyBuilder response, String fileName, File file) {
        return response.contentType(MediaType.APPLICATION_OCTET_STREAM).
                header(Constant.FILE_NAME, fileName).
                contentLength(file.length()).
                header(ACCESS_CONTROL_EXPOSE_HEADERS, Constant.FILE_NAME).
                header(HttpHeaders.CONTENT_DISPOSITION, String.format(
                        Constant.CONTENT_DISPOSITION_KEY,
                        fileName
                ));
    }

    private static ResponseEntity.BodyBuilder addHeaders(ResponseEntity.BodyBuilder response, String fileName) {
        return response.contentType(MediaType.APPLICATION_OCTET_STREAM).
                header(Constant.FILE_NAME, fileName).
                header(ACCESS_CONTROL_EXPOSE_HEADERS, Constant.FILE_NAME).
                header(HttpHeaders.CONTENT_DISPOSITION, String.format(
                        Constant.CONTENT_DISPOSITION_KEY,
                        fileName
                ));
    }

    private static class Constant {
        public static final String FILE_NAME = "fileName";
        public static final String CONTENT_DISPOSITION_KEY = "attachment; filename= %s";

        private Constant() {
        }
    }
}