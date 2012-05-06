package com.zzzhc.analyzer;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;

public class RemoveWhitespaceFilter extends TokenFilter {
	private final TypeAttribute typeAtt;

	public RemoveWhitespaceFilter(Version matchVersion, TokenStream in) {
		super(in);
		if (input.hasAttribute(TypeAttribute.class)) {
			typeAtt = input.getAttribute(TypeAttribute.class);
		} else {
			typeAtt = null;
		}
	}

	@Override
	public boolean incrementToken() throws IOException {
		while (true) {
			if (!input.incrementToken()) {
				return false;
			}
			if (typeAtt == null || Dict.WHITESPACE_TYPE != typeAtt.type()) {
				return true;
			}
		}
	}

}
