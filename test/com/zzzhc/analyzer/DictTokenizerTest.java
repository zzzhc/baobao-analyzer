package com.zzzhc.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

public class DictTokenizerTest {

	@Test
	public void testIncrementToken() throws IOException {
		Dict dict = new Dict();
		for (String word : "明天 明天起 一个 幸福 ,".split("\\s+")) {
			dict.addWord(word);
		}
		dict.addWord(" ");
		dict.optimize();

		String input = "从明天起，做一个幸福的人";
		Reader in = new StringReader(input);
		DictTokenizer tokenizer = new DictTokenizer(in);
		tokenizer.setDict(dict);

		String[] expected = { "从 unknown 0-1 1", "明天起 word 1-4 1", "明天 word 1-3 0",
				", word 4-5 1", "做 unknown 5-6 1", "一个 word 6-8 1", "幸福 word 8-10 1",
				"的人 unknown 10-12 1", };

		doTest(expected, tokenizer);

		for (int i = 0; i < 3; i++) {
			tokenizer.reset(new StringReader(input));
			doTest(expected, tokenizer);
		}
	}

	@Test
	public void testIncrementToken1() throws IOException {
		Dict dict = new Dict();
		dict.addWord("明天").optimize();

		Reader in = new StringReader("从明天");
		DictTokenizer tokenizer = new DictTokenizer(in);
		tokenizer.setDict(dict);

		String[] expected = { "从 unknown 0-1 1", "明天 word 1-3 1" };
		doTest(expected, tokenizer);
	}

	@Test
	public void testIncrementToken2() throws IOException {
		Dict dict = new Dict();
		dict.addWord("明天起").addWord("开始").addWord(",").optimize();

		Reader in = new StringReader("从明天");
		DictTokenizer tokenizer = new DictTokenizer(in);
		tokenizer.setDict(dict);

		String[] expected = { "从明天 unknown 0-3 1", "从明 unknown 0-2 0",
				"明天 unknown 1-3 0" };
		doTest(expected, tokenizer);

		in = new StringReader("从明天开始");
		expected = new String[] { "从明天 unknown 0-3 1", "从明 unknown 0-2 0",
				"明天 unknown 1-3 0", "开始 word 3-5 1" };
		doTest(expected, tokenizer);
	}

	@Test
	public void testOffset() throws IOException {
		Dict dict = new Dict();
		dict.addWord("abcd").addWord("ab").addWord("bc").addWord("cd").optimize();

		Reader in = new StringReader("abcd");
		DictTokenizer tokenizer = new DictTokenizer(in);
		tokenizer.setDict(dict);
		String[] expected = { "abcd word 0-4 1", "ab word 0-2 0", "bc word 1-3 0",
				"cd word 2-4 0" };
		doTest(expected, tokenizer);
	}

	@Test
	public void testRangeCell() throws IOException {
		Dict dict = new Dict();
		dict.addAllSpecialTypes();
		Reader in = new StringReader("simple");
		DictTokenizer tokenizer = new DictTokenizer(in);
		tokenizer.setDict(dict);
		String[] expected = { "simple english 0-6 1"};
		doTest(expected, tokenizer);
	}

	@SuppressWarnings("unused")
	private List<String> tokens(DictTokenizer tokenizer) throws IOException {
		CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
		List<String> result = new ArrayList<String>();
		while (tokenizer.incrementToken()) {
			String term = new String(termAtt.buffer(), 0, termAtt.length());
			result.add(term);
		}
		return result;
	}

	private void doTest(String[] expected, DictTokenizer tokenizer)
			throws IOException {
		CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = tokenizer.getAttribute(OffsetAttribute.class);
		TypeAttribute typeAtt = tokenizer.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionAtt = tokenizer
				.getAttribute(PositionIncrementAttribute.class);
		int i = 0;
		while (tokenizer.incrementToken()) {
			assertTrue(i <= expected.length);
			String one = expected[i++];
			String[] ss = one.split("\\s+");

			String term = ss[0];
			assertEquals(term, new String(termAtt.buffer(), 0, termAtt.length()));

			String type = ss[1];
			assertEquals(type, typeAtt.type());

			String offsets[] = ss[2].split("\\-|,");
			int start = Integer.parseInt(offsets[0]);
			assertEquals(start, offsetAtt.startOffset());
			int end = Integer.parseInt(offsets[1]);
			assertEquals(end, offsetAtt.endOffset());

			int position = Integer.parseInt(ss[3]);
			assertEquals(position, positionAtt.getPositionIncrement());
		}
	}

}
