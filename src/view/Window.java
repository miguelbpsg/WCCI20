package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.math.plot.Plot2DPanel;

import utils.Messages;
import controller.Controller;
import model.chromosome.Chromosome;
import model.crossover.ContinuousCrossover;
import model.crossover.Crossover;
import model.crossover.StandardCrossover;
import model.initialization.Initialization;
import model.initialization.IncrementalInitialization;
import model.mutation.Mutation;
import model.mutation.ReplaceMutation;
import model.mutation.ExtraTestMutation;
import model.replacement.Replacement;
import model.replacement.DirectReplacement;
import model.replacement.ElitistReplacement;
import model.selection.StochasticUniversal;
import model.selection.Ranking;
import model.selection.Remains;
import model.selection.Roulette;
import model.selection.Selection;
import model.selection.Tournament;
import model.selection.Truncation;

@SuppressWarnings("serial")
public class Window extends JFrame{
	private final Controller c;
	
	private Initialization initialization;
	private Selection selection;
	private Crossover crossover;
	private Mutation mutation;
    private Replacement replacement;
    
    private Hebra hebra;
    
    private JPanel pLeft;
	private JPanel pRight;
	private JPanel pResult;
	
	private Plot2DPanel pGraph;
	
    private JLabel lTruncationRatio;
    private JLabel lTournamentPlayers;
    private JLabel lTournamentRatio;
    private JLabel lElitistRatio;

    private JLabel lResetPanel = new JLabel();
    private JLabel lFitness = new JLabel();
    
	private JTextField tfPopulation;
	private JTextField tfIters;
	private JTextField tfMaxTests;
    private JTextField tfTruncRatio;
    private JTextField tfTournamentPlayers;
    private JTextField tfTournamentRatio;
	private JTextField tfCrossoverProb;
    private JTextField tfMutationProb;
    private JTextField tfElitistRatio;

    private JTextArea taFSM;
    private JTextArea taTests;
    private JTextArea taMutants;
    
    private JComboBox<String> cbHeuristic;
	private JComboBox<Initialization> cbInitMethods;
	private JComboBox<Selection> cbSelecMethod;
	private JComboBox<Crossover> cbCrossMethod;
	private JComboBox<Mutation> cbMutMethod;
	private JComboBox<Replacement> cbReplMethod;

	private JButton bGenerateData;
	private JButton bCombinatory;
	private JButton bGreedy;
    private JButton bPlay;

    public Window(final Controller c) {
		this.c = c;
		createWindow();
	}
	
	private void createWindow() {
		setTitle(Messages.TITLE);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponents(getContentPane());

		setExtendedState(MAXIMIZED_BOTH);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void addComponents(Container pane) {
		pane.setLayout(new GridLayout(1,2,10,10));

		setupTopLeft();
		setupRight();
		setupBotLeft();

		pane.add(pLeft);
		pane.add(pRight);
	}
	
	private void setupTopLeft() {
		pLeft = new JPanel();
		pLeft.setLayout(new GridLayout(20, 2, 10, 10));

//Tamaño
		tfPopulation = new JTextField();
		tfPopulation.setText("100");
		pLeft.add(new JLabel(Messages.POBLACION, JLabel.RIGHT));
		pLeft.add(tfPopulation);

//Iteraciones
		tfIters = new JTextField();
		tfIters.setText("100");
		pLeft.add(new JLabel(Messages.ITERACIONES, JLabel.RIGHT));
		pLeft.add(tfIters);

//Profundidad inicial
		tfMaxTests = new JTextField();
		tfMaxTests.setText("80");
		pLeft.add(new JLabel(Messages.MAX_TESTS, JLabel.RIGHT));
		pLeft.add(tfMaxTests);
		
//Heuristica
		final String[] heuristics = {"Additive Heuristic","Multiplicative Heuristic"};
		cbHeuristic = new JComboBox<>(heuristics);
		pLeft.add(new JLabel(Messages.HEURISTIC, JLabel.RIGHT));
		pLeft.add(cbHeuristic);
		
//Método Inicializacion
		final Initialization[] initMethods = {new IncrementalInitialization()};
	
		cbInitMethods = new JComboBox<>(initMethods);
		pLeft.add(cbInitMethods);
		pLeft.add(new JLabel());
	
		ActionListener cmbInitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initialization = (Initialization)cbInitMethods.getSelectedItem();
			}
		};
		
