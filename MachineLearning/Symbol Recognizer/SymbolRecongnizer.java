import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

/**
 * 
 * @author Group 10
 *
 */
@SuppressWarnings("serial")
public class SymbolRecongnizer extends JFrame
{
	/* Global Variables */
	protected ButtonBar controls; // Control Bar
	protected SketchBoard board; // Drawing Board
	protected ResultsPanel resultsPanel; // Results Panel
	protected Sketch sketch; // The Active Sketch
	private final Font BIGFONT = new Font("Arial", 0, 18); // Title Font
	private final int CENTER = JLabel.CENTER; // Center Alignment
	private static final String WIKI = "http://en.wikipedia.org/wiki/";
	private int returnNum; // Number of symbols to return
	private Classifier dots; // Will be dots classifier
	//private Classifier velocity; // Will be velocity classifier
	private Classifier strokes; // Will be stroke classifier
	private ClassifierResults resultsDots;
	//private ClassifierResults resultsVel;
	private ClassifierResults resultsStr;
	private String[] webPages; // An array for the web pages
	//private String[] trainingData = {"Phillip0.trs", "Phillip1.trs", "Daniel0.trs", 
			//"Daniel1.trs", "Dan0.trs", "Michael0.trs", "Michael1.trs"};
	private String[] trainingData = {"Phillip0.trs", "Daniel0.trs",
			"Dan0.trs", "Michael0.trs"};
	
	/**
	 * main:
	 * Starts SymbolRecongnizer
	 * @param args - command line arguments if needed
	 */
	public static void main(String[] args){	new SymbolRecongnizer(); }
	
