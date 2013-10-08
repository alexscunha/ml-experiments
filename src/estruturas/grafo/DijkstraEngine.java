package estruturas.grafo;


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraEngine {

	  /**
     * Infinity value for distances.
     */
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;

    /**
     * Some value to initialize the priority queue with.
     */
    private static final int INITIAL_CAPACITY = 8;
	
    /**
     * This comparator orders cities according to their shortest distances,
     * in ascending fashion. If two cities have the same shortest distance,
     * we compare the cities themselves.
     */
	private final Comparator<Vertice> shortestDistanceComparator = new Comparator<Vertice>()
	    {
		        public int compare(Vertice left, Vertice right)
		        {
		            int shortestDistanceLeft = getShortestDistance(left);
		            int shortestDistanceRight = getShortestDistance(right);

		            if (shortestDistanceLeft > shortestDistanceRight)
		            {
		                return +1;
		            }
		            else if (shortestDistanceLeft < shortestDistanceRight)
		            {
		                return -1;
		            }
		            else // equal
		            {
		                return 0;
		            }
		        }

	
		};
	
    /**
     * The graph.
     */
    private final Grafo grafo;
    
    /**
     * The working set of cities, kept ordered by shortest distance.
     */
    private final PriorityQueue<Vertice> unsettledNodes = new PriorityQueue<Vertice>(INITIAL_CAPACITY, shortestDistanceComparator);
    
    /**
     * The set of cities for which the shortest distance to the source
     * has been found.
     */
    private final Set<Vertice> settledNodes = new HashSet<Vertice>();
    
    /**
     * The currently known shortest distance for all cities.
     */
    private final Map<Vertice, Integer> shortestDistances = new HashMap<Vertice, Integer>();

    /**
     * Predecessors list: maps a city to its predecessor in the spanning tree of
     * shortest paths.
     */
    private final Map<Vertice, Vertice> predecessors = new HashMap<Vertice, Vertice>();
    
    
    /**
     * Constructor.
     */
    public DijkstraEngine(Grafo g)
    {
        this.grafo = g;
    }
    
    
    /**
	 * Test a node.
	 * 
     * @param v the node to consider
     * 
     * @return whether the node is settled, ie. its shortest distance
     * has been found.
     */
    private boolean isSettled(Vertice v)
    {
        return settledNodes.contains(v);
    }

    /**
     * @return the shortest distance from the source to the given city, or
     * {@link DijkstraEngine#INFINITE_DISTANCE} if there is no route to the destination.
     */    
    public int getShortestDistance(Vertice v)
    {
        Integer d = shortestDistances.get(v);
        return (d == null) ? INFINITE_DISTANCE : d;
    }

	/**
	 * Set the new shortest distance for the given node,
	 * and re-balance the queue according to new shortest distances.
	 * 
	 * @param city the node to set
	 * @param distance new shortest distance value
	 */        
    private void setShortestDistance(Vertice v, int distance)
    {
        /*
         * This crucial step ensures no duplicates are created in the queue
         * when an existing unsettled node is updated with a new shortest 
         * distance.
         * 
         * Note: this operation takes linear time. If performance is a concern,
         * consider using a TreeSet instead instead of a PriorityQueue. 
         * TreeSet.remove() performs in logarithmic time, but the PriorityQueue
         * is simpler. (An earlier version of this class used a TreeSet.)
         */
        unsettledNodes.remove(v);

        /*
         * Update the shortest distance.
         */
        shortestDistances.put(v, distance);
        
		/*
		 * Re-balance the queue according to the new shortest distance found
		 * (see the comparator the queue was initialized with).
		 */
		unsettledNodes.add(v);        
    }
    
    /**
     * @return the city leading to the given city on the shortest path, or
     * <code>null</code> if there is no route to the destination.
     */
    public Vertice getPredecessor(Vertice v)
    {
        return predecessors.get(v);
    }
    
    private void setPredecessor(Vertice a, Vertice b)
    {
        predecessors.put(a, b);
    }
    
    
    /**
     * Initialize all data structures used by the algorithm.
     * 
     * @param start the source node
     */
    private void init(Vertice start)
    {
        settledNodes.clear();
        unsettledNodes.clear();
        
        shortestDistances.clear();
        predecessors.clear();
        
        // add source
        setShortestDistance(start, 0);
        //unsettledNodes.add(start);
    }
    
    /**
     * Run Dijkstra's shortest path algorithm on the map.
     * The results of the algorithm are available through
     * and 
     * upon completion of this method.
     * 
     * @param start the starting city
     * @param destination the destination city. If this argument is <code>null</code>, the algorithm is
     * run on the entire graph, instead of being stopped as soon as the destination is reached.
     */
    public int execute(Vertice start, Vertice destination)
    {
        init(start);
        
        // the current node
        Vertice u;
  
        // extract the node with the shortest distance
        while (!unsettledNodes.isEmpty()) // obtem o profixo da fila
        {
        	u = unsettledNodes.poll();
        	
        	
            assert !isSettled(u);
            
            // destination reached, stop
            if (u == destination) 
            	break;
            
            settledNodes.add(u);
            
            relaxNeighbors(u);
        }
        return getDistance(destination);
    }

    /**
	 * Compute new shortest distance for neighboring nodes and update if a shorter
	 * distance is found.
	 * 
	 * @param u the node
	 */
    private void relaxNeighbors(Vertice u)
    {
        for (Vertice v : grafo.getDestinations(u))
        {
            // skip node already settled
            if (isSettled(v))
            	continue;
            
            int shortDist = getShortestDistance(u) + grafo.getDistance(u, v);
            
            if (shortDist < getShortestDistance(v))
            {
            	// assign new shortest distance and mark unsettled
                setShortestDistance(v, shortDist);
                                
                // assign predecessor in shortest path
                setPredecessor(v, u);
            }
        }        
    }
    
	private int getDistance(Vertice target) {
		Set<Vertice> vertex = shortestDistances.keySet();
		
		
		for( Vertice v : vertex){
			if( v.getTag().equalsIgnoreCase(target.getTag()))
				return  shortestDistances.get(v);
		}
		return 0;
		
	}
	
	private void showShortestDistances() {
		Set<Vertice> vertex = shortestDistances.keySet();
		
		for( Vertice v : vertex){
			System.out.println("Vertice [" + v.getTag() + "] = " + shortestDistances.get(v));
		}
		
	}
    
	public static void main(String[] args) {
		Grafo g = new Grafo();
		int distancia = 0;
		
		g.addVertice("software");
		g.addVertice("windows");
		g.addAresta("software", "windows", 1);
		
		g.addVertice("software");
		g.addVertice("linux");
		g.addAresta("software", "linux", 1);
		
		g.addVertice("MSN");
		g.addVertice("windows");
		g.addAresta("windows", "MSN", 1);
		
		g.addVertice("software");
		g.addVertice("MSN");
		g.addAresta("software", "MSN", 3);
		
		g.DFS("software");
		
		DijkstraEngine de = new DijkstraEngine(g);
		distancia = de.execute(g.getVertice("software"), g.getVertice("MSN"));
		
		de.showShortestDistances();
		
		System.out.println("Distancia: " + distancia);

	}




}
