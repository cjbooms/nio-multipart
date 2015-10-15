package com.suncronoss.nio.file.multipart;

/**
 * <p>
 *     Multipart context:
 *     <ul>
 *         <li>Content Type</li>
 *         <li>Content Length</li>
 *         <li>Character Encoding</li>
 *     </ul>
 * <p/>
 *
 * Created by sriz0001 on 12/10/2015.
 */
public class MultipartContext {

    private final String contentType;
    private final int contentLength;
    private final String charEncoding;

    public MultipartContext(final String contentType, final int contentLength, final String charEncoding) {

        if (!MultipartUtils.isMultipart(contentType)){
            throw new IllegalStateException("Invalid content type '" + contentType + "'. Expected a multipart request");
        }

        this.contentType = contentType;
        this.contentLength = contentLength;
        this.charEncoding = charEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getCharEncoding() {
        return charEncoding;
    }
}