	/**
	 * Constructor:
	 * Creates the GUI for the SymbolRecongnizer
	 */
	public SymbolRecongnizer()
	{
		// Name the GUI
		super("Symbol Recongnizer");
		
		// Style the GUI
		try{ UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
		catch (Exception e) 
		{
			System.out.println("Exception while trying to set the look&feel");
			return;
		}
		
		// Default number to list is 5
		returnNum = 5;
		
		// Intialize web page array
		webPages = new String[returnNum];
		
		// Create the Main Panel
		JPanel mainPanel = new JPanel();
		
		// Create the Sub-Panels
		controls = new ButtonBar(this);
		board = new SketchBoard(this);
		resultsPanel = new ResultsPanel(this);
		
		// Make a new Sketch
		sketch = new Sketch();
		
		// Create a panel to hold the drawing board
		JLabel titleLabel = new JLabel("Sketch Here");
		titleLabel.setFont(BIGFONT);
		titleLabel.setHorizontalAlignment(CENTER);		
		JPanel drawPanel = new JPanel();
		drawPanel.setLayout(new BorderLayout());
		drawPanel.add(BorderLayout.NORTH,titleLabel);
		drawPanel.add(BorderLayout.SOUTH,board);
		
		// Add the components
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(BorderLayout.SOUTH, controls);
		mainPanel.add(BorderLayout.WEST, drawPanel);
		mainPanel.add(BorderLayout.EAST, resultsPanel);
		
		// Prepare for Display
		getContentPane().add(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		// Display end Result
		pack();
		setVisible(true);
		
		// Initialize the classifiers
		dots = new DotsClassifier();
		// velocity = new VelocityClassifier();
		strokes = new StrokeClassifier();
		
		// Add traing data to each classifier
		for(int i = 0; i < trainingData.length; i++) // Dots
			dots.addTrainingSet(trainingData[i]);
		//for(int i = 0; i < trainingData.length; i++) // Velocity
			//dots.addTrainingSet(trainingData[i]);
		for(int i = 0; i < trainingData.length; i++) // Strokes
			strokes.addTrainingSet(trainingData[i]);
	}
	
	/**
	 * Controls the controls
	 * @author Group 10
	 *
	 */
	@SuppressWarnings("serial")
	private class ButtonBar extends JPanel implements ActionListener
	{		
		/* Class Variables */
		private SymbolRecongnizer mainFrame;
		JButton erase;
		JButton classify;
		JButton again;		
		JButton exit;
		
		public ButtonBar(SymbolRecongnizer frame)
		{
			// Call to use JPanel Constructor for defaults
			super();
			
			// Specify the parent frame
			mainFrame = frame;
			
			// Create panel to put buttons on
			JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			// Create Buttons
			erase = new JButton("Erase Current Sketch");
			classify = new JButton("What is this symbol?");
			again = new JButton("Find another symbol?");
			exit = new JButton("Exit Symbol Recongnizer");
			
			// Add Buttons
			buttons.add(erase);
			buttons.add(classify);
			buttons.add(again);	
			buttons.add(exit);	
			
			// Add panel to parent
			setLayout(new BorderLayout());
			add(buttons,BorderLayout.NORTH);
			
			// Add the listeners to the buttons
			erase.addActionListener(this);
			classify.addActionListener(this);
			again.addActionListener(this);
			exit.addActionListener(this);	
			
			// Disable some functions at start up
			again.setEnabled(false);			
		}
		
		/**
		 * Performs the actions of the buttons
		 * @param ae - ActionEvent event
		 */
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(erase))
				eraseClick(mainFrame); // Erase was clicked
			else if (ae.getSource().equals(classify)) 
				classifyClick(mainFrame); // Classify was clicked
			else if (ae.getSource().equals(again)) 
				againClick(mainFrame); // Again was clicked
			else if (ae.getSource().equals(exit)) 
				exitClick(); // Exit was clicked
		}
		
		public void eraseClick(SymbolRecongnizer mainFrame)
		{
			// Make a new sketch
			mainFrame.sketch = new Sketch();
			
			// Repaint the sketch board
			mainFrame.board.repaint();
		}
		
		public void classifyClick(SymbolRecongnizer mainFrame)
		{
			// Create a new classifier results
			resultsDots = dots.classify(sketch, returnNum);
			// resultsVel = velocity.classify(sketch, returnNum);
			resultsStr = strokes.classify(sketch, returnNum);
			
			/* Begin Selection process */
			
			// For Testing ONLY //
			/*
			Labels labelInfo = new Labels();
			char symbol;
			String name;
			String listItem;
			resultsDots.orderResults();
			for(int i = 0; i < returnNum; i++)
			{
				// Get symbol
				symbol = resultsDots.labels[i];
				//System.out.println(symbol);
				if(symbol == '\u0000')
				{
					System.out.println("WHAT!!!!! Why is this NULL");
					//System.out.println(resultsDots.toString());
					return;
				}
				
				// Get name 
				name = labelInfo.names.get(labelInfo.codes.indexOf(symbol))[0];
				
				// Get page
				webPages[i] = WIKI + labelInfo.pages.get(labelInfo.codes.indexOf(symbol))[0];
				
				// Combine
				listItem = symbol + " - " + name + ": " + webPages[i];
				
				// Add to list box
				mainFrame.resultsPanel.listModel.addElement(listItem);
			}
			*/
			Labels labelInfo = new Labels();
			char symbol;
			String name;
			String listItem;
			resultsStr.orderResults();
			for(int i = 0; i < returnNum; i++)
			{
				// Get symbol
				symbol = resultsStr.labels[i];
				//System.out.println(symbol);
				if(symbol == '\u0000')
				{
					System.out.println("WHAT!!!!! Why is this NULL");
					System.out.println(resultsStr.toString());
					return;
				}
				
				// Get name 
				name = labelInfo.names.get(labelInfo.codes.indexOf(symbol))[0];
				
				// Get page
				webPages[i] = WIKI + labelInfo.pages.get(labelInfo.codes.indexOf(symbol))[0];
				
				// Combine
				listItem = symbol + " - " + name + ": " + webPages[i];
				
				// Add to list box
				mainFrame.resultsPanel.listModel.addElement(listItem);
			}
			
			// End For Testing //
			
			/* End Selection Proces */
			
			// Update Button Choices
			mainFrame.resultsPanel.showResults();
			
			displayFuncts();
			
		}
		
		public void againClick(SymbolRecongnizer mainFrame)
		{
			// Make a new sketch
			mainFrame.sketch = new Sketch();
			
			// Clear the results
			mainFrame.resultsPanel.clearResults();
			
			// New Functions
			restartFuncts();
			
			// Repaint the sketch board
			mainFrame.board.repaint();
		}
		
		public void exitClick(){ System.exit(0); }
		
		public void displayFuncts()
		{
			again.setEnabled(true);
			classify.setEnabled(false);
			erase.setEnabled(false);
		}
		
		public void restartFuncts()
		{
			again.setEnabled(false);
			classify.setEnabled(true);
			erase.setEnabled(true);
		}
	}
	
