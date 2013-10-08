package estruturas.grafo;

import java.util.List;

/* Usado para colocar em acao o algoritmo de Djikistra */
public interface RoutesHierarchy {	   
	   
	   /**
		 * Enter a new segment in the graph.
		 */
		public void addDirectRoute(Vertice start, Vertice end, int distance);
		
		/**
		 * Get the value of a segment.
		 */
		public int getDistance(Vertice start, Vertice end);
		
		/**
		 * Get the list of cities that can be reached from the given city.
		 */
		public List<Vertice> getDestinations(Vertice u); 
		
		/**
		 * Get the list of cities that lead to the given city.
		 */
		public List<Vertice> getPredecessors(Vertice u);
		
		/**
		 * @return the transposed graph of this graph, as a new RoutesMap instance.
		 */
		public RoutesHierarchy getInverse();
}
