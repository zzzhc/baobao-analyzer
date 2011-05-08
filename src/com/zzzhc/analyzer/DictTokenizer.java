package com.zzzhc.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;


/**
 * 1. 同义词 2. 数字，英语 3. ip/host/email ?
 * 
 * @author zzzhc
 * 
 */
public class DictTokenizer extends Tokenizer {
  private Dict dict;
  private CharReader reader;
  
  private CharTermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private TypeAttribute typeAtt;
  private PositionIncrementAttribute positionIncrementAtt;
  
  public void setDictionary(Dict dict) {
    this.dict = dict;
  }
  
  public DictTokenizer(Reader in) {
    super(in);
    init();
  }
  
  public DictTokenizer(AttributeSource source, Reader in) {
    super(source, in);
    init();
  }
  
  public DictTokenizer(AttributeFactory factory, Reader in) {
    super(factory, in);
    init();
  }
  
  private void init() {
    reader = new CharReader(input);
    termAtt = addAttribute(CharTermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
    positionIncrementAtt = addAttribute(PositionIncrementAttribute.class);
  }
  
  public void reset(Reader in) throws IOException {
    super.reset(in);
    reader.setReader(in);
    unknown.reset();
    unknownOffset = -1;
    matchCell = null;
    cellWordOffset = 0;
  }
  
  public boolean incrementToken() throws IOException {
    clearAttributes();
    return moveToNextToken();
  }
  
  public final static int MAX_UNKNOWN_WORD_SIZE = 500;
  private Word unknown = new Word(500);
  private int unknownOffset = -1;
  private Word pending = new Word(500);
  
  private Cell matchCell;
  private int cellWordOffset;
  
  boolean fillAttributes() {
    int len = unknown.length();
    if (len > 0) {
      if (unknownOffset >= 0) {
        positionIncrementAtt.setPositionIncrement(0);
      }
      int offset = reader.offset();
      if (matchCell != null) {
        offset -= matchCell.depth;
      }
      
      if (unknownOffset == -1) {
        termAtt.copyBuffer(unknown.buffer, 0, len);
        offsetAtt.setOffset(offset - len, offset);
      } else {
        termAtt.copyBuffer(unknown.buffer, unknownOffset, 2);
        offset = offset - len + unknownOffset;
        offsetAtt.setOffset(offset, offset + 2);
      }
      unknownOffset++;
      typeAtt.setType("unknown");
      
      if (len <= 2 || unknownOffset >= len - 1) {
        unknown.reset();
        unknownOffset = -1;
      }
      
      return true;
    }
    
    if (matchCell == null) {
      return false;
    }
    int offset = reader.offset();
    if (cellWordOffset > 0) {
      positionIncrementAtt.setPositionIncrement(0);
    }
    char[][] words = matchCell.words;
    char[] word = words[cellWordOffset++];
    termAtt.copyBuffer(word, 0, word.length);
    typeAtt.setType("word");
    int start = offset - matchCell.depth;
    offsetAtt.setOffset(start, start + word.length);
    
    if (cellWordOffset == words.length) {
      matchCell = null;
      cellWordOffset = 0;
    }
    return true;
  }
  
  boolean moveToNextToken() throws IOException {
    if (fillAttributes()) {
      return true;
    }
    
    int i;
    char c;
    Cell cell = dict.root;
    boolean hasWord = false;
    boolean hasPending = false;
    pending.reset();
    
    for (;;) {
      i = reader.read();
      if (i == CharReader.EOF) {
        break;
      }
      
      c = (char) i;
      cell = cell.child(c);
      if (cell == null) {
        if (hasWord) {
          reader.reset();
          return fillAttributes();
        } else {
          cell = dict.root;
          if (hasPending) {
            hasPending = false;
            unknown.append(pending.buffer[0]);
            reader.reset();
            continue;
          }
          unknown.append(c);
          reader.mark();
          
          if (unknown.length() >= MAX_UNKNOWN_WORD_SIZE - 2) {
            return fillAttributes();
          }
        }
      } else {
        if (cell.end) {
          matchCell = cell;
          return fillAttributes();
        } else if (cell.wordEnd) {
          hasWord = true;
          matchCell = cell;
          reader.mark();
        } else {
          pending.append(c);
          if (!hasPending) {
            hasPending = true;
            reader.mark();
          }
        }
      }
    }
    if (pending.length() > 0) {
      unknown.append(pending);
    }
    return fillAttributes();
  }
  
  public static void main(String[] args) throws IOException {
    Dict dict = new Dict();
    dict.addWord("中国");
    dict.addWord("中国人");
    dict.addWord("我们");
    dict.addWord("一丁点");
    dict.optimize();
    Reader in = new StringReader("--一丁我们是abc中国123中国人");
    DictTokenizer tokenizer = new DictTokenizer(in);
    tokenizer.setDictionary(dict);
    while (tokenizer.incrementToken()) {
      CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
      OffsetAttribute offsetAtt = tokenizer.getAttribute(OffsetAttribute.class);
      TypeAttribute typeAtt = tokenizer.getAttribute(TypeAttribute.class);
      PositionIncrementAttribute position = tokenizer
          .getAttribute(PositionIncrementAttribute.class);
      
      System.out.println(term + " " + offsetAtt + "," + typeAtt + " "
          + position);
    }
  }
  
}
