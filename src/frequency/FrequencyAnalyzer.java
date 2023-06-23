package frequency;

import java.util.LinkedHashMap;
import frequency.Counter;

public class FrequencyAnalyzer {
	static LinkedHashMap<String, Integer> words;
	static LinkedHashMap<Integer, Integer> count;
	static LinkedHashMap<String, String> percents;

	public static LinkedHashMap<String, Integer> analyze(String[] wordArr) {
		words = Counter.sort(Counter.countWords(wordArr));
		percents = Counter.countPercents(words);
		count = Counter.countValues(words);
		return words;
	}
}