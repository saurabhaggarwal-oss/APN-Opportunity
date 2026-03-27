package com.ttn.ck.apn.utils;


import jakarta.validation.constraints.NotNull;

import java.io.*;

/**
 * {@code AutoClosableInputStream} is a wrapper around any {@link InputStream}
 * that automatically closes the underlying stream when the end of the stream (EOF) is reached.
 *
 * <p>This class is especially useful when processing large or unknown-sized streams,
 * as it ensures proper resource cleanup without relying solely on manual {@code close()} calls.</p>
 *
 * <p>The stream can be initialized using a {@code String} path, {@code File},
 * {@code FileDescriptor}, or any other {@code InputStream}.</p>
 *
 * <p><b>Behavior:</b>
 * <ul>
 *     <li>Each read method delegates to the wrapped stream.</li>
 *     <li>Once EOF is detected (read returns {@code -1}), the stream is automatically closed.</li>
 *     <li>Explicit calls to {@code close()} are safe and idempotent.</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * try (AutoClosableInputStream stream = new AutoClosableInputStream("input.txt")) {
 *     byte[] buffer = new byte[1024];
 *     int len;
 *     while ((len = stream.read(buffer)) != -1) {
 *         // process buffer
 *     }
 *     // No need to explicitly call close() – it auto-closes at EOF
 * }
 * }</pre>
 * </p>
 *
 * @author Nirbhay Khurana
 * @since   1.0
 */
public class AutoClosableInputStream extends InputStream {
    private final InputStream wrapped;
    private boolean isClosed = false;

    public AutoClosableInputStream(@NotNull String filePath) throws FileNotFoundException {
        this.wrapped = new FileInputStream(filePath);
    }

    public AutoClosableInputStream(@NotNull File file) throws FileNotFoundException {
        this.wrapped = new FileInputStream(file);
    }

    public AutoClosableInputStream(@NotNull FileDescriptor fdObj) {
        this.wrapped = new FileInputStream(fdObj);
    }

    public AutoClosableInputStream(@NotNull InputStream stream) {
        this.wrapped = stream;
    }

    @Override
    public int read() throws IOException {
        int result = wrapped.read();
        checkAndClose(result);
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int result = wrapped.read(b);
        checkAndClose(result);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = wrapped.read(b, off, len);
        checkAndClose(result);
        return result;
    }

    @NotNull
    @Override
    public byte[] readAllBytes() throws IOException {
        try {
            return wrapped.readAllBytes();
        } finally {
            closeStream();
        }
    }

    @NotNull
    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] result = wrapped.readNBytes(len);
        if (result.length < len) {
            closeStream(); // likely EOF
        }
        return result;
    }

    private void checkAndClose(int readResult) throws IOException {
        if (readResult == -1) {
            closeStream();
        }
    }

    private void closeStream() throws IOException {
        if (!isClosed) {
            isClosed = true;
            wrapped.close();
        }
    }

    @Override
    public void close() throws IOException {
        closeStream();
    }
}
