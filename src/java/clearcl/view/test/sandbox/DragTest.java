package clearcl.view.test.sandbox;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class DragTest extends Application
{
  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage)
  {
    Scene scene = new Scene(new Test(),512,512);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}

class Test extends StackPane
{
  private Timer timer = new Timer();
  private Rectangle rect;
  private volatile double pressedX, pressedY;
  private volatile double mFactor = 1;
  private volatile boolean mDragStarted;

  public Test()
  {
    //setMinSize(1000, 1000);
    setStyle("-fx-border-color: blue;");

    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        Platform.runLater(new Runnable()
        {
          @Override
          public void run()
          {
            if (rect != null)
              getChildren().remove(rect);

            rect = new Rectangle(10, 10, 200, 200);
            rect.setFill(Color.RED);
            getChildren().add(rect);
          }
        });
      }
    }, 0, 100);

    setOnMousePressed(new EventHandler<MouseEvent>()
    {

      public void handle(MouseEvent event)
      {
        System.out.println("PRESSED");
        mDragStarted = true;
        pressedX = event.getX();
        pressedY = event.getY();
      }
    });

    setOnMouseDragged((e) -> {
      System.out.println("DRAGGED");
      if (mDragStarted)
      {
        double lDeltaX = getTranslateX() + getScaleX()
                         * (e.getX() - pressedX);
        double lDeltaY = getTranslateY() + getScaleY()
                         * (e.getY() - pressedY);
        setTranslateX(lDeltaX);
        setTranslateY(lDeltaY);

        System.out.println("lDeltaX=" + lDeltaX);
        System.out.println("lDeltaY=" + lDeltaY);
        System.out.println("getTranslateX()=" + getTranslateX());
        System.out.println("getTranslateY()=" + getTranslateY());
        e.consume();
      }
    });

    setOnMouseDragExited((e) -> {
      System.out.println("EXITED");
    });

    setOnScroll((e) -> {
      double lDelta = e.getDeltaY() * 0.001;
      // System.out.println("lDelta=" + lDelta);
      mFactor = Math.exp(lDelta);
      System.out.println("mFactor=" + mFactor);
      if (mFactor < 0.5)
        mFactor = 0.5;
      else if (mFactor > 2)
        mFactor = 2;

      setScaleX(mFactor * getScaleX());
      setScaleY(mFactor * getScaleY());/**/

      System.out.println("getScaleX()=" + getScaleX());
      // e.consume();
    });
  }
}
