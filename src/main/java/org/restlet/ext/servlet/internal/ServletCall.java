/**
 * Copyright 2005-2020 Talend
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.servlet.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.LanguageReader;
import org.restlet.engine.io.UnclosableInputStream;
import org.restlet.engine.io.UnclosableOutputStream;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Call that is used by the Servlet HTTP server connector.
 *
 * @author Jerome Louvel
 */
public class ServletCall extends ServerCall {

    /** The HTTP Servlet request to wrap. */
    private volatile HttpServletRequest  request;

    /** The request headers. */
    private volatile Series <Header>     requestHeaders;

    /** The HTTP Servlet response to wrap. */
    private volatile HttpServletResponse response;

    /**
     * Constructor.
     *
     * @param server
     *            The parent server.
     * @param request
     *            The HTTP Servlet request to wrap.
     * @param response
     *            The HTTP Servlet response to wrap.
     */
    public ServletCall(final Server server, final HttpServletRequest request,
            final HttpServletResponse response) {

        super(server);
        this.request = request;
        this.response = response;
    }


    /**
     * Constructor.
     *
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server port.
     * @param request
     *            The Servlet request
     * @param response
     *            The Servlet response.
     */
    public ServletCall(final String serverAddress, final int serverPort,
            final HttpServletRequest request, final HttpServletResponse response) {

        super(serverAddress, serverPort);
        this.request = request;
        this.response = response;
    }


    /**
     * Not supported. Always returns false.
     */
    @Override
    public boolean abort() {

        return false;
    }


    @Override
    public void flushBuffers() throws IOException {

        this.getResponse().flushBuffer();
    }


    @Override
    public List <Certificate> getCertificates() {

        final Certificate[] certificateArray = (Certificate[]) this.getRequest()
                .getAttribute("javax.servlet.request.X509Certificate");

        if (certificateArray != null) {
            return Arrays.asList(certificateArray);
        }

        return null;
    }


    @Override
    public String getCipherSuite() {

        return (String) this.getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
    }


    @Override
    public String getClientAddress() {

        return this.getRequest().getRemoteAddr();
    }


    @Override
    public int getClientPort() {

        return this.getRequest().getRemotePort();
    }


    /**
     * Returns the server domain name.
     *
     * @return The server domain name.
     */
    @Override
    public String getHostDomain() {

        return this.getRequest().getServerName();
    }


    /**
     * Returns the request method.
     *
     * @return The request method.
     */
    @Override
    public String getMethod() {

        return this.getRequest().getMethod();
    }


    /**
     * Returns the server protocol.
     *
     * @return The server protocol.
     */
    @Override
    public Protocol getProtocol() {

        return Protocol.valueOf(this.getRequest().getScheme());
    }


    /**
     * Returns the HTTP Servlet request.
     *
     * @return The HTTP Servlet request.
     */
    public HttpServletRequest getRequest() {

        return this.request;
    }


    // [ifdef gae] method
    @Override
    public Representation getRequestEntity() {

        Representation result = null;

        if (this.getRequest().getContentType() != null
                && MediaType.APPLICATION_WWW_FORM.isCompatible(new MediaType(
                        this.getRequest().getContentType()))) {
            final Form form = new Form();
            final Map <String, String[]> map = this.request.getParameterMap();

            for (final Entry <String, String[]> entry : map.entrySet()) {
                for (int i = 0; i < entry.getValue().length; i++) {
                    form.add(entry.getKey(), entry.getValue()[i]);
                }
            }

            result = form.getWebRepresentation();

            // Extract some interesting header values
            final Header header = this.getRequestHeaders().getFirst(
                    HeaderConstants.HEADER_CONTENT_LANGUAGE, true);

            if (header != null) {
                new LanguageReader(header.getValue()).addValues(result
                        .getLanguages());
            }
        } else {
            result = super.getRequestEntity();
        }

        return result;
    }


    @Override
    public InputStream getRequestEntityStream(final long size) {

        try {
            return new UnclosableInputStream(this.getRequest().getInputStream());
        } catch (final IOException e) {
            return null;
        }
    }


    /**
     * Returns the list of request headers.
     *
     * @return The list of request headers.
     */
    @Override
    public Series <Header> getRequestHeaders() {

        if (this.requestHeaders == null) {
            this.requestHeaders = new Series <>(Header.class);

            // Copy the headers from the request object
            String headerName;
            String headerValue;

            for (final Enumeration <String> names = this.getRequest().getHeaderNames(); names
                    .hasMoreElements();) {
                headerName = names.nextElement();

                for (final Enumeration <String> values = this.getRequest().getHeaders(
                        headerName); values.hasMoreElements();) {
                    headerValue = values.nextElement();
                    this.requestHeaders.add(headerName, headerValue);
                }
            }
        }

        return this.requestHeaders;
    }


