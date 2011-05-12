package com.zzzhc.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Stack;

public class Dict implements Iterable<String> {
  public final Cell root;
  private int size;
  private boolean optimized;
  
  public Dict() {
    root = new ArrayCell();
  }
  
  public int size() {
    return size;
  }
  
  public Dict addWord(String word) {
    int len = word.length();
    if (len == 0) {
      return this;
    }
    
    char[] chars = word.toCharArray();
    Cell cell = root;
    char c;
    boolean exists = true;
    for (int i = 0; i < len; i++) {
      c = chars[i];
      Cell child = cell.child(c);
      if (child == null) {
        child = new NormalCell(c);
        child.depth = i + 1;
        cell.addChild(child);
        exists = false;
      }
      cell = child;
    }
    
    if (!cell.wordEnd) {
      size++;
      optimized = false;
      cell.wordEnd = true;
      cell.type = "word";
    }
    if (!exists) {
      cell.end = true;
    }
    return this;
  }
  
  public Dict addWhiteSpace() {
    int max = 1 << 16;
    for (int i = 0; i < max; i++) {
      char c = (char) i;
      if (Character.isWhitespace(c) || Character.isISOControl(c)) {
        Cell cell = root.child(c);
        if (cell == null) {
          size++;
          root.addChild(NormalCell.wordCell("whitespace", c));
        }
      }
    }
    return this;
  }
  
  private boolean isPunctuation(int c) {
    int type = Character.getType(c);
    return type == Character.CONNECTOR_PUNCTUATION
        || type == Character.DASH_PUNCTUATION
        || type == Character.END_PUNCTUATION
        || type == Character.START_PUNCTUATION
        || type == Character.FINAL_QUOTE_PUNCTUATION
        || type == Character.INITIAL_QUOTE_PUNCTUATION
        || type == Character.OTHER_PUNCTUATION;
  }
  
  public Dict addPunctuation() {
    int max = 1 << 16;
    for (int i = 0; i < max; i++) {
      if (isPunctuation(i)) {
        char c = (char) i;
        Cell cell = root.child(c);
        if (cell == null) {
          size++;
          root.addChild(NormalCell.wordCell("punctuation", c));
        }
      }
    }
    return this;
  }
  
  private final static char[][] NUMBER_RANGES;
  
  static {
    NUMBER_RANGES = new char[5][];
    NUMBER_RANGES[0] = new char[] {'\u0030', '\u0039'};
    NUMBER_RANGES[1] = new char[] {'\u0660', '\u0669'};
    NUMBER_RANGES[2] = new char[] {'\u06F0', '\u06F9'};
    NUMBER_RANGES[3] = new char[] {'\u0966', '\u096F'};
    NUMBER_RANGES[4] = new char[] {'\uFF10', '\uFF19'};
  }
  
  public Dict addNumber() {
    for (int i = 0; i < NUMBER_RANGES.length; i++) {
      char[] range = NUMBER_RANGES[i];
      for (char c = range[0]; c <= range[1]; c++) {
        RangeCell cell = new RangeCell(c);
        cell.type = "number";
        for (int j = 0; j < NUMBER_RANGES.length; j++) {
          cell.addRange(NUMBER_RANGES[j][0], NUMBER_RANGES[j][1]);
        }
        root.addChild(cell);
      }
    }
    return this;
  }
  
  public Dict addEnglish() {
    for (char c = 'a'; c <= 'z'; c++) {
      RangeCell cell = new RangeCell(c);
      cell.type = "english";
      cell.addRange('a', 'z');
      root.addChild(cell);
    }
    return this;
  }
  
  public Dict addAllSpecialTypes() {
    addWhiteSpace();
    addPunctuation();
    addNumber();
    addEnglish();
    return this;
  }
  
  public Cell lookup(String word) {
    return lookup(word.toCharArray(), 0, word.length());
  }
  
  public Cell lookup(char[] word, int offset, int len) {
    return lookup0(root, word, offset, len);
  }
  
  private Cell lookup0(Cell cell, char[] word, int offset, int len) {
    char c = word[offset++];
    Cell child = cell.child(c);
    if (child == null) {
      return cell;
    }
    if (offset == len) {
      return child;
    }
    return lookup0(child, word, offset, len);
  }
  
  public boolean isOptimized() {
    return optimized;
  }
  
  public void optimize() {
    char[][] words = new char[0][];
    StringBuilder word = new StringBuilder();
    optimize0(root, words, word);
    
    optimize1(root);
    // TODO, repeat optimize1 ?
  }
  
  private void optimize0(Cell cell, char[][] words, StringBuilder word) {
    int len = word.length();
    if (cell.wordEnd) {
      cell.addWords(words);
      cell.addWord(word.toString().toCharArray());
      words = cell.words;
    }
    for (Cell child : cell.children()) {
      word.append(child.c);
      optimize0(child, words, word);
      word.setLength(len);
    }
  }
  
  private void optimize1(Cell cell) {
    if (cell.end) {
      char[][] words = cell.words;
      if (words.length > 0) {
        char[] word = words[words.length - 1];
        int len = word.length;
        for (int i = 1; i < len; i++) {
          Cell result = lookup(word, i, len);
          if (result != null && result.wordEnd) {
            cell.addWords(result.words);
          }
        }
      }
    }
    for (Cell child : cell.children()) {
      optimize1(child);
    }
  }
  
  public void load(Reader in) throws IOException {
    BufferedReader reader = new BufferedReader(in);
    String word = null;
    while ((word = reader.readLine()) != null) {
      addWord(word);
    }
  }
  
  private class WordIterator implements Iterator<String> {
    private Stack<Iterator<Cell>> stack = new Stack<Iterator<Cell>>();
    private Stack<Character> chars = new Stack<Character>();
    private Cell cell;
    private Iterator<Cell> children;
    
    private boolean hasNext = false;
    
    public WordIterator() {
      cell = root;
      children = cell.children().iterator();
      hasNext = moveToNext();
    }
    
    private String word() {
      StringBuilder ss = new StringBuilder();
      for (Character c : chars) {
        ss.append(c.charValue());
      }
      return ss.toString();
    }
    
    private boolean moveToNext() {
      for (;;) {
        if (children.hasNext()) {
          Cell child = children.next();
          stack.push(children);
          chars.push(child.c);
          children = child.children().iterator();
          
          if (child.wordEnd) {
            return true;
          }
        } else {
          if (stack.size() == 0) {
            return false;
          }
          children = stack.pop();
          chars.pop();
        }
      }
    }
    
    public boolean hasNext() {
      return hasNext;
    }
    
    public String next() {
      String word = word();
      hasNext = moveToNext();
      return word;
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
  }
  
  public Iterator<String> iterator() {
    return new WordIterator();
  }
  
}
