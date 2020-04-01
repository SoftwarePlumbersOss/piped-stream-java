/*
 * Copyright (c) 2020 Software Plumbers LLC. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
 */
package com.softwareplumbers.common.pipedstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** A functional interface for something which consumes an output stream.
 *
 * We cannot use a regular Consumer as IOException is checked.
 * 
 * @author Jonathan Essex
 */
@FunctionalInterface
public interface OutputStreamConsumer {
    public abstract void consume(OutputStream os) throws IOException;
    
    /** Create an OutputStreamConsumer from an InputStreamSupplier.
     * 
     * Calling consume on the resulting output stream consumer will get a stream
     * from the supplier iss, and write the entire content to the output stream
     * passed to the consume method.
     * 
     * @param iss Provided of an input stream
     * @return An output stream consumer.
     */
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
