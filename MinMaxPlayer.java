import java.awt.Color;
import java.util.ArrayList;

/** This gomoku player uses alpha-beta search to find the best next move.
 *  Author: ecraya14
 **/


public class MinMaxPlayer extends GomokuPlayer {

	//board = current state of board, i.e. which cells are occupied
	// defined by color, Color.Black, Color.White or null 
	//me = the colour this player is playing
	int v = 0;
	int alpha = 0;
	int beta = 0;
	int[][] emptyBoard;
	long startTime;
	long timeLimit = (long) 10*1000;

	@Override
	public Move chooseMove(Color[][] board, Color me) {
		startTime = System.currentTimeMillis();
		int minInfinity = -1100000000;
		int maxInfinity = 1100000000;
		emptyBoard = null;
		int[] rowCol;
		int depth = 4; //set max depth to 4, branching factor is '64!'
		
		rowCol = maxValue(board, me, depth, minInfinity, maxInfinity,startTime);

		//position 0: is score in rowCol array
		return new Move(rowCol[1], rowCol[2]); //Best move
	}


	private int[] maxValue(Color[][] board, Color me, int depth, int alpha, int beta, long startTime) {
		int score=0;
		int bestRow = -1;
		int bestCol = -1;

		Color opposition;
		if (me == Color.BLACK)
			opposition = Color.WHITE;
		else
			opposition = Color.BLACK;

		//time exceeded
		long currentTime = System.currentTimeMillis()- startTime;
		if (currentTime>= timeLimit)
			return new int[] {score, bestRow, bestCol};
		//Depth reached
		if (depth == 0) {
			score = evaluateState(board, me);
			return new int[] {score, bestRow, bestCol}; ///score, row and col for best move
		}
		//gameover
		int gameOver = isGameOver(board, me);
		if(gameOver ==10000 || gameOver == -10000) {
			score = evaluateState(board, me);
			return new int[] {score, bestRow, bestCol};
		}
		//get cells which are null (unoccupied)
		ArrayList<int[]> emptyCells = emptyCells(board, me);
		if (emptyCells.isEmpty()) {
			score = evaluateState(board, me);
			return new int[] {score, bestRow, bestCol}; ///score, row and col for best move
		}
		int v = alpha;

		//recursive call, so
		//for each action cell in successors (state) i.e. for all children
		for (int[] move : emptyCells) {
			int row = move[0];
			int col = move[1];
			board[row][col] = me; //test the move
			v = Math.max(v, minValue(board, opposition, depth-1, alpha, beta,startTime)[0]); //get v (score)
			board[row][col] = null; //set state back, remove move

			if (v >= beta) //beta cut off
				return new int[] {v, bestRow, bestCol};
			if(v > alpha) { //update 
				bestRow = row;
				bestCol = col;
				alpha = v;
			}
		}
		return new int[] {v, bestRow, bestCol};

	}

	private int[] minValue(Color[][] board, Color opposition, int depth, int alpha, int beta, long startTime) {
		int score=0;
		int bestRow = -1;
		int bestCol = -1;
		Color me;
		if (opposition == Color.BLACK)
			me = Color.WHITE;
		else
			me = Color.BLACK;

		//time exceeded
		if ((System.currentTimeMillis()- startTime)>= timeLimit)
			return new int[] {score, bestRow, bestCol};
		//Depth reached
		if (depth == 0) {
			score = evaluateState(board, opposition);
			return new int[] {score, bestRow, bestCol}; //RETURNS SCORE,  row and col for best move
		}
		int gameOver = isGameOver(board, me);
		if(gameOver ==10000 || gameOver == -10000) {
			score = evaluateState(board, me);
			return new int[] {score, bestRow, bestCol}; //RETURNS SCORE,  row and col for best move
		}

		//get cells which are null (unoccupied)
		ArrayList<int[]> emptyCells = emptyCells(board, opposition);
		if (emptyCells.isEmpty()) { 
			score= evaluateState(board, opposition); 
			return new int[] {score, bestRow, bestCol}; //RETURNS SCORE,  row and col for best move
		}
		int v = beta;

		//recursive call, so
		//for each action cell in successors (state) i.e. for all children
		for (int[] move : emptyCells) {
			int row = move[0];
			int col = move[1];
			board[row][col] = opposition; //test the move
			v = Math.min(v, maxValue(board, me, depth-1, alpha, beta,startTime)[0]);
			board[row][col] = null; //set state back, remove move	

			//if maximum it can get is less than the lowest value, do not search more in this branch
			if (v <= alpha)
				return new int[] {v, bestRow, bestCol};
			if (v < beta) { //update
				bestRow = row;
				bestCol = col;
				beta = v;
			}
		}
		return new int[] {v, bestRow, bestCol};	//RETURNS SCORE,  row and col for best move
	}

