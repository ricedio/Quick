package com.quick.core.lib;

import com.quick.BuildConfig;
import com.quick.utils.LuaUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class http {

    private static HashMap<String, String> sHeader;

    public static void setHeader(HashMap<String, String> header) {
        sHeader = header;
    }

    public static HashMap<String, String> getHeader() {
        return sHeader;
    }


    public static HttpResult get(String url) {
        LuaHttp task = new LuaHttp(url, "GET", null, null, null);
        return task.connection();
    }

    public static HttpResult get(String url, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "GET", null, null, header);

        return task.connection();
    }

    public static HttpResult get(String url, String cookie, HashMap<String, String> header) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "GET", null, cookie, header) : new LuaHttp(url, "GET", cookie, null, header);

        return task.connection();
    }

    public static HttpResult get(String url, String cookie) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "GET", null, cookie, null) : new LuaHttp(url, "GET", cookie, null, null);

        return task.connection();
    }

    public static HttpResult get(String url, String cookie, String charset) {
        LuaHttp task = new LuaHttp(url, "GET", cookie, charset, null);

        return task.connection();
    }

    public static HttpResult get(String url, String cookie, String charset, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "GET", cookie, charset, header);

        return task.connection();
    }

    public static HttpResult download(String url, String data) {
        LuaHttp task = new LuaHttp(url, "GET", null, null, null);

        return task.connection(data);
    }

    public static HttpResult download(String url, String data, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "GET", null, null, header);

        return task.connection(data);
    }

    public static HttpResult download(String url, String data, String cookie) {
        LuaHttp task = new LuaHttp(url, "GET", cookie, null, null);

        return task.connection(data);
    }

    public static HttpResult download(String url, String data, String cookie, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "GET", cookie, null, header);

        return task.connection(data);
    }


    public static HttpResult delete(String url) {
        LuaHttp task = new LuaHttp(url, "DELETE", null, null, null);

        return task.connection();
    }

    public static HttpResult delete(String url, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "DELETE", null, null, header);

        return task.connection();
    }

    public static HttpResult delete(String url, String cookie, HashMap<String, String> header) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "DELETE", null, cookie, header) : new LuaHttp(url, "DELETE", cookie, null, header);

        return task.connection();
    }

    public static HttpResult delete(String url, String cookie) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "DELETE", null, cookie, null) : new LuaHttp(url, "DELETE", cookie, null, null);

        return task.connection();
    }

    public static HttpResult delete(String url, String cookie, String charset) {
        LuaHttp task = new LuaHttp(url, "DELETE", cookie, charset, null);

        return task.connection();
    }

    public static HttpResult delete(String url, String cookie, String charset, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "DELETE", cookie, charset, header);

        return task.connection();
    }


    public static HttpResult post(String url, String data) {
        LuaHttp task = new LuaHttp(url, "POST", null, null, null);

        return task.connection(data);
    }

    public static HttpResult post(String url, String data, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "POST", null, null, header);

        return task.connection(data);
    }

    public static HttpResult post(String url, String data, String cookie) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "POST", null, cookie, null) : new LuaHttp(url, "POST", cookie, null, null);

        return task.connection(data);
    }

    public static HttpResult post(String url, String data, String cookie, HashMap<String, String> header) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "POST", null, cookie, header) : new LuaHttp(url, "POST", cookie, null, header);

        return task.connection(data);
    }

    public static HttpResult post(String url, String data, String cookie, String charset) {
        LuaHttp task = new LuaHttp(url, "POST", cookie, charset, null);

        return task.connection(data);
    }

    public static HttpResult post(String url, String data, String cookie, String charset, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "POST", cookie, charset, header);

        return task.connection(data);
    }


    public static HttpResult post(String url, HashMap<String, String> data) {
        return post(url, formatMap(data));
    }

    public static HttpResult post(String url, HashMap<String, String> data, String cookie) {
        return post(url, formatMap(data), cookie);
    }

    public static HttpResult post(String url, HashMap<String, String> data, String cookie, HashMap<String, String> header) {
        return post(url, formatMap(data), cookie, header);
    }

    public static HttpResult post(String url, HashMap<String, String> data, String cookie, String charset) {
        return post(url, formatMap(data), cookie, charset);
    }

    public static HttpResult post(String url, HashMap<String, String> data, String cookie, String charset, HashMap<String, String> header) {
        return post(url, formatMap(data), cookie, charset, header);
    }

    private static String formatMap(HashMap<String, String> data) {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            buf.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (!data.isEmpty())
            buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }


    private final static String boundary = "----qwertyuiopasdfghjklzxcvbnm";

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file) {
        return post(url, data, file, null, null, null);
    }

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file, String cookie) {
        return post(url, data, file, cookie, new HashMap<String, String>());
    }

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file, HashMap<String, String> header) {
        return post(url, data, file, null, header);
    }

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file, String cookie, HashMap<String, String> header) {
        return cookie.matches("[\\w\\-.:]+") && Charset.isSupported(cookie) ? post(url, data, file, cookie, null, header) : post(url, data, file, null, cookie, header);
    }

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file, String cookie, String charset) {
        return post(url, data, file, cookie, charset, null);
    }

    public static HttpResult post(String url, HashMap<String, String> data, HashMap<String, String> file, String cookie, String charset, HashMap<String, String> header) {
        if (header == null)
            header = new HashMap<>();
        header.put("Content-Type", "multipart/form-data;boundary=" + boundary);
        LuaHttp task = new LuaHttp(url, "POST", cookie, charset, header);
        return task.connection(new Object[]{formatMultiDate(data, file, charset)});
    }

    private static byte[] formatMultiDate(HashMap<String, String> data, HashMap<String, String> file, String charset) {
        if (charset == null)
            charset = "UTF-8";
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                buff.write(String.format("--%s\r\nContent-Disposition:form-data;name=\"%s\"\r\n\r\n%s\r\n", boundary, entry.getKey(), entry.getValue()).getBytes(charset));
            } catch (IOException e) {
                if(BuildConfig.DEBUG)
			    e.printStackTrace();
            }
        }

        for (Map.Entry<String, String> entry : file.entrySet()) {
            try {
                buff.write(String.format("--%s\r\nContent-Disposition:form-data;name=\"%s\";filename=\"%s\"\r\nContent-Type:application/octet-stream\r\n\r\n", boundary, entry.getKey(), entry.getValue()).getBytes(charset));
                buff.write(LuaUtil.readAll(new FileInputStream(entry.getValue())));
                buff.write("\r\n".getBytes(charset));
            } catch (IOException e) {
                if(BuildConfig.DEBUG)
			    e.printStackTrace();
            }
        }
        try {
            buff.write(String.format("--%s--\r\n", boundary).getBytes(charset));
        } catch (IOException e) {
            if(BuildConfig.DEBUG)
			    e.printStackTrace();
        }

        return buff.toByteArray();
    }
    
    public static HttpResult put(String url, String data) {
        LuaHttp task = new LuaHttp(url, "PUT", null, null, null);

        return task.connection(data);
    }

    public static HttpResult put(String url, String data, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "PUT", null, null, header);

        return task.connection(data);
    }

    public static HttpResult put(String url, String data, String cookie) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "PUT", null, cookie, null) : new LuaHttp(url, "PUT", cookie, null, null);

        return task.connection(data);
    }

    public static HttpResult put(String url, String data, String cookie, HashMap<String, String> header) {
        LuaHttp task = cookie.matches("[\\w\\-]+") && Charset.isSupported(cookie) ? new LuaHttp(url, "PUT", null, cookie, header) : new LuaHttp(url, "PUT", cookie, null, header);

        return task.connection(data);
    }

    public static HttpResult put(String url, String data, String cookie, String charset) {
        LuaHttp task = new LuaHttp(url, "PUT", cookie, charset, null);

        return task.connection(data);
    }

    public static HttpResult put(String url, String data, String cookie, String charset, HashMap<String, String> header) {
        LuaHttp task = new LuaHttp(url, "PUT", cookie, charset, header);

        return task.connection(data);
    }


    public static class LuaHttp {

        private String mUrl;

        private byte[] mData;

        private String mCharset;

        private String mCookie;

        private HashMap<String, String> mHeader;

        private String mMethod;

        public LuaHttp(String url, String method, String cookie, String charset, HashMap<String, String> header) {
            mUrl = url;
            mMethod = method;
            mCookie = cookie;
            mCharset = charset;
            mHeader = header;
        }

        public HttpResult connection(Object... p1) {
            // TODO: Implement this method
            try {
                URL url = new URL(mUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);
                HttpURLConnection.setFollowRedirects(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.5");

                if (mCharset == null)
                    mCharset = "UTF-8";
                conn.setRequestProperty("Accept-Charset", mCharset);

                if (mCookie != null)
                    conn.setRequestProperty("Cookie", mCookie);

                if (sHeader != null) {
                    Set<Map.Entry<String, String>> entries = sHeader.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }

                if (mHeader != null) {
                    Set<Map.Entry<String, String>> entries = mHeader.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }

                if (mMethod != null)
                    conn.setRequestMethod(mMethod);

                if (!"GET".equals(mMethod) && p1.length != 0) {
                    mData = formatData(p1);

                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-length", "" + mData.length);
                }

                conn.connect();

                //download
                if ("GET".equals(mMethod) && p1.length != 0) {
                    File f = new File((String) p1[0]);
                    if (!f.getParentFile().exists())
                        f.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(f);
                    InputStream in = conn.getInputStream();
                    long len = conn.getContentLength();
                    long off = 0;
                    try {
                        int byteread = 0;
                        byte[] buffer = new byte[8 * 1024];
                        while ((byteread = in.read(buffer)) != -1) {
                            out.write(buffer, 0, byteread);
                            off += byteread;
                        }
                        //in.close();
                        //out.close();
                    } catch (Exception e) {
                    }
                    return new HttpResult(conn.getResponseCode(), f.getAbsolutePath(), null, conn.getHeaderFields());
                }

                //post upload
                if (p1.length != 0) {
                    OutputStream os = conn.getOutputStream();
                    os.write(mData);
                }

                int code = conn.getResponseCode();
                Map<String, List<String>> hs = conn.getHeaderFields();
                String encoding = conn.getContentEncoding();
                List<String> cs = hs.get("Set-Cookie");
                StringBuilder cok = new StringBuilder();
                if (cs != null) {
                    for (String s : cs) {
                        cok.append(s).append(";");
                    }
                }

                List<String> ct = hs.get("Content-Type");
                if (ct != null) {
                    for (String s : ct) {
                        int idx = s.indexOf("charset");
                        if (idx != -1) {
                            idx = s.indexOf("=", idx);
                            if (idx != -1) {
                                int idx2 = s.indexOf(";", idx);
                                if (idx2 == -1)
                                    idx2 = s.length();
                                mCharset = s.substring(idx + 1, idx2);
                                break;
                            }
                        }
                    }
                }

                if(mCharset==null){
                    mCharset="UTF-8";
                }

                StringBuilder buf = new StringBuilder();
                try {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, mCharset));
                    String line;
                    while ((line = reader.readLine()) != null)
                        buf.append(line).append('\n');
                    is.close();
                } catch (Exception e) {
                    if(BuildConfig.DEBUG)
			    e.printStackTrace();
                }
                InputStream is = conn.getErrorStream();
                if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, mCharset));
                    String line;
                    while ((line = reader.readLine()) != null)
                        buf.append(line).append('\n');
                    is.close();
                }
                return new HttpResult(code, new String(buf), cok.toString(), hs);
            } catch (Exception e) {
                if(BuildConfig.DEBUG)
			    e.printStackTrace();
                return new HttpResult(-1, e.getMessage(), null, null);
            }

        }

        private byte[] formatData(Object[] p1) throws UnsupportedEncodingException, IOException {
            // TODO: Implement this method
            byte[] bs = null;
            if (p1.length == 1) {
                Object obj = p1[0];
                if (obj instanceof String)
                    bs = ((String) obj).getBytes(mCharset);
                else if (obj.getClass().getComponentType() == byte.class)
                    bs = (byte[]) obj;
                else if (obj instanceof File)
                    bs = LuaUtil.readAll(new FileInputStream((File) obj));
                else if (obj instanceof Map)
                    bs = formatData((Map) obj);
            }
            return bs;
        }

        private byte[] formatData(Map obj) throws UnsupportedEncodingException {
            // TODO: Implement this method
            StringBuilder buf = new StringBuilder();
            Set<Map.Entry<String, String>> entries = mHeader.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                buf.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            return buf.toString().getBytes(mCharset);
        }
    }

    public interface HttpCallback {
        public void onDone(HttpResult result);
    }

    public static class HttpResult {
        public int code;
        public String text;
        public String cookie;
        public Map<String, List<String>> header;

        public HttpResult(int code,
                          String text,
                          String cookie,
                          Map<String, List<String>> header) {

            this.code = code;
            this.text = text;
            this.cookie = cookie;
            this.header = header;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