	/**
	 * 
	 * Renders a canvas for the user to draw on
	 * @author Group 10
	 *
	 */
	@SuppressWarnings("serial")
	private class SketchBoard extends JPanel
	{
		/* Class Constants */
		public static final int WIDTH=256;
		public static final int HEIGHT=256;
		
		/* Class Variables */
		private SymbolRecongnizer mainFrame;
		
		/**
		 * Constuctor:
		 * Creates all the parameters need to correctly draw the sketch board.
		 * @param frame - the parent frame
		 */
		public SketchBoard(SymbolRecongnizer frame)
		{
			// Call to use JPanel Constructor for defaults
			super();
			
			// Specify the parent frame
			mainFrame = frame;
			
			// Set Size of the drawing board
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			setSize(new Dimension(WIDTH, HEIGHT));
			
			// Listen to mouse press/release events
			MouseListener mLis = new MouseAdapter() 
			{
				public void mousePressed(MouseEvent me) 
				{
					mainFrame.sketch.addMouseEvent(Sketch.MOUSE_PRESS);
					Point p = new Point(me.getPoint().x, me.getPoint().y);
					mainFrame.sketch.addPoint(p);
				}
				
				public void mouseReleased(MouseEvent me) 
				{
					mainFrame.sketch.addMouseEvent(Sketch.MOUSE_RELEASE);
					repaint();
				}
			};
			
			//Listen to mouse drag (move while pressed) events and add points to the sketch
			MouseMotionListener mMLis = new MouseMotionAdapter() 
			{
				public void mouseDragged(MouseEvent me) 
				{
					Point p = new Point(me.getPoint().x, me.getPoint().y);
					if ((p.x >= 0) && (p.y >= 0)&&(p.x < SketchBoard.WIDTH) && (p.y < SketchBoard.HEIGHT))
					{
						mainFrame.sketch.addPoint(p);
						repaint();
					}
				}				
			};
			
			// Adds the mouse events to this panel
			addMouseListener(mLis);
			addMouseMotionListener(mMLis);
		}
		
		/**
		 * Redraws the sketch board
		 * @param g - rendering info
		 */
		public void paintComponent(Graphics g) 
		{
			// Make the canvas
			g.setColor(Color.WHITE);			
			g.fillRect(0, 0, getSize().width, getSize().height);
			
			g.setColor(Color.BLACK);
			if((mainFrame.sketch != null) && (mainFrame.sketch.events.size() > 1)) 
			{
				for (int i = 0; i < mainFrame.sketch.events.size() - 1; i++) 
				{
					Object o1 =  mainFrame.sketch.events.elementAt(i);
					Object o2 =  mainFrame.sketch.events.elementAt(i + 1);
					
					//Draw lines
					if((o1 instanceof Point)&&(o2 instanceof Point)) 
					{
						Point p1 = (Point) o1;
						Point p2 = (Point) o2;
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					} else if (i < mainFrame.sketch.events.size() - 2)
					{
						// Draw single points
						Object o3 = mainFrame.sketch.events.elementAt(i+2);
						if(!(o1 instanceof Point) && (o2 instanceof Point) && !(o3 instanceof Point))
						{
							Point p = (Point) o2;
							g.drawLine(p.x, p.y, p.x, p.y);
						}
					}
				}	
			}
		}
	}
	
	/**
	 * 
	 * @author Group 10
	 *
	 */
	@SuppressWarnings("serial")
	private class ResultsPanel extends JPanel implements ActionListener
	{
		/* Class Constants */
		public static final int WIDTH = 280;
		public static final int HEIGHT = 170;		
		
		/* Class Variables */
		private SymbolRecongnizer mainFrame;
		public JList resultsList;
		private JLabel titleLabel;
		private JLabel numberLabel;
		private JButton go;
		private JButton inc;
		private JButton dec;
		private JPanel top;
		private JPanel bottom;
		private JPanel holder;
		private JScrollPane listScroller;
		DefaultListModel listModel;
		
