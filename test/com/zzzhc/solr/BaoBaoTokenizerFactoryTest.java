package com.zzzhc.solr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class BaoBaoTokenizerFactoryTest {

	@Test
	public void testFactory() throws IOException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("dictFile", "benchmark/3rd/dic/t-base.dic");
		args.put("encoding", "UTF-8");
		args.put("stopwordFile", "benchmark/stopword.txt");

		BaoBaoTokenizerFactory factory = new BaoBaoTokenizerFactory(args);
		assertNotNull(factory.getDict());
		assertEquals(args, factory.getOriginalArgs());

		StringReader input = new StringReader("一一列举");
		Tokenizer tokenizer = factory.create(input);
		assertTrue(tokenizer.incrementToken());
		CharTermAttribute attr = tokenizer
				.getAttribute(CharTermAttribute.class);
		assertEquals("一一列举", attr.toString());
	}

}
