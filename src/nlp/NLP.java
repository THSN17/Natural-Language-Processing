package nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class NLP {

	/**
	 * @param <E>
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList[] getTriplesAndTrees(String doc) {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse, lemma, depparse, natlog, openie");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(doc);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values
		// with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		ArrayList[] triplesAndTrees = new ArrayList[2];
		ArrayList<RelationTriple> allTriples = new ArrayList<>();
		ArrayList<Tree> allTrees = new ArrayList<>();
		for (CoreMap sentence : sentences) {
			Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
			allTriples.addAll(triples);

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			allTrees.add(tree);
		}
		triplesAndTrees[0] = allTriples;
		triplesAndTrees[1] = allTrees;
		return triplesAndTrees;
	}

}