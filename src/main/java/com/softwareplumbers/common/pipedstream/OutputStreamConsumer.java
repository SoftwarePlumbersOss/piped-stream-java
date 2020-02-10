package com.softwareplumbers.common.pipedstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jonathan
 */
@FunctionalInterface
public interface OutputStreamConsumer {
    public abstract void consume(OutputStream os) throws IOException;
    
    public static OutputStreamConsumer of(InputStreamSupplier iss) {
        return out -> {
            try (InputStream is = iss.get()) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) >= 0) { out.write(buf, 0, len); }               
            } finally {
                out.close();
            }
        };
    }
}
