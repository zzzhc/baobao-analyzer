package com.zzzhc.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


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
      System.out.println(word);
    }
    assertEquals(words.length, result.size());
    System.out.println(result);
    for (String word : words) {
      assertTrue(result.contains(word));
    }
  }
  
}
