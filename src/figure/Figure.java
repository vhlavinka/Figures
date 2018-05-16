package figure;
 
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.Serializable;
 
public abstract class Figure implements Serializable {  
  // all figures have a "shape" and an "outline stroke"

  protected float strokeWidth = 1.0f;
  protected Shape shape;
  
  protected Color lineColor = Color.BLACK;
  protected Color fillColor = null;
 
  protected double xLoc = 0, yLoc = 0;
  
  protected double scale = 1.0;
  
  protected String title = "-- untitled --";

  // the stroke is created from strokeWidth + BasicStroke
  // but BasicStroke is not serializable, so the actual
  // stroke used is created "on the fly"
  protected transient Stroke stroke = null;
  
  // the actual drawing is done by the subclass
  public abstract void draw(Graphics2D g2);
  public abstract Shape getPositionShape();
   
  public void setLocation(double xLoc, double yLoc) {
    this.xLoc = xLoc;
    this.yLoc = yLoc;
  }
 
  public void incLocation(double xInc, double yInc) {
    this.xLoc += xInc;
    this.yLoc += yInc;
  }
 
  public Point2D.Double getLocation() {
    return new Point2D.Double(xLoc, yLoc);
  }
  
  public double getScale() {
    return scale;
  }
  
  public void setScale(double scale) {
    this.scale = scale;
  }
 
  public final void setStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    stroke = null;
  }
 
  public void setLineColor(Color color) {
    this.lineColor = color;
  }
 
  public void setFillColor(Color color) {
    this.fillColor = color;
  }

  public final void setTitle(String title) {
    this.title = title;
  }
  
  @Override
  public String toString() {
    return title;
  }
}
