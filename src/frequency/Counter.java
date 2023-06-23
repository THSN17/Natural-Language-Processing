package frequency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Counter {
	public static LinkedHashMap<String, Integer> countWords(String[] words) {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
		for (String word : words) {
			if (map.containsKey(word)) {
				map.put(word, map.get(word).intValue() + 1);
			} else {
				map.put(word, 1);
			}
		}
		return map;
	}

	public static LinkedHashMap<Integer, Integer> countValues(HashMap<String, Integer> wordmap) {
		LinkedHashMap<Integer, Integer> count = new LinkedHashMap<Integer, Integer>();
		for (Integer i : wordmap.values()) {
			if (count.containsKey(i)) {
				count.put(i, count.get(i).intValue() + 1);
			} else {
				count.put(i, 1);
			}
		}
		return count;
	}

	public static LinkedHashMap<String, String> countPercents(HashMap<String, Integer> wordmap) {
		LinkedHashMap<String, String> lawmap = new LinkedHashMap<String, String>();
		int total = 0;
		for (String word : wordmap.keySet()) {
			total += wordmap.get(word).intValue();
		}
		for (String word : wordmap.keySet()) {
			lawmap.put(word, round(wordmap.get(word) / ((double) total) * 100, 5));
		}
		return lawmap;
	}

	public static LinkedHashMap<String, Integer> sort(HashMap<String, Integer> count) {
		HashMap<String, Integer> use = count;
		List<Integer> sortedValue = new ArrayList<Integer>(use.values());
		Collections.sort(sortedValue);

		LinkedHashMap<String, Integer> sortedLawmap = new LinkedHashMap<String, Integer>();
		for (Integer value : sortedValue) {
			for (String key : use.keySet()) {
				if (count.get(key) == value) {
					sortedLawmap.put(key, value);
					use.remove(key);
					break;
				}
			}
		}
		return sortedLawmap;
	}

	public static String round(double value, int places) {
		if (places < 0)
			return "" + value;
		String rounded = "" + (new BigDecimal(value).setScale(places, BigDecimal.ROUND_HALF_UP)).doubleValue();
		while (rounded.split("\\.")[1].length() < places)
			rounded += "0";
		return rounded;
	}
}