/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.pipedstream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author jonathan
 */
public class PersistentInputStreamSupplier implements InputStreamSupplier {
    
    private InputStreamSupplier supplier;

    @Override
    public InputStream get() throws IOException {
        return supplier.get();
    }
    
    public PersistentInputStreamSupplier(InputStreamSupplier supplier) {
        this.supplier = supplier;
    }
    
    public PersistentInputStreamSupplier(byte[] data) {
        this(()->new ByteArrayInputStream(data));
    }
}
