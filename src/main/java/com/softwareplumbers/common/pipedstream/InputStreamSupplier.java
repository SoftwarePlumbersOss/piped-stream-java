/*
 * Copyright (c) 2020 Software Plumbers LLC. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  
 */
package com.softwareplumbers.common.pipedstream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** A functional interface for something that returns a stream. 
 * 
 * Because such operations typically throw an IOException, we can't use a regular Suppler.
 * We find the distinction between a Stream and a Stream Supplier is very useful in order
 * to reduce the risk that a stream will not be closed. If we retrieve a stream supplier
 * from somewhere, we can pass this object around without worrying about whether it needs
 * to be closed. When the stream actually needs to be used, we can write something like:
 * 
 * ```
 * try (InputStream is = supplier.get()) { ... }
 * ```
 * 
 * In order to handle the stream in a way that guarantees its closure.
 * 
 * Some utility methods are also provided.
 * 
 * @author SWPNET\jonessex
 *
 */
@FunctionalInterface
public interface InputStreamSupplier {
    
	/** Get a stream
     * @return an input stream
     * @throws java.io.IOException */
	InputStream get() throws IOException;
    
    /** Determine if a stream supplier is persistent.
     * 
     * A persistent stream supplier may be called multiple times and will always
     * return the same value.
     * 
     * @return true if the stream supplier is persistent.
     */
    default boolean isPersistent() { return false; }
    
    /** Create an Input Stream that receives information from some output stream.
     * 
     * Sometimes, inevitably, we find an API that wants to 'push' data (via an OutputStream)
     * when we really want to 'pull' it (via an InputStream). Writing the entire stream content
     * to a buffer and then reading it back out again is not a tremendously happy solution.
     * 
     * This method will immediately return an InputStream. It will spin off a separate thread
     * which will call output.consume in order to buffer data for consumption via the InputStream.
     * Data becomes available for consumption on the InputStream as soon as it is written by the
     * OutputStreamConsumer. 
     * 
     * @see PipedInputStream
     * @see PipedOutputStream
     * 
     * @param output
     * @return An input stream that will eventually receive all data written to output
     * @throws IOException 
     */
    public static InputStream pipe(OutputStreamConsumer output) throws IOException {
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out);
        
        new Thread(()-> { 
            try {
                output.consume(out); 
            } catch (IOException e) {
                // Hopefully this will force an IOException in the reading thread
                try { out.close(); } catch (IOException e2) { }
            } catch (RuntimeException e) {
                out.sendError(e);
            }
        }).start();

        return in;
    }
    
    /** Convert an output stream consumer into an input stream supplier.
     * 
     * Spins of a thread which will copy data written to the output stream into
     * the input stream. The thread terminates when the output stream is closed.
     * 
     * Any errors raised on the copy thread will be raised in the reading thread.
     * 
     * @param out an output stream consumer
     * @return an input stream supplier
     */
    public static InputStreamSupplier of(OutputStreamConsumer out) {
        return ()->pipe(out);
    }
    
    /** Create a copy of a input stream supplier.
     * 
     * Creates a persistent copy of the supplier, or returns the supplier if the
     * supplier is already persistent.
     * 
     * @param supplier
     * @return a copy of the supplier (or the supplier, if the supplier is persistent).
     * @throws IOException 
     */
    public static InputStreamSupplier copy(InputStreamSupplier supplier) throws IOException {
        if (supplier == null) return null;
        if (supplier.isPersistent()) 
            return supplier;
        else {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                OutputStreamConsumer.of(supplier).consume(os);
                return new PersistentInputStreamSupplier(os.toByteArray());
            }
        }
    }
    
    /** Mark a supplier as persistent.
     * 
     * If we know that a supplier is already persistent, we can mark it as such 
     * with this method. This will avoid unnecessary copy operations.
     * 
     * @param supplier
     * @return An input stream supplier which is persistent.
     */
    public static InputStreamSupplier markPersistent(InputStreamSupplier supplier) {
        return supplier == null ? null : new PersistentInputStreamSupplier(supplier);
    }
}