		cbInitMethods.addActionListener(cmbInitListener);
		
//Método Seleccion
		final Selection[] selecMethods = {new Ranking(), new Remains(), new Roulette(), 
				new Tournament(), new Truncation(0.1), new StochasticUniversal()};
		
		cbSelecMethod = new JComboBox<>(selecMethods);
		pLeft.add(cbSelecMethod);
		pLeft.add(new JLabel());

		ActionListener cmbSelecListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = (Selection)cbSelecMethod.getSelectedItem();
				switch(selection.getSelection()) {
				case 1:
				case 2:
				case 3:
				case 6:
                    lTruncationRatio.setVisible(false);
                    tfTruncRatio.setVisible(false);
                    lTournamentPlayers.setVisible(false);
                    tfTournamentPlayers.setVisible(false);
                    lTournamentRatio.setVisible(false);
                    tfTournamentRatio.setVisible(false);
					break;
				case 4:
                    lTruncationRatio.setVisible(false);
                    tfTruncRatio.setVisible(false);
                    lTournamentPlayers.setVisible(true);
                    tfTournamentPlayers.setVisible(true);
                    lTournamentRatio.setVisible(true);
                    tfTournamentRatio.setVisible(true);
					break;
				case 5:
                    lTruncationRatio.setVisible(true);
                    tfTruncRatio.setVisible(true);
                    lTournamentPlayers.setVisible(false);
                    tfTournamentPlayers.setVisible(false);
                    lTournamentRatio.setVisible(false);
                    tfTournamentRatio.setVisible(false);
					break;
				default:
					System.err.println("Selection's Choice Error");
				}
			}
		};
		
		cbSelecMethod.addActionListener(cmbSelecListener);
		
//elitismo de truncamiento
		lTruncationRatio = new JLabel(Messages.PORCENTAJE_TRUNCAMIENTO, JLabel.RIGHT);
		tfTruncRatio = new JTextField();
		tfTruncRatio.setText("0.125");
		pLeft.add(lTruncationRatio);
		pLeft.add(tfTruncRatio);

//participantes y victoria de torneo
		lTournamentPlayers = new JLabel(Messages.PARTICIPANTES_TORNEO, JLabel.RIGHT);
		tfTournamentPlayers = new JTextField();
		tfTournamentPlayers.setText("3");
		pLeft.add(lTournamentPlayers);
		pLeft.add(tfTournamentPlayers);
		lTournamentRatio = new JLabel(Messages.PORCENTAJE_VICTORIA_TORNEO, JLabel.RIGHT);
		tfTournamentRatio = new JTextField();
		tfTournamentRatio.setText("0.8");
		pLeft.add(lTournamentRatio);
		pLeft.add(tfTournamentRatio);

//Método Cruce
		final Crossover[] crossMethods = {new ContinuousCrossover(), new StandardCrossover()};
		
		cbCrossMethod = new JComboBox<>(crossMethods);
		pLeft.add(cbCrossMethod);
		pLeft.add(new JLabel());

		ActionListener cmbCrossListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				crossover = (Crossover)cbCrossMethod.getSelectedItem();
			}
		};
		
		cbCrossMethod.addActionListener(cmbCrossListener);
		
//Prob Cruce
		tfCrossoverProb = new JTextField();
		tfCrossoverProb.setText("0.6");
		pLeft.add(new JLabel(Messages.PROBABILIDAD_CRUCE, JLabel.RIGHT));
		pLeft.add(tfCrossoverProb);

//Método Mutación
		final Mutation[] mutationMethods = {new ExtraTestMutation(), new ReplaceMutation()};
		
		cbMutMethod = new JComboBox<>(mutationMethods);
		pLeft.add(cbMutMethod);
		pLeft.add(new JLabel());

		ActionListener cmbMutacListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mutation = (Mutation)cbMutMethod.getSelectedItem();
			}
		};
		
		cbMutMethod.addActionListener(cmbMutacListener);
			
//Prob Mutacion
		tfMutationProb = new JTextField();
		tfMutationProb.setText("0.08");
		pLeft.add(new JLabel(Messages.PROBABILIDAD_MUTACION, JLabel.RIGHT));
		pLeft.add(tfMutationProb);
		
