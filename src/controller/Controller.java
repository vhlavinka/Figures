package controller;

import figure.Figure;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import views.AddRectDialog;
import views.AddCircDialog;
import views.Canvas;
import views.FigureFrame;

public class Controller implements Serializable {

  private final FigureFrame frame = new FigureFrame();
  private final Canvas canvas = frame.getCanvas();

  // figures which appear in canvas and in list
  private final List<Figure> figureList = new ArrayList<>();

  // model for Figure selection list
  private final DefaultListModel listModel = new DefaultListModel();

  // dialogs
  private AddRectDialog addRectDialog = new AddRectDialog(frame, true);
  private AddCircDialog addCircDialog = new AddCircDialog(frame, true);
  
  // current figure on top and selected figure
  private Figure currentFigure = null;
  private Figure selectedFigure = null;
  private Figure highlightedFigure = null;
  
  // keep track of figure position for movement
  private int lastX, lastY;
  
  private static JFileChooser getFileChooser() {
    JFileChooser chooser = new JFileChooser();
      
    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      
    chooser.setAcceptAllFileFilterUsed(false);
      
    return chooser;
  }
  
  public Controller() {
    frame.setTitle("Figures");
    frame.setLocationRelativeTo(null);
    frame.setSize(800, 500);

    canvas.setFigures(figureList);

///////////////////////////
// ALLOW MOUSE MOVEMENT
//////////////////////////
    canvas.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (currentFigure == null) { // no figure available
                return;
            }
            int x = e.getX(), y = e.getY();

            if (currentFigure.getPositionShape().contains(x, y)) {
                selectedFigure = currentFigure;
            }
            lastX = x;
            lastY = y;
        }
 
        @Override
        public void mouseReleased(MouseEvent e) {
            selectedFigure = null;
        }
    }); // ends addMouseListener
        
    canvas.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if (selectedFigure == null) {
          return;
        }
        int x = e.getX(), y = e.getY();
 
        int incX = x - lastX;
        int incY = y - lastY;
 
        lastX = x;
        lastY = y;
 
        selectedFigure.incLocation(incX, incY);
        canvas.repaint();
      }
    }); // ends addMouseMotionListener

    frame.getFigureList().setModel(listModel);

    // keep figureList from taking too much vertical space
    frame.getFigureList().getParent().setPreferredSize(new Dimension(0, 0));

    // permit only single element selection
    frame.getFigureList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // set the spinner model
    frame.getScaleSpinner().setModel(new SpinnerNumberModel(1.0, 0.1, 5.0, 0.05));  

