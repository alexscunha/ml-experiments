package weka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Classe que realiza experimento de treino/teste automaticamente, utilizando a API Java
 * do Weka.
 * Trabalha, por enquanto, com classificação binária.
 * @author Alex
 * @version 1.3: metodos createClassifier(), novos classificadores
 * [1.2] Alteracoes no método removeAttribute(), acrescimo do método saveDecisionTree(). 
 * [1.1] metodo loadARFFFile() sobrecarregado.
 * Data: 20/08/2013
 */
public class WekaExperiment {
	// The training set object
	private Instances trainingData;
	// The training set object copy, to recover when necessary
//	private Instances trainingCopy;
	// The test set object
	private Instances testData;
	// Classifier
	private Classifier classifier;
	// Avaliador do treino ou teste
	public Evaluation eval;
	
	
	// Accuracy Matrix
	double accuracy[][];
	
	//
	private boolean trainingFileLoaded = false;
	private boolean testFileLoaded = false;
	
	public static final int TRAINING_FILE = 0;
	public static final int TEST_FILE = 1;
	
	public static final int J48 = 0;
	public static final int SVM = 1;
	public static final int NAYVE_BAYES = 2;
	public static final int RANDOM_FOREST = 3;
	public static final int ADABOOST = 4;
	public static final int LOGISTIC_REGRESSION = 5;
	