    @Override
    public InputStream getRequestHeadStream() {

        // Not available
        return null;
    }


    /**
     * Returns the full request URI.
     *
     * @return The full request URI.
     */
    @Override
    public String getRequestUri() {

        final String queryString = this.getRequest().getQueryString();

        if ((queryString == null) || ("".equals(queryString))) {
            return this.getRequest().getRequestURI();
        }

        return this.getRequest().getRequestURI() + '?' + queryString;
    }


    /**
     * Returns the HTTP Servlet response.
     *
     * @return The HTTP Servlet response.
     */
    public HttpServletResponse getResponse() {

        return this.response;
    }


    /**
     * Returns the response stream if it exists, null otherwise.
     *
     * @return The response stream if it exists, null otherwise.
     */
    @Override
    public OutputStream getResponseEntityStream() {

        try {
            return new UnclosableOutputStream(this.getResponse().getOutputStream());
        } catch (final IOException e) {
            return null;
        }
    }


    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     *
     * @return The response address.
     */
    @Override
    public String getServerAddress() {

        return this.getRequest().getLocalAddr();
    }


    /**
     * Returns the server port.
     *
     * @return The server port.
     */
    @Override
    public int getServerPort() {

        return this.getRequest().getServerPort();
    }


    @Override
    public Integer getSslKeySize() {

        Integer keySize = (Integer) this.getRequest().getAttribute(
                "javax.servlet.request.key_size");

        if (keySize == null) {
            keySize = super.getSslKeySize();
        }

        return keySize;
    }


    @Override
    public String getSslSessionId() {

        Object sessionId = this.getRequest().getAttribute(
                "javax.servlet.request.ssl_session_id");

        if ((sessionId != null) && (sessionId instanceof String)) {
            return (String) sessionId;
        }

        /*
         * The following is for the non-standard, pre-Servlet 3 spec used by
         * Tomcat/Coyote.
         */
        sessionId = this.getRequest().getAttribute(
                "javax.servlet.request.ssl_session");

        if (sessionId instanceof String) {
            return (String) sessionId;
        }

        return null;
    }


    @Override
    public Principal getUserPrincipal() {

        return this.getRequest().getUserPrincipal();
    }


    @Override
    public String getVersion() {

        String result = null;
        final int index = this.getRequest().getProtocol().indexOf('/');

        if (index != -1) {
            result = this.getRequest().getProtocol().substring(index + 1);
        }

        return result;
    }


    /**
     * Indicates if the request was made using a confidential mean.<br>
     *
     * @return True if the request was made using a confidential mean.<br>
     */
    @Override
    public boolean isConfidential() {

        return this.getRequest().isSecure();
    }


    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     *
     * @param response
     *            The high-level response.
     */
    @Override
    public void sendResponse(final Response response) throws IOException {

        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(this.getStatusCode()) && (response.getEntity() == null)) {
            try {
                // Add the response headers
                Header header;

                for (final Iterator <Header> iter = this.getResponseHeaders().iterator(); iter
                        .hasNext();) {
                    header = iter.next();

                    // We don't need to set the content length, especially
                    // because it could send the response too early on some
                    // containers (ex: Tomcat 5.0).
                    if (!HeaderConstants.HEADER_CONTENT_LENGTH.equals(
                            header.getName())) {
                        this.getResponse().addHeader(header.getName(),
                                header.getValue());
                    }
                }

                this.getResponse().sendError(this.getStatusCode(), this.getReasonPhrase());
            } catch (final IOException ioe) {
                this.getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            this.getResponse().setStatus(this.getStatusCode());

            // Add the response headers after setting the status because
            // otherwise some containers (ex: Tomcat 5.0) immediately send
            // the response if a "Content-Length: 0" header is found.
            Header header;
            Header contentLengthHeader = null;

            for (final Iterator <Header> iter = this.getResponseHeaders().iterator(); iter
                    .hasNext();) {
                header = iter.next();

                if (HeaderConstants.HEADER_CONTENT_LENGTH.equals(
                        header.getName())) {
                    contentLengthHeader = header;
                } else {
                    this.getResponse()
                            .addHeader(header.getName(), header.getValue());
                }
            }

            if (contentLengthHeader != null) {
                this.getResponse().addHeader(contentLengthHeader.getName(),
                        contentLengthHeader.getValue());
            }

            super.sendResponse(response);
        }
    }

}
