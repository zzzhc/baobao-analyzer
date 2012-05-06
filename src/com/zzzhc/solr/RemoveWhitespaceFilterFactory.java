package com.zzzhc.solr;

import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.BaseTokenFilterFactory;

import com.zzzhc.analyzer.RemoveWhitespaceFilter;

public class RemoveWhitespaceFilterFactory extends BaseTokenFilterFactory {

	@Override
	public TokenStream create(TokenStream in) {
		return new RemoveWhitespaceFilter(luceneMatchVersion, in);
	}

}
