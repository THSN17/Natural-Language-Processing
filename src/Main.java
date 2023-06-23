import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.trees.Tree;
import frequency.FrequencyAnalyzer;
import frequency.Reader;
import nlp.NLP;

/**
 * Main.java NLP Parsing for Summary Grading By Marc Fournier and Sabri Amer
 * 
 * Handles analysis of summary and document. Begins with keyword frequency
 * analysis. Next, does semantic triple analysis. Finally, runs parse tree
 * analysis to check for plagiarism.
 * 
 * The system scores summaries using a composite of the three analysis grades.
 */
public class Main {
	private static final String DOCNAME = "turing";
	private static final String SUMNAME = "turing-marc";

	private static final String DOCPATH = "res/" + DOCNAME + ".txt";
	private static final String SUMPATH = "res/" + SUMNAME + ".txt";

	private static final String DOCTITLE = Reader.readTitle(DOCPATH);
	private static final String[] DOCAUTHORS = Reader.readAuthors(DOCPATH);
	private static final String[] DOCTEXT = Reader.readFile(DOCPATH);
	private static final String[] SUMTEXT = Reader.readFile(SUMPATH);

	private static final double MIN_THRESHOLD = 0;
	private static final double MAX_THRESHOLD = 0.99;

	private static final double LENGTH_WEIGHT = 0.6;
	private static final double FREQ_WEIGHT = 0.3;
	private static final double TRIPLES_WEIGHT = 0.5;
	private static final double TREE_WEIGHT = 0.2;

	private static final ArrayList<RelationTriple> DOCTRIPLES = new ArrayList<>();
	private static final ArrayList<RelationTriple> SUMTRIPLES = new ArrayList<>();
	private static final ArrayList<Tree> DOCTREES = new ArrayList<>();
	private static final ArrayList<Tree> SUMTREES = new ArrayList<>();

	public static void main(String[] args) {
		double freqScore = 0, triplesScore = 0, treeScore = 0;
		double totalScore;

		if (SUMTEXT.length >= LENGTH_WEIGHT * DOCTEXT.length) {
			System.out.printf("Summary is at least %2.0f%% as long as document.%n", LENGTH_WEIGHT * 100);
			System.out.println();
			System.out.println("This typically means that it is not a good");
			System.out.println("summary, regardless of whether it passes");
			System.out.println("other tests.");
			System.out.println();
			System.out.printf("Total score: %4.1f / 100%n", 0d);
			return;
		}

		freqScore = doFrequencyAnalysis();
		// System.out.println(freqScore);

		if (freqScore < 0.07) {
			System.out.println("Low keyword score: probably did not summarize correct text.");
		} else {
			triplesScore = doTriplesAnalysis();
			treeScore = doTreeAnalysis();
		}

		totalScore = freqScore + triplesScore + treeScore;

		System.out.println("TITLE:");
		System.out.println("\t" + DOCTITLE);
		System.out.println("AUTHORS:");
		for (String s : DOCAUTHORS)
			System.out.println("\t" + s);
		System.out.println("SUMMARY:");
		System.out.println("\t" + SUMPATH);
		System.out.println();

		if (treeScore < TREE_WEIGHT) {
			System.out.println("This summary is most likely plagiarized.");
			System.out.println();
			System.out.printf("Total score:       %4.1f / 100%n", 0d);
			return;
		}

		System.out.printf("Frequency score:   %4.1f /  %2.0f%n", freqScore * 100, FREQ_WEIGHT * 100);
		System.out.printf("Triples score:     %4.1f /  %2.0f%n", triplesScore * 100, TRIPLES_WEIGHT * 100);
		System.out.printf("Tree score:        %4.1f /  %2.0f%n", treeScore * 100, TREE_WEIGHT * 100);
		System.out.printf("                  -----------%n");
		System.out.printf("Total score:       %4.1f / 100%n", totalScore * 100);
	}

	// If plagiarism is detected, the summary gets a 0
	// Otherwise, 20 free points for not cheating
	private static double doTreeAnalysis() {
		for (Tree dt : DOCTREES) {
			for (Tree st : SUMTREES) {
				if (dt.constituents().equals(st.constituents())) {
					return 0;
				}
			}
		}
		return TREE_WEIGHT;
	}

