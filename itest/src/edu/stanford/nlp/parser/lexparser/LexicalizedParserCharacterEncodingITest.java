package edu.stanford.nlp.parser.lexparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import edu.stanford.nlp.io.IOUtils;
import junit.framework.TestCase;

/** Test that the parser does obey character encodings.
 *
 *  @author Christopher Manning
 */
public class LexicalizedParserCharacterEncodingITest extends TestCase {

  private static final String input = "café";

  private static final byte[] utf8Bytes = { 0x28, 0x52, 0x4f, 0x4f, 0x54, 0x0a,
          0x20, 0x20,  0x28, 0x4e, 0x50, 0x20, 0x28, 0x4e, 0x4e, 0x50, 0x20, 0x63, 0x61, 0x66, (byte) (0xc3 - 256), (byte) (0xa9 - 256), 0x29, 0x29, 0x29, 0x0a,
  };

  private static final byte[] iso8859Bytes = { 0x28, 0x52, 0x4f, 0x4f, 0x54, 0x0a,
          0x20, 0x20,  0x28, 0x4e, 0x50, 0x20, 0x28, 0x4e, 0x4e, 0x50, 0x20, 0x63, 0x61, 0x66, (byte) (0xe9 - 256), 0x29, 0x29, 0x29, 0x0a,
  };

  private static final byte[] gb18030Bytes = { 0x28, 0x52, 0x4f, 0x4f, 0x54, 0x0a,
          0x20, 0x20,  0x28, 0x4e, 0x50, 0x20, 0x28, 0x4e, 0x4e, 0x50, 0x20, 0x63, 0x61, 0x66, (byte) (0xa8 - 256), (byte) (0xa6 - 256), 0x29, 0x29, 0x29, 0x0a,
  };


  public void testCharEncodingUtf8() throws IOException {
    tryCharEncoding("utf-8", utf8Bytes);
  }

  public void testCharEncodingIso8859() throws IOException {
    tryCharEncoding("iso-8859-1", iso8859Bytes);
  }

  public void testCharEncodingGB18030() throws IOException {
    tryCharEncoding("gb18030", gb18030Bytes);
  }

  private static void tryCharEncoding(String encoding, byte[] expected) throws IOException {
    byte[] contents = new byte[128]; // Make big enough for something reasonable!
    File tmpInput = File.createTempFile("parser", null);
    // tmpInput.deleteOnExit();
    PrintWriter pw = IOUtils.getPrintWriter(tmpInput, encoding);
    pw.println(input);
    pw.close();

    File tmpFile = File.createTempFile("parser", null);
    System.err.println("Sending output to " + tmpFile.getCanonicalPath());
    // tmpFile.deleteOnExit();

    PrintStream ps = new PrintStream(tmpFile);
    System.setOut(ps);
    // todo: need to specify encoding on command-line to give it a chance!
    LexicalizedParser.main(new String[]{"-encoding", encoding, "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz", tmpInput.getCanonicalPath()});
    ps.close();

    InputStream is = new FileInputStream(tmpFile);
    int offset = 0;
    int numRead;
    do {
      int length = contents.length - offset;
      numRead = is.read(contents, offset, length);
      offset += numRead;
    } while (numRead > 0);
    is.close();
    for (int i = 0; i < Math.min(expected.length, offset); i++) {
      assertEquals("Byte " + i + " should be " + expected[i] + " but was " + contents[i] + ".",
              expected[i], contents[i]);
    }
    if (expected.length > offset) System.err.println("First non-received byte was " + expected[offset]);
    if (expected.length < offset) System.err.println("First wrongly received byte was " + contents[expected.length]);
    assertEquals("Was expecting " + expected.length + " bytes but got " + offset + " bytes.",
            expected.length, offset);
  }

}