package Player;

import Game.Model.Board;

import java.util.Arrays;

public abstract class Bot extends Player {
    protected boolean active;

    public Bot(Board board){
        System.out.println("Making a new bot...");
        active = true;
        this.board = board;
        deck = getRandomDeck();
        System.out.println("Deck is ready...");
        readyCards = getRandomReadyCards();
        System.out.println("Ready Cards are ready...");
        nextReadyCard = getRandomNextCard();


        System.out.println("readyCards: " + readyCards);
        System.out.println("deck: " + Arrays.toString(deck));
        System.out.println("nextReadyCard: " + nextReadyCard);
        System.out.println("Bot was made!\n");

    }


    public void inactive(){
        active = false;
    }





}