	public WekaExperiment(){
		 // Create an empty training set with a initial set capacity of 10
		trainingData = null;
		classifier = null;
		eval = null;
		accuracy = new double[2][6];
	}
	
	
	/**
	 * Lê um arquivo ARFF para o objeto de instancias da classe. 
	 * @param inputFile O nome do arquivo ARFF.
	 * @param typeFile Use a constante TRAINING_FILE para se referir ao arquivo de treino, e
	 * TEST_FILE para se referir ao arquivo de teste.
	 * @return true se a carga das instâncias foi feita com sucesso, false caso contrário.
	 * @throws FileNotFoundException 
	 */
	public boolean loadARFFFile(String inputFile, int typeFile) {
		Reader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File( inputFile )));
			if ( typeFile == TRAINING_FILE ){
				trainingData = new Instances( reader );
//				trainingCopy = new Instances( reader );
				trainingData.setClassIndex(trainingData.numAttributes() - 1);
//				trainingCopy.setClassIndex(trainingData.numAttributes() - 1);
				trainingFileLoaded = true;
			} else if ( typeFile == TEST_FILE ) {
				testData = new Instances( reader );
				testData.setClassIndex(testData.numAttributes() - 1);
				testFileLoaded = true;
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("ERRO: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean loadARFFFile(File inputFile, int typeFile ){
		return this.loadARFFFile(inputFile.getAbsolutePath(), typeFile);
	}

	
	/**
	 * Treina o classificador de acordo com as instäncias de treinamento carregadas.
	 * @param theClassifier O classificador utilizado para construção do modelo
	 * @throws Exception 
	 */
	public void buildClassifier( int theClassifier ) throws Exception  {
		if( !trainingFileLoaded)
			return;
//		classifier = (Classifier)new J48();
		classifier = createClassifier( theClassifier );
		classifier.buildClassifier( trainingData);
		
		
	}
	
	/**
	 * Cria um classificador de acordo com o parâmetro especificado. Se "theClassifier" não
	 * for um classificador válido, devolve o defaulta: J48
	 * @param theClassifier Constante de classe referente ao classificador desejado.
	 * @return Uma instancia do classificador.
	 */
	public Classifier createClassifier( int theClassifier ){
		
		if( theClassifier == J48) {
			return new J48();
		} else if ( theClassifier == NAYVE_BAYES ){
			return new NaiveBayes();
		} else if (  theClassifier == RANDOM_FOREST ){
			return new RandomForest();
		} else if ( theClassifier == ADABOOST ) {
			return new AdaBoostM1();
		} else if ( theClassifier == LOGISTIC_REGRESSION) {
			return new Logistic();			
		} else if ( theClassifier == SVM) {
			return new LibSVM();
		} else
			return new J48();
	}
	
	/**
	 * Devolve um String contendo a arvore de decisao gerada pelo classificador
	 * @return As regras para a arvore de decisão.
	 */
	public String getDecisionTree(){
		return classifier.toString();
	}
	
	/**
	 * Retorna um array de strings contendo os labels correspondentes às classes positivas e negativas
	 * @return Um array de Strings
	 * OBS: Os labels são apenas para modelos de classificação binária, ou seja,
	 * dois rótulos.
	 */
	public String[] getLabel(){
		if( !trainingFileLoaded )
			return null;
		
		String[] label = new String[2];
		// indice do atributo de classe
		int index = trainingData.classIndex();
		// Recuperando o atributo de classe
		Attribute att = trainingData.attribute(index);
		Enumeration<?> e = att.enumerateValues();
		for(int i=0; e.hasMoreElements(); i++)
			label[i] = (String) e.nextElement();

		return label;
	}
	
	/**
	 * Retorna um List com os atributos encontrados no arquivo de treinamento.
	 * @return Um ArrayList com os atributos.
	 */
	public List<String> getAttributes(){
		List<String> list = new ArrayList<String>();
		
		Enumeration<?> e = trainingData.enumerateAttributes();
		while( e.hasMoreElements() ){
			list.add( ((Attribute) e.nextElement()).name() );
		}
		return list;
	}
	
	/**
	 * Retorna um String com a sequencia de atributos do arquivo de treino ou teste,
	 * separados por "," (virgula)
	 * @param typeFile A constante de classe TRAINING_FILE ou TEST_FILE
	 * @return Um String com a sequência de atributos.
	 */
	public String getAttributesAsString( int typeFile ){
		StringBuilder str = new StringBuilder();
		Instances dataset = null;
		
		if( typeFile == TRAINING_FILE )
			dataset = trainingData;
		else if( typeFile == TEST_FILE )
			dataset = testData;
		else
			dataset = trainingData;
		
		Enumeration<?> e = dataset.enumerateAttributes();
		while( e.hasMoreElements() ){
			if( str.length() > 0)
				str.append(", ");
			str.append(((Attribute) e.nextElement()).name()  );
		}
		return str.toString();
	}
	/**
	 * Retorna um List com o indice dos atributos encontrados no arquivo de treinamento.
	 * @return Um ArrayList com os indices dos atributos.
	 */
	public List<String> getAttributeIndex(){
		List<String> list = new ArrayList<String>();
		
		Enumeration<?> e = trainingData.enumerateAttributes();
		while( e.hasMoreElements() ){
			Attribute att = (Attribute) e.nextElement();			
			
			list.add( String.valueOf( att.index()+1 ) );
		}
		return list;
	}
	
	/**
	 * Obtem o número de atributos de um arquivo de Treino ou Teste
	 * @param typeFile A constante de classe TRAINING_FILE ou TEST_FILE, que sinaliza
	 * qual dos arquivos deseja-se obter o número de atributos.
	 * @return O número de atributos. O valor -1 indica que "typeFile" foi informado
	 * incorretamente.
	 */
	public int getNumAttributes( int typeFile ){
		Instances dataset = null;
		
		if ( typeFile == TRAINING_FILE )
			dataset = trainingData;
		else if ( typeFile == TEST_FILE )
			dataset = testData;
		else
			return -1;
		
		return dataset.numAttributes();
	}
	/**
	 * Realiza as predições no arquivo de teste de acordo com o modelo de classificação
	 * criado a partir do arquivo de treinamento. Aqui é feito uma classificação MANUAL.
	 */
	public void printClassifierPredictions(){
		if( !trainingFileLoaded && !testFileLoaded)
			return;

	
		int confusionMatrix[][] = new int[2][2];
		// Recuperando os rotulos para as classes positivas e negativas
		//String[] rotulos = getLabel();
		
		System.out.print("\nstarting doClassifier()...");
		
		// varrer todas as instancias do arquivo de teste
		for(int i=0; i < testData.numInstances(); i++ ){
			Instance instanciaAtual = testData.instance(i);

			// value =>   0: yes   1: no
			try{
				// get the value predicted for the model.
				int value = (int) classifier.classifyInstance( instanciaAtual);
				// you query the corresponding label, i.e, getting the label found in the test set
				//String label = testData.classAttribute().value(value);
				
				// the value found in in the test set instance
				int realvalue = (int) instanciaAtual.classValue(); 
				
				System.out.print("given value: " + testData.classAttribute().value(
                        (int) instanciaAtual.classValue()) );
				System.out.println(". predicted value: "
	                    + testData.classAttribute().value(value));
				if( value == realvalue ) { // acertou a predicao
					confusionMatrix[value][realvalue]++;
				} else { // errou a predição
					confusionMatrix[realvalue][value]++;
				}
				// Obtem a probabilidade de classificacao para yes ou no.
//				double[] distr = classifier.distributionForInstance(instanciaAtual); 
//				
//				for(int j=0; j<distr.length; j++)
//					System.out.println("distr[" + j + "] = " + distr[j]);
				
			} catch (Exception e){
				e.printStackTrace();
			}
			
			
		} // for
		System.out.print("[done]\n");
	       System.out.println("Confuxion Matrix:");
	         System.out.println("TP   FN");
	         for(int row_i=0; row_i<confusionMatrix.length; row_i++){
	             for(int col_i=0; col_i<confusionMatrix.length; col_i++){
	                 System.out.printf("%4d",confusionMatrix[row_i][col_i]);
	                 System.out.print("|");
	             }
	             System.out.println();
	         }
	         System.out.println("FP   TN");
		
	} // doClassifier
	
	
	/**
	 * Avalia os dados do arquivo de treinamento ou teste
	 * @param typeData constante TRAINING_DATA para arquivo de treino, ou TEST_DATA para arquivo
	 * de teste
	 * @throws Exception
	 */
	public void evaluateData( int typeFile ) throws Exception{
        Instances data = null;
		if( typeFile == TRAINING_FILE )
			data = trainingData;
		else if ( typeFile == TEST_FILE )
			data = testData;
		else
			return; 
		// Testando o modelo
//        Evaluation eval = new Evaluation( data );
		eval = new Evaluation( data );
        
        /*
        double[] predictions = eval.evaluateModel(classifier, data );
    	
        // Obter o array de predicoes de classe (0: yes, 1: no), para cada instancia. 
        System.out.println("predictions: ");
		for (double pred : predictions) {
			System.out.println(pred);
		} */
        
        eval.evaluateModel(classifier, data );
        
//		System.out.println("getNumInstances(): " + eval.numInstances());
//		System.out.println("Atributo de Classe: " + data.classAttribute());
//		System.out.println(getLabel().toString());
//        System.out.println("totalCost(): " + eval.totalCost());
//        System.out.println("errorRate(): " + eval.errorRate());
//        System.out.println("avgCost(): " + eval.avgCost());
//        System.out.println("correct(): " + eval.correct());
//        System.out.println("incorrect(): " + eval.incorrect());
//        System.out.println("kappa(): " + eval.kappa());
//        System.out.println("meanAbsoluteError(): " + eval.meanAbsoluteError());
//        System.out.println("rootMeanSquaredError(): " + eval.rootMeanSquaredError());
//        System.out.println("relativeAbsoluteError(): " + eval.relativeAbsoluteError());
//        System.out.println("rootRelativeSquaredError(): " + eval.rootRelativeSquaredError());
//        System.out.println("Percentage of instances correctly classified: " + eval.pctCorrect());
//        System.out.println("Percentage of instances incorrectly classified: " + eval.pctIncorrect());
//        System.out.println("Percentage of instances not classified: " + eval.pctUnclassified());
//        System.out.println("unclassified(): " + eval.unclassified());
//        // yes:0     no:1
//        System.out.println("TruePositiveRate(): " + eval.truePositiveRate(0));
//        System.out.println("FalsePositiveRate(): " + eval.falsePositiveRate(0));
//        System.out.println("TrueNegativeRate(): " + eval.trueNegativeRate(1));
//        System.out.println("FalsePositiveRate(): " + eval.falseNegativeRate(1));
//        System.out.println("weightedTruePositiveRate(): " + eval.weightedTruePositiveRate());
//        System.out.println("weightedFalsePositiveRate(): " + eval.weightedFalsePositiveRate());
//        System.out.println("weightedTrueNegativeRate(): " + eval.weightedTrueNegativeRate());
//        System.out.println("weightedFalseNegativeRate(): " + eval.weightedFalseNegativeRate());
//        System.out.println("weightedFMeasure(): " + eval.weightedFMeasure());
//        System.out.println("weightedPrecision(): " + eval.weightedPrecision());
//        System.out.println("weightedRecall(): " + eval.weightedRecall());
//        System.out.println("weightedareaUnderROC(): " + eval.weightedAreaUnderROC());
//        System.out.println("numOfFalseNegative() positive class: " + eval.numFalseNegatives(0));
//        System.out.println("numOfFalseNegative() negative class: " + eval.numFalseNegatives(1));
//        System.out.println("numOfFalsePositive() positive class: " + eval.numFalsePositives(0));
//        System.out.println("numOfFalsePositive() negative class: " + eval.numFalsePositives(1));
//        System.out.println("numTruePositive() positive class: " + eval.numTruePositives(0));
//        System.out.println("numTruePositive() negative class: " + eval.numTruePositives(1));
//        System.out.println("numTrueNegative() positive class: " + eval.numTrueNegatives(0));
//        System.out.println("numTrueNegative() negative class: " + eval.numTrueNegatives(1));
//        System.out.println("precision() positive: " + eval.precision(0));
//        System.out.println("precision() negative: " + eval.precision(1));
//        System.out.println("recall() positive: " + eval.recall(0));
//        System.out.println("recall() negative: " + eval.recall(1));
//        System.out.println("fMeasure() positive: " + eval.fMeasure(0));
//        System.out.println("fMeasure() negative: " + eval.fMeasure(1));
//        System.out.println("areaUnderROC() positive: " + eval.areaUnderROC(0));
//        System.out.println("areaUnderROC() negative: " + eval.areaUnderROC(0));
//        System.out.println("toMatrixString():\n" + eval.toMatrixString());
//        System.out.println("toMatrixString():\n" + eval.toMatrixString("Matriz de Confusao"));
//        System.out.println("toClassDetailString():\n" + eval.toClassDetailsString());
//        System.out.println("toClassDetailString():\n" + eval.toClassDetailsString("Detalhamento Acuracia por classe"));
//
//
//        // Get the confusion matrix
//        double[][] cmMatrix = eval.confusionMatrix();
//   
//        System.out.println("Confuxion Matrix:");
//        System.out.println("TP   FN");
//        for(int row_i=0; row_i<cmMatrix.length; row_i++){
//            for(int col_i=0; col_i<cmMatrix.length; col_i++){
//                System.out.print(cmMatrix[row_i][col_i]);
//                System.out.print("|");
//            }
//            System.out.println();
//        }
//        System.out.println("FP   TN");
//        
//        System.out.println("Metodo summaryString(): " + eval.toSummaryString());
////        System.out.println("predictions(): ");
////        FastVector fv = eval.predictions();
////        
////        for( int i=0; i< fv.size(); i++){
////        	System.out.println( fv.elementAt(i));
////        }
        

	}
	
	
	/**
	 * Remove atributos do arquivo de treinamento.
	 * @param listOfAttributes Um string contendo a sequencia de atributos que serão
	 * desconsiderados. Por exemplo: "1,2,3" ou com range "1-3,6,9"
	 * @param invertSelection true, quando deve ser removido os atributos cujos índices não 
	 * estão presentes em listOfAttributes, ou false, caso os atributos a serem removidos sejam
	 * os que estão implícitos em listOfAttributes. 
	 * @param typeFile A constante de classe TRAINING_FILE ou TESTE_FILE
	 * @throws Exception
	 * Fonte:
	 * (1) - http://weka.wikispaces.com/Remove+Attributes
	 * OBS: internamente, o atributo de classe é acrescentado.
	 */
	public void removeAttribute( String listOfAttributes, boolean invertSelection, int typeFile) throws Exception {
		/* Use este trecho para usar com o metodo setOption()
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "1,3"; // ou "1,3-5,7"
		*/
		
		Instances dataset = null;
		if( typeFile == TRAINING_FILE )
			dataset = trainingData;
		else if( typeFile == TEST_FILE )
			dataset = testData;
		else
			dataset = trainingData;
		
		Remove remove = new Remove();
				
//		remove.setOptions( options );
		//Adicionando o atributo de classe
		listOfAttributes += (","+(dataset.classIndex()+1));
		remove.setAttributeIndices( listOfAttributes );
		remove.setInvertSelection(new Boolean( invertSelection ));
		remove.setInputFormat( dataset );
		// Filter gera um novo objeto com os atributos selecionados. Portanto, o arquivo
		// de treino ou teste deve ser atualizado
		if ( typeFile == TRAINING_FILE)
			trainingData = Filter.useFilter( dataset, remove);
		else
			testData = Filter.useFilter( dataset, remove);

//		System.out.println("\nAtributos originais");
//		System.out.println("------------------------------");
//		Enumeration<?> e = trainingData.enumerateAttributes();
//		while( e.hasMoreElements() ){
//			System.out.println( ((Attribute) e.nextElement()).name() );
//		}
		
//		System.out.println("\nAtributos remanescentes");
//		System.out.println("------------------------------");
//		e = newData.enumerateAttributes();
//		while( e.hasMoreElements() ){
//			System.out.println( ((Attribute) e.nextElement()).name() );
//		}
//		return null;
//		return newData;
	}
	
	
	
	
	
	/**
	 * Exibe na tela a lista de atributos encontrada no arquivo de instancias ARFF
	 * @param data O arquivo de instancias ARFF.
	 */
	public void showAttributes( Instances data ){
		Enumeration<?> e = data.enumerateAttributes();
		while( e.hasMoreElements() ){
			System.out.println(  ((Attribute) e.nextElement()).name() );
		}
		
	}
	
	/**
	 * Obtem o FMeasure da classe positiva determinada pelo avaliador de árvore de decisão
	 * @return O valor do FMeasure para a classe positiva. O valor -1 indica que o avaliador não foi
	 * instanciado.
	 */
	public double getFMeasure(){
		if( eval == null)
			return -1;
		return eval.fMeasure(0);
	}
	
	/**
	 * Salva a arvore de decisão gerada pelo classificador em um arquivo texto.
	 * @param outputFile O nome para o arquivo a ser gerado.
	 */
	public void saveDecisionTree( String outputFile ){
		/* Declaracoes para arquivo de saida*/
		BufferedWriter bw = null;
		File fileOut = new File( outputFile);
				
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bw.write( classifier.toString());
			bw.flush();
			bw.close();
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws Exception {
		WekaExperiment we = new WekaExperiment();
		
		we.loadARFFFile("G:\\experiments\\hiponimos\\Experimentos 7\\[10-90]-TREINO-14027.arff", WekaExperiment.TRAINING_FILE);
		we.loadARFFFile("G:\\experiments\\hiponimos\\Experimentos 7\\[10-90]-TESTE-7013.arff", WekaExperiment.TEST_FILE);
		
		System.out.println("+----------------------------------------------------------------------------------------------------+");
		System.out.println(" Numero de Instancias: " + we.trainingData.numInstances());
		System.out.println(" Numero de Atributos do modelo: " + we.trainingData.numAttributes());
		System.out.println("+----------------------------------------------------------------------------------------------------+");
		// toString(): exibe o conteudo do arquivo completo.
		//System.out.println(" Conteudo de toString():\n" + we.trainingData.toString());
		//System.out.println("+----------------------------------------------------------------------------------------------------+");
		// Sumario dos atributos o arquivo de treinamento
		System.out.println( "Conteudo de toSummaryString(): \n" + we.trainingData.toSummaryString());
		System.out.println("+----------------------------------------------------------------------------------------------------+");
		System.out.println(" ========== Classifier Model =========\n");
		
		System.out.println( "Training File Atributes BEFORE: " + we.getAttributesAsString(TRAINING_FILE));
		we.removeAttribute("1,2,5",true,TRAINING_FILE);
		System.out.println( "Training File Atributes AFTER : " + we.getAttributesAsString(TRAINING_FILE));
		we.buildClassifier( J48 );
		

		
		System.out.println(" ==== Evaluation on Training Set =====\n");
		we.evaluateData(TRAINING_FILE);
		System.out.println("Positive in Training File: " + we.getFMeasure());
		// Mostra a arvore de decisao criada pelo classificador
		System.out.println(" " + we.getDecisionTree());
//		we.printClassifierPredictions();
		System.out.println(" ==== Evaluation on Test Set =====\n");
		System.out.println( "Test File Atributes BEFORE: " + we.getAttributesAsString(TEST_FILE));
		we.removeAttribute("1,2,5",true, TEST_FILE);
		we.evaluateData(TEST_FILE);
		System.out.println( "Test File Atributes AFTER: " + we.getAttributesAsString(TEST_FILE));
		System.out.println("Positive in Test File: " + we.getFMeasure());

		
	}
}
