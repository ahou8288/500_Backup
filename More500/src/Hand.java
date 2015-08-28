import java.util.ArrayList;
import java.util.Collections;


public class Hand{
	
	public Hand(){
		this.cards=new ArrayList<Card>();
	}
	
	public Hand(ArrayList<Card> startingCards){
		this();
		this.cards.addAll(startingCards);
		
	}
	public ArrayList<Card> cards;
	
	public int length(){
		return cards.size();
	}
	
	public void sort(){
		Collections.sort(this.cards);
	}
	
	//Find the number of protected cards
	public int protectedWinners(){
		int sum=protectedWinners(0)+protectedWinners(1)+protectedWinners(2)+protectedWinners(3);
		return sum;
	}
	
	public int protectedWinners(int curSuit){
		int bidVal=0;
		for (Card curCard:cards){
			if (curCard.suit==curSuit){
				//Count the cards below and the cards above
				//CARDS BELOW
				int cardsBelowCount=0;
				//GAPS ABOVE
				int gapsAbove=13-curCard.value;
				
				for (Card comparisonCard : cards){ //Look at all the other cards
					if (comparisonCard.suit==curCard.suit){ //Only consider cards in the same suit
						if (comparisonCard.value>curCard.value){ //This card is above so there is one less gap
							gapsAbove--;
						} else if (comparisonCard.value<curCard.value) { //The player has a card below the current card
							cardsBelowCount++;
						}
					}
				}
				if (gapsAbove<=cardsBelowCount){ //If you can protect the card
					bidVal++;
				}
			}
		}
		return bidVal;
	}
}