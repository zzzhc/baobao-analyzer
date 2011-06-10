package com.zzzhc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.zzzhc.analyzer.Dict;
import com.zzzhc.analyzer.BaoBaoAnalyzer;

public class CNAnalyzerBenchmark {
  
  static class Result implements Comparable<Result> {
    Analyzer analyzer;
    String sample;
    double time;
    int tokenNum;
    Set<String> tokenSet = new HashSet<String>();
    
    public Result(Analyzer analyzer, String sample) {
      this.analyzer = analyzer;
      this.sample = sample;
    }
    
    public double speed() {
      return sample.length() / time;
    }
    
    public void addToken(CharTermAttribute termAtt) {
      tokenNum++;
      // String token = new String(termAtt.buffer(), 0,
      // termAtt.length()).intern();
      // tokenSet.add(token);
    }
    
    @Override
    public int compareTo(Result o) {
      return (int) ((o.time - this.time) * 1000);
    }
  }
  
  private String sample;
  private List<Analyzer> analyzers = new ArrayList<Analyzer>();
  
  public CNAnalyzerBenchmark(String sample) {
    this.sample = sample;
  }
  
  public void addAnalyzer(Analyzer a) {
    analyzers.add(a);
  }
  
  private List<String> loadFeatures(Analyzer analyzer) {
    List<String> features = new ArrayList<String>();
    TokenStream stream = analyzer.tokenStream("", new StringReader("abc"));
    Iterator<Class<? extends Attribute>> iterator = stream
        .getAttributeClassesIterator();
    while (iterator.hasNext()) {
      Class<? extends Attribute> attrClass = iterator.next();
      features.add(attrClass.getSimpleName().replace("Attribute", ""));
    }
    Collections.sort(features);
    return features;
  }
  
  void reportFeatures() {
    Map<Analyzer,List<String>> result = new HashMap<Analyzer,List<String>>();
    Set<String> set = new HashSet<String>();
    for (Analyzer a : analyzers) {
      List<String> features = loadFeatures(a);
      result.put(a, features);
      set.addAll(features);
    }
    List<String> features = new ArrayList<String>(set);
    Collections.sort(features);
    
    String format = analyzerNameFormat();
    for (String feature : features) {
      format += "%" + (feature.length() + 2) + "s";
    }
    format += "\n";
    
    List<Object> objs = new ArrayList<Object>();
    
    System.out.println("supported features:");
    objs.add("");
    objs.addAll(features);
    System.out.printf(format, objs.toArray());
    
    for (Analyzer a : analyzers) {
      objs.clear();
      objs.add(analyzerName(a));
      List<String> list = result.get(a);
      for (String feature : features) {
        if (list.contains(feature)) {
          objs.add("Y");
        } else {
          objs.add("N");
        }
      }
      System.out.printf(format, objs.toArray());
    }
    
    System.out.println();
  }
  
  String analyzerName(Analyzer a) {
    return a.getClass().getSimpleName();
  }
  
  String analyzerNameFormat() {
    int max = 0;
    for (Analyzer a : analyzers) {
      int len = analyzerName(a).length();
      if (len > max) {
        max = len;
      }
    }
    return "%" + max + "s";
  }
  
  Result runAnalyzer(Analyzer analyzer, String data) throws IOException {
    Result result = new Result(analyzer, data);
    
    Runtime.getRuntime().gc();
    //System.out.println(analyzerName(analyzer));
    long startTime = System.nanoTime();
    TokenStream stream = analyzer.tokenStream("", new StringReader(data));
    CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
    while (stream.incrementToken()) {
      result.addToken(termAtt);
    }
    long endTime = System.nanoTime();
    result.time = (endTime - startTime) / 1000000000.0;
    return result;
  }
  
  void report(Map<Analyzer,Result> results, String label) {
    System.out.println(label);
    // name,chars,time,tokens,speed
    System.out.printf(analyzerNameFormat() + "%15s%15s%15s%20s\n", "name",
        "chars", "time", "tokens", "speed(chars/second)");
    
    String format = analyzerNameFormat();
    format += "%15d%15.3f%15d%20.2f\n";
    
    List<Result> values = new ArrayList<CNAnalyzerBenchmark.Result>(
        results.values());
    Collections.sort(values);
    
    List<Object> objs = new ArrayList<Object>();
    for (Result result : values) {
      objs.clear();
      objs.add(analyzerName(result.analyzer));
      objs.add(result.sample.length());
      objs.add(result.time);
      objs.add(result.tokenNum);
      objs.add(result.speed());
      System.out.printf(format, objs.toArray());
    }
    System.out.println();
  }
  
  public void run() throws IOException {
    reportFeatures();
    
    int batch = 0;
    for (int i = 1; i <= 100; i *= 10) {
      batch++;
      StringBuilder ss = new StringBuilder();
      for (int j = 0; j < i; j++) {
        ss.append(sample);
      }
      String data = ss.toString();
      Map<Analyzer,Result> results = new HashMap<Analyzer,CNAnalyzerBenchmark.Result>();
      for (Analyzer a : analyzers) {
        Result result = runAnalyzer(a, data);
        results.put(a, result);
      }
      report(results, "test " + batch + ", sample length=" + data.length());
    }
  }
  
  
  public static void main(String[] args) throws IOException {
    disableLog();
    
    String sample = loadSample();
    CNAnalyzerBenchmark benchmark = new CNAnalyzerBenchmark(sample);
    
    IKAnalyzer ikAnalyzer = new IKAnalyzer();
    benchmark.addAnalyzer(ikAnalyzer);
    
    MMSegAnalyzer mmsegAnalyzer = new MMSegAnalyzer();
    benchmark.addAnalyzer(mmsegAnalyzer);
    
    PaodingAnalyzer paodingAnalyzer = new PaodingAnalyzer();
    benchmark.addAnalyzer(paodingAnalyzer);
    
    StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_31,
        new HashSet<String>());
    benchmark.addAnalyzer(standardAnalyzer);
    
    Dict dict = new Dict();
    dict.addAllSpecialTypes();
    BufferedReader dictReader = new BufferedReader(new InputStreamReader(
        new FileInputStream("benchmark/3rd/dic/t-base.dic"), "UTF-8"));
    dict.load(dictReader);
    dictReader.close();
    dict.optimize();
    dict.addStopWords(loadWords("benchmark/stopword.txt"));
    
    BaoBaoAnalyzer dictAnalyzer = new BaoBaoAnalyzer(dict);
    benchmark.addAnalyzer(dictAnalyzer);
    
    benchmark.run();
  }

  static void disableLog() {
    Logger globalLogger = Logger.getLogger("");
    Handler[] handlers = globalLogger.getHandlers();
    for (Handler handler : handlers) {
      globalLogger.removeHandler(handler);
    }
  }
  
  static String loadSample() throws IOException {
    StringBuilder ss = new StringBuilder();
    File dir = new File("benchmark/data");
    char[] buf = new char[8 * 1024];
    for (File f : dir.listFiles()) {
      Reader reader = new BufferedReader(new InputStreamReader(
          new FileInputStream(f), "UTF-8"));
      int len = 0;
      while ((len = reader.read(buf)) != -1) {
        ss.append(buf, 0, len);
      }
    }
    return ss.toString();
  }
  
  static Collection<String> loadWords(String path) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(path), "UTF-8"));
    
    List<String> words = new ArrayList<String>();
    String word = null;
    while ((word = reader.readLine()) != null) {
      words.add(word.trim());
    }
    return words;
  }
}
