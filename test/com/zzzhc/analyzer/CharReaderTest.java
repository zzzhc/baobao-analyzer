package com.zzzhc.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class CharReaderTest {
  private String input;
  private CharReader reader;
  
  @Before
  public void setUp() throws Exception {
    input = "abc";
    reader = new CharReader(new StringReader(input));
  }
  
  @Test
  public void testRead() throws IOException {
    assertEquals(0, reader.offset());
    for (int i = 0; i < input.length(); i++) {
      char c = (char) reader.read();
      assertEquals(input.charAt(i), c);
      assertEquals(i + 1, reader.offset());
    }
    assertEquals(CharReader.EOF, reader.read());
    assertEquals(input.length(), reader.offset());
  }
  
  @Test
  public void testSetReader() throws IOException {
    reader.read();
    reader.mark();
    while (reader.read() != CharReader.EOF) {}
    reader.setReader(new StringReader(input));
    assertEquals(0, reader.offset());
    assertFalse(reader.reset());
    assertEquals('a', (char)reader.read());
  }
  
  @Test
  public void testReadQuanJiao() throws IOException {
    reader.setReader(new StringReader("， "));
    assertEquals(',', (char) reader.read());
    assertEquals(' ', (char) reader.read());
  }
  
  @Test
  public void testMarkReset() throws IOException {
    reader.read();
    reader.mark();
    reader.read();
    reader.reset();
    assertEquals(1, reader.offset());
    assertEquals('b', (char) reader.read());
  }
  
  @Test
  public void testMarkOverBufferSize() throws IOException {
    input = "abcdefg";
    reader = new CharReader(new StringReader(input), 2);
    reader.read();
    reader.mark();
    for (int i = 0; i < 3; i++) {
      reader.read();
    }
    reader.reset();
    assertEquals(4, reader.offset());
    assertEquals('e', (char) reader.read());
  }
  
}
