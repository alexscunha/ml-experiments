package machine.learning;

public class Itemset {
	private String tag1;
	private String tag2;
	private int support; // suporte da regra
	private String label;
	private int id;
	
	public Itemset(int id, String tag1, String tag2, int support, String label) {
		this.id = id;
		this.support = support;
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.label = label;
	}
	/**
	 * @return the tag1
	 */
	public String getTag1() {
		return tag1;
	}

	/**
	 * @param tag1 the tag1 to set
	 */
	public void setTag1(String tag1) {
		this.tag1 = tag1;
	}

	/**
	 * @return the tag2
	 */
	public String getTag2() {
		return tag2;
	}

	/**
	 * @param tag2 the tag2 to set
	 */
	public void setTag2(String tag2) {
		this.tag2 = tag2;
	}

	/**
	 * @return the support
	 */
	public int getSupport() {
		return support;
	}

	/**
	 * @param support the support to set
	 */
	public void setSupport(int support) {
		this.support = support;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public String toString(){
		return "[" + id + ", " + support +  ", " + tag1 + ", " + tag2 + ", " + label + "]"; 
	}


}
