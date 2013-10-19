package com.zzzhc.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * 1. 同义词 2. 数字，英语 3. ip/host/email ?
 * 
 * @author zzzhc
 * 
 */
public class DictTokenizer extends Tokenizer {
  private Dict dict;
  private CharReader reader;
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);;
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute positionIncrementAtt= addAttribute(PositionIncrementAttribute.class);
  
  public void setDict(Dict dict) {
    this.dict = dict;
  }
  
  public DictTokenizer(Reader in) {
    super(in);
  }
  
  public DictTokenizer(AttributeFactory factory, Reader in) {
    super(factory, in);
  }
  
  public void end() {
	  reader = null;
  }
  
  public void reset() throws IOException {
    super.reset();
    
    unknown.reset();
    unknownOffset = -1;
    matchCell = null;
    cellWordOffset = 0;
    
    //offsetAtt.setOffset(0, 0);
    //positionIncrementAtt.setPositionIncrement(0);
  }
  
  public boolean incrementToken() throws IOException {
    if (reader == null) {
	  reader = new CharReader(input);
    }
    clearAttributes();
    return moveToNextToken();
  }
  
  public final static int MAX_UNKNOWN_WORD_SIZE = 500;
  private Word unknown = new Word(500);
  private int unknownOffset = -1;
  private Word pending = new Word(500);
  
  private Cell matchCell;
  private int cellWordOffset;
  
  final boolean fillAttributes() {
    int len = unknown.length();
    if (len > 0) {
      if (unknownOffset >= 0) {
        positionIncrementAtt.setPositionIncrement(0);
      }
      int offset = reader.offset();
      if (matchCell != null) {
    	  if (matchCell.type == "word") {
    		  offset -= matchCell.depth;
    	  } else {
    		  offset -= pending.length();
    	  }
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
    typeAtt.setType(matchCell.type);
    if (matchCell.type != "word") {
      termAtt.copyBuffer(pending.buffer, 0, pending.length());
      offsetAtt.setOffset(offset - pending.length(), offset);
      matchCell = null;
      cellWordOffset = 0;
    } else {
      char[][] words = matchCell.words;
      char[] word = words[cellWordOffset];
      termAtt.copyBuffer(word, 0, word.length);
      
      int start = offset - matchCell.depth + matchCell.offsets[cellWordOffset];
      offsetAtt.setOffset(start, start + word.length);
      
      cellWordOffset++;
      if (cellWordOffset == words.length) {
        matchCell = null;
        cellWordOffset = 0;
      }
    }
    return true;
  }
  
  final boolean moveToNextToken() throws IOException {
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
      		if (hasWord) {
      			reader.reset();
      			return fillAttributes();
      		}
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
            pending.reset();
            continue;
          }
          unknown.append(c);
          reader.mark();
          
          if (unknown.length() >= MAX_UNKNOWN_WORD_SIZE - 2) {
            return fillAttributes();
          }
        }
      } else {
        pending.append(c);
        if (cell.end) {
          matchCell = cell;
          return fillAttributes();
        } else if (cell.wordEnd) {
          hasWord = true;
          matchCell = cell;
          reader.mark();
        } else {
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
  
}
