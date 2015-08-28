
public class Stats {

	public static void main(String[] args) {
		int numTests=50; //Number of tests
		int team1Wins=0; //How many times team 1 has won
		int[] teamRounds=new int[2]; //How many rounds each team needed to win
		for (int i=0;i<numTests;i++){
			int[] gameResult=Game.runGame(false);
			team1Wins+=gameResult[0];
			teamRounds[gameResult[0]]+=gameResult[1];
		}
		int team0Wins=numTests-team1Wins;
		System.out.printf("Team 0 - %d wins.\nTeam 1 - %d wins.\n", team0Wins,team1Wins);
		if (team0Wins!=0&&team1Wins!=0){
			System.out.printf("Team 0 - %d rounds. Team 1 - %d rounds.\n", teamRounds[0]/team0Wins,teamRounds[1]/team1Wins);
		} else {
			System.out.printf("T0 %d rounds. T1 %d rounds.", teamRounds[0],teamRounds[1]);
		}
	}
}
