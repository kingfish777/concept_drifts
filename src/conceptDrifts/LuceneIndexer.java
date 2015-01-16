package conceptDrifts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 * Creates a Lucene index from a directory in a given language.
 */
public class LuceneIndexer {

	private static Logger logger = Logger.getLogger("conceptDrifts");
	private static final Version LUCENE_VERSION = Version.LUCENE_4_10_3;

	/**
	 * The main method gives an example of invocation.
	 * 
	 * @param args
	 *            the arguments; not used
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		logger.setLevel(Level.INFO);
		String corpusDirectory = "data/sample";
		String indexDirectory = "data/sample_index";
		indexSingleFile(corpusDirectory + "/books909.txt", indexDirectory);
		getIndexTerms(indexDirectory);
	}

	public static void indexSingleFile(String filename, String indexDirectory)
			throws IOException {
		Analyzer analyzer = new WordnetAnalyzer();
		IndexWriterConfig indexConfig = new IndexWriterConfig(LUCENE_VERSION,
				analyzer);
		IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
				indexDirectory)), indexConfig);
		Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(
				new FileInputStream(filename))));
		Document doc = AmazonReview.Document(scanner);
		while (doc != null) {
			writer.addDocument(doc);
			doc = AmazonReview.Document(scanner);
		}
		writer.commit();
		logger.info("Number of documents: " + writer.numDocs());
		writer.close();
		scanner.close();
	}

	/**
	 * Gets the index terms and writes them in the index directory.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void getIndexTerms(String luceneIndexDirectory)
			throws Exception {
		File file = new File(luceneIndexDirectory);
		IndexReader indexReader = IndexReader.open(FSDirectory.open(file));
		Fields fields = MultiFields.getFields(indexReader);
		Terms terms = fields.terms("contents");
		TermsEnum termsEnum = terms.iterator(null);
		BytesRef text;
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				luceneIndexDirectory + "/indexTerms.txt")));
		while ((text = termsEnum.next()) != null) {
			out.write(text.utf8ToString() + "\n");
		}
		out.close();
		indexReader.close();
	}

}
