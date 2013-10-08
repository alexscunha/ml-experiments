package estruturas.grafo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import estruturas.grafo.Vertice.VertexState;

/**
 * Classe que disponibiliza as funcionalidades de um grafo direcionado.
 * @author Alex
 * Ultima Atualizacao: 06/06/2013
 * O que há de novo: o grafo não permite o acréscimo de uma aresta duplicada.
 */
public class Grafo implements Cloneable {
	enum VertexState { Unvisited, InProgress, Visited };
	
	public Grafo() {
		vertices = new ArrayList<Vertice>();
		arestas = new ArrayList<Aresta>();
	}

	/** Colecao de vertices do grafo */	 
	List<Vertice> vertices;

	/** Obtem um List de Vertices do grafo
	 * @return the vertices
	 */
	public List<Vertice> getVertices() {
		return vertices;
	}

	/** Adiciona um List de Vertices ao grafo
	 * @param vertices Os vertices para atribuicao
	 */
	public void setVertices(List<Vertice> vertices) {
		this.vertices = vertices;
	}
	
	/** 
	 * Adiciona um vertice ao grafo. 
	 * Se o Vertice ja existir (pelo nome), o objeto nao é criado. Caso contrario, um 
	 * novo objeto do tipo Vertice é criado e retornado.
	 * @param nome - Um String que representa o conteudo do vertice
	 * @return O vertice criado
	 */
	public Vertice addVertice(String nome) {
		Vertice v;
		if ( (v = getVertice( nome )) == null ) {
			v = new Vertice(nome);
			vertices.add(v);
		}
		return v;
	}

	/**
	 * Método que localiza e retorna a referencia para um vertice existente no grafo.
	 * @param nome O conteudo para identificacao do vertice
	 * @return A referencia para o vertice (quando existente) ou null.
	 */
	public Vertice getVertice(String nome){

        for (Vertice u : vertices) {
        	if ( u.getTag().equalsIgnoreCase( nome ))
        		return u;
        }
        return null;
	}

	/**
	 * Verifica a existencia de um vertice no grafo.
	 * @param v - O vertice desejado
	 * @return <b>true</b>, caso o vertice pertença ao grafo, ou <b>false</b> caso contrario.
	 */
	public boolean exists(Vertice v){

        for (Vertice u : vertices) {
        	if ( u.getTag().equalsIgnoreCase( v.getTag() ) )
        		return true;
        }
        return false;
	}

	 public int getNumIncidentes(Vertice u){
		int count=0;
		for(Aresta arco: arestas){
			if( arco.getDestino().equals(u))
				count++;
		}
		return count;
	 }
	 
	 
	List<Aresta> arestas;
 
	
	/**
	 * Adiciona uma aresta ao grafo.
	 * O método se encarreda de adcionar a aresta como ajacente ao vertice de origem.
	 * @param origem - String que identifica o vertice de origem
	 * @param destino - String que identifica o vertice de destino
	 * @param peso - O peso da aresta
	 * @return Uma referencia para a aresta criada caso os vertices ja existam. Caso contrario,
	 * retorna null.
	 * 
	 */
	public Aresta addAresta(String origem, String destino, int peso) {
		Vertice u,v;
		Aresta arco=null;
		if ( (u=getVertice(origem))==null || (v=getVertice(destino))==null )
			return arco;
	
		return addAresta(u,v,peso);
	}
	public Aresta addAresta(Vertice origem, Vertice destino, int peso) {
		Aresta e; 
		/* Se a aresta já existir, inclusive com o mesmo peso, não cria  */
		if( (e=getAresta(origem,destino))==null ) {
			e = new Aresta(origem, destino, peso);
			origem.addAdj(e);
			arestas.add(e);

		} 
		return e;
	}
	
	public Aresta getAresta( Vertice origem, Vertice destino ){
		Aresta arc = null;
		for(int i=0 ; i < arestas.size(); i++ ){
			arc = arestas.get(i);
			if ( arc.getOrigem().equals( origem) && arc.getDestino().equals( destino) )
				return arc;
		}
		return null;
	}
	
	/**
	 * @return the arestas
	 */
	public List<Aresta> getArestas() {
		return arestas;
	}

	/**
	 * @param arestas the arestas to set
	 */
	public void setArestas(List<Aresta> arestas) {
		this.arestas = arestas;
	}

	
	public boolean isEmpty(){
		return vertices.size() == 0;
	}

