package com.zzzhc.solr;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import com.zzzhc.analyzer.RemoveWhitespaceFilter;

public class RemoveWhitespaceFilterFactory extends TokenFilterFactory {

	public RemoveWhitespaceFilterFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public TokenStream create(TokenStream in) {
		return new RemoveWhitespaceFilter(luceneMatchVersion, in);
	}

}
