import java.util.ArrayList;
import java.util.Collections;
public class Game {
	public static void main(String[] args){
		runGame(true);
	}
	public static int[] runGame(boolean displayOn){
		//Stores an instance of each of the players
		PlayerInt[] players = makePlayers();//Create the players
		
		//For statistics
		int numTurns=0;
		
		//player 0 and 2 and part of team 0
		//player 1 and 3 are part of team 1
		Team team0 = new Team(players[0], players[2]);
		Team team1 = new Team(players[1], players[3]);
		Team[] teams = {team0, team1};

		int firstBidPlayer=0; //Begin with the first player
		while(gameNotOver(teams)){//Check if the points cause the game to end
			
			//Deal the cards
			Hand[] hands = generateHands(); //hands[4] contains the kitty
			
			//Show the programmer what is in everyones hand
			DisplayMethods.DispHands(hands);
			//GuiDisp.FormHands(hands); //This is a different display
			
			//Run the bidding
			ArrayList<Bid> prevBids=new ArrayList<Bid>(); //Create an Arraylist of all the bids ever
			int currentBidPlayer=firstBidPlayer; //Begin with the first player//TODO change this
			
			while(!isBiddingOver(prevBids)){
				Bid tempBid;
				if (prevBids.size()>4&&prevBids.get(prevBids.size()-4).suit==-1){
					tempBid = new Bid(-1,0); //Player has already passed previously
				} else {
					tempBid=players[currentBidPlayer].getBid(hands[currentBidPlayer],teams,prevBids); //Collects the players bid
				}
				prevBids.add(tempBid); //Stores the players bid
				DisplayMethods.DispBid(tempBid,currentBidPlayer); //Display the bid
				currentBidPlayer=(currentBidPlayer+1)%4; //Cycle around the players
			}
			
			firstBidPlayer=(firstBidPlayer+1)%4; //Next player begins the bidding next round.
			
			if (AllPlayersPassed(prevBids)){
				System.out.println("All players passed. Ending game.");
			} else {
				//Determine which player won the bidding
				int leadPlayer=currentBidPlayer; //The bidding when with a round of passes and returned to the person who won the bidding.
				
				int bidWinner=leadPlayer; //Stored for point scoring purposes
				int bidWinnerTeam=bidWinner%2;
				Bid bidSelected=prevBids.get(prevBids.size()-4);
				int selectedBidValue=bidSelected.pointValue();
				
				for (int i=0; i<hands.length; i++){
					hands[i] = incBowers(hands[i], bidSelected.suit);
				}
				
				//Give the player the kitty
				DisplayMethods.DispHand(hands[leadPlayer]); System.out.println(); DisplayMethods.DispHand(hands[4]); System.out.println();
				hands[leadPlayer]=players[leadPlayer].useKitty(hands[4],hands[leadPlayer],prevBids); //Give the player the kitty to use
				DisplayMethods.DispAfterKitty(hands[leadPlayer]); //Display the new hand of after the kitty was used
				
				//make an arraylist to store hands of tricks
				ArrayList<Hand> gameTricks = new ArrayList<Hand>();
				
				team0.resetTricks();
				team1.resetTricks();
				for (int i=0;i<10;i++){ //Run exactly 10 rounds
					
					ArrayList<Card> trickCards=new ArrayList<Card>(); //Create a new blank ArrayList to store the cards of this trick.
					for (int j=0; j<4;j++){ //Loop through each player
						int currentPlayer=(leadPlayer+j)%4; //Player that needs to play a card
						Card tempCard =players[currentPlayer].getCard(prevBids,hands[currentPlayer],trickCards);//Get a card from the player
						trickCards.add(tempCard); //Store the card
					}
					
					//All 4 cards from the trick have been played
					//Store them for later use
					gameTricks.add(new Hand(trickCards));
					
					int winner=trickWinner.findVictor(trickCards,bidSelected); //Find the winner of the last trick
					winner=(winner+leadPlayer)%4; //convert to actual player index
					
					int winningTeam = winner%2;
					teams[winningTeam].addTrick();
					
					DisplayMethods.DispTrick(trickCards,winner,leadPlayer); //Show what happened in the past trick
					leadPlayer=winner; //Figure out who leads the next trick
				}
				
				
				DisplayMethods.DispTrickCount(teams,bidWinnerTeam,bidSelected);
				
				int otherTeamIndex=(bidWinnerTeam+1)%2;
				if (bidSelected.value>teams[bidWinnerTeam].tricksWon){ //if the bid was not reached //TODO if 10 is reach round up to 250
					teams[bidWinnerTeam].points-=selectedBidValue;
				} else {
					teams[bidWinnerTeam].points+=selectedBidValue;
				}
				teams[otherTeamIndex].points+=10*teams[otherTeamIndex].tricksWon;
				
				DisplayMethods.DispTeamPoints(team0,team1);
			}
			numTurns++;
		}
		if (team0.points>=500){
			return new int[] {0,numTurns};
		} else {
			return new int[] {1,numTurns};
		}
	}
	
