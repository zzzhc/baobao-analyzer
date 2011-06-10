package com.zzzhc.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public abstract class Cell {
  private final static char[] EMPTY_WORD = new char[0];
  private final static char[][] EMPTY_WORDS = new char[0][];
  private final static int[] EMPTY_OFFSETS = new int[0];
  
  static class CharArrayComparator implements Comparator<char[]> {
    
    public final static CharArrayComparator instance = new CharArrayComparator();
    
    // borrow from String
    public int compare(char[] v1, char[] v2) {
      int len1 = v1.length;
      int len2 = v2.length;
      int n = Math.min(len1, len2);
      int k = 0;
      int lim = n;
      while (k < lim) {
        char c1 = v1[k];
        char c2 = v2[k];
        if (c1 != c2) {
          return c1 - c2;
        }
        k++;
      }
      return len1 - len2;
    }
    
  }
  
  public char c;
  public boolean wordEnd;
  public boolean end;
  public int depth;
  public char[] masterWord = EMPTY_WORD;
  public char[][] words = EMPTY_WORDS;
  public int[] offsets = EMPTY_OFFSETS;
  public String type;
  
  public Cell(char c) {
    this.c = c;
  }
  
  public void addWords(char[][] words) {
    for (char[] word : words) {
      addWord(word, 0);
    }
  }
  
  private boolean isWordExists(char[] word) {
    CharArrayComparator comparator = CharArrayComparator.instance;
    for (char[] w : words) {
      if (comparator.compare(w, word) == 0) {
        return true;
      }
    }
    return false;
  }
  
  public void addWord(char[] word, int offset) {
    if (isWordExists(word)) {
      return;
    }
    
    int len = words.length;
    char[][] newWords = new char[len + 1][];
    System.arraycopy(words, 0, newWords, 0, len);
    newWords[len] = word;
    
    int[] newOffsets = new int[len + 1];
    System.arraycopy(offsets, 0, newOffsets, 0, len);
    newOffsets[len] = offset;
    
    words = newWords;
    offsets = newOffsets;
    if (word.length > masterWord.length) {
      masterWord = word;
    }
  }
  
  public abstract void addChild(Cell child);
  
  public abstract Cell child(char c);
  
  public abstract Iterable<Cell> children();
  
  public int hashCode() {
    return c;
  }
  
}

final class ArrayCell extends Cell {
  
  private Cell[] children;
  
  public ArrayCell() {
    this((char) 0);
  }
  
  public ArrayCell(char c) {
    super(c);
    children = new Cell[1 << 16];
  }
  
  public void addChild(Cell child) {
    children[child.c] = child;
  }
  
  public Cell child(char c) {
    return children[c];
  }
  
  public Iterable<Cell> children() {
    ArrayList<Cell> list = new ArrayList<Cell>();
    for (Cell child : children) {
      if (child != null) {
        list.add(child);
      }
    }
    return list;
  }
}

final class CellMap implements Iterable<Cell> {
  final static int MAXIMUM_CAPACITY = 1 << 15;
  
  static class Entry {
    final Cell value;
    Entry next;
    
    public Entry(Cell value) {
      this.value = value;
    }
    
    public int hashCode() {
      return value.hashCode();
    }
  }
  
  static int indexFor(int h, int length) {
    return h & (length - 1);
  }
  
  private Entry[] table;
  private int size;
  private int threshold = 1;
  private float loadFactor = 0.75f;
  
  public CellMap() {
    table = new Entry[1];
  }
  
  public boolean add(Cell cell) {
    int i = indexFor(cell.hashCode(), table.length);
    Entry e = table[i];
    boolean doResize = e != null;
    while (e != null) {
      if (e.value.c == cell.c) {
        return false;
      }
      e = e.next;
    }
    
    Entry newEntry = new Entry(cell);
    newEntry.next = table[i];
    table[i] = newEntry;
    
    size++;
    if (doResize || size > threshold) {
      resize(2 * table.length);
    }
    
    return true;
  }
  
  private void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity >= MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }
    
    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable);
    table = newTable;
    threshold = (int) (newCapacity * loadFactor);
  }
  
  private void transfer(Entry[] newTable) {
    Entry[] src = table;
    int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++) {
      Entry e = src[j];
      if (e != null) {
        src[j] = null;
        do {
          Entry next = e.next;
          int i = indexFor(e.hashCode(), newCapacity);
          e.next = newTable[i];
          newTable[i] = e;
          e = next;
        } while (e != null);
      }
    }
  }
  
  public Cell get(char c) {
    int i = indexFor(c, table.length);
    Entry e = table[i];
    while (e != null) {
      if (e.value.c == c) {
        return e.value;
      }
      e = e.next;
    }
    return null;
  }
  
  public int size() {
    return size;
  }
  
  class CellIterator implements Iterator<Cell> {
    
    private int i;
    private Entry e;
    
    public CellIterator() {
      i = 0;
      moveToNext();
    }
    
    private void moveToNext() {
      if (e != null && e.next != null) {
        e = e.next;
        return;
      }
      while (i < table.length) {
        e = table[i++];
        if (e != null) {
          return;
        }
      }
      e = null;
    }
    
    public boolean hasNext() {
      return e != null;
    }
    
    public Cell next() {
      Cell cell = e.value;
      moveToNext();
      return cell;
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
  }
  
  public Iterator<Cell> iterator() {
    return new CellIterator();
  }
  
}

class NormalCell extends Cell {
  private static CellMap EMPTY_CELL_MAP = new CellMap();
  private CellMap children = EMPTY_CELL_MAP;
  
  public static NormalCell wordCell(String type, char c) {
    NormalCell cell = new NormalCell(c);
    cell.type = type;
    cell.wordEnd = true;
    cell.end = true;
    return cell;
  }
  
  public NormalCell(char c) {
    super(c);
  }
  
  public void addChild(Cell child) {
    if (children == EMPTY_CELL_MAP) {
      children = new CellMap();
    }
    children.add(child);
    end = false;
  }
  
  public Cell child(char c) {
    return children.get(c);
  }
  
  public Iterable<Cell> children() {
    return children;
  }
  
}

class RangeCell extends NormalCell {
  private static final char[][] EMPTY = new char[0][];
  
  private char[][] ranges = EMPTY;
  
  public RangeCell(char c) {
    super(c);
    this.wordEnd = true;
  }
  
  public void addRange(char from, char to) {
    char[][] newRanges = new char[ranges.length + 1][2];
    System.arraycopy(ranges, 0, newRanges, 0, ranges.length);
    char[] range = newRanges[ranges.length];
    range[0] = from;
    range[1] = to;
    ranges = newRanges;
  }
  
  public Cell child(char c) {
    Cell cell = super.child(c);
    if (cell != null) {
      return cell;
    }
    
    for (int i = 0; i < ranges.length; i++) {
      char[] range = ranges[i];
      if (c >= range[0] && c <= range[1]) {
        return this;
      }
    }
    return null;
  }
  
}