package com.ibm.cloudoe.samples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rita.wordnet.RiWordnet;

@Path("/service")
public class ServiceController {

	public static void main(String[] args) throws InvalidFormatException,
			IOException, JSONException {
		// TODO Auto-generated method stub

		System.out.println(new ServiceController()
		.getParents("pug", "doberman"));
		
//		System.out.println(new ServiceController()
//				.getNgram("Obama says he learned of Clinton using private email through news reports"));
	}

	@GET
	public String getMessage() {
		return "Hello Pardhu";
	}

	@GET
	@Produces("application/json")
	@Path("/ngram/{sentence}")
	public String getNgram(@PathParam("sentence") String sentence)
			throws InvalidFormatException, IOException, JSONException {

		URL tokenurl = this.getClass().getClassLoader()
				.getResource("models/en-token.bin");
		URL posUrl = this.getClass().getClassLoader()
				.getResource("models/en-pos-maxent.bin");
		URL chunkerUrl = this.getClass().getClassLoader()
				.getResource("models/en-chunker.bin");

		tokenurl = tokenurl == null ? new URL(
				"file:///Users/pradyumnad/KDM/nlplab7/WebContent/models/en-token.bin")
				: tokenurl;
		TokenizerModel model = new TokenizerModel(tokenurl);

		TokenizerME tokenizer = new TokenizerME(model);
		String[] tokens = tokenizer.tokenize(sentence);

		posUrl = posUrl == null ? new URL(
				"file:///Users/pradyumnad/KDM/nlplab7/WebContent/models/en-pos-maxent.bin")
				: posUrl;
		POSModel posModel = new POSModel(posUrl);
		POSTaggerME taggerME = new POSTaggerME(posModel);
		String[] tags = taggerME.tag(tokens);

		chunkerUrl = chunkerUrl == null ? new URL(
				"file:///Users/pradyumnad/KDM/nlplab7/WebContent/models/en-chunker.bin")
				: chunkerUrl;
		ChunkerModel cModel = new ChunkerModel(chunkerUrl);
		ChunkerME chunkerME = new ChunkerME(cModel);
		String[] chunks = chunkerME.chunk(tokens, tags);
		
		Span[] spans = chunkerME.chunkAsSpans(tokens, tags);
		String[] chunkStrings = Span.spansToStrings(spans, tokens);
		
		List<String> ngrams = new ArrayList<String>();
		for (int i = 0; i < spans.length; i++) {
			System.out.println(spans[i]);
			if (spans[i].getType().equals("NP")) {
				String[] split = chunkStrings[i].split(" ");

				List<String> temp = ServiceController.ngram(Arrays.asList(split), 1, " ");
				ngrams.addAll(temp);
				System.out.println(temp.toString());
			}
		}
		
		System.out.println(Arrays.toString(tags));
		
		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
//		object.put("tokens", tokens);
//		object.put("tags", tags);
		object.put("chunks", chunkStrings);
		object.put("ngrams", ngrams);
		return object.toString();
	}

	public static List<String> ngram(List<String> input, int n, String separator) {
		System.out.println("Params :");
		System.out.println(input.toString()+" -- "+n+" -- "+separator);
		if (input.size() <= n) {
			return input;
		}

		List<String> outGrams = new ArrayList<String>();
		for (int i = 0; i < input.size(); i++) {
			String gram = "";

			if ((i + n) <= input.size()) {
				for (int j = 0; j < (n + i); j++) {
					gram += input.get(j) + separator;
				}

				gram = gram.substring(0, gram.lastIndexOf(separator));
				outGrams.add(gram);
			}
		}
		return outGrams;
	}

	@GET
	@Produces("application/json")
	@Path("/synon/{word}")
	public String getSynon(@PathParam("word") String word) throws JSONException {
		RiWordnet wordnet = new RiWordnet();
		String[] temp = wordnet.getAllSynonyms(word, "n", 5);
		System.out.println(Arrays.toString(temp));
		
		JSONObject object = new JSONObject();
		object.put("word", word);
		object.put("results", temp);
		return object.toString();
	}

	@GET
	@Produces("application/json")
	@Path("/parents/{word1}/{word2}")
	public String getParents(@PathParam("word1") String word1, @PathParam("word2") String word2) throws JSONException {
		RiWordnet wordnet = new RiWordnet();
		
		String pos = wordnet.getBestPos(word1);
		String[] parents = wordnet.getCommonParents(word1, word2, pos);
		
		JSONObject object = new JSONObject();
		object.put("results", parents);
		return object.toString();
	}
	
	@GET
	@Produces("application/json")
	@Path("/tokenize/{sentence}")
	public String getTokenize(@PathParam("sentence") String sentence)
			throws JSONException, IOException {
		URL url = this.getClass().getClassLoader().getResource("files/1.txt");

		Reader reader = new StringReader(sentence);

		Tokenizer tokenizer = new StandardTokenizer();
		tokenizer.setReader(reader);

		CharTermAttribute token = tokenizer
				.getAttribute(CharTermAttribute.class);

		JSONArray array = new JSONArray();

		tokenizer.reset();

		while (tokenizer.incrementToken()) {
			array.put(token.toString());
		}

		tokenizer.end();
		tokenizer.close();

		JSONObject object = new JSONObject();
		object.put("sentence", sentence);
		// object.put("path", url.getPath());
		object.put("results", array);
		return object.toString();
	}
}
