package com.qa.framework.library.httpclient;


import com.library.common.StringHelper;
import com.qa.framework.cache.DriverCache;
import com.qa.framework.config.PropConfig;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Cookie;

import java.io.*;
import java.util.Set;

/**
 * Created by apple on 15/11/20.
 */
public class HttpConnectionImp {
    private final Logger logger = Logger
            .getLogger(this.getClass());
    private HttpRequestBase baseRequest;

    /**
     * Instantiates a new Http connection imp.
     *
     * @param baseRequest the base request
     */
    public HttpConnectionImp(HttpRequestBase baseRequest) {
        this.baseRequest = baseRequest;
    }

    /**
     * Gets response result.
     *
     * @param storeCookie the store cookie
     * @param useCookie   the use cookie
     * @return the response result
     */
    public String getResponseResult(boolean storeCookie, boolean useCookie) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpClientContext clientContext = HttpClientContext.create();
        if (useCookie) {
            CookieCache.clear();
            Set<Cookie> driverCookies = DriverCache.get().manage().getCookies();
            org.apache.http.client.CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : driverCookies) {
                BasicClientCookie basicCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                basicCookie.setDomain(cookie.getDomain());
                basicCookie.setPath(cookie.getPath());
                basicCookie.setExpiryDate(cookie.getExpiry());
                cookieStore.addCookie(basicCookie);
            }
            String hostName = baseRequest.getURI().getHost();
            for (Cookie cookie : driverCookies) {
                if (!cookie.getDomain().equalsIgnoreCase(hostName)) {
                    BasicClientCookie basicCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                    basicCookie.setDomain(hostName);
                    basicCookie.setPath(cookie.getPath());
                    basicCookie.setExpiryDate(cookie.getExpiry());
                    cookieStore.addCookie(basicCookie);
                }
            }
            CookieCache.set(cookieStore);
            clientContext.setCookieStore(cookieStore);
        }
        String responseBody = null;
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(baseRequest, clientContext);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (storeCookie) {
                CookieStore cookieStore = clientContext.getCookieStore();
                CookieCache.set(cookieStore);
            }
            if (status >= 200 && status < 300) {
                //logger.info("expected response status");

            } else {
                //logger.info("unexpected response status");
            }
            HttpEntity entity = httpResponse.getEntity();
            responseBody = entity != null ? EntityUtils.toString(entity) : null;
            if (responseBody != null) {
                //统一处理为utf-8
                responseBody = new String(responseBody.getBytes("UTF-8"), "UTF-8");
            }
        } catch (IOException e) {
            logger.error(e);
            return null;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                //logger.error(e.getMessage());
            }
        }
        if (responseBody != null) {
            return removeBOM(responseBody);
        } else {
            return null;
        }
    }

    /**
     * Remove bom string.
     *
     * @param responseBody the response body
     * @return the string
     */
    public String removeBOM(String responseBody) {
        BufferedReader reader = null;
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(responseBody.getBytes());

        // read it with BufferedReader
        reader = new BufferedReader(new InputStreamReader(new BOMInputStream(is)));

        String line = null;
        String lines = "";
        try {
            while ((line = reader.readLine()) != null) {
                lines = lines + line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}
