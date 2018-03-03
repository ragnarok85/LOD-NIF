package lod.nif.main;


public class Report {
	private String article;
	private String numTriples;
	private boolean inLink;
	private String indexArticle;
	private int timesProcessed;
	private String outputName;
	private String outputBz2;
	
	public Report(){
		this.timesProcessed = 1;
	}
	
	@Override
	public String toString() {
		return outputName + "\t"
				+ inLink + "\t"
						+ indexArticle + "\t"
							+ numTriples + "\t"
								+ timesProcessed + "\t"
										+ article  + "\t"
												+ outputBz2  + "\n";
	}
	
	//article, outputName, notInLinksList.contains(article), indexArticle, numTriples
	public Report(String article, String outputName, boolean inLink, String indexArticle, String numTriples){
		this.article = article;
		this.timesProcessed = 1;
		this.outputName = outputName;
		this.inLink = inLink;
		this.indexArticle = indexArticle + "\t";
		this.numTriples = numTriples + "\t";
	}
	
	public String getArticle() {
		return article;
	}
	public void setArticle(String article) {
		this.article = article;
	}

	public boolean isInLink() {
		return inLink;
	}
	public void setInLink(boolean inLink) {
		this.inLink = inLink;
	}
	public int getTimesProcessed() {
		return timesProcessed;
	}
	public void setTimesProcessed(int timesProcessed) {
		this.timesProcessed = timesProcessed;
	}
	
	public void incrementTimesProcessed(){
		this.timesProcessed++;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public String getNumTriples() {
		return numTriples;
	}

	public void setNumTriples(String numTriples) {
		this.numTriples += numTriples + "\t";
	}

	public String getIndexArticle() {
		return indexArticle;
	}

	public void setIndexArticle(String indexArticle) {
		this.indexArticle += indexArticle + "\t";
	}
	
	public void update(String indexArticle, String numTriples){
		this.indexArticle += indexArticle + "\t";
		this.numTriples += numTriples + "\t";
		this.timesProcessed++;
	}

	public String getOutputBz2() {
		return outputBz2;
	}

	public void setOutputBz2(String outputBz2) {
		this.outputBz2 = outputBz2;
	}
}
