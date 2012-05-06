package com.zzzhc.analyzer;

import java.io.IOException;
import java.io.Reader;

final class CharReader {
  
  public final static int EOF = -1;
  private static int BUFFER_SIZE = 8 * 1024;
  private static int UNMARKED = -1;
  
  private Reader in;
  private int offset;
  private int markOffset;
  
  private char[] buf;
  private int bufLen;
  
  private int mark;
  private int index;
  
  private boolean eof;
  
  public CharReader(Reader in) {
    this(in, BUFFER_SIZE);
  }
  
  public CharReader(Reader in, int bufferSize) {
    buf = new char[bufferSize];
    setReader(in);
  }
  
  public int read() throws IOException {
    if (index == bufLen) {
      fillBuffer();
    }
    if (eof && index == bufLen) {
      return EOF;
    }
    
    char c = buf[index++];
    offset++;
    /*
     * 全角字符从的unicode编码从65281~65374 半角字符从的unicode编码从 33~126 空格比较特殊,全角为 12288,半角为
     * 32 而且除空格外,全角/半角按unicode编码排序在顺序上是对应的 所以可以直接通过用+-法来处理非空格数据,对空格单独处理
     */
    if (c > 65280 && c < 65375) {
      c = (char) (c - 65248);
    }
    if (c >= 'A' && c <= 'Z') {
      c += 32;
    } else if (c == 12288) {
      c = 32;
    }
    return c;
  }
  
  private void fillBuffer() throws IOException {
    if (mark == 0) {
      mark = UNMARKED;
      markOffset = 0;
    }
    if (mark != UNMARKED) {
      System.arraycopy(buf, mark, buf, 0, bufLen - mark);
      bufLen -= mark;
      index -= mark;
      mark = 0;
    } else {
      bufLen = 0;
      index = 0;
    }
    for (;;) {
      int len = in.read(buf, bufLen, buf.length - bufLen);
      if (len == -1) {
        eof = true;
        break;
      }
      if (len == 0) {
        continue;
      }
      bufLen += len;
      break;
    }
  }
  
  public void mark() {
    mark = index;
    markOffset = offset;
  }
  
  public boolean reset() {
    if (mark == UNMARKED) {
      return false;
    }
    index = mark;
    offset = markOffset;
    return true;
  }
  
  public void setReader(Reader in) {
    this.in = in;
    bufLen = 0;
    mark = UNMARKED;
    markOffset = 0;
    index = 0;
    eof = false;
    offset = 0;
  }
  
  public int offset() {
    return offset;
  }
  
  public final boolean eof() {
  		return eof;
  }
  
}