	//Given the state(all occupied cells), what is the next best move, so we've to find empty cells first
	private ArrayList<int[]> emptyCells(Color[][] board, Color me) {
		ArrayList<int[]> moves = new ArrayList<int[]>();

		for(int i = 0; i < GomokuBoard.ROWS; i++) {
			for (int j = 0; j < GomokuBoard.COLS; j++) {
				if (board[i][j] == null) {
					//for each arraylist position, we add two values, grid identifier. 
					moves.add(new int[] {i, j});
				}
			}
		}
		return moves;
	}
	/*
	//give more points to the same color cells on same diagonal, col or row without any
	//opposing player cells, subract points if there are opposing player cells in same area.
	//In this state how many points will the player receive
	//for 1s: 1 - for 2s: 400 - for 3s: 12000 - for 4s: 400,000 - (both sides open) 3,000,000 - win: 11,000,000
	// we subtract the same corresponding amount for x number of opposing player cells.
	*/
	public int evaluateState(Color[][] board, Color me) {
		Color opposition;
		if (me == Color.BLACK)
			opposition = Color.WHITE;
		else
			opposition = Color.BLACK;

		int pointsMe = 0; //count number of points this player ('me') gets on the current state of board.

		//for each sequence of same color(either me or opposition) 1-5, count number of occurences and
		//give points accordingly
		//we check all rows, columns, and left and right diagonals
		for (int sequence = 1; sequence<=5; sequence++) {

			// analyse rows
			for(int col=0; col<8;col++) {
				for(int row = 0; row<=8-sequence; row++) {
					if (sequence == 1) {
						if (me == board[row][col]) {
							pointsMe++;
						}
						else if (opposition == board[row][col]) {
							pointsMe--;
						}
					}
					else if (sequence==2) {
						if (me == board[row][col] && me == board[row+1][col]) {
							pointsMe+= 400;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col]) {
							pointsMe-= 400;
						}
					}
					else if (sequence==3) {
						if (me == board[row][col] && me == board[row+1][col] && me == board[row+2][col]) {
							pointsMe+= 12000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col] && opposition == board[row+2][col]) {
							pointsMe-= 12000;
						}
					}
					else if (sequence==4) {
						if (me == board[row][col] && me == board[row+1][col] && me == board[row+2][col] && me == board[row+3][col]) {
							//check if both sides are open e.g. -XXXX- or -XXXX0, 0XXXX-(one side) or
							// 0XXXX0 (closed) or XXXXO, 0XXXX (blocked by O and grid) for 'me' and
							//opposing player
							if((row+4)<8) { //check grid border
								if(null == board[row+4][col]) {
									if ((row -1) >=0){ //check grid border
										if (null == board[row-1][col]) {
											pointsMe+= 3000000; //both sides open, very high score
											}
										else
											pointsMe+=400000; //only one side open
									} else
										pointsMe+= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0){ //check grid border
										if (null == board[row-1][col])
											pointsMe+= 400000; //only one side open
										else
											pointsMe+=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe+= 0; //no way to win, blocked on both sides
								}
							//check the other end, if there's no cell after the sequence
							} else if((row-1)>=0) { //check grid border
								if (null == board[row-1][col]) {
									pointsMe+= 400000; //only one side open
								} else
									pointsMe+= 0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe+= 0;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col] && opposition == board[row+2][col] && opposition == board[row+3][col]) {
							//same thing, check if both sides are open for opposing player
							if((row+4)<8) { //check grid border
								if(null == board[row+4][col]) {
									if ((row -1) >=0){ //check grid border
										if (null == board[row-1][col])
											pointsMe-= 3000000; //both sides open, very high(low) score
										else
											pointsMe-=400000; //only one side open
									} else
										pointsMe-= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0){ //check grid border
										if (null == board[row-1][col])
											pointsMe-= 400000; //only one side open
										else
											pointsMe-=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe-= 0; //no way to win, blocked on both sides
								}
							//check the other end if there's no cell after the sequence
							} else if((row-1)>=0) { //check grid border
								if (null == board[row-1][col]) {
									pointsMe-= 400000;
								} else
									pointsMe-= 0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe-= 0;
						}
					}
					else if (sequence==5) {
						if (me == board[row][col] && me == board[row+1][col] && me == board[row+2][col] && me == board[row+3][col] && me == board[row+4][col]) {
							pointsMe+= 11000000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col] && opposition == board[row+2][col] && opposition == board[row+3][col] && opposition == board[row+4][col]) {
							pointsMe-= 11000000;
						}
					}
				}
			} //end rows

