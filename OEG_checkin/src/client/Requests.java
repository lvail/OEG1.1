package client;

import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Point;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import shared.Messages;
import shared.action.Action;

import java.awt.Component;
import javax.swing.border.BevelBorder;
import javax.swing.ScrollPaneConstants;
import javax.swing.JPanel;
import java.awt.SystemColor;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import javax.swing.JFormattedTextField;
import java.awt.Rectangle;

/**
 * @author unknown;
 *
 */


//TODO Make Cell info fields scrollable?
@SuppressWarnings("serial")
public class Requests extends JApplet {
	private JTextField x2Field;
	private JTextField y2Field;
	private IntTextField bidField;
	private JTextField x1Field;
	private JTextField y1Field;
	private JLabel roundField;
	private JLabel totalRoundField;			//NEW
	private JLabel timerField;
	private JLabel hBankField;
	private JLabel hoverLabel;
	private JRadioButton SLButton;

	private ClientApplet clientApplet;
	public ClientOperator operator;
	private JLabel ipLabel;
	private JLabel hTeamField;
	private JTextField seismicCostField;
	private JLabel ownerField;
	private DefaultListModel listModel;
	private DefaultListModel messagesModel;
	private JButton btnRemoveAction;
	private JList actionQueueList;
	private JLabel oilTextArea;
	private JLabel gasTextArea;
	private JLabel layerTextArea;
	private JLabel rockTypeTextArea;
	private JScrollPane mapScrollPane;
	public final ButtonGroup buttonGroup = new ButtonGroup();
	private Map map;
	public static String teamName;
	private JButton bidButton;
	private JButton drillButton;
	private JButton requestButton;
	private JScrollPane messageScrollPane;
	private JList messageList;
	private JPanel panel_1;
	private JPanel actionsPanel;
	private JPanel legendPanel;
	private JLabel teamOwnedOil;
	private JLabel teamOwnedLand;
	private JLabel compOwnedLand;
	private JLabel compOwnedOil;
	private JLabel SeismicIntel;
	
