package com.zzzhc.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

public class BaoBaoAnalyzer extends Analyzer {
  
  private Dict dict;
  
  public BaoBaoAnalyzer(Dict dict) {
    this.dict = dict;
  }
  
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final DictTokenizer source = new DictTokenizer(reader);
    source.setDict(dict);
    return new TokenStreamComponents(source);
  }
}
