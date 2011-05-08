package com.zzzhc.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.ReusableAnalyzerBase;

public class DictAnalyzer extends ReusableAnalyzerBase {
  
  private Dict dict;
  
  public DictAnalyzer(Dict dict) {
    this.dict = dict;
  }
  
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final DictTokenizer source = new DictTokenizer(reader);
    source.setDictionary(dict);
    return new TokenStreamComponents(source);
  }
}