	// Eclipse doesn't like ArrayList arrays with multiple types of ArrayList in
	// them (which, honestly, is pretty understandable), so we suppress warnings for
	// them
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static double doTriplesAnalysis() {
		System.out.println("Running triples analysis...");
		String[] badSubjects = new String[] { "this", "it", "one", "we", "i", "they", "he", "she", "you" };

		StringBuilder document = new StringBuilder();
		StringBuilder summary = new StringBuilder();

		ArrayList[] docTriplesTrees;
		ArrayList[] sumTriplesTrees;

		int i = 0;
		for (String word : DOCTEXT) {
			document.append(word);
			if (word.equals(""))
				continue;
			char c = word.charAt(word.length() - 1);

			// Stanford NLP doesn't like things that are too long, so make sure they're only
			// up to about 500 words. Also make it end with a full sentence.
			if (i >= 500 && (c == '.' || c == '!' || c == '?')) {
				docTriplesTrees = NLP.getTriplesAndTrees(document.toString());
				DOCTRIPLES.addAll(docTriplesTrees[0]);
				DOCTREES.addAll(docTriplesTrees[1]);
				i = 0;
				document = new StringBuilder();
			} else {
				document.append(" ");
				i++;
			}
		}
		docTriplesTrees = NLP.getTriplesAndTrees(document.toString());
		DOCTRIPLES.addAll(docTriplesTrees[0]);
		DOCTREES.addAll(docTriplesTrees[1]);

		i = 0;
		for (String word : SUMTEXT) {
			summary.append(word);
			if (word.equals(""))
				continue;
			char c = word.charAt(word.length() - 1);

			// Stanford NLP doesn't like things that are too long, so make sure they're only
			// up to about 500 words. Also make it end with a full sentence.
			if (i >= 500 && (c == '.' || c == '!' || c == '?')) {
				sumTriplesTrees = NLP.getTriplesAndTrees(summary.toString());
				SUMTRIPLES.addAll(sumTriplesTrees[0]);
				SUMTREES.addAll(sumTriplesTrees[1]);
				i = 0;
				summary = new StringBuilder();
			} else {
				summary.append(" ");
				i++;
			}
		}
		sumTriplesTrees = NLP.getTriplesAndTrees(summary.toString());
		SUMTRIPLES.addAll(sumTriplesTrees[0]);
		SUMTREES.addAll(sumTriplesTrees[1]);

		String s;
		int matchingTriples = 0, totalTriples = DOCTRIPLES.size();
		int toAdd, maxToAdd = 0;
		for (RelationTriple doct : DOCTRIPLES) {
			s = doct.subjectGloss();
			if (Arrays.asList(badSubjects).contains(s)) {
				totalTriples--;
				continue;
			}
			maxToAdd = 0;
			for (RelationTriple sumt : SUMTRIPLES) {
				toAdd = 0;

				// Same subjects count for +1
				if (doct.subjectGloss().equals(sumt.subjectGloss())) {
					// System.out.println(doct + "\t\t" + sumt);
					toAdd = 1;

					// Same objects count for +5
					if (doct.objectGloss().equals(sumt.objectGloss())) {
						toAdd += 4;
					}
					// Same relations count for +3
					if (doct.relationGloss().equals(sumt.relationGloss())) {
						toAdd += 2;
					}
				}

				if (toAdd > maxToAdd) {
					maxToAdd = toAdd;
				}
			}
			matchingTriples += maxToAdd;
		}

		return Math.min(TRIPLES_WEIGHT, matchingTriples * 1.0 / totalTriples * TRIPLES_WEIGHT / 0.1);
	}

	// Uses BM25 (sort of)
	private static double doFrequencyAnalysis() {
		System.out.println("Running frequency analysis...");
		String[] badWords = new String[] { "the", "of", "to", "a", "an", "be", "is", "am", "are", "this", "that",
				"these", "those", "in", "it", "not", "as", "which", "one", "can", "may", "are", "were", "will", "for",
				"by", "but", "would", "should", "could", "such", "with", "if", "there", "here", "from", "any", "only",
				"just", "what", "more", "must", "between", "possible", "do", "we", "i", "have", "has", "had", "on",
				"some", "at", "no", "so", "very", "or", "and", "thus", "fuck", "might", "about", "than", "then", "make",
				"was", "our", "out", "all", "well", "cannot", "when", "rather", "its", "it's", "does", "his", "her",
				"their", "other", "say", "part", "used", "use", "uses", "user", "been", "view", "quite", "them", "give",
				"gave", "into", "in", "fact", "like", "given", "kind", "right", "how", "us" };

		LinkedHashMap<String, Integer> documentWords = FrequencyAnalyzer.analyze(DOCTEXT);
		LinkedHashMap<String, Integer> summaryWords = FrequencyAnalyzer.analyze(SUMTEXT);

		int documentNumDistinctWords = documentWords.size();
		int documentNumTotalWords = 0;
		LinkedHashMap<String, Integer> documentWordsTrimmed = new LinkedHashMap<>();
		int i = 0;
		for (String word : documentWords.keySet()) {
			if (i >= MIN_THRESHOLD * documentNumDistinctWords && i <= MAX_THRESHOLD * documentNumDistinctWords) {
				if (!Arrays.asList(badWords).contains(word)) {
					documentWordsTrimmed.put(word, documentWords.get(word));
				}
			}
			documentNumTotalWords += documentWords.get(word);
			i++;
		}

		int summaryNumDistinctWords = summaryWords.size();
		LinkedHashMap<String, Integer> summaryWordsTrimmed = new LinkedHashMap<>();
		i = 0;
		for (String word : summaryWords.keySet()) {
			if (i >= MIN_THRESHOLD * summaryNumDistinctWords && i <= MAX_THRESHOLD * summaryNumDistinctWords) {
				if (!Arrays.asList(badWords).contains(word)) {
					summaryWordsTrimmed.put(word, summaryWords.get(word));
				}
			}
			i++;
		}

		double bm25score = 0;
		double k1 = 1.6, b = 0.75;
		for (String word : summaryWordsTrimmed.keySet()) {
			double termFrequency = 0;
			if (documentWordsTrimmed.containsKey(word)) {
				termFrequency = documentWordsTrimmed.get(word) * 1.0 / documentNumTotalWords;
			} else {
				for (String author : DOCAUTHORS) {
					if (Arrays.asList(author.toLowerCase().split(" ")).contains(word)) {
						termFrequency = 0.015;
					}
				}
				if (Arrays.asList(DOCTITLE.toLowerCase().split(" ")).contains(word)) {
					termFrequency = 0.015;
				}
			}
			bm25score += IDF(word) * termFrequency * (1 + k1)
					/ (termFrequency + k1 * (1 - b + b * documentNumTotalWords / 11836.0));
		}
		return Math.min(FREQ_WEIGHT, bm25score / 2 * FREQ_WEIGHT / 0.04);
	}

	private static double IDF(String s) {
		int r = s.length() % 3;
		return Math.max(0, Math.log((2 - r + 0.5) / (r + 0.5)));
	}
}
