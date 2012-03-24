package com.zzzhc.solr;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.solr.analysis.BaseTokenizerFactory;
import org.apache.solr.analysis.TokenizerFactory;

import com.zzzhc.analyzer.Dict;
import com.zzzhc.analyzer.DictTokenizer;

public class BaoBaoTokenizerFactory extends BaseTokenizerFactory implements
		TokenizerFactory {

	private Dict dict;

	public Dict getDict() {
		return dict;
	}

	@Override
	public Tokenizer create(Reader input) {
		DictTokenizer tokenizer = new DictTokenizer(input);
		tokenizer.setDict(dict);
		return tokenizer;
	}

	@Override
	public void init(Map<String, String> args) {
		super.init(args);
		String dictFile = args.get("dictFile");
		String encoding = "UTF-8";
		if (args.containsKey("encoding")) {
			encoding = args.get("encoding");
		}

		dict = new Dict();
		dict.addAllSpecialTypes();
		try {
			if (dictFile == null) {
				log.error("dictFile is required!!");
			} else {
				loadFiles(dictFile, Dict.NORMAL_TYPE, encoding);
			}
			dict.optimize();

			String stopwordFile = args.get("stopwordFile");
			if (stopwordFile != null) {
				loadFiles(stopwordFile, Dict.STOPWORD_TYPE, encoding);
			}
		} catch (Exception e) {
			log.error("load dict failed", e);
		}
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
			log.error("file(" + filePath + ") doesn't exist");
			return;
		}
		if (!file.isFile()) {
			log.error("file(" + filePath + ") is not a regular file");
			return;
		}
		dict.loadFromFile(file, type, encoding);
	}

}