	private static PlayerInt[] makePlayers() { //Here we can change some code to decide who is each player
		PlayerInt[] players = new PlayerInt[4];
		players[0]=new DumbPlayer();
		players[1]=new DumbPlayer();
		players[2]=new DumbPlayer();
		players[3]=new DumbPlayer();
		return players;
	}

	private static boolean AllPlayersPassed(ArrayList<Bid> prevBids) {
		for (int i=1;i<4;i++){
			if (prevBids.get(0).compareTo(prevBids.get(i))!=0){
				return false; // Bids are still changing
			}
		}
		return true; //The same thing has been bid by everyone, it must be a pass.
	}

	private static Hand[] generateHands() {
		ArrayList<Card> deck=new ArrayList<Card>();
		
		//generate all the cards in the deck
		for (int suit =0;suit<=3;suit++){ 
			
			//longest possible suit is 13 cards long
			//highest of each suit is valued 13
			//lowest red = 3
			//lowest black = 4
			for(int value=3;value<=13;value++){
				if (!(suit<2 && value==3)){ // don't add suit2/3 value 3 (black 4s).  
					deck.add(new Card(suit,value));				
				}
			}
		}
		deck.add(new Card(4,1)); //this is the joker
		
		//Shuffle the deck
		Collections.shuffle(deck); //Java finally did something right!
		//Add cards to each hand
		Hand[] hands=new Hand[5];
		for (int i=0;i<4;i++){
			hands[i]=new Hand();
			for (int j=0;j<10;j++){
				Card newCard = deck.get(i*10+j);
				hands[i].cards.add(newCard);
			}
			hands[i].sort();
		}
		hands[4]=new Hand();
		//Add the kitty
		hands[4].cards.add(deck.get(40));
		hands[4].cards.add(deck.get(41));
		hands[4].cards.add(deck.get(42));
		
		return hands;
	}

	private static boolean gameNotOver(Team[] teams) {
		if(teams[1].points>=500||teams[0].points>=500){
			System.out.println("Game Over, player reached 500.");
			return false;
		}else if(teams[1].points<=-500||teams[0].points<=-500){
			System.out.println("Game Over, player reached -500.");
			return false;
		} else {
			return true;
		}
	}

	private static boolean isBiddingOver(ArrayList<Bid> prevBids) {
		int len=prevBids.size();
		if (len<4){
			return false;
		} else if (prevBids.get(len-1).suit==-1&&
				prevBids.get(len-2).suit==-1&&
				prevBids.get(len-3).suit==-1){
			return true;
		}
		return false;
	}
	
	public static Hand incBowers(Hand hand, int trumps){
		Hand tempHand=dupe(hand);
		
		for (int i=0; i<tempHand.length(); i++){
			Card card = tempHand.cards.get(i);

			//find the complementing suit
			int compSuit = 0;
			if (trumps == 3 || trumps == 1){
				compSuit = trumps - 1;
			}
			else if (trumps == 0 || trumps == 2){
				compSuit = trumps + 1;
			}
			
			//change the value of all cards in trumps to make room for the top cards
			if (card.suit == trumps){
				if (card.value <= 13 && card.value >= 11) { //unchanged face cards
					card.value -= 3;
				} 
				else if (card.value <=9){//number cards
					card.value -= 2;
				}
				else if (card.value == 10){ //right bower
					card.value +=2;
				}
			}

			//deal with left bower
			if (card.suit == compSuit && card.value == 10){
				card.suit = trumps;
				card.value = 11;
			}
			
			//deal with joker
			if (card.suit == 4){
				card.value = 13;
				card.suit = trumps;
			}
			
		}
		tempHand.sort();
		return tempHand;
	}
	private static Hand dupe(Hand hand) {
		Hand newHand = new Hand();
		for (int i=0;i<hand.length();i++){
			Card temp=hand.cards.get(i);
			newHand.cards.add(new Card(temp.suit,temp.value));
		}
		return newHand;
	}

}
