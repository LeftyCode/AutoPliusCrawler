package com.leftycode.autoscarper.util;

import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

@Component
public class ImageDownloader {

    public byte[] download(final String urlArg) throws IOException {
        String encodedUrl = URI.create(urlArg).toASCIIString();
        URL url = new URL(encodedUrl);
        String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/56.0.2924.87 Safari/537.36";
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);
        InputStream in = new BufferedInputStream(con.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1!=(n=in.read(buf)))
        {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }
}