	 public void clear(){
		 vertices.clear();
		 arestas.clear();
	 }
	 
	public String toString() {
        String r = "";
        for (Vertice u : vertices) {
        	if( u.adj.size()!= 0 ) {
        		r += u.tag + " -> ";
        	} else
        		continue;
        	
            for (Aresta e : u.adj) {
//            	r += u.tag + " -> ";
                Vertice v = e.destino;
                r += v.tag + ", ";
            }
            r += "\n";
        }
        return r;
    }

	

	/**
	 * Efetua um percurso em profundidade para evitar relações de multiplas-instancias, na qual
	 * é feita uma poda no grafo para restringi-lo a uma arvore, de forma que cada vertice só
	 * tenha um pai. Para isso, o algoritmo DFS() foi modificado para efetuar a tarefa.
	 * @return O grafo modificado, sem as relacoes de multiplas-instancias.
	 */
	
	public Grafo DFS(){
		Grafo Sii = new Grafo();
		
		for(int i=0; i < vertices.size(); i++ ){
			vertices.get(i).setStatus(Vertice.VertexState.Unvisited);
		}
		
		
		for( Vertice u : vertices){
			runDFS( u, Sii );
		}
		
		return Sii;
		
	}
	
	
	private void runDFS(Vertice u, Grafo Sii){
		Vertice w = null;
		List<Aresta> uAdjacentes = null;
				
		u.setStatus(Vertice.VertexState.Visited);
		Sii.addVertice( u.getTag());
		
		uAdjacentes = u.getAdj();
			
		for(Aresta arco: uAdjacentes){
			w = arco.getDestino();
			
			if( w.getStatus() == Vertice.VertexState.Unvisited ) {							
				runDFS(w,Sii);
				Sii.addAresta(arco.getOrigem().getTag(), arco.getDestino().getTag(), arco.getPeso());								
			}
		}
		
		u.setStatus(Vertice.VertexState.Finished);
	}

	
//	/* Deep-First-Search (DFS): Busca em Profundidade */
	/**
	 * Efetua um percurso em profundidade, baseado no algoritmo Deep-First-Search(DFS).
	 * Este método faz o percurso utilizando um vertice inicial, até o vertice n que esteja
	 * na rede do vértice inicial. 
	 * OBS: ESTE ALGORITMO É MATRIZ, o qual deve ser usado como código inicial para adequar
	 * às necessidades de cada problema.
	 * @param tag - String que identifica o vertice de partida.
	 */
	public void DFS(String tag){
		Vertice u = null;
				
		if ((u = getVertice(tag)) == null) {
			System.err.println("vertice nao encontrado em runDFS()");
			return;
		}
		
		for(int i=0; i < vertices.size(); i++ ){
			vertices.get(i).setStatus(Vertice.VertexState.Unvisited);
		}
		
		runDFS( u );
		
	}
//	
	
	
	/**
	 * Método secundário, que efetua realmente o percurso no grafo a partir de um 
	 * vertice específico
	 * OBS: ESTE ALGORITMO É MATRIZ, o qual deve ser usado como código inicial para adequar
	 * às necessidades de cada problema.
	 * DEPENDENCIA: só pode ser chamado a partir de DFS();
	 * @param u - O vertice de busca.
	 */
	private void runDFS(Vertice u){
		Vertice w = null;
		List<Aresta> uAdjacentes = null;
				
		u.setStatus(Vertice.VertexState.Visited);
		
		uAdjacentes = u.getAdj();
		
		for(Aresta arco: uAdjacentes){
			w = arco.getDestino();
			if( w.getStatus() == Vertice.VertexState.Unvisited )
				runDFS(w);
		}
		
		u.setStatus(Vertice.VertexState.Finished);
		//System.out.println( u.getTag());
	}
	
	
	/**
	 * @return the list of all valid destinations from the given city.
	 */
	public List<Vertice> getDestinations(Vertice v)
	{
		
		List<Vertice> list = new ArrayList<Vertice>();
		List<Aresta> arcos = v.getAdj();
				
		for( Aresta a : arcos){
			list.add( a.getDestino() );
	
		}
		
		return list;
	}
	
