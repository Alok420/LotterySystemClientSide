package lotterysystem;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Arc;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

/**
 *
 * @author panda
 */
public class LotterySystem extends Application {

    double rotationtime;
    ArrayList generatedNumbers = new ArrayList();
    double pointerX;
    double pointerY;
    long milli = 5000;
    Media beep, sound;
    MediaPlayer mediaPlayer;
    MediaPlayer beepPlayer;
    RotateTransition rotateTransition;
    ImageView playImage;
    double textarr[][] = new double[10][3];
    Arc pointer;
    AnchorPane root;
    Button timer;
    Integer starttime = 60;
    Integer seconds = starttime;
    String serverMessage = "", timerstr = "";
    int stopnum;
    Button positionedButton, counter, winner, loinbtn;
    HttpURLConnection uc, uc2;
    final static String INET_ADDR = "224.0.0.3";
    InetAddress address = null;
    final static int PORT = 8888;
    byte[] buf = new byte[256];
    MulticastSocket s;
    Reader r, r2;
    InputStream buffer, buffer2;
    Thread playGame;
    Pane loginpanel;
    String usernametext;

    @Override
    public void start(Stage stage) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setHeight(screenSize.height);
        stage.setWidth(screenSize.getSize().width + 20);
        stage.setMaximized(true);
        stage.setFullScreen(true);

        Random rand = new Random();
        loinbtn = (Button) root.getScene().lookup("#loginbtn");
        loginpanel = (Pane) root.getScene().lookup("#loginpanel");
        TextField username = (TextField) root.getScene().lookup("#username");
        TextField password = (TextField) root.getScene().lookup("#password");
        timer = (Button) root.getScene().lookup("#time");
        counter = (Button) root.getScene().lookup("#counter");
        winner = (Button) root.getScene().lookup("#winner");
        HBox header = (HBox) root.getScene().lookup("#header");
        HBox footer = (HBox) root.getScene().lookup("#footer");
        VBox rightcontent = (VBox) root.getScene().lookup("#rightcontent");
        rightcontent.setLayoutX(stage.getWidth() - 504);
        Group rotation = (Group) root.getScene().lookup("#rotation");

        loginpanel.setPrefWidth(screenSize.width);
        loginpanel.setPrefHeight(screenSize.height);
//        loginpanel.setStyle(" -fx-text-fill: white;");

        sound = new Media(new File("play.mp3").toURI().toString());
        beep = new Media(new File("beep.mp3").toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        beepPlayer = new MediaPlayer(beep);
//      -----------------------------------------flashing button------------------------------------
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.1), timer);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setCycleCount(Animation.INDEFINITE);

        final Interpolator timeVsDistanceInterpolator = new BestFitSplineInterpolator(
                new double[]{0.0, 0.25, 0.5, 0.75, 1.0},
                new double[]{0.0, 0.1, 0.4, 0.85, 1.0}
        );

        rotateTransition = new RotateTransition(Duration.millis(milli), rotation);
        int rt = rand.nextInt(500) + 1000;
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle((-36 * 1) + (360 * 5));
        rotationtime = rt;
        rotateTransition.setInterpolator(timeVsDistanceInterpolator);
        rotateTransition.setAutoReverse(false);
        header.setPrefWidth(screenSize.getWidth());
        header.setPrefHeight(50);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(2, 2, 2, 2));
        header.setSpacing(10);

        footer.setPrefWidth(screenSize.getWidth());
        footer.setPrefHeight(60);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(2, 2, 2, 2));
        footer.setLayoutX(0);
        footer.setLayoutY(stage.getHeight() - 150);
        footer.setSpacing(20);
        Button btn = null;
        for (int i = 0; i < 10; i++) {
            btn = new Button("" + i);
            btn.setId("btn" + i);
            footer.getChildren().add(btn);
        }
        rotateTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
//                    System.err.println("finished");
//                    System.err.println("" + generatedNumbers.size());
                    positionedButton = (Button) root.getScene().lookup("#btn" + stopnum);
                    positionedButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    if (generatedNumbers.size() >= 10) {
                        generatedNumbers.remove(0);
                        generatedNumbers.add(stopnum);

                    } else {
                        generatedNumbers.add(stopnum);
                    }
                    winner.setText("" + stopnum);
                    counter.setText(generatedNumbers.toString().replace("[", "").replace("]", ""));

