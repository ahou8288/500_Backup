import java.util.ArrayList;

public class JemmaPlayer implements PlayerInt{
	
	public Team team;
	
	//Constructor
	public JemmaPlayer() {	}
	
	@Override
	public Bid getBid(Hand hand, Team[] teams, ArrayList<Bid> prevBids) {
		int maxPoints=0;
		Bid maxBid=new Bid(-1,0);
		//test
		//Try the current hand in each of the suits
		for (int curSuit=0;curSuit<4;curSuit++){
			Hand tempHand=Game.incBowers(hand,curSuit); //get a hand with bowers
			int tempValue=getInitaialBidValue(tempHand,curSuit); //find the number of winners
			tempValue=BidIncreaseEst(tempValue,curSuit,prevBids); //add what you estimate your partner has
			Bid tempBid=new Bid(curSuit,tempValue);
			if (tempBid.pointValue()>maxPoints){ //see if the bid is the best bid.
				maxPoints=tempBid.pointValue();
				maxBid=tempBid;
			}
			//Test
			//System.out.printf("s%d v%d p%d. ",curSuit,tempValue,tempBid.pointValue());
		}
		//TODO check bid is not > 10
		if (prevBids.size()==0){
			if (maxPoints>=40){
				return maxBid;
			} else {
				return new Bid(-1,0); //TODO pass first bid
			}
		} else {
			if (maxPoints>getUsefulBid(prevBids).pointValue()){
				return maxBid;
			} else {
				return new Bid(-1,0);
			}
		}
	}
	


	int getInitaialBidValue(Hand hand, int curSuit) {
		int bidVal=0;
		for (Card tempCard : hand.cards){
			if (tempCard.suit==curSuit){
				bidVal++;
			}
		}
		
		for (int suit=0;suit<4;suit++){
			if (suit!=curSuit){
				bidVal+=hand.protectedWinners();
			}
		}
		
		return bidVal;
	}

	public int BidIncreaseEst(int currentVal, int curSuit, ArrayList<Bid> prevBids){
		int z=prevBids.size(); //Store size because it is called twice
		if (z>=2&&prevBids.get(z-2).suit==curSuit){//look at partners suit
			if (z<4||(z>=4&&prevBids.get(z-2).suit!=curSuit)){ //don't give the bonus if you bid this suit last time
				currentVal+=3; //The partner bid the same suit so we can bid 3 higher
			}
		}
		/*TODO somebody had 5 winners
		 * their partner bid and they were meant to get 8
		 * program did not add 1 to get to 9 bid
		 */
		
		if (currentVal<5){ //Add 2 to get to 6
			currentVal+=2;
		} else if (currentVal<9){ //Add 1 to get to a 9 bid
			currentVal++;
		}
		
		if (currentVal>10){// don't bid over 10
			return 10;
		} else {
			return currentVal;
		}
	}

	private Bid getUsefulBid(ArrayList<Bid> prevBids) {
		int z=prevBids.size();
		
		if (z==0){ //if there are no bids
			return new Bid(-1,0);
		}
		
		for (int i=1;i<4;i++){
			if (prevBids.get(z-i).suit>-1||z-i==0){ //If the bid is not a pass, or it is the first bid
				return prevBids.get(z-i);
			}
		}
		return null;
	}
	@Override
	public Hand useKitty(Hand kitty, Hand hand, ArrayList<Bid> prevBids) {
		
		//info needed:
		/* Number of cards per suit
		 * Highest card in each suit
		 * 3 lowest cards in hand
		 */ 
		
		//combine kitty with the hand
		hand.cards.addAll(kitty.cards);
		int trumps = prevBids.get(prevBids.size()-1).suit;
		
		
		//go through each suit, and count the cards;
		//index [i][0] keeps track of number of cards in i-th suit
		//index [i][1] keeps track of highest card in i-th suit
		//min3 keeps track of the 3 lowest, nontrumps in the hand
		int[][] counts = new int[4][2];
		Card min3[] = {new Card(1,13), new Card(1,13), new Card(1,13)};
		//initialise the min3 to have high cards in it
		
		
		for (int i=0; i<hand.length(); i++){
			Card current = hand.cards.get(i);
			counts[current.suit][0] += 1;
			
			//replace highest card if suitable
			if (current.value > counts[current.suit][1]){
				counts[current.suit][1] = current.value;
			}
			
			//replace lowest card if suitable
			//very clumsy method for keeping these in order. Is there a better way?
			if(current.value < min3[0].value && current.suit !=trumps){
				min3[2] = min3[1];
				min3[1] = min3[0];
				min3[0] = current;
			}
			else if(current.value < min3[1].value && current.suit !=trumps){
				min3[2] = min3[1];
				min3[1] = current;
			}
			else if(current.value < min3[2].value && current.suit !=trumps){
				min3[2] = current;
			}
		}
		
		int rmblLeft = 3;//how many cards that are left to be removed
		int considerable = 3; //keeps count of how many suits are to be considered for short suiting
		
		//keep doing this until there is nothing left in removeSuit or there are no more cards to remove
		while (rmblLeft>0 && considerable>0){
		
			//check each suit for being all losers and <3, lowest max and not trumps
			//if it meets this criteria its the best suit so far to get rid of
			int currentMax=13;
			int removeSuit = -1;
			for (int i=0; i<4; i++){
				if (counts[i][0]<=rmblLeft && counts[i][0]>0 && allLosers(hand, i, counts[i][0]) && counts[i][1]<currentMax &&  i != trumps){
					removeSuit = i; //mark the suit which is offsuitable with losers, which has lowest max - not concerned with 
					currentMax = counts[i][1];
				}
				else{
					counts[i][0] = 0; //if its not short suitable, mark it to be ignorred - might as well have none of them
				}
			}
			//if there was a suit to get rid of, do it
			if (removeSuit>0){
				for (int i=0; i<hand.length(); i++){
					if (hand.cards.get(i).suit == removeSuit){
						hand.cards.remove(i);
						rmblLeft--;
					}
				}
				//omit the entry in count, so it doesn't try to remove it again
				counts[removeSuit][0] = 0;
			}			
		
			//note how many suits are now possibly short suit worthy
			considerable = 0;
			for (int i=0; i<4; i++){
				if (counts[i][0]!=0 && counts[i][0] < rmblLeft && i !=trumps){
					considerable ++;
				}
			}
			
		}
		
		//if there are still too many cards when nothing can be short suited get rid of three lowest non trumps
		for (int i=0; i<3; i++){
			if (rmblLeft != 0 && hand.cards.remove(min3[i]))
			{
					rmblLeft --;
			}
		}
				
		hand.sort();
		return hand;

	}
	
	// determine whether a suit is all losers
	// only works for kitty because assumes <=3 cards in that suit
	private  boolean allLosers(Hand hand, int suit, int countOfSuit){
		Card topCard = new Card(suit, 13); //NOte this 13 is always the highest card in a suit
		Card secCard = new Card(suit, 12);
		
		if (hand.cards.contains(topCard)){
			return false;
		}
		//if you don't have the A, but have the K and another, its not all losers
		else if(countOfSuit > 1 && hand.cards.contains(secCard)){
			return false;
		}
		else return true;
		
	}
	
	@Override
	public Card getCard(ArrayList<Bid> prevBids, Hand hand, ArrayList<Card> trickCards) {
		int playerNum=trickCards.size()+1;
		if (playerNum==4){
			/*
			 * Find the lowest legal non trump that wins
			 * Find the lowest trump that wins
			 * PlayLow
			 */
		} 
		return null;
	}
}