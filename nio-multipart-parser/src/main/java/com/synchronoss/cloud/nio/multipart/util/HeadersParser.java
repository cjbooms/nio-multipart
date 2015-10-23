package com.synchronoss.cloud.nio.multipart.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Utility methods to parse the headers section into a {@link Map} of header name - list of header values
 * </p>
 * Created by mele on 22/10/2015.
 */
public class HeadersParser {

    private HeadersParser() { }

    /**
     * <p>
     *     Parse the headers section into a {@link Map}
     * </p>
     * @param headersSection The header section.
     * @param charset The charset
     * @return The {@link Map} having as keys the header names and as values a list of the header values.
     */
    public static Map<String, List<String>> parseHeaders(final InputStream headersSection, final String charset) {

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String name = null;
        StringBuffer value = null;
        for (; ;) {
            String line = readLine(headersSection, charset);
            if ((line == null) || (line.trim().length() < 1)) {
                break;
            }

            // Parse the header name and value
            // Check for folded headers first
            // Detect LWS-char see HTTP/1.0 or HTTP/1.1 Section 2.2
            // discussion on folded headers
            if ((line.charAt(0) == ' ') || (line.charAt(0) == '\t')) {
                // we have continuation folded header
                // so append value
                if (value != null) {
                    value.append(' ');
                    value.append(line.trim());
                }
            } else {
                // make sure we save the previous name,value pair if present
                if (name != null) {
                    addHeader(headers, name, value.toString());
                }

                // Otherwise we should have normal HTTP header line
                // Parse the header name and value
                int colon = line.indexOf(":");
                if (colon < 0) {
                    throw new IllegalStateException("Unable to parse header: " + line);
                }
                name = line.substring(0, colon).trim();
                value = new StringBuffer(line.substring(colon + 1).trim());
            }

        }

        // make sure we save the last name,value pair if present
        if (name != null) {
            addHeader(headers, name, value.toString());
        }

        return headers;
    }

    static void addHeader(final Map<String, List<String>> headers, final String name, final String value){
        String nameLc = name.toLowerCase();
        List<String> headerValues = headers.get(nameLc);
        if (headerValues == null){
            headerValues = new ArrayList<String>();
            headers.put(nameLc, headerValues);
        }
        headerValues.add(value);
    }

    static byte[] readRawLine(final InputStream inputStream) {

        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int ch;
            while ((ch = inputStream.read()) >= 0) {
                buf.write(ch);
                if (ch == '\n') { // be tolerant (RFC-2616 Section 19.3)
                    break;
                }
            }
            if (buf.size() == 0) {
                return null;
            }
            return buf.toByteArray();

        }catch (Exception e){
            throw new IllegalStateException("Error reading the headers line", e);
        }
    }

    static String readLine(final InputStream inputStream, final String charset) {
        byte[] rawdata = readRawLine(inputStream);
        if (rawdata == null) {
            return null;
        }
        // strip CR and LF from the end
        int len = rawdata.length;
        int offset = 0;
        if (len > 0) {
            if (rawdata[len - 1] == '\n') {
                offset++;
                if (len > 1) {
                    if (rawdata[len - 2] == '\r') {
                        offset++;
                    }
                }
            }
        }
        return getString(rawdata, 0, len - offset, charset);

    }

    static String getString(final byte[] data, final int offset, final int length, final String charset) {

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        if (charset == null || charset.length() == 0) {
            throw new IllegalArgumentException("Charset cannot be null or empty");
        }

        try {
            return new String(data, offset, length, charset);
        } catch (Exception e) {
            return new String(data, offset, length);
        }
    }

}
