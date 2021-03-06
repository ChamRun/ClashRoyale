package Menu;

import Accounts.Database;
import Audio.Audio;
import Game.Controller.GameController;
import Game.Model.Board;
import Player.*;
import com.sun.tools.javac.Main;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {


    /**
     * Common Controller
     */

    User user;

    @FXML
    public void setUser(Event event, User user) {
        /*
        this.user = user;
        System.out.println(user.getName() + " opened main menu.");
        hello.setText("Hello " + user.getName());

         */
        user = new User(new Database("localhost", "sa", "SQLpass", "test.dbo.users"), "Test");
    }

    @FXML
    public void gotoNewGame(Event event){
        System.out.println(event.getEventType() + " on " + event.getTarget());
        //switchToScene(event, "AskDifficulty");
        switchToScene(event, "View/NewGame.fxml");
    }

    @FXML
    public void gotoMainMenu(MouseEvent event) {
        switchToScene(event, "View/MainMenu.fxml");
    }

    @FXML
    public void gotoProfile(MouseEvent event){
        //chart
        switchToScene(event, "View/Profile.fxml");

    }

    @FXML
    public void gotoDeck(MouseEvent event) {
        switchToScene(event, "View/Deck.fxml");
    }

    @FXML
    public void logout(Event event){
        System.out.println("logging out...");
        //are you sure?
        switchToScene(event, "../Accounts/View/login.fxml");
    }

    public void gotoExit(MouseEvent event) {
        switchToScene(event, "View/Exit.fxml");
    }

    public void setUser(User user) {

        this.user = user;
        System.out.println("setUser(" + user.getName() + ")");

    }

    public GameController switchToScene(javafx.event.Event event, String sceneName){

        Audio.click();

        System.out.println(event.getEventType() + " on " + event.getTarget());
        System.out.println("Trying to switch to " + sceneName);

        FXMLLoader loader = null;

        try {
            loader = new FXMLLoader(getClass().getResource(sceneName));
            loader.load();
            Parent root = loader.getRoot();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(getTitle(sceneName, ".fxml"));
            stage.show();

            System.out.println(sceneName);

            if (!sceneName.startsWith("..")){
                ((Controller) loader.getController()).setUser(user);
            }
            else if (sceneName.startsWith("../Game")){
                System.out.println("returning Controller... ");
                return loader.getController();
            }
        }
        catch (ExceptionInInitializerError e){
            System.out.println(sceneName + ": wrong?");
        }
        catch (IOException e){
            e.printStackTrace();
        }

        if (sceneName.contains("NewGame")) {
            ((Controller) loader.getController()).easy.setSelected(true);
        }
        else if (sceneName.contains("Profile")) {
            ((Controller) loader.getController()).refreshProfile(event);
        }


        return null;

    }

    private String getTitle(String url, String suffix) {
        String[] strings = (url.replace(suffix, "")).split("/");
        return strings[strings.length - 1];
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("initialize of Menu.Controller");

        if (url.toString().endsWith("Profile.fxml")) {





        }
        else if (url.toString().endsWith("Deck.fxml")){
            //Load decks...
        }

    }

    public void restart(MouseEvent event) {
        String[] args = new String[0];

        try {
            Main.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit(MouseEvent event) {
        System.out.println("Exiting...");
        System.exit(0);
    }


    /**
     * Profile Controller
     */
    @FXML
    private PieChart chart;
    @FXML
    private Text levelText;
    @FXML
    private Text coins;
    @FXML
    private ProgressBar levelBar;

    @FXML
    public void refreshProfile(Event event){

        levelText.setText(("Level " + user.getLevel().toString()));
        coins.setText(user.getCoins() + "");




        if (chart.getData().size() != 0) {
            chart.getData().clear();

            System.out.println("Removing all from chart...");
        }

        PieChart.Data wins = new PieChart.Data("Wins", user.getWins());
        PieChart.Data loses = new PieChart.Data("Loses", user.getLoses());

        ObservableList<PieChart.Data> chartData =
                FXCollections.observableArrayList(
                        wins, loses);

        chart.getData().addAll(chartData);

        loses.getNode().setStyle("-fx-pie-color: blue");
        wins.getNode().setStyle("-fx-pie-color: red");

        chartData.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                data.getName(), ": ", data.getPieValue()
                        )
                )
        );


        levelBar.setProgress(user.getLevelProgress());



    }


    /**
     * NewGame Controller
     */
    @FXML
    private ToggleGroup tg;
    @FXML
    private RadioButton easy;
    @FXML
    private RadioButton medium;
    @FXML
    private RadioButton hard;
    @FXML
    private Text chooseDifficultyText;

    @FXML
    public void startGame(Event event) {
        System.out.println(event);

        RadioButton selectedButton = (RadioButton) tg.getSelectedToggle();

        if (selectedButton == null){
            System.out.println("No button is selected :/");
            chooseDifficultyText.setFill(Paint.valueOf("red"));

            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    chooseDifficultyText.setFill(Paint.valueOf("black"));
                }
            }, 1000);
            return;

        }

        //ToDo: setting searchFightableRange...
        Board board = new Board(19, 35, 0);
        Bot bot = null;

        if (selectedButton == easy){
            System.out.println("Starting easy game...");
            bot = new EasyBot();
        }
        else if (selectedButton == medium){
            System.out.println("Starting medium game...");
            bot = new MediumBot();
        }
        else if (selectedButton == hard){
            System.out.println("Starting hard game...");
            bot = new HardBot();
        }

        if (bot == null){
            System.out.println("Bot is null!");
            return;
        }

        switchToScene(event, "../Game/View/GameView.fxml").setter(board, user, bot);




    }



    /*
    @FXML void test(){
        System.out.println(user.getEmail());
    }

     */


    /**
     * Deck Controller
     */

    @FXML Text chooseEight;


    @FXML
    void radioButtonAction(ActionEvent event) {

        /*
        System.out.println(event);
        //System.out.println(event);
        //System.out.println(((RadioButton) event.getSource()).getChildrenUnmodifiable());



        RadioButton radioButton = (RadioButton) event.getSource();
        String cardName = getRadiobuttonCardname(radioButton);

        if (radioButton.isSelected()){
            user.addCard(cardName);
        }
        else{
            user.removeCard(cardName);
        }

         */
    }

    @FXML
    void saveDeck(MouseEvent event){
        System.out.println("Trying to save new Deck...");
        System.out.println("old: " + Arrays.toString(user.getDeck()));


        Parent root = ((Node)event.getSource()).getScene().getRoot();
        RadioButton[] radioButtons = getRootRadiobuttons(root);

        String[] newDeck = new String[8];
        int i = -1;

        for (RadioButton radioButton: radioButtons){

            if (radioButton.isSelected()){
                i++;
                if (i == 8){
                    chooseEight.setFill(Paint.valueOf("red"));

                    (new Timer()).schedule(new TimerTask() {
                        @Override
                        public void run() {
                            chooseEight.setFill(Paint.valueOf("white"));
                        }
                    }, 1000);
                    return;
                }

                newDeck[i] = getRadiobuttonCardname(radioButton);
            }
        }

        if (i != 7) {
            chooseEight.setFill(Paint.valueOf("red"));

            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    chooseEight.setFill(Paint.valueOf("white"));
                }
            }, 1000);
            return;
        }


        if (user.saveDeck(newDeck)){
            switchToScene(event, "View/MainMenu.fxml");
            return;
        }

        System.out.println("We couldn't save cards :/ WHY?!");



    }


    @FXML
    void selectCurrentDeck(ActionEvent event) {
        Parent root = ((Node)event.getSource()).getScene().getRoot();
        RadioButton[] radioButtons = getRootRadiobuttons(root);
        System.out.println(Arrays.toString(radioButtons));

        String[] selectedDeck = user.getDeck();


        for (RadioButton radioButton: radioButtons){
            radioButton.setSelected(false);
        }


        for (String cardName: selectedDeck){
            for (RadioButton radioButton: radioButtons){

                if (cardName.equalsIgnoreCase(getRadiobuttonCardname(radioButton))){
                    radioButton.setSelected(true);
                }
            }
        }
    }

    private RadioButton[] getRootRadiobuttons(Parent root) {
        ObservableList<Node> children = root.getChildrenUnmodifiable();
        //System.out.println(children);

        RadioButton[] radioButtons = new RadioButton[12];
        int i = 0;

        for (Node node: children){
            //System.out.println(node.getClass());
            if (node instanceof RadioButton){
                radioButtons[i] = (RadioButton) node;
                i++;
            }
        }

        //System.out.println(Arrays.toString(radioButtons));
        return radioButtons;
    }

    private String getRadiobuttonCardname(RadioButton radioButton) {
        ObservableList<Node> images = radioButton.getChildrenUnmodifiable();
        String imageUrl = ((ImageView)images.get(0)).getImage().getUrl();
        String cardName = getTitle(imageUrl, ".png");
        //System.out.println(cardName);
        return cardName;
    }


}
