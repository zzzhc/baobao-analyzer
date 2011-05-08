package com.zzzhc.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CellMapTest {
  private CellMap map;
  
  @Before
  public void setUp() throws Exception {
    map = new CellMap();
  }
  
  @Test
  public void testAddGet() {
    int max = 1 << 16 - 1;
    for (int i = 0; i < max; i++) {
      map.add(new NormalCell((char) i));
    }
    assertEquals(max, map.size());
    for (int i = 0; i < max; i++) {
      Cell cell = map.get((char) i);
      assertNotNull(cell);
      assertEquals((char) i, cell.c);
    }
  }
  
  @Test
  public void testIterator() {
    String input = "abcdefg";
    for (int i = 0; i < input.length(); i++) {
      map.add(new NormalCell(input.charAt(i)));
    }
    List<Character> list = new ArrayList<Character>();
    for (Cell cell : map) {
      list.add(cell.c);
    }
    for (int i = 0; i < input.length(); i++) {
      list.contains(Character.valueOf((char) i));
    }
  }
  
}