////////////////////////////////////
// MENU COMPONENTS - FILE, FIGURES
///////////////////////////////////
    frame.getLoadSamples().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        List<Figure> samples = Helpers.getSampleFigureList();
        figureList.clear();

        for (Figure sample : samples) {
          figureList.add(sample);          
        }
        
        listModel.removeAllElements();
        for (Figure figure : figureList) {
          listModel.addElement(figure);
        }
        
        currentFigure = samples.get(0);
        canvas.repaint();
      }
    }); // ends getLoadSamples

    // Invoke the addRect dialog
    frame.getAddRectDialog().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        addRectDialog.setLocationRelativeTo(null);
        addRectDialog.setTitle("Add a RectangleFigure");

        addRectDialog.getHeightField().setText("" + 100);
        addRectDialog.getWidthField().setText("" + 200);
        addRectDialog.getStrokeWidthField().setText("" + 1);
        addRectDialog.getTitleField().setText("");

        addRectDialog.getLineColorField().setEditable(false);
        addRectDialog.getFillColorField().setEditable(false);

        addRectDialog.getLineColorField().setBackground(Color.black);
        addRectDialog.getFillColorField().setBackground(Color.white);

        addRectDialog.setVisible(true);
        
        currentFigure = figureList.get(0);
          System.out.println(figureList);
      }
    }); // ends getAddRectDialog
    
    // Invoke the addCircle dialog
    frame.getAddCircDialog().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            addCircDialog.setLocationRelativeTo(null);
            addCircDialog.setTitle("Add a Circle Figure");
            
            addCircDialog.getDiameter().setText("" + 100);
            addCircDialog.getStrokeWidth().setText("" + 1);
            addCircDialog.getTitleField().setText("");
            
            addCircDialog.getLineColor().setEditable(false);
            addCircDialog.getFillColor().setEditable(false);
            
            addCircDialog.getLineColor().setBackground(Color.black);
            addCircDialog.getFillColor().setBackground(Color.white);
            
            addCircDialog.setVisible(true);
            
           currentFigure = figureList.get(0);
        } 
    }); // ends getAddCircDialog

    // addRectDialog needs the remaining arguments to do its work in events
    Helpers.addEventHandlers(addRectDialog, figureList, listModel, frame, canvas);
    Helpers.addEventHandlers(addCircDialog, figureList, listModel, frame, canvas);
    
    frame.getSaveFile().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
            JFileChooser chooser = Controller.getFileChooser();
            int status = chooser.showSaveDialog(frame);
            
            if(status != JFileChooser.APPROVE_OPTION){
                return;
            }
            
            File file = chooser.getSelectedFile();
            File thisDirectory = new File(System.getProperty("user.dir"));
            
            // Check if it has file extension, lastDot should not = -1 if so
            int lastDot = file.getName().lastIndexOf(".");
            // Obtain extension and store in String "extension"
            String extension = file.getName().substring(lastDot + 1); 
            // If no extension, append .fig
            if( !(file.getName().contains(extension)) || lastDot == -1 ){   
                StringBuilder fileName = new StringBuilder();                              
                fileName.append(file.getName());                  
                fileName.append(".fig");
                file = new File(System.getProperty("user.dir"), fileName.toString());                
            }
            
            Path path = file.toPath();
            
            try {
                FileOutputStream ostr = new FileOutputStream(file);
                ObjectOutputStream oostr = new ObjectOutputStream(ostr);
                
                oostr.writeObject(figureList);
                oostr.close();
            } 
            catch(IOException ex) {
                ex.printStackTrace(System.err);
                JOptionPane.showMessageDialog(frame, "Cannot save figures");
            }
        }    
    }); // ends getSaveFile
    
    frame.getLoadFile().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = Controller.getFileChooser();
            int status = chooser.showOpenDialog(frame);

            if (status != JFileChooser.APPROVE_OPTION) {
                return;
            }
            
            File file = chooser.getSelectedFile();
            Path path = file.toPath();

            try {
                System.out.println("\n--------- Load -------------");

                System.out.println("full path: " + path);

                Path working = Paths.get(System.getProperty("user.dir"));

                System.out.println("working path: " + working);

                Path relative = working.relativize(path);

                System.out.println("relative path: " + relative);

                FileInputStream istr = new FileInputStream(file);
                ObjectInputStream oistr = new ObjectInputStream(istr);
                
                Object theObj = oistr.readObject();
                oistr.close();
                 
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                JOptionPane.showMessageDialog(frame, "Cannot open file " + file);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace(System.err);
                JOptionPane.showMessageDialog(frame, "Cannot load file" + file);
            }  
        } 
    }); // ends getLoadFile

/////////////////////////////////
// BUTTONS - MOVE, REMOVE, SCALE
////////////////////////////////
    frame.getMoveTopButton().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                int selectedPos = frame.getFigureList().getSelectedIndex();
                highlightedFigure = figureList.get(selectedPos);

                // add selected figure at position 0 to both figure list and list model                      
                listModel.add(0, highlightedFigure);
                figureList.add(0, highlightedFigure);

                // next, delete selected figure at last position on both lists
                int oldPos = figureList.lastIndexOf(highlightedFigure);
                listModel.remove(oldPos);
                figureList.remove(oldPos);

                // update current figure on top
                currentFigure = figureList.get(0);

                // reset spinner value      
                currentFigure = figureList.get(0);
                double newScale = currentFigure.getScale();
                frame.getScaleSpinner().setValue(newScale);

                canvas.repaint();
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println("No figure selected");
            } catch (Exception ex){
                System.err.println("An unknown error has occured");
            }
          }
      }); // ends getMoveToTopButton
    
    frame.getRemoveButton().addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            try {
                int index = frame.getFigureList().getSelectedIndex();

                listModel.removeElement(frame.getFigureList().getSelectedValue()); // removes from list model
                figureList.remove(index); // remove from figure list

                canvas.repaint();
                try {
                    currentFigure = figureList.get(0);
                    double newScale = currentFigure.getScale();
                    frame.getScaleSpinner().setValue(newScale);
                } catch (IndexOutOfBoundsException ex) {
                    frame.getScaleSpinner().setValue(1.0);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println("No figure selected");
            } catch (Exception ex){
                System.err.println("An unknown error has occured");
            }
        }
      }); // ends getRemoveButton
    
     frame.getScaleSpinner().addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            double d = (Double) frame.getScaleSpinner().getValue();
            currentFigure.setScale(d);
            canvas.repaint();
        }
     }); // ends getScaleSpinner
        
  } // ends Controller Constructor

  public static void main(String[] args) {
    Controller app = new Controller();
    app.frame.setVisible(true);
  }
}
