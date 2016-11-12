package clearcl.view;

import java.util.concurrent.CountDownLatch;

import com.sun.javafx.application.PlatformImpl;

import clearcl.ClearCLImage;
import clearcl.enums.ImageType;
import clearcl.view.jfx.PanZoomScene;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * utility to display ClearCL images and monitoring the change of their content.
 *
 * @author royer
 */
public class ClearCLImageView
{

  private Stage mStage = null;
  private ClearCLImagePanel mImagePanel;
  private GridPane mControlPane;

  /**
   * Opens a window showing the image content. View can be panned and zoomed.
   * 
   * @param pImage
   * @return
   */
  public static ClearCLImageView view(ClearCLImage pImage)
  {
    ClearCLImageView lViewImage = new ClearCLImageView(pImage,
                                                       "Image");
    return lViewImage;
  }

  /**
   * Creates a view for a given image, window title.
   * 
   * @param pClearCLImage
   *          image
   * @param pWindowTitle
   *          window title
   */
  public ClearCLImageView(ClearCLImage pClearCLImage,
                          String pWindowTitle)
  {
    this(pClearCLImage,
         pWindowTitle,
         (int) (Math.max(pClearCLImage.getWidth(), 512)),
         (int) (Math.max(pClearCLImage.getHeight(), 512)));
  }

