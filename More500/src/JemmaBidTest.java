import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class JemmaBidTest {

	@Test
	public void testLowCards() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,6));
		assertEquals(0,JP.getInitaialBidValue(tHand,1));
		tHand.cards.add(new Card(2,6));
		tHand.cards.add(new Card(3,12)); //Not an ace
		assertEquals(0,JP.getInitaialBidValue(tHand,1));
	}
	@Test
	public void testAces() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,13));
		assertEquals(1,JP.getInitaialBidValue(tHand,1));
		tHand.cards.add(new Card(3,13));
		assertEquals(2,JP.getInitaialBidValue(tHand,1));
		tHand.cards.add(new Card(2,12)); //Not an ace
		assertEquals(2,JP.getInitaialBidValue(tHand,1));
	}
	
	@Test
	public void trumpCount() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,13));
		assertEquals(1,JP.getInitaialBidValue(tHand,0));
		tHand.cards.add(new Card(1,6));
		assertEquals(1,JP.getInitaialBidValue(tHand,0));
		tHand.cards.add(new Card(0,5));
		tHand.cards.add(new Card(0,6));
		tHand.cards.add(new Card(0,4));
		assertEquals(1,JP.getInitaialBidValue(tHand,0)); //these cards are below 9
	}
	
	@Test
	public void testProtected() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,12));
		assertEquals(0,JP.getInitaialBidValue(tHand,1)); //Unprotected
		tHand.cards.add(new Card(0,6));
		assertEquals(1,JP.getInitaialBidValue(tHand,1)); //Protected
		tHand.cards.add(new Card(0,5));
		tHand.cards.add(new Card(0,7));
		tHand.cards.add(new Card(0,4));
		assertEquals(1,JP.getInitaialBidValue(tHand,1)); //Still 1 protected card
		tHand.cards.add(new Card(0,8));
		// Above 13,11,10,9
		// Below 4,5,6,7
		// So the 8 is protected
		assertEquals(2,JP.getInitaialBidValue(tHand,1));
	}
	@Test
	public void testProtected2() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,13));
		assertEquals(1,JP.getInitaialBidValue(tHand,1)); //Winner
		tHand.cards.add(new Card(0,12));
		assertEquals(2,JP.getInitaialBidValue(tHand,1)); //Protected by card above
	}
	@Test
	public void testProtected3() {
		JemmaPlayer JP=new JemmaPlayer();
		Hand tHand=new Hand();
		tHand.cards.add(new Card(0,13));
		assertEquals(1,JP.getInitaialBidValue(tHand,1)); //Winner
		tHand.cards.add(new Card(0,11));
		assertEquals(1,JP.getInitaialBidValue(tHand,1)); //Not protected by card above
		tHand.cards.add(new Card(0,10));
		assertEquals(2,JP.getInitaialBidValue(tHand,1)); //added protection
	}
	@Test
	public void testBidIncrease() {
		//Create sample bidding
		ArrayList<Bid> prevBids=new ArrayList<Bid>();
		Bid pass = new Bid(-1,0);
		prevBids.add(new Bid(0,6)); //Partner bid 6 spades
		prevBids.add(pass);
		
		JemmaPlayer JP=new JemmaPlayer();

		assertEquals(6,JP.BidIncreaseEst(4,1,prevBids)); //Bidding different suit to partner
		assertEquals(8,JP.BidIncreaseEst(4,0,prevBids)); //Bidding same suit as partner
		prevBids.add(pass); //now partner has passed
		
		//Check the adding conditions
		assertEquals(6,JP.BidIncreaseEst(4,0,prevBids)); //Adds 2
		assertEquals(6,JP.BidIncreaseEst(5,0,prevBids)); //Adds 2
		assertEquals(7,JP.BidIncreaseEst(6,0,prevBids)); //Adds 1
		assertEquals(8,JP.BidIncreaseEst(7,0,prevBids)); //Adds 1
		assertEquals(9,JP.BidIncreaseEst(8,0,prevBids)); //Adds 1
		assertEquals(9,JP.BidIncreaseEst(9,0,prevBids)); //Adds 0
		assertEquals(10,JP.BidIncreaseEst(10,0,prevBids)); //Adds 0
		
		//test over 10
		prevBids.add(new Bid(0,6)); //Partner bid 6 spades
		prevBids.add(pass);
		assertEquals(10,JP.BidIncreaseEst(10,0,prevBids)); //Don't increase to over 10
	}
}
