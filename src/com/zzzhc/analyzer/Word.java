package com.zzzhc.analyzer;

final class Word {
  
  public final char[] buffer;
  private int length;
  
  public Word(int maxSize) {
    buffer = new char[maxSize];
  }
  
  public void append(Word word) {
    System.arraycopy(word.buffer, 0, buffer, length, word.length);
    length += word.length;
  }
  
  public void append(char c) {
    buffer[length++] = c;
  }
  
  public int length() {
    return length;
  }
  
  public void reset() {
    length = 0;
  }
  
}
