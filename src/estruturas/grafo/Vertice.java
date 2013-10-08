package estruturas.grafo;

import java.util.ArrayList;
import java.util.List;

public class Vertice /*implements Comparable<Vertice> */{

	String tag;
	List<Aresta> adj;
	public enum VertexState { Unvisited, Visited, Finished }; 
	VertexState status;
	
	
	public Vertice(String tag) {
		this.tag = tag;
		this.adj = new ArrayList<Aresta>();
	}

	
	/**
	 * @return the status
	 */
	public VertexState getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(VertexState status) {
		this.status = status;
	}
	


	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	//Aqui eu vou guardar todos os vertices adj
	public void addAdj(Aresta e) {
		adj.add(e);
	}
	
	public List<Aresta> getAdj() {
		return adj;
	}

	public void setAdj(List<Aresta> adj) {
		this.adj = adj;
	}
	
//	public int compareTo( Vertice outro){
//		return  this.tag.compareTo( outro.tag);
//	}
	
	 public boolean equals(Vertice v)
	 {
	        return this.tag.equals(v.tag);
	 }
	 
	 public boolean equals(Object o)
	 {
	        return this == o || equals((Vertice) o);
	 }
	 
	 public String toString(){
		 return this.tag;
	 }

}
