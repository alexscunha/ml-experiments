package machine.learning;

import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

import weka.WekaExperiment;

/**
 * Classe que executa o Menu com as opções necessárias para realização de experimentos de
 * DataMining.
 * @author Alex
 * @version 1.1 (15/08/2013)
 * Modificações feitas: pequenos ajustes de correção no código, pra deixar mais clara a interaçao
 * com o usuário.
 */
public class MainDataMining {
	
	public static Scanner in = new Scanner(System.in);

	
	/**
	 *  Interface com o usuario para a opcao "Selecionar instancias Randomicamente".
	 */
	public static void uiRandomFile(){
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| O usuario informa o NUMERO de instancias que deseja selecionar aleatoriamente. Em seguida, deve                          |");
		System.out.println("| indicar qual arquivo sera utilizado como base para a selecao: arquivo de POSITIVOS ou NEGATIVOS.                         |");
		System.out.println("| Nada impede que o codigo seja modificado para que as instancias sejam sorteadas de um arquivo que contem                 |");
		System.out.println("| tanto (+) quanto (-).                                                                                                    |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		System.out.println("=====================================================================================================================");

		System.out.println("Positive File = " + DataMining.configGetProperty("fullPositiveFile"));
		System.out.println("Negative File = " + DataMining.configGetProperty("fullNegativeFile"));
		
		int quant;
		System.out.print("\nDigite a quantidade de instancias a selecionar (0 para retornar ao menu): ");
		quant = Integer.parseInt( in.next() );
		
		if( quant == 0 )
			return;
		
		String tipo;
		System.out.print("Positiva (+) ou Negativa (-): ");
		tipo = in.next();
		
		if ( tipo.equals("+")) {
			/* Apenas positivas */
			DataMining.createRandomFile( quant, DataMining.configGetProperty("fullPositiveFile") );
		} else if ( tipo.equals("-")) {
			/* Apenas negativas */
			DataMining.createRandomFile( quant, DataMining.configGetProperty("fullNegativeFile") );
			/* Saida:  IN-Supervised_Instances_NO[01]-RamdomFile-quant */
		} else
			System.err.println("Tipo invalido!\n");	
		
	} // uiRandomFile()
	
	/**
	 * Interface referente à opção "Dividir arquivo de TREINO (2/3) e TESTE (1/3)"
	 */
	public static void uiSplitTrainTest(){
		String trainingFile, testFile;
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, o programa utiliza um arquivo com instancias positivas e negativas. Este arquivo de entrada é dividido em   |");
		System.out.println("| dois arquivos: 1 de TREINO (com 2/3 das instancias positivas e negativas) e  um  de TESTE (com  1/3  das  instancias     |");
		System.out.println("| positivas e negativas), conservando a proporcao de instancias positivas em cada um dos arquivos.                         |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		System.out.print(  "  Deseja continuar (S/N)? ");
		String resposta = in.nextLine();
		
		if ( resposta.equalsIgnoreCase("S")) {
			System.out.println("[ Base File: " + DataMining.configGetProperty("baseFile") + "]");
			System.out.print("Deseja selecionar um arquivo de base diferente (S/N)? = ");
			resposta = in.nextLine();
			
			if (resposta.equalsIgnoreCase("S")) { // quer escolher o arquivo
				JFileChooser fc = new JFileChooser( DataMining.configGetProperty("drive") + DataMining.configGetProperty("inputDir") );
				fc.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );     

				System.out.print("Selecione o arquivo COMPLETO: ");
				int result = fc.showOpenDialog( null );
                // if user clicked Cancel button on dialog, return
				if ( result == JFileChooser.APPROVE_OPTION ) {
					String file = fc.getSelectedFile().getName();
					System.out.print(file + "\n" ); 
					
					System.out.print("Digite o prefix desejado para o arquivo de saida: ");
					String prefix = in.nextLine();
					trainingFile = DataMining.createRandomTrainingData( file, prefix + "-TREINO-" );
					System.out.println("\tArquivo de TREINO: " + trainingFile);
					testFile = DataMining.extractRemainingInstancesForTest( file, trainingFile, prefix + "-TESTE-" );
					System.out.println("\tArquivo de TESTE : " + testFile);
					
				}
			} else {  // quer usar as definicoes de arquivo estaticas
				trainingFile = DataMining.createRandomTrainingData( DataMining.configGetProperty("baseFile"), "Training_File_All_");
				System.out.println("\tArquivo de TREINO: " + trainingFile);
				testFile = DataMining.extractRemainingInstancesForTest(DataMining.configGetProperty("baseFile"), trainingFile, "Test_File_All_" );
				System.out.println("\tArquivo de TESTE : " + testFile);
			}
		}
	}
	
	
	public static void uiConvertARFF(){
		String[] att = DataMining.configGetProperty("attList").split("\t");
		String trainingFile, testFile;
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, um par de arquivos TREINO e TESTE são informados para que seja feita sua devida conversão para o            |");
		System.out.println("| formato .ARFF do Weka. Um conjunto de atributos padrão é mostrado. Entretanto, pode ser modificado caso necessário.      |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		
		System.out.println("  Training File: " + DataMining.configGetProperty("trainingFile"));
		System.out.println("  Teste File...: " + DataMining.configGetProperty("testFile"));
		
		System.out.print(  "  Deseja utilizar os arquivos Treino/Teste padrão ou selecionar manualmente? (A)Auto,(M)anual?: ");
		String resposta = in.nextLine();		
		
		if ( resposta.equalsIgnoreCase("A")) { // AUTOMATICA
		    trainingFile = DataMining.configGetProperty("trainingFile");
		    testFile     = DataMining.configGetProperty("testFile");	
		} else if ( resposta.equalsIgnoreCase("M")) { // MANUAL
			JFileChooser fc = new JFileChooser( DataMining.configGetProperty("drive") + DataMining.configGetProperty("inputDir") );
			fc.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );     

			System.out.print("\n   Selecione o arquivo de TREINO: ");
			int result = fc.showOpenDialog( null );
            // if user clicked Cancel button on dialog, return
			if ( result == JFileChooser.APPROVE_OPTION ) {
				trainingFile = fc.getSelectedFile().getAbsolutePath();
				System.out.print(trainingFile + "\n" ); 
			} else
				return;

			System.out.print("   Selecione o arquivo de TESTE: ");
			result = fc.showOpenDialog( null );
            // if user clicked Cancel button on dialog, return
			if ( result == JFileChooser.APPROVE_OPTION ) {	
				testFile = fc.getSelectedFile().getAbsolutePath();
				System.out.print(testFile + "\n");
			} else
				return;
			
		} else {
			System.out.println("Resposta inválida. Digite \"A\" ou \"M\" da próxima vez.\n" );
			return;
		}
		
		// Exibindo a lista de atributos extraído do config.dat
		System.out.print("\nAtributos: [ ");
		for(int i=0; i< att.length; i++)
			System.out.print( att[i] + "\t");
		System.out.println("]");
		
		// Perguntando se o usuário quer selecionar os atributos
		System.out.print("   Deseja selecionar os atributos manualmente (M) ou considerar todos (T)?: ");
		resposta = in.nextLine();
		
		if (resposta.equalsIgnoreCase("M")) {
			String atributo = null;
			ArrayList<String> list = new ArrayList<String>();
			System.out.println("Digite os atributos (um nome em branco encerra a digitacao)");
			for(int i=1; ; i++){
				System.out.print("Atributo " + i + ": ");
				atributo = in.nextLine();
				if ( atributo.length() == 0)
					break;
				list.add( atributo );
			}
			
			att = (String[]) list.toArray( new String[list.size()]);
			// listando os novos atributos
			System.out.print("\nAtributos selecionados: [ ");
			for(int i=0; i< att.length; i++)
				System.out.print( att[i] + "\t");
			System.out.println("]");
		}
		
		DataMining.makeARFF( trainingFile , DataMining.TRAINING, att);
		DataMining.makeARFF( testFile, DataMining.TEST, att);
	
	} // fim uiConvertARFF()
	
	public static void uiEstimationValidation(){
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, um arquivo de TREINO gerado pela opcao 5 do menu é subdividido em TREINO e VALIDACAO, para fins             |");
		System.out.println("| de crianção do MODELO DE CLASSIFICACAO (no Weka). O arquivo de TREINO passado como argumento gera dois novos arquivos:   |");
		System.out.println("| um prefixo-TR-quant com 2/3 das instancias (+ e -), e um prefixo-VL-quant com 1/3 das instancias.                        |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		System.out.print(  "  Deseja Continuar (S/N)?: ");

		JFileChooser fc = new JFileChooser( DataMining.configGetProperty("drive") + DataMining.configGetProperty("inputDir"));
		fc.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );     

		System.out.print("Selecione o arquivo de TREINO: ");
		int result = fc.showOpenDialog( null );
        // if user clicked Cancel button on dialog, return
		if ( result == JFileChooser.CANCEL_OPTION )
			return;
		else {
			System.out.print("\nDigite o prefixo para o arquivo: ");
			String prefix = in.nextLine();
			DataMining.createValidationData( fc.getSelectedFile().getName(), prefix );
			System.out.print( fc.getSelectedFile().getName() + " [DONE]\n");
		}
	}
	
	
	public static void uiPartition(){
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao o usuario define qual particao deseja criar. O metodo createPartition(), entao, calcula a quantidade         |");
		System.out.println("| de instancias necessarias (POSITIVAS e NEGATIVAS), de acordo com a proporcao da particao. O resultado é um arquivo no    |");
		System.out.println("| formato, por exemplo, \"[10-90]-FULL-quant.\" no diretorio de saida. Outros arquivos (EST,VAL,TREINO e TEST) são criados   |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		
		for(;;){
			int particao;
			String sourceFile = DataMining.configGetProperty("baseFile");
			System.out.println("\nSource File = [ " + sourceFile + " ]\n");
		
			System.out.println("(1)-[10,90]  (2)-[20,80]  (3)-[30,70]  (4)-[40.60]  (5)-[50,50] :");
			System.out.print("Digite o numero da divisao de PARTICAO desejada (0 para sair): ");
			particao = Integer.parseInt( in.nextLine() );

			if( particao == 0 )
				return;
			
			if ( particao > 0 && particao < 6 )
				DataMining.createPartition( particao , DataMining.configGetProperty("filePositive"), DataMining.configGetProperty("fileNegative") );
		} // fim for.

	}
	
	public static void uiSelectInstances(){
		String baseFile = DataMining.configGetProperty("baseFile");
		String fullFile = DataMining.configGetProperty("fullFile");
		String sourceFile = null;
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, o usuario determina o rotulo que deseja filtrar dentro do arquivo geral de instancias. Como resultado,      |");
		System.out.println("| o programa gera um arquivo de nome \"out-Selected[Pos/neg]Instances\" no diretorio de escrita.                             |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		System.out.println("1- Full File = [ " + fullFile + " ]");
		System.out.println("2- Base File = [ " + baseFile + " ]");
		System.out.print("Qual dos dois arquivos deseja processar: (1) ou (2)?: ");
		int escolha = Integer.parseInt(in.nextLine());
		sourceFile = (escolha==1? fullFile: baseFile);
		System.out.println("\tSourceFile = " + sourceFile);
		
		System.out.print("Digite o rotulo que deseja filtrar: ");
		String label = in.nextLine();
		
		DataMining.selectInstances( sourceFile, label);
	}
	
	public static void uiSelectAttributes(){
//		String retorno = DataMining.configGetProperty("filePositive");
//		System.out.println("Atributo de config: " );
//		System.out.println("Testando o Math.round()");
//		System.out.println("Math.round(9.3)= " + Math.round(9.3));
//		System.out.println("Math.round(9.6)= " + Math.round(9.6));
//		System.out.println("Testando indexOf(':')");
//		System.out.println("indice: " + "g:/teste".indexOf(':'));
//		System.out.println("indice: " + "teste".indexOf(':'));
		
		String sourceFile = DataMining.configGetProperty("baseFile");
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, o programa exibira uma lista de atributos extraidos do cabecalho do arquivo de instancias SOURCE FILE, de   |");
		System.out.println("| de modo que o usuario possa selecionar aqueles que deseja manter para o experimento. Ao final, sera gerado um arquivo de |");
		System.out.println("| baseado em SOURCE FILE, apenas com os atributos assinalados.                                                             |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");

		System.err.println("Source File = [ " + sourceFile + " ]");
		DataMining.cutAttribute( sourceFile );
	}
	
	public static void uiFilterInstancesByAtt() {		
		String baseFile = DataMining.configGetProperty("baseFile");
		String fullFile = DataMining.configGetProperty("fullFile");
		String sourceFile = null;
		
		System.out.println();
		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
		System.out.println("| Nesta opcao, o programa utiliza o arquivo definido como \'source file\' para filtrar as instâncias rotuladas com um        |");
		System.out.println("| com um texto especificado, cujo valor seja maior que zero.                                                               |");
		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
		System.out.println("1- Full File = [ " + fullFile + " ]");
		System.out.println("2- Base File = [ " + baseFile + " ]");
		System.out.print("Qual dos dois arquivos deseja processar: (1) ou (2)?: ");
		int escolha = Integer.parseInt(in.nextLine());
		sourceFile = (escolha==1? fullFile: baseFile);
		
		System.out.println("\tSourceFile = " + sourceFile);
		
		System.out.print("Digite o nome do atributo chave para o filtro: ");
		String atributo = in.nextLine();
		System.out.print("Digite o identificador da condicao (geq/leq/g/l/eq/neq): ");
		String condicao = in.nextLine();
		System.out.print("Digite o valor do threshold: ");
		double threshold = Double.parseDouble(in.nextLine());

		DataMining.filterInstancesByAttribute( sourceFile, atributo, condicao, threshold);
		
		//DataMining.filterInstancesByAttribute( sourceFile, "suporte", "gt", 5);

	}

	public static void uiAutomaticExperiment() {
//		System.out.println();
//		System.out.println("+--[Help]------------------------------------------------------------------------------------------------------------------+");
//		System.out.println("| Nesta opcao, o programa utiliza o arquivo definido como \'source file\' para filtrar as instâncias rotuladas com um        |");
//		System.out.println("| com um texto especificado, cujo valor seja maior que zero.                                                               |");
//		System.out.println("+--------------------------------------------------------------------------------------------------------------------------+");
//		
//		int particao;
//		System.err.println("Diretório de procura (config): " + DataMining.configGetProperty("drive") + 
//				                                               DataMining.configGetProperty("inputDir"));
//		System.out.println("\nDigite o numero da PARTICAO que deseja processar: ");
//		System.out.print("(1)-[10,90]  (2)-[20,80]  (3)-[30,70]  (4)-[40.60]  (5)-[50,50] :");
//		particao = Integer.parseInt( in.nextLine() );
//
//		if ( particao < 1 || particao > 5 )
//			return;
		System.out.println("Classificadores Disponíveis: (1)J48         (2)Nayve Bayes          (3)Random Forest");
		System.out.println("                             (4)AdaBoostM1  (5)Logistic Regression  (6)SVM");
		System.out.print("Digite o identificador (numero) do classificador desejado: ");
		int classificador = Integer.parseInt( in.nextLine());
		
		switch( classificador){
		case 1:
			classificador = WekaExperiment.J48;
			break;
		case 2:
			classificador = WekaExperiment.NAYVE_BAYES;
			break;
		case 3:
			classificador = WekaExperiment.RANDOM_FOREST;
			break;
		case 4:
			classificador = WekaExperiment.ADABOOST;
			break;		
		case 5: 
			classificador = WekaExperiment.LOGISTIC_REGRESSION;
			break;			
		case 6:
			classificador = WekaExperiment.SVM;
			break;						
		default: 
			classificador = WekaExperiment.J48;
		}

		try {
			System.out.println();
			DataMining.doAutomaticExperiment( classificador );
			System.out.println();
		} catch (Exception e){
			System.err.println( "ERRO: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		int option;		

		/* Obrigatorio fazer a configuracao, senao assume a DEFAULT */
		DataMining.loadConfig("config.dat");
		while ( true ) {
			System.out.println("Data Mining Preparation tools: ");
			System.out.println("================================");
			System.out.println("1 - Selecionar instancias Randomicamente [createRanfomFile()]");
			System.out.println("2 - Dividir arquivo de TREINO (2/3) e TESTE (1/3)");
			System.out.println("3 - Converter para o formato ARFF [makeARFF()]");
			System.out.println("4 - Preparar ESTIMACAO e VALIDACAO [createValidationData()]");
			System.out.println("5 - Criar Arquivo de Particao randomico [createPartition()]");
			System.out.println("6 - Selecionar Instancias (+) ou (-) em um arquivo de Saida");
			System.out.println("7 - Selecionar Atributos");
			System.out.println("8 - Filtrar instancias pelo valor de um atributo");
			System.out.println("9 - Executar experimento automático de DataMining");
			System.out.println("10 - Saída");
			System.out.print  (">> ");
			option = Integer.parseInt(in.nextLine());
			
			switch( option) {
			case 1: /* ===================== CASE 1 ============================ */
			{
				uiRandomFile();
				break;
			}
			case 2: /* ===================== CASE 2 ============================ */
			{
				uiSplitTrainTest();
				break;
			}
			case 3: /* ===================== CASE 3 ============================ */
			{			
				uiConvertARFF();
				break;
			}
			case 4: /* ===================== CASE 4 ============================ */
			{
				uiEstimationValidation();
				break;
			}
			case 5: /* ===================== CASE 5 ============================ */
			{
				uiPartition();
				break;
			}	
			case 6: /* ===================== CASE 6 ============================ */
			{
				uiSelectInstances();
				break;
			}
			case 7: /* ===================== CASE 7 ============================ */
			{
				uiSelectAttributes();
				break;
			}
			case 8: /* ===================== CASE 8 ============================ */
			{
				uiFilterInstancesByAtt();
				break;
			}
			case 9: /* ===================== CASE 9 ============================ */
			{
				uiAutomaticExperiment();
				break;
			}
			case 10: /* ===================== CASE 10 ============================ */
				System.exit(0);
			} // fim switch()
			
			
		} // fim while


	}


}
