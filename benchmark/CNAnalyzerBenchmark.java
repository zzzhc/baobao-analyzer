import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

import com.zzzhc.analyzer.Dict;
import com.zzzhc.analyzer.DictAnalyzer;

public class CNAnalyzerBenchmark {
  
  public static void main(String[] args) throws IOException {
    /*
     * IKAnalyzer ikAnalyzer = new IKAnalyzer(); testAnalyzer(ikAnalyzer);
     * 
     * MMSegAnalyzer mmsegAnalyzer = new MMSegAnalyzer();
     * testAnalyzer(mmsegAnalyzer);
     * 
     * PaodingAnalyzer paodingAnalyzer = new PaodingAnalyzer();
     * testAnalyzer(paodingAnalyzer);
     * 
     * SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer(
     * Version.LUCENE_31, false); testAnalyzer(smartChineseAnalyzer);
     * 
     * StandardAnalyzer standardAnalyzer = new
     * StandardAnalyzer(Version.LUCENE_31, new HashSet<String>());
     * testAnalyzer(standardAnalyzer);
     */

    Dict dict = new Dict();
    dict.addAllSpecialTypes();
    BufferedReader dictReader = new BufferedReader(new InputStreamReader(
        new FileInputStream("dict.txt"), "UTF-8"));
    dict.load(dictReader);
    dictReader.close();
    dict.addWord("中文").addWord("西方").addWord("语言");
    dict.addWord("最大").addWord("区别").addWord("在于");
    dict.addWord("语句").addWord("词汇").addWord("之间");
    dict.addWord("没有").addWord("明显").addWord("分词");
    dict.addWord("界限").addWord("但是").addWord("计算机");
    dict.addWord("自然").addWord("语言").addWord("处理");
    dict.addWord("词汇").addWord("进行").addWord("分析");
    dict.addWord("因此").addWord("分词").addWord("效果");
    dict.addWord("直接").addWord("影响").addWord("检索");
    dict.addWord("准确性");
    dict.optimize();
    DictAnalyzer dictAnalyzer = new DictAnalyzer(dict);
    testAnalyzer(dictAnalyzer);
  }
  
  static void testAnalyzer(Analyzer a) throws IOException {
    String data = "中文(chinese)与西方语言最大的区别" + "就在于语句的词汇之间没有明显的分词界限，"
        + "但是计算机自然语言处理是按词汇来进行分析的，" + "因此中文分词的效果直接影响中文检索和自然语言处理的准确性。";
    StringBuilder ss = new StringBuilder();
    for (int i = 0; i < 1000000; i++) {
      ss.append(data);
    }
    String s = ss.toString();
    
    long startTime = System.currentTimeMillis();
    
    String attributes = "";
    TokenStream stream = a.tokenStream("", new StringReader(s));
    Iterator<Class<? extends Attribute>> iterator = stream
        .getAttributeClassesIterator();
    while (iterator.hasNext()) {
      Class<? extends Attribute> attrClass = iterator.next();
      attributes += " " + attrClass.getSimpleName();
    }
    stream = a.tokenStream("", new StringReader(s));
    while (stream.incrementToken()) {
     // CharTermAttribute att = stream.getAttribute(CharTermAttribute.class);
     // TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
     // OffsetAttribute offAtt = stream.getAttribute(OffsetAttribute.class);
     // System.out.println(att + "," + typeAtt + "," + offAtt);
    }
    long endTime = System.currentTimeMillis();
    System.out.println(a.getClass().getSimpleName() + " attributes: "
        + attributes);
    double seconds = (endTime - startTime) / 1000.0;
    System.out.println("chars=" + s.length() + ",time=" + seconds + "seconds"
        + ",speed=" + (int) (s.length() / seconds) + "chars/second");
  }
}
