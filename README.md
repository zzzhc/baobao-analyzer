# 包包分词器 - 一个基于字典的快速中文分词器

## 特性

* 简单 1000LOC
* 高效 10M+ chars/second
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