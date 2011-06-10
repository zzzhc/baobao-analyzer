package com.zzzhc.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.zzzhc.analyzer.Cell.CharArrayComparator;

public class DictTest {
  private String[] words;
  private Dict dict;
  
  @Before
  public void setUp() throws Exception {
    dict = new Dict();
    words = "abcd a ab abc bc 123".split("\\s+");
    for (String word : words) {
      dict.addWord(word);
    }
  }
  
  @Test
  public void testSize() {
    assertEquals(words.length, dict.size());
    dict.addWord("test");
    assertEquals(words.length + 1, dict.size());
  }
  
  @Test
  public void testChild() {
    for (String word : words) {
      char[] chars = word.toCharArray();
      Cell cell = dict.root.child(chars[0]);
      assertNotNull(cell);
      for (int i = 1; i < chars.length; i++) {
        cell = cell.child(chars[i]);
        assertNotNull(cell);
      }
    }
  }
  
  @Test
  public void testLookupString() {
    for (String word : words) {
      Cell cell = dict.lookup(word);
      assertNotNull(cell);
    }
  }
  
  @Test
  public void testOptimize() {
    dict.optimize();
    Cell cell = dict.lookup("abcd");
    char[][] words = cell.words;
    Arrays.sort(words, CharArrayComparator.instance);
    assertEquals(5, words.length);
    int i = 0;
    for (String word : "a ab abc abcd bc".split("\\s+")) {
      assertEquals(word, new String(words[i++]));
    }
  }
  
  @Test
  public void testIterator() {
    List<String> result = new ArrayList<String>();
    for (String word : dict) {
      result.add(word);
    }
    assertEquals(words.length, result.size());
    for (String word : words) {
      assertTrue(result.contains(word));
    }
  }
  
  @Test
  public void testWhiteSpace() {
    dict.addWhiteSpace();
    for (int i = 0; i < 65536; i++) {
      char c = (char) i;
      if (Character.isWhitespace(c) || Character.isISOControl(c)) {
        Cell cell = dict.root.child(c);
        assertNotNull(cell);
        assertEquals("whitespace", cell.type);
      }
    }
  }
  
  @Test
  public void testPunctuation() {
    dict.addPunctuation();
    for (int i = 0; i < 65536; i++) {
      char c = (char) i;
      int type = Character.getType(c);
      if (type == Character.CONNECTOR_PUNCTUATION
          || type == Character.DASH_PUNCTUATION
          || type == Character.END_PUNCTUATION
          || type == Character.START_PUNCTUATION
          || type == Character.FINAL_QUOTE_PUNCTUATION
          || type == Character.INITIAL_QUOTE_PUNCTUATION
          || type == Character.OTHER_PUNCTUATION) {
        Cell cell = dict.root.child(c);
        assertNotNull(cell);
        assertEquals("punctuation", cell.type);
      }
    }
  }
  
  @Test
  public void testNumber() {
    dict.addNumber();
    String[] words = {"1", "123", "111"};
    for (String word : words) {
      Cell cell = dict.lookup(word);
      assertNotNull(cell);
      assertEquals("number", cell.type);
    }
  }
  
  @Test
  public void testEnglish() {
    dict.addEnglish();
    String[] words = {"a", "abc", "aaa", "zza", "chinese"};
    for (String word : words) {
      Cell cell = dict.lookup(word);
      assertNotNull(cell);
      assertEquals("english", cell.type);
    }
  }
  
  @Test
  public void testOO() {
    String s = "督令\n" + 
    "督促\n" + 
    "督促检查\n" + 
    "督俞\n" + 
    "督军\n" + 
    "督办\n" + 
    "督学\n" + 
    "督察\n" + 
    "督导\n" + 
    "督工\n" + 
    "督师\n" + 
    "督府\n" + 
    "督战\n" + 
    "督托\n" + 
    "督抚\n" + 
    "督率\n" + 
    "督理\n" + 
    "督署\n" + 
    "督考\n" + 
    "督脉\n" + 
    "督课\n" + 
    "督责\n" + 
    "督造\n" + 
    "督阵\n" + 
    "督饬\n";
    for (String word : s.split("\n")) {
      dict.addWord(word);
    }
    dict.optimize();
    for (String word : dict) {
      System.out.println(word);
    }
    Cell cell = dict.lookup("督促");
    for (Cell c : cell.children()) {
      System.out.println("char: " + c.c);
    }
    System.out.println(cell);
  }
}