	public int getDistance(Vertice start, Vertice end)
	{
		List<Aresta> arcos = start.getAdj();
		
		for( Aresta a : arcos){
			if( a.getDestino().getTag().equals( end.getTag()) )
				return a.getPeso();
	
		}
		return 0 ;
	}
	
	 public Object clone() {
//	        try {
//	            return (Grafo)super.clone();
//	        }
//	        catch (CloneNotSupportedException e) {
//	            return null;
//	        }
		 
		 try {
			Grafo clone = (Grafo)super.clone();
			
			   //Clona o resto. 
			clone.arestas.addAll(arestas);
			clone.vertices.addAll(vertices);
	        return clone;          
		} catch (CloneNotSupportedException e) {

			e.printStackTrace();
			return null;
		} // Clona os tipos primitivos;

	 }
	 

	 

	 
	public boolean saveToFile(String outputFile) throws IOException {
		/* Declaracoes para arquivo de saida*/
		BufferedWriter bw = null;
		File fileOut = new File( outputFile);
		
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
								
		for(Vertice v: getVertices()){
			bw.write("vertice\t" + v.getTag());
			bw.newLine();
			
            for (Aresta e : v.getAdj() ) {
            	
            	bw.write("aresta\t" + e.getOrigem().getTag() + "\t" + e.getDestino().getTag() +  
            			 "\t" + e.getPeso());
            	bw.newLine();	            	
            }
            bw.flush();			
		}
		bw.close();
		return true;
	}
	 
	public boolean restoreFromFile(String inputFile) throws IOException {
		/* Declaracoes para arquivo de saida*/
		BufferedReader br = null;
		File fileIn = new File( inputFile);
		
		StringBuilder linha = new StringBuilder();
		String[] token = null;
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
		
		vertices.clear();
		arestas.clear();

		
		while( br.ready()){
			linha.delete(0, linha.length());
			linha.append(br.readLine());
			
			token = linha.toString().split("\t");
			
			if(token[0].equalsIgnoreCase("vertice")) {
				addVertice(token[1]);				
			} else if (token[0].equalsIgnoreCase("aresta")) {
				// Adiciona o vertice de destino
				addVertice(token[2]);
				// Adiciona a aresta completa: origem e destino.
				// Automaticamente, a aresta é adicionada ao set de adjacentes do vertice de origem.
				addAresta(token[1],token[2], Integer.parseInt(token[3]));			
				
			}
		}
		br.close();
		return true;
	}
	
	public Grafo getSubGrafo(String tag){
		Vertice source,target, element;
		
		if( (source=getVertice(tag))==null )
			return null;
		Grafo g = new Grafo();
		
		Queue<Vertice> fila = new LinkedList(); 
		fila.add(source);
		
		
		while( !fila.isEmpty() ) {
			element = fila.poll();
			source = g.addVertice(element.getTag());
			
			for(Aresta edge: element.getAdj()){
				target = g.addVertice(edge.getDestino().getTag());
				g.addAresta(source, target, edge.getPeso());
				fila.add(edge.getDestino());				
			}
		
		}
		
		return g;
	}

	 public static void main(String args[]){
		 Grafo g = new Grafo();
		 Grafo sub = null;
		 Grafo out = null;
		 
		 g.addVertice("alex");
		 g.addVertice("Nathan");
		 g.addVertice("dan");
		 g.addVertice("duda");
		 g.addAresta("alex", "Nathan", 1);
		 g.addAresta("alex", "Nathan", 1);
		 g.addAresta("alex", "dan", 1);
		 g.addAresta("dan", "duda", 1);
		 
		 g.addVertice("alice");
		 g.addVertice("alessandra");
		 g.addAresta("alice", "alessandra", 1);
		 
		
		 System.out.println(g);
		 
		 sub = g.getSubGrafo("alex");
		 
		 System.out.println(sub);
		 
		 
		 
//		 try {
//			g.saveToFile("teste");
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
//		 
//		 g.clear();
//		 System.out.println("Esta vazio: " + g);
//		 
//		 try {
//			g.restoreFromFile("teste");
//		} catch (IOException e) {
//		
//			e.printStackTrace();
//		}
//		
//		 System.out.println(g);
		 
		 
		 
		 
//		 out = (Grafo)g.clone();
//		 out.getVertice("alex").setTag("Dan");
//		 System.out.println(out);
//		 System.out.println(g);
	 }
	
}
