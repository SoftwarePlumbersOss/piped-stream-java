package com.softwareplumbers.common.pipedstream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** A functional interface for something that returns a stream. 
 * 
 * Because such operations typically throw an IOException, we can't use a regular Suppler.
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
     * with this method.
     * 
     * @param supplier
     * @return An input stream supplier which is persistent.
     */
    public static InputStreamSupplier markPersistent(InputStreamSupplier supplier) {
        return supplier == null ? null : new PersistentInputStreamSupplier(supplier);
    }
}