  /**
   * Creates a view for a given image, window title, and window dimensions.
   * 
   * @param pClearCLImage
   *          image
   * @param pWindowTitle
   *          window title
   * @param pWindowWidth
   *          window width
   * @param pWindowHeight
   *          window height
   */
  public ClearCLImageView(ClearCLImage pClearCLImage,
                          String pWindowTitle,
                          int pWindowWidth,
                          int pWindowHeight)
  {
    super();

    PlatformImpl.startup(() -> {
    });
    final CountDownLatch lCountDownLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      mStage = new Stage();
      mStage.setTitle(pWindowTitle);

      StackPane lStackPane = new StackPane();
      lStackPane.backgroundProperty()
                .set(new Background(new BackgroundFill(Color.TRANSPARENT,
                                                       CornerRadii.EMPTY,
                                                       Insets.EMPTY)));/**/

      mImagePanel = new ClearCLImagePanel(pClearCLImage);

      mControlPane = new GridPane();
      RowConstraints row1 = new RowConstraints();
      row1.setMinHeight(10);
      RowConstraints row2 = new RowConstraints();
      row2.setFillHeight(false);
      RowConstraints row3 = new RowConstraints();
      row3.setFillHeight(true);
      RowConstraints row4 = new RowConstraints();
      row4.setFillHeight(false);
      RowConstraints row5 = new RowConstraints();
      row5.setMinHeight(10);
      mControlPane.getRowConstraints().addAll(row1,
                                              row2,
                                              row3,
                                              row4,
                                              row5);/**/

      ColumnConstraints col1 = new ColumnConstraints();
      col1.setMinWidth(5);

      ColumnConstraints col2 = new ColumnConstraints();
      col2.setFillWidth(false);
      col2.setHgrow(Priority.NEVER);

      ColumnConstraints col3 = new ColumnConstraints();
      col3.setFillWidth(false);
      col3.setHgrow(Priority.NEVER);

      ColumnConstraints col4 = new ColumnConstraints();
      col4.setFillWidth(false);
      col4.setHgrow(Priority.NEVER);

      ColumnConstraints col5 = new ColumnConstraints();
      col5.setFillWidth(true);
      col5.setHgrow(Priority.ALWAYS);

      ColumnConstraints col6 = new ColumnConstraints();
      col6.setFillWidth(false);
      col6.setHgrow(Priority.NEVER);

      ColumnConstraints col7 = new ColumnConstraints();
      col7.setMinWidth(5);

      mControlPane.getColumnConstraints().addAll(col1,
                                                 col2,
                                                 col3,
                                                 col4,
                                                 col5,
                                                 col6,
                                                 col7);/**/

      mControlPane.backgroundProperty()
                  .set(new Background(new BackgroundFill(Color.TRANSPARENT,
                                                         CornerRadii.EMPTY,
                                                         Insets.EMPTY)));/**/

      ComboBox<RenderMode> lRenderModeComboBox = new ComboBox<>();
      lRenderModeComboBox.getItems().setAll(RenderMode.values());
      lRenderModeComboBox.valueProperty()
                         .bindBidirectional(mImagePanel.getRenderModeProperty());
      GridPane.setColumnSpan(lRenderModeComboBox, 2);
      mControlPane.add(lRenderModeComboBox, 4, 1);

      ToggleButton lAutomaticMinMaxToggleButton = new ToggleButton("Auto");

      // lAutomaticMinMaxCheckBox.setStyle("-fx-text-fill: white;");
      lAutomaticMinMaxToggleButton.selectedProperty()
                                  .bindBidirectional(mImagePanel.getAutoProperty());
      GridPane.setColumnSpan(lAutomaticMinMaxToggleButton, 3);
      GridPane.setHalignment(lAutomaticMinMaxToggleButton,
                             HPos.CENTER);
      mControlPane.add(lAutomaticMinMaxToggleButton, 1, 1);

      final Slider lMin = new Slider(0, 1, 0);
      lMin.setMaxHeight(Double.MAX_VALUE);
      lMin.setOrientation(Orientation.VERTICAL);
      lAutomaticMinMaxToggleButton.selectedProperty()
                                  .bindBidirectional(lMin.disableProperty());
      lMin.valueProperty()
          .bindBidirectional(mImagePanel.getMinProperty());
      GridPane.setVgrow(lMin, Priority.ALWAYS);
      mControlPane.add(lMin, 1, 2);

      final Slider lMax = new Slider(0, 1, 1);
      lMax.setMaxHeight(Double.MAX_VALUE);
      lMax.setOrientation(Orientation.VERTICAL);
      lAutomaticMinMaxToggleButton.selectedProperty()
                                  .bindBidirectional(lMax.disableProperty());
      lMax.valueProperty()
          .bindBidirectional(mImagePanel.getMaxProperty());
      GridPane.setVgrow(lMax, Priority.ALWAYS);
      mControlPane.add(lMax, 2, 2);

      final Slider lGamma = new Slider(-1, 1, 0);
      lGamma.setMaxHeight(Double.MAX_VALUE);
      lGamma.setOrientation(Orientation.VERTICAL);
      lAutomaticMinMaxToggleButton.selectedProperty()
                                  .bindBidirectional(lGamma.disableProperty());
      lGamma.valueProperty()
            .addListener((v) -> {
              mImagePanel.getGammaProperty()
                         .set((float) (Math.pow(2,
                                                2 * (lGamma.getValue()))));
            });
      GridPane.setVgrow(lGamma, Priority.ALWAYS);
      mControlPane.add(lGamma, 3, 2);

      if (pClearCLImage.getImageType() == ImageType.IMAGE3D)
      {
        final Slider lZ = new Slider(0,
                                     pClearCLImage.getDepth() - 1,
                                     0);
        lZ.setMaxHeight(Double.MAX_VALUE);
        lZ.setOrientation(Orientation.VERTICAL);

        lZ.valueProperty()
          .bindBidirectional(mImagePanel.getZProperty());
        GridPane.setVgrow(lZ, Priority.ALWAYS);
        // GridPane.setColumnSpan(lZ, 4);
        // GridPane.setFillWidth(lZ, true);
        mControlPane.add(lZ, 5, 2);
      }

      lAutomaticMinMaxToggleButton.selectedProperty().set(true);

      lStackPane.getChildren().addAll(mImagePanel, mControlPane);

      PanZoomScene lPanZoomScene = new PanZoomScene(lStackPane,
                                                    mImagePanel,
                                                    pWindowWidth,
                                                    pWindowHeight,
                                                    Color.BLACK);

      /*lPanZoomScene.setScale(Math.pow(Math.min(((double)pWindowWidth) / pClearCLImage.getWidth(),
                                      ((double)pWindowHeight) / pClearCLImage.getHeight()),0.8));/**/

      mStage.setScene(lPanZoomScene);
      mStage.show();
      lCountDownLatch.countDown();
    });
    try
    {
      lCountDownLatch.await();
    }
    catch (InterruptedException e)
    {
    }

  }

  /**
   * Waits (blocking call) while window is showing.
   */
  public void waitWhileShowing()
  {
    while (isShowing())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  /**
   * Returns true if image view is showing.
   * 
   * @return true if showing
   */
  public boolean isShowing()
  {
    return mStage.isShowing();
  }

}