		public ResultsPanel(SymbolRecongnizer frame)
		{
			// Call to use JPanel Constructor for defaults
			super();
			
			// Specify the parent frame
			mainFrame = frame;
			
			// Make results list			
			listModel = new DefaultListModel();
			resultsList = new JList(listModel);
			
			// Add results list to a scroll pane for aestetics
			listScroller = new JScrollPane(resultsList);
			listScroller.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			
			// Make buttons
			go = new JButton("Go to Wiki Page");
			inc = new JButton("Increase Number Listed");
			dec = new JButton("Decrease Number Listed");
			
			// Make title label for aestetics
			titleLabel = new JLabel("Results");			
			titleLabel.setFont(BIGFONT);
			titleLabel.setHorizontalAlignment(CENTER);
			
			// Make number label
			numberLabel = new JLabel("" + returnNum);
			
			// Create panels for proper display
			top = new JPanel();
			top.setLayout(new FlowLayout(FlowLayout.CENTER));
			top.add(inc);
			top.add(numberLabel);
			top.add(dec);
			bottom = new JPanel();
			bottom.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottom.add(go);	
			holder = new JPanel();
			holder.setLayout(new BorderLayout());
			holder.add(top, BorderLayout.NORTH);
			holder.add(bottom, BorderLayout.SOUTH);
			
			// Add to panel
			setLayout(new BorderLayout());
			add(titleLabel,BorderLayout.NORTH);
			add(listScroller,BorderLayout.CENTER);
			add(holder,BorderLayout.SOUTH);
			
			// Add the listeners to the buttons
			go.addActionListener(this);		
			inc.addActionListener(this);
			dec.addActionListener(this);		
			
			// Disable some functions at start up
			go.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(go))
				goClick(mainFrame); // go was clicked
			else if (ae.getSource().equals(inc)) 
				incClick(mainFrame); // increase was clicked
			else if (ae.getSource().equals(dec)) 
				decClick(mainFrame); // decrease was clicked
		}
		
		public void showResults()
		{
			// Disable inc and dec
			inc.setEnabled(false);
			dec.setEnabled(false);
			
			// Enable Go
			go.setEnabled(true);			
		}
		
		public void clearResults()
		{
			// Disable go button
			go.setEnabled(false);
			
			// Disable inc and dec
			inc.setEnabled(true);
			dec.setEnabled(true);
			
			// Clear results list		
			listModel.removeAllElements();
		}
		
		private void goClick(SymbolRecongnizer mainFrame)
		{
			String os = System.getProperty( "os.name" );
			System.out.println( os );
			if(os.equals("Windows XP"))
			{
				// Opens a browser for windows
				String[] cmd = new String[5];			 
				cmd[0] = "cmd.exe";
				cmd[1] = "/C";
				cmd[2] = "rundll32";
				cmd[3] = "url.dll,FileProtocolHandler";
				if(resultsList.getSelectedIndex() == -1)
				{
					return;
				}
				cmd[4] = webPages[resultsList.getSelectedIndex()];
				try
				{
					Runtime.getRuntime().exec( cmd );
				}
				catch(Exception e) { }
			}
			else // Un-Tested
			{
				String[] cmd = new String[2];			 
				cmd[0] = "firefox";
				if(resultsList.getSelectedIndex() == -1)
				{
					return;
				}
				cmd[1] = webPages[resultsList.getSelectedIndex()];
				try
				{
					Runtime.getRuntime().exec( cmd );
				}
				catch(Exception e) { }
			}
		}
		
		private void incClick(SymbolRecongnizer mainFrame)
		{
			if(mainFrame.returnNum < 142)
			{
				mainFrame.returnNum++;
				numberLabel.setText("" + returnNum);
				
				// Intialize web page array
				webPages = new String[returnNum];
			}
		}
		
		private void decClick(SymbolRecongnizer mainFrame)
		{
			if(mainFrame.returnNum > 1)
			{
				mainFrame.returnNum--;
				numberLabel.setText("" + returnNum);
				
				// Intialize web page array
				webPages = new String[returnNum];
			}
		}
	}
}
