package com.softwareplumbers.common.pipedstream;

import java.io.IOException;
import java.io.InputStream;

/** A functional interface for something that returns a stream. 
 * 
 * Because such operations typically throw an IOException, we can't use a regular Suppler.
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
    
    public static InputStreamSupplier of(OutputStreamConsumer out) {
        return ()->pipe(out);
    }
}