//                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                mediaPlayer.stop();
                beepPlayer.seek(Duration.ZERO);
                beepPlayer.play();
                beepPlayer.seek(Duration.ZERO);
                serverMessage = "stop";
            }
        });
        Timeline t = new Timeline();
        t.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    URL timeurl = new URL("http://mumbaimetrogames.com/timer.php");
                    uc2 = (HttpURLConnection) timeurl.openConnection();
                    uc2.addRequestProperty("User-Agent", "Mozilla/4.76");
                    InputStream raw2 = uc2.getInputStream();
                    buffer2 = new BufferedInputStream(raw2);
                    r2 = new InputStreamReader(buffer2);
                    int c2;
                    timerstr = "";
                    while ((c2 = r2.read()) != -1) {
                        timerstr += (char) c2;
                    }
                    timer.setTextAlignment(TextAlignment.CENTER);
                    timer.setText("" + timerstr);
                    int timer2 = Integer.parseInt(timerstr);
                    if (timer2 >= 10 && timer2 <= 15) {
                        if (fadeTransition.getStatus() == Animation.Status.RUNNING) {
                        } else {
                            fadeTransition.play();

                        }
                    } else {
                        fadeTransition.stop();
                    }
                    raw2.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    URL logout = new URL("http://mumbaimetrogames.com/Controller/logoutbyapp.php?username=" + usernametext);
                    HttpURLConnection logoutuc = (HttpURLConnection) logout.openConnection();
                    logoutuc.addRequestProperty("User-Agent", "Mozilla/4.76");
                    logoutuc.setRequestMethod("GET");
                    BufferedReader br = new BufferedReader(new InputStreamReader(logoutuc.getInputStream()));
                    String loginMessage = br.readLine();
                    alert("Logout message", "Logged out message : " + loginMessage);
                    System.exit(0);
                    stage.close();
                    playGame.destroy();
                    playGame = null;

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        t.getKeyFrames().add(keyFrame);
        t.playFromStart();
        loinbtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                usernametext = username.getText();
                String passwordtext = password.getText();
                try {
                    URL url = new URL("http://mumbaimetrogames.com/Controller/LoginControllerFromApp.php?username=" + usernametext + "&password=" + passwordtext + "&type=branch");
                    HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                    uc.addRequestProperty("User-Agent", "Mozilla/4.76");

                    uc.setRequestMethod("GET");
                    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    String loginMessage = br.readLine();
                    if (loginMessage.equals("branchloggedin")) {
//                        alert("Message", loginMessage);
                        root.getChildren().remove(loginpanel);
                    } else {
                        alert("Message", loginMessage);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        stage.show();
        if (stage.isShowing()) {
            int i = 0;
            double mls = sound.getDuration().toMillis();
            int playtime = (int) Math.ceil(rotationtime / mls);
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.setCycleCount(playtime);

            playGame = new Thread(new Runnable() {
                String startMessage;

                @Override
                public void run() {
                    try {
                        while (true) {
                            URL u = new URL("http://mumbaimetrogames.com/playfile.php");
                            uc = (HttpURLConnection) u.openConnection();
                            uc.addRequestProperty("User-Agent", "Mozilla/4.76");

                            InputStream raw = uc.getInputStream();
                            buffer = new BufferedInputStream(raw);
                            r = new InputStreamReader(buffer);

                            int c;
                            serverMessage = "";
                            while ((c = r.read()) != -1) {
                                serverMessage += (char) c;
//                                System.err.println(serverMessage);
                            }
                            serverMessage.trim();
                            String strarr[] = serverMessage.split(" ");
                            if (strarr.length > 0) {
                                serverMessage = strarr[0];
                            }
                            if (strarr.length > 1) {
                                stopnum = Integer.parseInt(strarr[1]);
                                rotateTransition.setToAngle((-36 * stopnum) + (360 * 5));
                            } else {

                            }

                            if (serverMessage.equals("play")) {
                                if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
//                                    System.err.println("playing");
                                } else {
                                    if (positionedButton != null) {
                                        positionedButton.setStyle("-fx-background-color: transparent;");
                                    }
                                    rotateTransition.play();
                                    mediaPlayer.play();
                                }
                            }
                            raw.close();

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            playGame.start();

        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    void alert(String title, String message) {
        Alert al = new Alert(Alert.AlertType.ERROR);
        al.setTitle(title);
        al.setContentText(message);
        al.showAndWait();
    }
}

class BestFitSplineInterpolator extends Interpolator {

    final PolynomialSplineFunction f;

    BestFitSplineInterpolator(double[] x, double[] y) {
        f = new SplineInterpolator().interpolate(x, y);
    }

    @Override
    protected double curve(double t) {
        return f.value(t);
    }
}
