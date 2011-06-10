# 包包分词器 - 一个基于字典的快速中文分词器

## 特性

* 简单 1000LOC
* 高效 7M+ chars/second
* 支持中文，英语，数字
* 自动识别未登录词
* 支持OffsetAttribute
* 支持TypeAttribute
* 支持PositionIncrementAttribute

## usage

```java
    Dict dict = new Dict();
    dict.addAllSpecialTypes();
    BufferedReader dictReader = new BufferedReader(new InputStreamReader(
        new FileInputStream("dict.txt"), "UTF-8"));
    dict.load(dictReader);
    dictReader.close();
    dict.optimize();
    DictAnalyzer dictAnalyzer = new DictAnalyzer(dict);
```

## benchmark

ant benchmark
<pre>
supported features:
                  CharTerm  Offset  PositionIncrement  Term  Type
      IKAnalyzer         Y       Y                  N     Y     N
   MMSegAnalyzer         Y       Y                  N     Y     Y
 PaodingAnalyzer         Y       Y                  N     Y     Y
StandardAnalyzer         Y       Y                  Y     Y     Y
  BaoBaoAnalyzer         Y       Y                  Y     Y     Y

test 1, sample length=26265
            name          chars           time         tokens speed(chars/second)
 PaodingAnalyzer          26265          0.610          12542            43036.87
   MMSegAnalyzer          26265          0.314          14007            83566.52
      IKAnalyzer          26265          0.262          16016           100177.91
StandardAnalyzer          26265          0.141          22366           185727.87
  BaoBaoAnalyzer          26265          0.038          18185           695682.16

test 2, sample length=262650
            name          chars           time         tokens speed(chars/second)
 PaodingAnalyzer         262650          0.187         125420          1402139.61
      IKAnalyzer         262650          0.163         160160          1613693.16
   MMSegAnalyzer         262650          0.158         140070          1664009.53
  BaoBaoAnalyzer         262650          0.041         181850          6362134.44
StandardAnalyzer         262650          0.020         223660         12905789.80

test 3, sample length=2626500
            name          chars           time         tokens speed(chars/second)
      IKAnalyzer        2626500          2.251        1601600          1166564.72
 PaodingAnalyzer        2626500          1.462        1254200          1796381.55
   MMSegAnalyzer        2626500          1.043        1400700          2519010.94
  BaoBaoAnalyzer        2626500          0.352        1818500          7458959.20
StandardAnalyzer        2626500          0.202        2236600         13015280.16
</pre>