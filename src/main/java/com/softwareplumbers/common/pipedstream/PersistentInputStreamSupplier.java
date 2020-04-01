/*
 * Copyright (c) 2020 Software Plumbers LLC. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
 */
package com.softwareplumbers.common.pipedstream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** An InputStreamSupplier which has been tagged as persistent.
 * 
 * An persistent InputStreamSupplier will always return the same data when
 * get() is called. Thus it does not need to be copied.
 * 
 * Note: this class may be removed from the public interface of this package.
 *
 * @author Jonathan Essex
 */
public class PersistentInputStreamSupplier implements InputStreamSupplier {
    
    /** Underlying supplier */
    private InputStreamSupplier supplier;

    @Override
    public InputStream get() throws IOException {
        return supplier.get();
    }
    
    /** Mark an underlying input stream as persistent */
    public PersistentInputStreamSupplier(InputStreamSupplier supplier) {
        this.supplier = supplier;
    }
    
    /** Create a persistent input stream from a byte array */
    public PersistentInputStreamSupplier(byte[] data) {
        this(()->new ByteArrayInputStream(data));
    }
}
