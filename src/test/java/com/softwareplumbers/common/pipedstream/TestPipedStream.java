/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.pipedstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jonathan
 */
public class TestPipedStream {
    
    public static void writeGarbage(int count, OutputStream out) throws IOException {
        for (int i = 0; i < count; i ++) out.write(i & 0xFF);        
    }
    
    public static void readGarbage(int count, InputStream in) throws IOException {
        int read = 0;
        for (int i = 0; i < count && read >=0 ; i ++) {
            read = in.read();
            assertEquals(i & 0xFF, read);      
        }
    }
    
    @Test
    public void testBasicPipe() throws IOException {
        PipedInputStream in = new PipedInputStream(1024);
        PipedOutputStream out = new PipedOutputStream(in);
        
        new Thread(()->{
            try {
                writeGarbage(100, out);
                Thread.sleep(100);
                writeGarbage(500, out);
                Thread.sleep(100);
                writeGarbage(1000, out);
                out.close();
            } catch (IOException | InterruptedException e) {
            }
            
        }).start();
        
        readGarbage(100, in);
        readGarbage(500, in);
        readGarbage(1000, in);
    }
    
    class TestError extends RuntimeException {
        
    }


    @Test(expected = TestError.class)
    public void testErrorAtStart() throws IOException {
        PipedInputStream in = new PipedInputStream(1024);
        PipedOutputStream out = new PipedOutputStream(in);
        
        new Thread(()->{
            try {
                out.sendError(new TestError());
                writeGarbage(100, out);
                Thread.sleep(100);
                writeGarbage(500, out);
                Thread.sleep(100);
                writeGarbage(1000, out);
                out.close();
            } catch (IOException | InterruptedException e) {
            }
            
        }).start();
        
        readGarbage(100, in);
        readGarbage(500, in);
        readGarbage(1000, in);
    }    
    
    @Test(expected = TestError.class)
    public void testErrorInMiddle() throws IOException {
        PipedInputStream in = new PipedInputStream(1024);
        PipedOutputStream out = new PipedOutputStream(in);
        
        new Thread(()->{
            try {
                writeGarbage(100, out);
                Thread.sleep(100);
                writeGarbage(500, out);
                out.sendError(new TestError());
                Thread.sleep(100);
                writeGarbage(1000, out);
                out.close();
            } catch (IOException | InterruptedException e) {
            }
            
        }).start();
        
        readGarbage(100, in);
        readGarbage(500, in);
        readGarbage(1000, in);
    } 
    
    @Test(expected = TestError.class)
    public void testOnlySendError() throws IOException {
        PipedInputStream in = new PipedInputStream(1024);
        PipedOutputStream out = new PipedOutputStream(in);
        
        new Thread(()->{
            try {
                Thread.sleep(1000);
                out.sendError(new TestError());
                out.close();
            } catch (IOException | InterruptedException e) {
            }
            
        }).start();
        
        readGarbage(100, in);
    } 
}