	static {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
			// handle exception
		}
	}

	/**
	 * Create the panel.
	 */
	public Requests() {
		this.clientApplet = new ClientApplet(this);

		setBackground(Color.WHITE);
		
		getContentPane().setLayout(null);
		
		//IF STATEMENTS TO SET MINIMUM PANEL SIZE

		panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setLayout(null);
		panel_1.setBounds(0, 0, 1280, 920);
		getContentPane().add(panel_1);
		
		topInfoL(panel_1);
		topInfoR(panel_1);
		bottomInfo(panel_1);
		mapPanel(panel_1);
		actionPanel(panel_1);
		queuePanel(panel_1);
		cellInfoPanel(panel_1);
		messagePanel(panel_1);
		legendPanel(panel_1);
	}

	/** Create the top info panel, including team name label and bank amount labels
	 * @param panel The panel that this info panel should be added to.
	 */
	private void topInfoL(JPanel panel) {
		
				hoverLabel = new JLabel("t");
				hoverLabel.setBounds(10, 12, 500, 23);
				panel_1.add(hoverLabel);
				hoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
				hoverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				hoverLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
		JPanel topInfoL = new JPanel();
		topInfoL.setBorder(null);
//		int x = (int)(dim.width*.0078);
//		int y = (int)(dim.height*.0022);
//		int width = (int)(dim.width*.4922);
//		int height = (int)(dim.height*.0436);
		topInfoL.setBounds(10, 2, 630, 40);
		panel.add(topInfoL);
		topInfoL.setLayout(new BorderLayout(0, 0));
						
						JPanel teamPanel = new JPanel();
						topInfoL.add(teamPanel, BorderLayout.WEST);
						teamPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
						
								JLabel hTeamLabel = new JLabel("Team:");
								teamPanel.add(hTeamLabel);
								hTeamLabel.setBorder(null);
								hTeamLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
								hTeamLabel.setForeground(Color.BLACK);
								
										hTeamField = new JLabel();
										teamPanel.add(hTeamField);
										hTeamField.setFont(new Font("Tahoma", Font.BOLD, 25));
										hTeamField.setBorder(null);
										hTeamField.setBackground(SystemColor.menu);
										hTeamField.setForeground(Color.BLACK);
										hTeamField.setText("");
								
								JPanel bankPanel = new JPanel();
								FlowLayout flowLayout = (FlowLayout) bankPanel.getLayout();
								flowLayout.setAlignment(FlowLayout.RIGHT);
								topInfoL.add(bankPanel, BorderLayout.EAST);
								
										JLabel hBankLabel = new JLabel("Bank: ");
										bankPanel.add(hBankLabel);
										hBankLabel.setBorder(null);
										hBankLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
										hBankLabel.setForeground(Color.BLACK);
										
												hBankField = new JLabel();
												bankPanel.add(hBankField);
												hBankField.setHorizontalAlignment(SwingConstants.RIGHT);
												hBankField.setAlignmentX(Component.CENTER_ALIGNMENT);
												hBankField.setFont(new Font("Tahoma", Font.BOLD, 25));
												hBankField.setBorder(null);
												hBankField.setForeground(Color.BLACK);
												hBankField.setBackground(SystemColor.menu);
												hBankField.setText("$1,000,000");
	}
	/** Create the top right info panel, including timer label
	 * and round number info.
	 * @param panel The panel that this info panel should be added to.
	 */
	private void topInfoR(JPanel panel) {
		JPanel topInfoR = new JPanel();
//		int x = (int)(dim.width*.5);
//		int y = (int)(dim.height*.0022);
//		int width = (int)(dim.width*.4922);
//		int height = (int)(dim.height*.0435);
		topInfoR.setBounds(640, 2, 630, 40);
		panel.add(topInfoR);
		topInfoR.setLayout(new BorderLayout(0, 0));
						
						JPanel roundPanel = new JPanel();
						topInfoR.add(roundPanel, BorderLayout.WEST);
						FlowLayout fl_roundPanel = new FlowLayout(FlowLayout.LEFT, 5, 5);
						roundPanel.setLayout(fl_roundPanel);
						
								JLabel roundLabel = new JLabel("Round:");
								JLabel midLabel = new JLabel(" of ");
								roundPanel.add(roundLabel);
								roundLabel.setForeground(Color.BLACK);
								roundLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
								
										roundField = new JLabel();
										roundPanel.add(roundField);
										roundField.setForeground(Color.BLACK);
										roundField.setBorder(null);
										roundField.setFont(new Font("Tahoma", Font.BOLD, 25));
										roundField.setBackground(SystemColor.menu);
										roundField.setText(""); // change "" to Operator.getRound
										totalRoundField = new JLabel();
										roundPanel.add(midLabel);
										midLabel.setForeground(Color.BLACK);
										midLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
										roundPanel.add(totalRoundField);
										totalRoundField.setForeground(Color.BLACK);
										totalRoundField.setBorder(null);
										totalRoundField.setFont(new Font("Tahoma", Font.BOLD, 25));
										totalRoundField.setBackground(SystemColor.menu);
										totalRoundField.setText("");
						
						JPanel timerPanel = new JPanel();
						topInfoR.add(timerPanel, BorderLayout.EAST);
						timerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
						
								JLabel timerLabel = new JLabel("Timer:");
								timerPanel.add(timerLabel);
								timerLabel.setForeground(Color.BLACK);
								timerLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
								
										timerField = new JLabel();
										timerPanel.add(timerField);
										timerField.setHorizontalAlignment(SwingConstants.RIGHT);
										timerField.setForeground(SystemColor.desktop);
										timerField.setBorder(null);
										timerField.setFont(new Font("Tahoma", Font.BOLD, 25));
										timerField.setBackground(SystemColor.menu);
										timerField.setText(""); // change "" to Operator.getTime
		listModel = new DefaultListModel();
	}
	
	/** Create the bottom info panel, including the Connected To label
	 * and the developer test button.
	 * @param panel The panel that this info panel should be added to.
	 */
	private void bottomInfo(JPanel panel) {
		JPanel bottomInfo = new JPanel();
//		int x = (int)(dim.width*.0078);
//		int y = (int)(dim.height*.9783);
//		int width = (int)(dim.width*.3906);
//		int height = (int)(dim.height*.0217);
		bottomInfo.setBounds(10, 900, 500, 15);
		panel.add(bottomInfo);
		bottomInfo.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel lblConnectedTo = new JLabel("Connected To:");
		bottomInfo.add(lblConnectedTo);
		
				ipLabel = new JLabel("None");
				bottomInfo.add(ipLabel);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		bottomInfo.add(horizontalGlue);

		
			JButton btnEndRound = new JButton("TEST");
		btnEndRound.setBounds(430, 1, 68, 18);
		if(Messages.TESTMODE)
			bottomInfo.add(btnEndRound);
		btnEndRound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				operator.endRound();
			}
		});
	}
	
	/** Create the map panel, where the Map grid is placed, inside a scrollbar.
	 * @param panel The panel that this map panel should be added to.
	 */
	private void mapPanel(JPanel panel) {
		mapScrollPane = new JScrollPane();
//		int x = (int)(dim.width*.0078);
//		int y = (int)(dim.height*.0489);
//		int width = (int)(dim.width*.6641);
//		int height = (int)(dim.height*.9239);
		mapScrollPane.setBounds(10, 45, 850, 850);
		panel.add(mapScrollPane);
		mapScrollPane
		.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapScrollPane
		.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mapScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		mapScrollPane.getHorizontalScrollBar().setUnitIncrement(10);


		JPanel panel_2 = new JPanel();
		mapScrollPane.setColumnHeaderView(panel_2);
		panel_2.setLayout(null);

		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setBounds(232, 19, 1, 1);
		panel_2.add(layeredPane);
	}
	
	/** Create the action panel, where the all the
	 * action fields and buttons are placed.
	 * @param panel The panel that this action panel should be added to.
	 */
	private void actionPanel(JPanel panel) {
		actionsPanel = new JPanel();
		actionsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
//		int x = (int)(dim.width*.6797);
//		int y = (int)(dim.height*.3033);
//		int width = (int)(dim.width*.3133);
//		int height = (int)(dim.height*.1120);
		actionsPanel.setBounds(870, 279, 401, 103);
		panel.add(actionsPanel);
		actionsPanel.setLayout(null);
		actionsPanel.setVisible(false);

		JLabel x1Label = new JLabel("X1");
		x1Label.setHorizontalAlignment(SwingConstants.RIGHT);
		x1Label.setBounds(6, 37, 25, 14);
		actionsPanel.add(x1Label);
		x1Label.setRequestFocusEnabled(false);
		x1Label.setForeground(Color.BLACK);
		x1Label.setFont(new Font("Tahoma", Font.BOLD, 12));

		x1Field = new JTextField();
		x1Field.setHorizontalAlignment(SwingConstants.CENTER);
		x1Field.setEditable(false);
		x1Field.setBounds(41, 35, 25, 20);
		actionsPanel.add(x1Field);
		x1Field.setColumns(10);

		y1Field = new JTextField();
		y1Field.setEditable(false);
		y1Field.setHorizontalAlignment(SwingConstants.CENTER);
		y1Field.setBounds(41, 66, 25, 20);
		actionsPanel.add(y1Field);
		y1Field.setColumns(10);

		JLabel y1Label = new JLabel("Y1");
		y1Label.setHorizontalAlignment(SwingConstants.RIGHT);
		y1Label.setBounds(6, 68, 25, 14);
		actionsPanel.add(y1Label);
		y1Label.setRequestFocusEnabled(false);
		y1Label.setForeground(Color.BLACK);
		y1Label.setFont(new Font("Tahoma", Font.BOLD, 12));

		final JLabel x2Label = new JLabel("X2");
		x2Label.setBounds(76, 37, 16, 14);
		actionsPanel.add(x2Label);
		x2Label.setFont(new Font("Tahoma", Font.BOLD, 12));
		x2Label.setForeground(Color.BLACK);

		x2Field = new JTextField();
		x2Field.setHorizontalAlignment(SwingConstants.CENTER);
		x2Field.setEditable(false);
		x2Field.setBounds(102, 35, 25, 20);
		actionsPanel.add(x2Field);
		x2Field.setColumns(10);

		final JLabel y2Label = new JLabel("Y2");
		y2Label.setBounds(76, 68, 16, 14);
		actionsPanel.add(y2Label);
		y2Label.setFont(new Font("Tahoma", Font.BOLD, 12));
		y2Label.setForeground(Color.BLACK);

		y2Field = new JTextField();
		y2Field.setEditable(false);
		y2Field.setHorizontalAlignment(SwingConstants.CENTER);
		y2Field.setBounds(102, 66, 25, 20);
		actionsPanel.add(y2Field);
		y2Field.setColumns(10);
		
		final JLabel bidLabel = new JLabel("$");
		bidLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		bidLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		bidLabel.setBounds(76, 53, 16, 14);
		actionsPanel.add(bidLabel);

		bidButton = new JButton("Bid");
		bidButton.setBounds(177, 50, 80, 23);
		actionsPanel.add(bidButton);
		bidButton.setRequestFocusEnabled(false);

		bidField = new IntTextField();
		
		
		bidField.setBounds(99, 51, 68, 20);
		actionsPanel.add(bidField);
		bidField.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		bidField.setBackground(SystemColor.window);
		bidField.setColumns(10);

		seismicCostField = new JTextField();
		seismicCostField.setBounds(137, 35, 120, 20);
		actionsPanel.add(seismicCostField);
		seismicCostField.setEditable(false);
		seismicCostField.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		seismicCostField.setColumns(10);

		drillButton = new JButton("Drill");
		drillButton.setBounds(102, 32, 80, 55);
		actionsPanel.add(drillButton);

		requestButton = new JButton("Seismic Request");
		requestButton.setBounds(134, 65, 120, 23);
		actionsPanel.add(requestButton);
		
		JPanel radioPanel = new JPanel();
		radioPanel.setBounds(1, 5, 304, 26);
		actionsPanel.add(radioPanel);
		radioPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
				SLButton = new JRadioButton("Seismic Line");
				radioPanel.add(SLButton);
				SLButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						//map.colorSeismicKnowledge(null);
						map.repaint(); //calls paintColorsMode();
						map.sMode = 1;

						x2Label.setVisible(true);
						x2Field.setVisible(true);
						y2Label.setVisible(true);
						y2Field.setVisible(true);
						requestButton.setVisible(true);
						seismicCostField.setVisible(true);

						bidButton.setVisible(false);
						bidField.setVisible(false);
						bidLabel.setVisible(false);
						drillButton.setVisible(false);																																					 map.curBGColor =new Color(82, 61, 0);
					}
				});
				SLButton.setSelected(true);
				SLButton.setActionCommand("S");
				
				buttonGroup.add(SLButton);
				SLButton.setBackground(UIManager.getColor("Button.background"));
				
						JRadioButton bidButton1 = new JRadioButton("Bid");
						radioPanel.add(bidButton1);
						bidButton1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								//map.colorBiddableLand(null);
								map.repaint(); //calls paintColorsMode();
								map.curBGColor =new Color(184,138,0);

								bidButton.setVisible(true);
								bidField.setVisible(true);
								bidLabel.setVisible(true);

								x2Label.setVisible(false);
								x2Field.setVisible(false);
								y2Label.setVisible(false);
								y2Field.setVisible(false);
								drillButton.setVisible(false);
								requestButton.setVisible(false);
								seismicCostField.setVisible(false);
							}
						});
						bidButton1.setActionCommand("B");
						buttonGroup.add(bidButton1);
						bidButton1.setBackground(UIManager.getColor("Button.background"));
						
								JRadioButton drillButton1 = new JRadioButton("Drill");
								radioPanel.add(drillButton1);
								drillButton1.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent arg0) {
										//map.colorDrillableLand(null);
										map.repaint(); //calls paintColorsMode();
										map.curBGColor =new Color(240,240,240);

										bidButton.setVisible(false);
										bidField.setVisible(false);
										bidLabel.setVisible(false);

										x2Label.setVisible(false);
										x2Field.setVisible(false);
										y2Label.setVisible(false);
										y2Field.setVisible(false);
										drillButton.setVisible(true);
										requestButton.setVisible(false);
										seismicCostField.setVisible(false);
									}
								});
								drillButton1.setActionCommand("D");
								buttonGroup.add(drillButton1);
								drillButton1.setBackground(UIManager.getColor("Button.background"));
		
		requestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int x1Value = Integer.parseInt(x1Field.getText());
					int y1Value = Integer.parseInt(y1Field.getText());
					int x2Value = Integer.parseInt(x2Field.getText());
					int y2Value = Integer.parseInt(y2Field.getText());
					
					if (operator.makeNewSeismicRequest(x1Value, y1Value, x2Value, y2Value)) {

						addMessage("Valid. Seismic Data Request Made.");
						seismicCostField.setText("");
					} else {
						addMessage("Problem with request. Verify valid coordinates and enough funds.");
					}
					x1Field.setText("");
					y1Field.setText("");
					x2Field.setText("");
					y2Field.setText("");
				} catch (NumberFormatException nfe) {
					addMessage("Problem with request. Please select a cell.");
				}
			}
		});
		drillButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int xValue = Integer.parseInt(x1Field.getText());
					int yValue = Integer.parseInt(y1Field.getText());

					if (operator.makeNewDrill(xValue,yValue)) {
						addMessage("Valid. Drill Request Made.");
					} else {
						addMessage("Problem with drill. Make sure the land is owned and there are enough funds.");
					}
				} catch (NumberFormatException nfe) {
					addMessage("Problem with drill. Please select a cell.");
				}
			}
		});
		bidButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int xValue = Integer.parseInt(x1Field.getText());
					int yValue = Integer.parseInt(y1Field.getText());
					int bidValue = Integer.parseInt(bidField.getText());
					
					if (operator.makeNewBid(xValue,yValue,bidValue)) {
						addMessage("Valid. Bid Request Placed.");
						x1Field.setText("");
						y1Field.setText("");
						bidField.setText("");
					} else {
						addMessage("Invalid Bid. Make sure there are enough funds, or that the minimum bid is met.");
					}

				} catch (NumberFormatException nfe) {
					addMessage("Invalid Bid. Please enter an integer amount in the bid field, or select a cell.");
				}
			}
		});
	}

	/** Create the queue panel, where the Action queue, inside a scrollPane.
	 * @param panel The panel that this action queue panel should be added to.
	 */
	private void queuePanel(JPanel panel) {
		JPanel queuePanel = new JPanel();
		queuePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
//		int x = (int)(dim.width*.6797);
//		int y = (int)(dim.height*.4272);
//		int width = (int)(dim.width*.3133);
//		int height = (int)(dim.height*.1467);
		queuePanel.setBounds(870, 393, 401, 135);
		panel.add(queuePanel);
		queuePanel.setLayout(null);

		btnRemoveAction = new JButton("Remove Action");
		btnRemoveAction.setBounds(130, 101, 136, 23);
		btnRemoveAction.setEnabled(false);
		queuePanel.add(btnRemoveAction);

		JScrollPane actionQueueScrollPane = new JScrollPane();
		actionQueueScrollPane.setBorder(null);
		actionQueueScrollPane.setBounds(1, 1, 398, 98);
		queuePanel.add(actionQueueScrollPane);
		actionQueueList = new JList(listModel);
		actionQueueList.setBorder(null);
		actionQueueScrollPane.setViewportView(actionQueueList);
		actionQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		actionQueueList.setSelectedIndex(0);
		actionQueueList.setVisibleRowCount(5);

		btnRemoveAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = actionQueueList.getSelectedIndex();
				if(index != -1) {
					Action toRemove = (Action) listModel.get(index);
					operator.removeRequest(toRemove);
					listModel.remove(index);
					if(listModel.isEmpty()) {
						btnRemoveAction.setEnabled(false);
					}
				}
			}
		});
	}
	
	/** Create the cell info panel, where information about a cell is placed.
	 * @param panel The panel that this info panel should be added to.
	 */
	private void cellInfoPanel(JPanel panel) {
		JPanel cellInfoPanel = new JPanel();
		cellInfoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
//		int x = (int)(dim.width*.6797);
//		int y = (int)(dim.height*.0489);
//		int width = (int)(dim.width*.3133);
//		int height = (int)(dim.height*.2424);
		cellInfoPanel.setBounds(870, 45, 401, 223);
		panel.add(cellInfoPanel);
		cellInfoPanel.setLayout(null);
		GridBagLayout gbl_cellInfoPanel = new GridBagLayout();
		gbl_cellInfoPanel.columnWidths = new int[]{5, 2, 244, 1, 0};
		gbl_cellInfoPanel.rowHeights = new int[]{65, 25, 25, 25, 25, 20, 0};
		gbl_cellInfoPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_cellInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		cellInfoPanel.setLayout(gbl_cellInfoPanel);
								
										JLabel cellInfoLabel = new JLabel("Cell Information");
										GridBagConstraints gbc_cellInfoLabel = new GridBagConstraints();
										gbc_cellInfoLabel.fill = GridBagConstraints.BOTH;
										gbc_cellInfoLabel.insets = new Insets(0, 0, 5, 5);
										gbc_cellInfoLabel.gridwidth = 2;
										gbc_cellInfoLabel.gridx = 1;
										gbc_cellInfoLabel.gridy = 0;
										cellInfoPanel.add(cellInfoLabel, gbc_cellInfoLabel);
										cellInfoLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
										cellInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
												
														JLabel lblOwner = new JLabel("Owner:");
														GridBagConstraints gbc_lblOwner = new GridBagConstraints();
														gbc_lblOwner.fill = GridBagConstraints.HORIZONTAL;
														gbc_lblOwner.insets = new Insets(0, 0, 5, 5);
														gbc_lblOwner.gridx = 1;
														gbc_lblOwner.gridy = 1;
														cellInfoPanel.add(lblOwner, gbc_lblOwner);
										
												ownerField = new JLabel();
												ownerField.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
												ownerField.setBackground(SystemColor.menu);
												GridBagConstraints gbc_ownerField = new GridBagConstraints();
												gbc_ownerField.fill = GridBagConstraints.BOTH;
												gbc_ownerField.insets = new Insets(0, 0, 5, 5);
												gbc_ownerField.gridx = 2;
												gbc_ownerField.gridy = 1;
												cellInfoPanel.add(ownerField, gbc_ownerField);
								
										JLabel lblLayer = new JLabel("Layer:");
										GridBagConstraints gbc_lblLayer = new GridBagConstraints();
										gbc_lblLayer.fill = GridBagConstraints.BOTH;
										gbc_lblLayer.insets = new Insets(0, 0, 5, 5);
										gbc_lblLayer.gridx = 1;
										gbc_lblLayer.gridy = 2;
										cellInfoPanel.add(lblLayer, gbc_lblLayer);
								
										layerTextArea = new JLabel();
										layerTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
										GridBagConstraints gbc_layerTextArea = new GridBagConstraints();
										gbc_layerTextArea.fill = GridBagConstraints.BOTH;
										gbc_layerTextArea.insets = new Insets(0, 0, 5, 5);
										gbc_layerTextArea.gridx = 2;
										gbc_layerTextArea.gridy = 2;
										cellInfoPanel.add(layerTextArea, gbc_layerTextArea);
										layerTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
										layerTextArea.setBackground(SystemColor.menu);
						
								JLabel lblRock = new JLabel("Rock Type:");
								GridBagConstraints gbc_lblRock = new GridBagConstraints();
								gbc_lblRock.anchor = GridBagConstraints.WEST;
								gbc_lblRock.fill = GridBagConstraints.VERTICAL;
								gbc_lblRock.insets = new Insets(0, 0, 5, 5);
								gbc_lblRock.gridwidth = 2;
								gbc_lblRock.gridx = 1;
								gbc_lblRock.gridy = 3;
								cellInfoPanel.add(lblRock, gbc_lblRock);
										
												rockTypeTextArea = new JLabel();
												rockTypeTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
												GridBagConstraints gbc_rockTypeTextArea = new GridBagConstraints();
												gbc_rockTypeTextArea.fill = GridBagConstraints.BOTH;
												gbc_rockTypeTextArea.insets = new Insets(0, 0, 5, 5);
												gbc_rockTypeTextArea.gridx = 2;
												gbc_rockTypeTextArea.gridy = 3;
												cellInfoPanel.add(rockTypeTextArea, gbc_rockTypeTextArea);
												rockTypeTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
												rockTypeTextArea.setBackground(SystemColor.menu);
								
										JLabel lblOil = new JLabel("Oil:");
										GridBagConstraints gbc_lblOil = new GridBagConstraints();
										gbc_lblOil.fill = GridBagConstraints.BOTH;
										gbc_lblOil.insets = new Insets(0, 0, 5, 5);
										gbc_lblOil.gridx = 1;
										gbc_lblOil.gridy = 4;
										cellInfoPanel.add(lblOil, gbc_lblOil);
						
								oilTextArea = new JLabel();
								oilTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
								GridBagConstraints gbc_oilTextArea = new GridBagConstraints();
								gbc_oilTextArea.fill = GridBagConstraints.BOTH;
								gbc_oilTextArea.insets = new Insets(0, 0, 5, 5);
								gbc_oilTextArea.gridx = 2;
								gbc_oilTextArea.gridy = 4;
								cellInfoPanel.add(oilTextArea, gbc_oilTextArea);
								oilTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
								oilTextArea.setBackground(SystemColor.menu);
				
						JLabel lblGas = new JLabel("Gas:");
						GridBagConstraints gbc_lblGas = new GridBagConstraints();
						gbc_lblGas.fill = GridBagConstraints.BOTH;
						gbc_lblGas.insets = new Insets(0, 0, 0, 5);
						gbc_lblGas.gridx = 1;
						gbc_lblGas.gridy = 5;
						cellInfoPanel.add(lblGas, gbc_lblGas);
		
				gasTextArea = new JLabel();
				gasTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				gasTextArea.setSize(239, 20);
				GridBagConstraints gbc_gasTextArea = new GridBagConstraints();
				gbc_gasTextArea.insets = new Insets(0, 0, 0, 5);
				gbc_gasTextArea.fill = GridBagConstraints.BOTH;
				gbc_gasTextArea.gridx = 2;
				gbc_gasTextArea.gridy = 5;
				cellInfoPanel.add(gasTextArea, gbc_gasTextArea);
				gasTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
				gasTextArea.setBackground(SystemColor.menu);
	}
	
	/** Create the message panel, where messages to the server
	 * are placed, inside a scrollbar.
	 * @param panel The panel that this info panel should be added to.
	 */
	private void messagePanel(JPanel panel) {

		messageScrollPane = new JScrollPane();
		messageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//		int x = (int)(dim.width*.6797);
//		int y = (int)(dim.height*.5783);
//		int width = (int)(dim.width*.3125);
//		int height = (int)(dim.height*.2391);
		messageScrollPane.setBounds(870, 538, 400, 200);
		panel.add(messageScrollPane);

		messagesModel = new DefaultListModel();
		messageList = new JList(messagesModel);
		messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messageList.setSelectionForeground(Color.BLACK);
		messageList.setSelectionBackground(SystemColor.inactiveCaptionBorder);
		messageList.setBackground(Color.LIGHT_GRAY);
		messageScrollPane.setViewportView(messageList);
		messageList.setMinimumSize(new Dimension(300,300));
		messageList.validate();
	}
	
	private void legendPanel(JPanel panel) {
		legendPanel = new JPanel();
		legendPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		legendPanel.setBounds(870, 748, 401, 150);
		panel.add(legendPanel);
		legendPanel.setLayout(new GridLayout(4, 1));
		SeismicIntel = new JLabel("Seismic Knowledge - Brown");
		SeismicIntel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		teamOwnedLand = new JLabel("Team Owned Land - Green");
		teamOwnedLand.setFont(new Font("Tahoma", Font.PLAIN, 15));
		compOwnedLand = new JLabel("Opponent Owned Land - Purple");
		compOwnedLand.setFont(new Font("Tahoma", Font.PLAIN, 15));
		teamOwnedOil = new JLabel("Team Oil Found - Black");
		teamOwnedOil.setFont(new Font("Tahoma", Font.PLAIN, 15));
		legendPanel.add(SeismicIntel);
		legendPanel.add(teamOwnedLand);
		legendPanel.add(compOwnedLand);
		legendPanel.add(teamOwnedOil);
		
		
		
	}
	
	public void setseismicCostField(String s){
		seismicCostField.setText(s);
	}

	public void sethoverLabel(int x, int y) {
		hoverLabel.setText(x + "," + y);
	}

	public void setStartX(int s) {
		x1Field.setText("" + s);
	}

	public void setStartY(int s) {
		y1Field.setText("" + s);
	}
	public int getStartX(){
		return Integer.parseInt(x1Field.getText());
	}
	public int getStartY(){
		return Integer.parseInt(y1Field.getText());
	}
	public int getEndX(){
		return Integer.parseInt(x2Field.getText());
	}
	public int getEndY(){
		return Integer.parseInt(y2Field.getText());
	}
	public void setendX(String s) {
		x2Field.setText("" + s);
	}

	public void setendY(String s) {
		y2Field.setText("" + s);
	}

	public void setTimer(int round, String time, int totalRnd) {
		timerField.setText(time);
		roundField.setText(round + "");
		totalRoundField.setText(totalRnd + "");
	}
	
	

	public void setBank(int amount) {
		DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
		String output = myFormatter.format(amount);
		hBankField.setText(output);
	}

	public void appendResult(Action in) {
		listModel.addElement(in);
		btnRemoveAction.setEnabled(true);
	}
	
	public boolean checkExists(Action check) {
		return listModel.indexOf(check) != -1;
	}

	public void setIP(String ip) {
		ipLabel.setText(ip);
	}

	public void setTeamName(String name) {
		teamName=name;
		hTeamField.setText(name);
	}

	public void setOperator(ClientOperator in) {
		operator = in;
	}

	/** Change the color of the countdown color based on the value passed.
	 * @param m Indicates which color the countdown should be set to.
	 */
	public void flashTimer(int m) {
		if (m == 1)
			timerField.setForeground(Color.red);
		else if (m == 0)
			timerField.setForeground(Color.orange);
		else
			timerField.setForeground(Color.black);
	}

	public void resetActionQueue() {
		listModel.clear();
	}

	/** Set the Cell Info area of the GUI to display the given information
	 * @param owner The owner of the cell.
	 * @param oil The oil content of the cell.
	 * @param gas The gas content of the cell.
	 * @param layers The layer elevations of the cell.
	 * @param rocktype The rocktype of the cell.
	 */
	public void setCellInfo(String owner, String oil, String gas, String layers, String rocktype) {
		ownerField.setText(owner);
		oilTextArea.setText(oil);
		gasTextArea.setText(gas);
		layerTextArea.setText(layers);
		rockTypeTextArea.setText(rocktype);
	}

	/** Create the Map grid of size p and set
	 * the state of the GUI to start as normal.
	 * @param p The grid size represented in a Point.
	 */
	public void constructMap(Point p) {
		map = new Map(this, 15, p);
		mapScrollPane.setViewportView(map);
		SLButton.doClick();
		actionsPanel.setVisible(true);

	}

	/** Add a message to the message box JList, and make sure
	 * the new message is displayed in the scrollbox.
	 * @param string The message to display.
	 */
	public void addMessage(String string) {
		messagesModel.addElement(string);
		messageScrollPane.validate();
		messageList.validate();
		int lastIndex = messagesModel.getSize() - 1;
		messageList.ensureIndexIsVisible(lastIndex);
	}

	/** Refresh the UI, including repainting the graphics and reload the
	 * Cell Info box using the last known selected coordinates.
	 */
	public void refreshGUI() {
		map.repaint();
		map.setCellInfo(map.lastX,map.lastY);
		
	}
	
	/** Disable the action buttons so that new actions
	 * are not able to be performed.
	 */
	public void disableActions() {
		bidButton.setEnabled(false);
		drillButton.setEnabled(false);
		requestButton.setEnabled(false);
	}
	
	/** Text field that can only contain numbers. Rejects non-integer input */
	class IntTextField extends JTextField {
		/** Do nothing but call the super constructor of JTextField.
		 */
		public IntTextField() {
			super("");
		}

		protected Document createDefaultModel() {
			return new IntTextDocument();
		}
		
		//isValid, true if contents is parseable as a number.
		public boolean isValid() {
			try {
				Integer.parseInt(getText());
				return true;
			} catch (NumberFormatException e) {
				return false;
			} catch (NullPointerException npe) {
				return false;
			}
		}

		/** Check if input contains a non-number, and do nothing */
		class IntTextDocument extends PlainDocument {
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (str == null)
					return;
				String oldString = getText(0, getLength());
				String newString = oldString.substring(0, offs) + str
						+ oldString.substring(offs);
				try {
					Integer.parseInt(newString + "0");
					super.insertString(offs, str, a);
				} catch (NumberFormatException e) {
				}
			}
		}

	}
}