			//analyse columns
			for(int row=0; row<8;row++) {
				for(int col = 0; col<=8-sequence; col++) { //start pos
					
					if (sequence==2) {
						if (me == board[row][col] && me == board[row][col+1]) {
							pointsMe+= 400;
						}
						else if (opposition == board[row][col] && opposition == board[row][col+1]) {
							pointsMe-= 400;
						}
					}
					else if (sequence==3) {
						if (me == board[row][col] && me == board[row][col+1] && me == board[row][col+2]) {
							pointsMe+= 12000;
						}
						else if (opposition == board[row][col] && opposition == board[row][col+1] && opposition == board[row][col+2]) {
							pointsMe-= 12000;
						}
					}
					else if (sequence==4) {
						if (me == board[row][col] && me == board[row][col+1] && me == board[row][col+2] && me == board[row][col+3]) {
							//check if both sides are open e.g. -XXXX- or -XXXX0, 0XXXX-(one side) or
							// 0XXXX0 (closed) or XXXXO, 0XXXX (blocked by O and grid) for 'me' and
							//opposing player
							if((col+4)<8) { //check grid border
								if(null == board[row][col+4]) {
									if ((col -1) >=0){ //check grid border
										if (null == board[row][col-1])
											pointsMe+= 3000000; //both sides open, very high score
										else
											pointsMe+=400000; //only one side open
									} else
										pointsMe+= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((col -1) >=0){ //check grid border
										if (null == board[row][col-1])
											pointsMe+= 400000;  //only one side open
										else
											pointsMe+=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe+= 0; //no way to win, blocked on both sides
								}
							//check the other end if there's no cell after the sequence
							} else if((col-1)>=0) { //check grid border
								if (null == board[row][col-1]) {
									pointsMe+= 400000; //only one side open
								} else
									pointsMe+= 0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe+= 0;
						}
						else if (opposition == board[row][col] && opposition == board[row][col+1] && opposition == board[row][col+2] && opposition == board[row][col+3]) {
							//same thing, check if both sides are open for opposing player
							if((col+4)<8) { //check grid border
								if(null == board[row][col+4]) {
									if ((col -1) >=0){ //check grid border
										if (null == board[row][col-1])
											pointsMe-= 3000000; //both sides open, very high(low) score
										else
											pointsMe-=400000; //only one side open
									} else
										pointsMe-= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((col -1) >=0){
										if (null == board[row][col-1])
											pointsMe-= 400000; //only one side open
										else
											pointsMe-=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe-= 0; //no way to win, blocked on both sides
								}
							//check the other end if there's no cell after the sequence
							} else if((col-1)>=0) {
								if (null == board[row][col-1]) {
									pointsMe-= 400000; //only one side open
								} else
									pointsMe-= 0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe-= 0;
						}
					}
					else if (sequence==5) {
						if (me == board[row][col] && me == board[row][col+1] && me == board[row][col+2] && me == board[row][col+3] && me == board[row][col+4]) {
							pointsMe+= 11000000;
						}
						else if (opposition == board[row][col] && opposition == board[row][col+1] && opposition == board[row][col+2] && opposition == board[row][col+3] && opposition == board[row][col+4]) {
							pointsMe-= 11000000;
						}
					}
				}
			} //end columns 
		

			//diagonals, left ones
			for(int row=0; row<=8-sequence;row++) {
				for(int col = 0; col<=8-sequence; col++) { //start pos
					if(sequence==2) {
						if(me == board[row][col] && me == board[row+1][col+1]) {
							pointsMe +=400;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col+1]) {
							pointsMe-= 400;
						}

					}
					else if(sequence ==3) {
						if(me == board[row][col] && me == board[row+1][col+1] && me == board[row+2][col+2]) {
							pointsMe +=12000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col+1] && opposition == board[row+2][col+2]) {
							pointsMe-= 12000;
						}
					}
					else if(sequence ==4) {
						if(me == board[row][col] && me == board[row+1][col+1] && me == board[row+2][col+2] && me == board[row+3][col+3]) {
							//check if both sides are open e.g. -XXXX- or -XXXX0, 0XXXX-(one side) or
							// 0XXXX0 (closed) or XXXXO, 0XXXX (blocked by O and grid) for 'me' and
							//opposing player
							if((row+4)<(8-sequence) && (col+4)<(8-sequence)) {
								if(null == board[row+4][col+4]) {
									if ((row -1) >=0 && (col-1) >=0){
										if (null == board[row-1][col-1]) {
											pointsMe+= 3000000; //both sides open, very high score
										} else
											pointsMe+=400000; //only one side open
									} else
										pointsMe+= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0 && (col-1) >=0){
										if (null == board[row-1][col-1]) {
											pointsMe+= 400000; //one side open
										} else
											pointsMe+=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe+= 0; //no way to win, blocked on both sides, edge on one side
								}
							//check the other end if there's no cell after the current sequence
							} else if((row -1) >=0 && (col-1) >=0)  { 
								if (null == board[row-1][col-1]) {
									pointsMe+= 400000; //only one side open
								} else
									pointsMe+=0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe+= 0;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col+1] && opposition == board[row+2][col+2] && opposition == board[row+3][col+3]) {
							//same thing, check if both sides are open for opposing player
							if((row+4)<(8-sequence) && (col+4)<(8-sequence)) {
								if(null == board[row+4][col+4]) {
									if ((row -1) >=0 && (col-1) >=0){
										if (null == board[row-1][col-1]) {
											pointsMe-= 3000000; //both sides open, very high(low) score
										} else
											pointsMe-=400000; //only one side open
									} else
										pointsMe-= 400000;
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0 && (col-1) >=0){
										if (null == board[row-1][col-1]) {
											pointsMe-= 400000; //one side open
										} else
											pointsMe-=0; //mixture, no way to win, blocked on both sides
									} else
										pointsMe-= 0; //no way to win, blocked on both sides, edge on one side
								}
							//check the other end if there's no cell after the current sequence
							} else if((row -1) >=0 && (col-1) >=0)  { 
								if (null == board[row-1][col-1]) {
									pointsMe-= 400000; //only one side open
								} else
									pointsMe-=0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe-= 0;
						}
					}
					else if(sequence ==5) {
						if(me == board[row][col] && me == board[row+1][col+1] && me == board[row+2][col+2] && me == board[row+3][col+3] && me == board[row+4][col+4]) {
							pointsMe +=11000000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col+1] && opposition == board[row+2][col+2] && opposition == board[row+3][col+3] && opposition == board[row+4][col+4]) {
							pointsMe-= 11000000;
						}
					}
				}
			} //end diagonals from top left side

			//diagonals, right ones
			for(int row=0; row<=8-sequence;row++) {
				for(int col = 7; col>=sequence-1; col--) { //start pos
					if(sequence==2) {
						if(me == board[row][col] && me == board[row+1][col-1]) {
							pointsMe +=400;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col-1]) {
							pointsMe-= 400;
						}

					}
					else if(sequence ==3) {
						if(me == board[row][col] && me == board[row+1][col-1] && me == board[row+2][col-2]) {
							pointsMe +=12000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col-1] && opposition == board[row+2][col-2]) {
							pointsMe-= 12000;
						}
					}
					else if(sequence ==4) {
						if(me == board[row][col] && me == board[row+1][col-1] && me == board[row+2][col-2] && me == board[row+3][col-3]) {
							//check if both sides are open e.g. -XXXX- or -XXXX0, 0XXXX-(one side) or
							// 0XXXX0 (closed) or XXXXO, 0XXXX (blocked by O and grid) for 'me' and
							//opposing player
							if((row+4)<=(8-sequence) && (col-4)>=(sequence-1)) {
								if(null == board[row+4][col-4]) {
									if ((row -1) >=0 && (col+1) <= 7){
										if (null == board[row-1][col+1])
											pointsMe+= 3000000; //both sides open, very high score
										else
											pointsMe+=400000; //only one side open
									} else
										pointsMe+= 400000; //only one side open
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0 && (col+1) <= 7){
										if (null == board[row-1][col+1])
											pointsMe+= 400000; //one side open
										else
											pointsMe+=0; ////mixture, no way to win, blocked on both sides
									} else
										pointsMe+=0; //edge on one side, no way to win
								}
							//check the other end if there's no cell AFTER the current sequence
							} else if ((row -1) >=0 && (col+1) <= 7){
								if (null == board[row-1][col+1])
										pointsMe+= 400000; //only one side open
								else
									pointsMe+=0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe+= 0; 
									
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col-1] && opposition == board[row+2][col-2] && opposition == board[row+3][col-3]) {
							//same thing, check if both sides are open for opposing player
							if((row+4)<=(8-sequence) && (col-4)>=(sequence-1)) {
								if(null == board[row+4][col-4]) {
									if ((row -1) >=0 && (col+1) <= 7){
										if (null == board[row-1][col+1])
											pointsMe-= 3000000; //both sides open, very high(low) score
										else
											pointsMe-=400000;
									} else
										pointsMe-= 400000;
								} else { //check if I am blocked or have one open side 
									if ((row -1) >=0 && (col+1) <= 7){
										if (null == board[row-1][col+1])
											pointsMe-= 400000; //one side open
										else
											pointsMe-=0; ////mixture, no way to win, blocked on both sides
									} else
										pointsMe-=0; //edge on one side, no way to win
								}
							//check the other end if there's no cell AFTER the current sequence
							} else if ((row -1) >=0 && (col+1) <= 7){
								if (null == board[row-1][col+1])
										pointsMe-= 400000;
								else
									pointsMe-=0; //mixture, no way to win, blocked on both sides
							} else
								pointsMe-= 0;
						}
					}
					else if(sequence ==5) {
						if(me == board[row][col] && me == board[row+1][col-1] && me == board[row+2][col-2] && me == board[row+3][col-3] && me == board[row+4][col-4]) {
							pointsMe +=11000000;
						}
						else if (opposition == board[row][col] && opposition == board[row+1][col-1] && opposition == board[row+2][col-2] && opposition == board[row+3][col-3] && opposition == board[row+4][col-4]) {
							pointsMe-= 11000000;
						}
					}
				}
			} //end diagonals from top right side
		} //end sequence 1-5

		
		return pointsMe; //returns number of total points on the board for the given state

	} //end evaluateState

	//if game over check / compare to winning sequences, returns 10,000, -10,000 OR 0 
	//The numbers are arbitrary
	private int isGameOver(Color[][] board, Color me) {
		Color opp;
		if (me == Color.BLACK)
			opp = Color.WHITE;
		else
			opp = Color.BLACK;

		//colums
		for (int row = 0; row<8;row++) {
			for (int col=0; col<4;col++ ) {
				//cannot win when col number is 4 or more in grid
 				if(board[row][col]== me && board[row][col+1]== me && board[row][col+2]== me && board[row][col+3]== me && board[row][col+4]== me)
					return 10000;
				if(board[row][col]== opp && board[row][col+1]== opp && board[row][col+2]== opp && board[row][col+3]== opp && board[row][col+4]== opp)
					return -10000;
			}
		}
		//rows
		for (int col = 0; col<8;col++) {
			for (int row=0; row<4;row++ ) {
				//cannot win when row number is 4 or more in grid
 				if(board[row][col]== me && board[row+1][col]== me && board[row+2][col]== me && board[row+3][col]== me && board[row+4][col]== me)
					return 10000;
				if(board[row][col]== opp && board[row+1][col]== opp && board[row+2][col]== opp && board[row+3][col]== opp && board[row+4][col]== opp)
					return -10000;
			}
		}
		//diagonals from left
		//main diagonal
		for(int start = 0; start<=3; start++) {
			for(int row=start, col=start; col<8; row++,col++) {
				if((col+4)>=8)
					break;
				else {
					//main diagonal from top left to right
					if (board[row][col] == me && board[row+1][col+1] == me && board[row+2][col+2] == me && board[row+3][col+3] == me && board[row+4][col+4] == me) {
						return 10000;
					}
					//opposition
					if (board[row][col] == opp && board[row+1][col+1] == opp && board[row+2][col+2] == opp && board[row+3][col+3] == opp && board[row+4][col+4] == opp) {
						return -10000;
					}
				}
			}
		}
		///check for diagonals under main starting from left
		for(int row = 0; row<=2; row++) {
			for(int col=row+1; col<8; col++) {
				if((col+4)>=8)
					break;
				else {
					if (board[row][col] == me && board[row+1][col+1] == me && board[row+2][col+2] == me && board[row+3][col+3] == me && board[row+4][col+4] == me) {
						return 10000;
					}
					//opposition
					if (board[row][col] == opp && board[row+1][col+1] == opp && board[row+2][col+2] == opp && board[row+3][col+3] == opp && board[row+4][col+4] == opp) {
						return -10000;
					}
				}
			}
		}
		///check for diagonals over main sarting from left
		for(int col = 0; col<=2; col++) {
			for(int row=col+1; row<8; row++) {
				if((row+4)>=8)
					break;
				else {
					if (board[row][col] == me && board[row+1][col+1] == me && board[row+2][col+2] == me && board[row+3][col+3] == me && board[row+4][col+4] == me) {
						return 10000;
					}
					//opposition
					if (board[row][col] == opp && board[row+1][col+1] == opp && board[row+2][col+2] == opp && board[row+3][col+3] == opp && board[row+4][col+4] == opp) {
						return -10000;
					}
				}
			}
		}

		//check for ALL diagonals from right
		int sequence =5;
		for(int row=0; row<=8-sequence;row++) {
			for(int col = 7; col>=sequence-1; col--) { //start pos
				if(me == board[row][col] && me == board[row+1][col-1] && me == board[row+2][col-2] && me == board[row+3][col-3] && me == board[row+4][col-4]) {
					return 10000;
				}
				else if (opp == board[row][col] && opp == board[row+1][col-1] && opp == board[row+2][col-2] && opp == board[row+3][col-3] && opp == board[row+4][col-4]) {
					return -10000;
				}
			}
		}
		return 0; //no win so far
		
	} //end isGameOver check
}