//Metodo Reemplazo
		final Replacement[] replacementMethods = {new DirectReplacement(1, null), new ElitistReplacement()};
		
		cbReplMethod = new JComboBox<>(replacementMethods);
		pLeft.add(cbReplMethod);
		pLeft.add(new JLabel());

		ActionListener cmbReempListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				replacement = (Replacement)cbReplMethod.getSelectedItem();
				switch(replacement.getReplacement()) {
				case 1:
	                lElitistRatio.setVisible(false);
	                tfElitistRatio.setVisible(false);
	                break;
				case 2:
	                lElitistRatio.setVisible(true);
	                tfElitistRatio.setVisible(true);
	                break;
				default:
					System.err.println("Error cambiando de reemplazo");
				}
			}
		};
		
		cbReplMethod.setSelectedIndex(1);
		cbReplMethod.addActionListener(cmbReempListener);
		
//Porcentaje Elitismo
	    lElitistRatio = new JLabel(Messages.PORCENTAJE_ELITISMO, JLabel.RIGHT);
	    tfElitistRatio = new JTextField();
	    tfElitistRatio.setText("0.02");
		pLeft.add(lElitistRatio);
		pLeft.add(tfElitistRatio);
 

		cbHeuristic.setSelectedIndex(0);
		cbInitMethods.setSelectedIndex(0);
		cbSelecMethod.setSelectedIndex(3);
		cbCrossMethod.setSelectedIndex(0);
		cbMutMethod.setSelectedIndex(0);
		cbReplMethod.setSelectedIndex(0);
		cmbInitListener.actionPerformed(null);
		cmbSelecListener.actionPerformed(null);
		cmbCrossListener.actionPerformed(null);
		cmbMutacListener.actionPerformed(null);
		cmbReempListener.actionPerformed(null);
		
		
		
