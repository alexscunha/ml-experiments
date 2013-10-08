package estruturas.grafo;

public class Aresta {

	Vertice origem;
	Vertice destino;
	int peso; /* coocorrencia */

	public Aresta(Vertice origem, Vertice destino, int peso) {
		this.origem = origem;
		this.destino = destino;
		this.peso = peso;
	}

	public Vertice getOrigem() {
		return origem;
	}

	public void setOrigem(Vertice origem) {
		this.origem = origem;
	}

	public Vertice getDestino() {
		return destino;
	}

	public void setDestino(Vertice destino) {
		this.destino = destino;
	}

	public int getPeso() {
		return peso;
	}

	public void setPeso(int peso) {
		this.peso = peso;
	}
	
	public String toString(){
		return origem.getTag() + " --(" + peso + ")--> " + destino.getTag();
	}
	

}
