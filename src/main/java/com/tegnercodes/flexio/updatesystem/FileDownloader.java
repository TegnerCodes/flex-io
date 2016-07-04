package com.tegnercodes.flexio.updatesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Downloads a file from a URL.
 */
class FileDownloader {

    private static final String pluginsDir = System.getProperty("pf4j.pluginsDir", "plugins");

    public File downloadFile(String fileUrl) throws IOException {
        File plugins = new File(pluginsDir);
        plugins.mkdirs();

        // create a temporary file
        File tmpFile = new File(pluginsDir, DigestUtils.getSHA1(fileUrl) + ".tmp");
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        // create the url
        URL url = new URL(fileUrl);

        // set up the URL connection
        URLConnection connection = url.openConnection();

        // connect to the remote site (may takes some time)
        connection.connect();

        // check for http authorization
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ConnectException("HTTP Authorization failure");
        }

        // try to get the server-specified last-modified date of this artifact
        long lastModified = httpConnection.getHeaderFieldDate("Last-Modified", System.currentTimeMillis());

        // try to get the input stream (three times)
        InputStream is = null;
        for (int i = 0; i < 3; i++) {
            try {
                is = connection.getInputStream();
                break;
            } catch (IOException e) {
            	System.err.println(e.getMessage());
            }
        }
        if (is == null) {
            throw new ConnectException("Can't get '" + url + " to '" + tmpFile + "'");
        }

        // reade from remote resource and write to the local file
        FileOutputStream fos = new FileOutputStream(tmpFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) >= 0) {
            fos.write(buffer, 0, length);
        }
        if (fos != null) {
            fos.close();
        }
        is.close();

        // rename tmp file to resource file
        String path = url.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        File file = new File(plugins, fileName);
        if (file.exists()) {
            file.delete();
        }
        tmpFile.renameTo(file);


        file.setLastModified(lastModified);

        return file;
    }

}
