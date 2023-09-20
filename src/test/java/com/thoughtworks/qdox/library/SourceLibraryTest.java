package com.thoughtworks.qdox.library;

import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

public class SourceLibraryTest {
    private SourceLibrary sourceLibrary;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        sourceLibrary = new SourceLibrary( null );
    }

    @AfterEach
    public void tearDown()
        throws Exception
    {
        deleteDir("target/test-source");
    }
    
    private File createFile(String fileName, String packageName, String className) throws Exception {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file);
        writer.write("// this file generated by JavaDocBuilderTest - feel free to delete it\n");
        writer.write("package " + packageName + ";\n\n");
        writer.write("public class " + className + " {\n\n  // empty\n\n}\n");
        writer.close();
        return file;
    }
    
    private void deleteDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                File file = children[i];
                if (file.isDirectory()) {
                    deleteDir(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }
    }

    //QDOX-221
    @Test
    public void testClosedStream() throws Exception {
        File badFile = createFile("target/test-source/com/blah/Bad.java", "com.blah", "@%! BAD {}}}}");
        InputStream stream = new FileInputStream( badFile );
        try {
            sourceLibrary.addSource( stream );
        }
        catch(ParseException ex) {
            try {
                stream.read();
                Assertions.fail("Stream should be closed");
            }
            catch(IOException ioe) {}
        }
    }
    
    //QDOX-221
    @Test
    public void testClosedReader() throws Exception {
        File badFile = createFile("target/test-source/com/blah/Bad.java", "com.blah", "@%! BAD {}}}}");
        Reader reader= new FileReader( badFile );
        try {
            sourceLibrary.addSource( reader );
        }
        catch(ParseException ex) {
            try {
                reader.read();
                Assertions.fail("Reader should be closed");
            }
            catch(IOException ioe) {}
        }
    }
    
    // ensure encoding is read
    @Test
    public void testUTF8() throws Exception {
        File file = new File( "src/test/resources/com/thoughtworks/qdox/testdata/UTF8.java");
        sourceLibrary.setEncoding( "UTF-8" );
        JavaSource src = sourceLibrary.addSource( file );
        Assertions.assertEquals("TEST-CHARS: \u00DF\u0131\u03A3\u042F\u05D0\u20AC", src.getClassByName( "UTF8" ).getComment());
    }

    @Test
    public void testLatin1() throws Exception {
        File file = new File( "src/test/resources/com/thoughtworks/qdox/testdata/Latin1.java");
        sourceLibrary.setEncoding( "ISO-8859-1" );
        JavaSource src = sourceLibrary.addSource( file );
        Assertions.assertEquals("TEST-CHARS: \u00C4\u00D6\u00DC\u00E4\u00F6\u00FC\u00DF", src.getClassByName( "Latin1" ).getComment());
    }

    @Test
    public void testModuleInfo() throws Exception {
    	File file = new File( "src/test/resources/com/thoughtworks/qdox/testdata/module-info.java");
    	Assertions.assertNull(sourceLibrary.addSource(file), "module-info.java should be ignored");
    	Assertions.assertNull(sourceLibrary.getJavaModules());
    }
    
}