//BOTONES
		bGenerateData = new JButton(Messages.GENERATE_DATA);
		pLeft.add(bGenerateData);

		ActionListener bGenerateDataListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				c.generateData(taFSM.getText(),20,100);
			}
		};
		bGenerateData.addActionListener(bGenerateDataListener);

		
		bCombinatory = new JButton(Messages.COMBINATORY);
		pLeft.add(bCombinatory);

		ActionListener bCombinatoryListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbHeuristic.getSelectedIndex() == 0)
					c.combinatoryAdd(taTests.getText(),taMutants.getText(), tfMaxTests.getText().equals("") ? 10 : Integer.parseInt(tfMaxTests.getText()), Window.this);
				else
					c.combinatoryMul(taTests.getText(),taMutants.getText(), tfMaxTests.getText().equals("") ? 10 : Integer.parseInt(tfMaxTests.getText()), Window.this);
			}
		};
		bCombinatory.addActionListener(bCombinatoryListener);
		

		bGreedy = new JButton(Messages.GREEDY);
		pLeft.add(bGreedy);

		ActionListener bGreedyListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbHeuristic.getSelectedIndex() == 0)
					c.greedyAdd(taTests.getText(),taMutants.getText(), tfMaxTests.getText().equals("") ? 10 : Integer.parseInt(tfMaxTests.getText()), Window.this);
				else
					c.greedyMul(taTests.getText(),taMutants.getText(), tfMaxTests.getText().equals("") ? 10 : Integer.parseInt(tfMaxTests.getText()), Window.this);
			}
		};
		bGreedy.addActionListener(bGreedyListener);
		
		
		bPlay = new JButton(Messages.GENETIC_ALGORITHM);
		pLeft.add(bPlay);

		ActionListener bPlayListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int size = tfPopulation.getText().equals("") ?  100 : Integer.parseInt(tfPopulation.getText());
				int iters = tfIters.getText().equals("") ? 100 : Integer.parseInt(tfIters.getText());
				int max_tests = tfMaxTests.getText().equals("") ? 80 : Integer.parseInt(tfMaxTests.getText());
				int players = tfTournamentPlayers.getText().equals("") ? 3 : Integer.parseInt(tfTournamentPlayers.getText());
				double vict = tfTournamentRatio.getText().equals("") ? 0.8 : Double.parseDouble(tfTournamentRatio.getText());
				double trunc = tfTruncRatio.getText().equals("") ? 0.125 : Double.parseDouble(tfTruncRatio.getText());
				double prob_cross = tfCrossoverProb.getText().equals("") ? 0.6 : Double.parseDouble(tfCrossoverProb.getText());
				double prob_mut = tfMutationProb.getText().equals("") ? 0.08 : Double.parseDouble(tfMutationProb.getText());
				double elit = tfElitistRatio.getText().equals("") ? 0.02 : Double.parseDouble(tfElitistRatio.getText());
				
	            if((hebra != null && !hebra.equals(null)) && hebra.isAlive())
	    			hebra.interrupt();
	            hebra = new Hebra(size, iters, max_tests,
	            		cbHeuristic.getSelectedIndex(),
						players, vict, trunc,
						prob_cross,
						prob_mut,
						elit
						);
	            hebra.start();
			}
		};
		
		bPlay.addActionListener(bPlayListener);
		
	}

	private void setupRight() {

		pRight = new JPanel();
		pRight.setLayout(new BorderLayout());

		pGraph = new Plot2DPanel();
		pGraph.addLegend("EAST");
		pGraph.setAxisLabel(0, Messages.GENERACION);
		pGraph.setAxisLabel(1, Messages.FITNESS);
		
		pRight.add(pGraph, BorderLayout.CENTER);

		lFitness = new JLabel();
		lResetPanel = new JLabel();
		pResult = new JPanel();
		pRight.add(pResult, BorderLayout.SOUTH);
		
    }

	private void setupBotLeft() {
		pLeft.add(new JLabel("Specification file:", JLabel.RIGHT));
		taFSM = new JTextArea("files/Spec0.txt");
		JScrollPane scrlFSM = new JScrollPane(taFSM);
		pLeft.add(scrlFSM);
		
		pLeft.add(new JLabel("Tests file:", JLabel.RIGHT));
		taTests = new JTextArea("files/Tests0.txt");
		JScrollPane scrlTests = new JScrollPane(taTests);
		pLeft.add(scrlTests);
		
		pLeft.add(new JLabel("Mutants file:", JLabel.RIGHT));
		taMutants = new JTextArea("files/Mutants0.txt");
		JScrollPane scrlMutants = new JScrollPane(taMutants);
		pLeft.add(scrlMutants);
	}

	
	public void refreshGraph(double[] generacion, double[] mejores, double[] peores, double[] medias, double[] absolutos) {

		pGraph.removeAllPlots();
		pGraph.addLinePlot("Absolute best", Color.GREEN, generacion, absolutos);
        pGraph.addLinePlot("Generational best", Color.BLUE, generacion, mejores);
        pGraph.addLinePlot("Average fitness", Color.LIGHT_GRAY, generacion, medias);
        pGraph.addLinePlot("Generational worse", Color.RED, generacion, peores);
        pGraph.revalidate();
        pRight.revalidate();
	}
	
	public void updateResult(Chromosome c) {

		lFitness.setText("\t\t\t\t");
		lFitness.revalidate();
		lFitness.removeAll();
		lResetPanel.setText("\t\t\t\t");
		lResetPanel.revalidate();
		lResetPanel.removeAll();
		pResult.removeAll();
		pResult.revalidate();
		pRight.revalidate();
		
		pResult = new JPanel();
		pResult.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		pResult.add(new JLabel(Messages.MEJOR_GLOBAL, JLabel.TRAILING), constraints);
		lFitness = new JLabel( ((Double)c.getFitness()) .toString() );

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		pResult.add(lFitness, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		pResult.add(new JLabel(Messages.CODIFICACION), constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		lResetPanel = new JLabel(c.toString());
		pResult.add(lResetPanel, constraints);
		lResetPanel.revalidate();
		lFitness.revalidate();
		pResult.revalidate();
		pRight.add(pResult, BorderLayout.SOUTH);
		pRight.revalidate();
	}
	
	private final class Hebra extends Thread {
		private int size;
		private int iters;
		private int max_tests;
		private int heur;
		private int participantes;
		private double vict;
		private double trunc;
		private double prob_cruce;
		private double prob_mut;
		private double elit;

		public Hebra(int size, int iters, int max_tests, int heur, int participantes, double vict, double trunc,
				double prob_cruce, double prob_mut, double elit) {
			super();
			this.size = size;
			this.iters = iters;
			this.max_tests = max_tests;
			this.heur = heur;
			this.participantes = participantes;
			this.vict = vict;
			this.trunc = trunc;
			this.prob_cruce = prob_cruce;
			this.prob_mut = prob_mut;
			this.elit = elit;
		}
		
		public void run() {
			try {
				Thread.sleep(1);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						c.run(size, iters, initialization, max_tests, heur,
								selection, participantes, vict, trunc,
								crossover, prob_cruce,
								mutation, prob_mut,
								taTests.getText(), taMutants.getText(),
								replacement, elit,
								Window.this);
					}
				});
			} catch (InterruptedException e) {
				System.err.println("matando hebra en ventana");
			}
		}
	}
}