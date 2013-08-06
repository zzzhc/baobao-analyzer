package com.zzzhc.solr;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

import com.zzzhc.analyzer.Dict;
import com.zzzhc.analyzer.DictTokenizer;

public class BaoBaoTokenizerFactory extends TokenizerFactory {

	private static Map<String, Dict> sharedDicts = new HashMap<String, Dict>();

	private Dict dict;

	public Dict getDict() {
		return dict;
	}

	public BaoBaoTokenizerFactory(Map<String, String> args) {
		super(args);
		synchronized (BaoBaoTokenizerFactory.class) {
			String dictFile = args.get("dictFile");
			if (sharedDicts.containsKey(dictFile)) {
				dict = sharedDicts.get(dictFile);
			} else {
				loadDict(args);
			}
		}
	}

	private void loadDict(Map<String, String> args) {
		String dictFile = args.get("dictFile");
		String encoding = "UTF-8";
		if (args.containsKey("encoding")) {
			encoding = args.get("encoding");
		}

		dict = new Dict();
		dict.addAllSpecialTypes();
		try {
			if (dictFile == null) {
				throw new RuntimeException("dictFile is required!!");
			} else {
				loadFiles(dictFile, Dict.NORMAL_TYPE, encoding);
			}
			dict.optimize();

			String stopwordFile = args.get("stopwordFile");
			if (stopwordFile != null) {
				loadFiles(stopwordFile, Dict.STOPWORD_TYPE, encoding);
			}
		} catch (Exception e) {
			throw new RuntimeException("load dict failed", e);
		}
		sharedDicts.put(dictFile, dict);
	}

	private void loadFiles(String filePath, String type, String encoding)
			throws IOException {
		for (String f : filePath.split(":")) {
			loadFile(f, type, encoding);
		}
	}

	private void loadFile(String filePath, String type, String encoding)
			throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new RuntimeException("file(" + filePath + ") doesn't exist");
		}
		if (!file.isFile()) {
			throw new RuntimeException("file(" + filePath
					+ ") is not a regular file");
		}
		dict.loadFromFile(file, type, encoding);
	}

	@Override
	public Tokenizer create(AttributeFactory factory, Reader input) {
		DictTokenizer tokenizer = new DictTokenizer(factory, input);
		tokenizer.setDict(dict);
		return tokenizer;
	}

}
