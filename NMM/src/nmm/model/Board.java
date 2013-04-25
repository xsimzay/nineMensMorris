package nmm.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JOptionPane;

import nmm.model.user.Player;
import nmm.view.MainWindow;

public class Board {
	
	// Class variables
	private ArrayList<Location> location_list;
	private ArrayList<Edge> edge_list;
	private GamePiece[][] boardArray;
	private MainWindow mw;
	private int current_phase;
	private boolean cheatMode;
	private boolean ignoreMessages;

	// Game phases
	public static final int GAMEOVER_PHASE = -1;
	public static final int PLACEMENT_PHASE = 0;
	public static final int MOVEMENT_PHASE = 1;
	public static final int REMOVAL_PHASE = 2;
	
	// Number to letter array.
	public static final char[] ALPHABET = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X'};
	public static final String[] BOARDREFERENCE = {"0,0","0,3","0,6","1,1","1,3","1,5","2,2","2,3","2,4","3,0","3,1","3,2","3,4","3,5","3,6","4,2","4,3","4,4","5,1","5,3","5,5","6,0","6,3","6,6"};
	
	/***
	 * This is a copy constructor
	 * which should return make
	 * a copy of the passed in Board
	 * object
	 * 
	 * Needs fixed as equalizing the
	 * lists does not change the reference
	 * @param bd
	 */
	public Board(Board bd){
		this.location_list = new ArrayList<Location>();
		this.edge_list = new ArrayList<Edge>();
		this.boardArray = new GamePiece[7][7];
		this.current_phase = bd.current_phase;
		this.cheatMode = bd.cheatMode;
		this.mw = bd.mw;
		
		copyLocList(bd);
		copyEdgList(bd);
		copyBoardAr(bd);
	}
	
	private void copyBoardAr(Board bd) {
		for(int i=0; i<7; i++){
			for(int j=0; j<7; j++){
				this.boardArray[i][j] = new GamePiece(bd.boardArray[i][j]);
			}
		}
	}

	private void copyEdgList(Board bd) {
		for(Location l: bd.location_list){
			this.location_list.add(new Location(l));
		}
	}

	private void copyLocList(Board bd) {
		for(Edge e: bd.edge_list){
			this.edge_list.add(new Edge(e));
		}
	}

	/***
	 * The normal constructor for the
	 * Board class.  The MainWindow param
	 * is used to send updates to the user
	 * from inside this class
	 * @param mw
	 */
	public Board(MainWindow mw){
		// Load the edge and location list from file
		location_list = new ArrayList<Location>();
		edge_list = new ArrayList<Edge>();
		this.LoadBoard();
		
		// Sort the location list for printing the visual.
		Collections.sort(location_list);
		
		this.current_phase = PLACEMENT_PHASE;
		this.boardArray = newBoard();
		this.cheatMode = false;
		this.mw = mw;
	}
	
	/***
	 * This function will update the board
	 * with the current pieces on it
	 * 
	 * As long as they are not null it will
	 * update with the current locations piece
	 */
	public void updateBoard(){
		for(int i=0; i<24; i++){
			String t[] = BOARDREFERENCE[i].split(",");
			int row = Integer.parseInt(t[0]);
			int col = Integer.parseInt(t[1]);
			String label = String.valueOf(ALPHABET[i]);
			Location loc = this.GetLocationByLabel(label);
			if(loc.getPiece() != null)
				this.boardArray[row][col] = loc.getPiece();
			else
				this.boardArray[row][col] = new GamePiece(new Player("", "red").getColor(), new Player("", "red"), -1);
		}
	}
	
	/***
	 * Will return the current 
	 * state of the board Array
	 * @return
	 */
	public GamePiece[][] getBoardArray() {
		return this.boardArray;
	}
	

	/***
	 * This function will determine the 
	 * current phase of the game
	 * 
	 * If it is still in placement, removal,
	 * endGame of simply currentPhase
	 *
	 * @param curplayer
	 * @return
	 */
	public int GetCurrentPhase(Player curplayer) {
		if(!curplayer.isHuman()){
			this.ignoreMessages = true;
		}
		else
			this.ignoreMessages = false;
		if (curplayer.getPiecesPlayed() < 9)
			return PLACEMENT_PHASE;

		// See if phase has been set to REMOVAL
		else if (current_phase == REMOVAL_PHASE || current_phase == GAMEOVER_PHASE){
			return current_phase;
		}
		
		// Only other option is movement phase.
		else
			return MOVEMENT_PHASE;
	}
	
	
	/***
	 * This function will turn on
	 * cheat mode for the users
	 */
	public void setCheatMode(){
		this.cheatMode = true;
	}
	
	/***
	 * This function will set the phase
	 * of the game to the passed in phase
	 * @param current_phase
	 */
	public void SetCurrentPhase(int current_phase) {
		this.current_phase = current_phase;
	}
	
	/***
	 * This function will return a location
	 * object with the given label
	 * @param label
	 * @return
	 */
	public Location GetLocationByLabel(String label)
	{
		for(int i = 0; i < location_list.size(); i++)
			if (location_list.get(i).getLabel().equals(label))
				return location_list.get(i);
				
		return null;
	}
	
	/***
	 * This method created an initial board for
	 * the GUI to use.  The place-able Pieces are
	 * initialized to a new piece with an ID of
	 * -1 so they are not drawn on the board.
	 * 
	 * Invalid places on the board (along the lines
	 * in empty spaces) are set to null
	 * @return
	 */
	private GamePiece[][] newBoard() {
		
		
		GamePiece[][] bd = new GamePiece[7][7];
		for(int i=0; i<7; i++)
			for(int j=0; j<7; j++){
				Player p = new Player("", "red");
				bd[i][j] = new GamePiece(p.getColor(), p, -1);
			}
		ArrayList<ArrayList<Integer>> notPlaceableSpots = validSpots();
		int k = 0;
		for(ArrayList<Integer> i: notPlaceableSpots){
			for(Integer j: i){
				bd[k][j] = null;
			}
			k++;
		}
		
		return bd;
	}
	
	/***
	 * This is a hard coded function to set
	 * the invalid spots of the game Board.
	 * 
	 * The spots correlate to lines on the board
	 * and empty spots where a place can not be placed
	 * @return
	 */
	private ArrayList<ArrayList<Integer>> validSpots() {
		ArrayList<ArrayList<Integer>> p = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> row0 = new ArrayList<Integer>(Arrays.asList(1,2,4,5));
		ArrayList<Integer> row1 = new ArrayList<Integer>(Arrays.asList(0,2,4,6));
		ArrayList<Integer> row2 = new ArrayList<Integer>(Arrays.asList(0,1,5,6));
		ArrayList<Integer> row3 = new ArrayList<Integer>(Arrays.asList(3));
		ArrayList<Integer> row4 = new ArrayList<Integer>(Arrays.asList(0,1,5,6));
		ArrayList<Integer> row5 = new ArrayList<Integer>(Arrays.asList(0,2,4,6));
		ArrayList<Integer> row6 = new ArrayList<Integer>(Arrays.asList(1,2,4,5));
		
		p.add(row0);
		p.add(row1);
		p.add(row2);
		p.add(row3);
		p.add(row4);
		p.add(row5);
		p.add(row6);
		return p;
	}
	
	/**************************************
	 * Gameplay Related Methods
	 **************************************/
	public boolean newMessageDialog(String error, int delay){
		if(this.ignoreMessages && delay != 5000)
			return false;
		this.mw.getGameBoard().setError(error, delay);
		this.mw.getGameBoard().startTimer();
		return false;
	}
	
	/***
	 * This function will attempt
	 * to place a piece on the board
	 * 
	 * As long as the space is not occupied
	 * and the piece is valid it will return true
	 * @param player
	 * @param pieceID
	 * @param locLabel
	 * @return
	 */
	public boolean PlacePiece(Player player, int pieceID, String locLabel)
	{
		GamePiece curPiece = player.getPiece(pieceID);
		Location newLoc = GetLocationByLabel(locLabel);
		
		if (curPiece == null || newLoc == null)
		{
			return newMessageDialog("Invalid piece id or location label", 1500);
		}

		// Make sure piece selection isn't placed already.
		if (curPiece.getStatus() != GamePiece.UNPLACED)
		{
			return newMessageDialog("Invalid Piece - It is already placed or dead", 1500);
		}

				
		// Make sure the location is empty
		if (!newLoc.ContainsPiece(null))
		{
			return newMessageDialog("There is a piece there already", 1500);
		}
		
		// We're ok to place the piece.
		newLoc.setPiece(curPiece);
		curPiece.setStatus(GamePiece.PLACED);
		return true;

	}

	/***
	 * This function will attempt to
	 * move a piece on the board
	 * 
	 * As long as the space is not occupied
	 * and the piece is the current users and
	 * is valid it will return true
	 * @param player
	 * @param pieceID
	 * @param locLabel
	 * @return
	 */
	public boolean MovePiece(Player player, int pieceID, String locLabel)
	{
		GamePiece curPiece = player.getPiece(pieceID);
		Location curLoc = GetPieceLocation(curPiece);
		Location newLoc = GetLocationByLabel(locLabel);
		
		if (curPiece == null || newLoc == null)
		{
			return newMessageDialog("Invalid piece id or location label", 1500);
		}
		
		// Detect if we are in fly mode. If not, make sure we're adjacent.
		// Make sure the locations are neighbors.
		if (!this.cheatMode && player.getScore() > 3 && !AreNeighbors(curLoc, newLoc))
		{
			return newMessageDialog("That spot is not adjacent", 1500);
		}
		
		// Make sure the location is empty
		if (!newLoc.ContainsPiece(null))
		{
			return newMessageDialog("There is a piece there already", 1500);
		}
		
		// We're ok to move the piece.
		curPiece.setStatus(GamePiece.MOVED);
		curPiece.setMv(new Movement(curLoc, newLoc));
		curPiece.setMoving(true);
		
		newLoc.setPiece(curPiece);
		curLoc.setPiece(null);
		
		// Check for a created mill.
		if (IsMill(newLoc))
		{
			// We will return false so current player is not nexted.
			// Set current phase to removal phase.
			String s = "%s has created a mill!";
			s = String.format(s, player.getName());
			this.newMessageDialog(s, 5000);
			//JOptionPane.showMessageDialog(this.mw, s);
			this.SetCurrentPhase(REMOVAL_PHASE);
			return false;
		}
		
		return true;

	}

	/***
	 * This function will attempt to
	 * remove a piece from the board
	 * 
	 * As long as their is a piece at
	 * that location, and it is the opponents
	 * it will return true
	 * @param player
	 * @param pieceID
	 * @return
	 */
	public boolean RemovePiece(Player player, int pieceID, boolean valid)
	{
		GamePiece curPiece = player.getPiece(pieceID);
		Location curLoc = GetPieceLocation(curPiece);
		
		if(!valid)
			return newMessageDialog("You cannot remove your own piece", 1500);

		
		if (curPiece == null)
		{
			return newMessageDialog("Invalid Piece ID - Piece not found", 1500);
		}
			
		if (!curPiece.inPlay())
		{
			return newMessageDialog("Invalid Piece - It is not placed or alive/in play", 1500);

		}
		
		if (player.getScore() > 3 && IsMill(curLoc))
		{
			return newMessageDialog("You cannot remove a member of a mill", 1500);

		}
		
		
		curLoc.setPiece(null);
		curPiece.setStatus(GamePiece.DEAD);
		// Grab the player's new calculated score.
		int score = player.getScore();
		
		if (score == 3 && !this.cheatMode){
			String s = "%s has engaged fly mode with 3 pieces remaining";
			s = String.format(s, player.getName());
			JOptionPane.showMessageDialog(this.mw, s);
		}
		
		// See if the player has less than 3 pieces remaining, that leads to gameover phase.
		// if not set the phase to movement phase.
		if(score < 3)
		{
			SetCurrentPhase(GAMEOVER_PHASE);
			// We will return false so we crown the correct victor 
			// We would stay on this player otherwise, because this method was
			// passed the opposite player of the current player to remove his piece.
			return false;
		} else
			SetCurrentPhase(MOVEMENT_PHASE);
		
		return true;

	}
	
	/***
	 * This function will determine if a mill is
	 * created from Location passed into it
	 * @param loc
	 * @return
	 */
	public boolean IsMill(Location loc)
	{
		int vertCount = CountAdjacent(loc, 0);
		int horizCount = CountAdjacent(loc, 1);
		
		if (Math.max(vertCount, horizCount) > 2)
			return true;
		else
			return false;
	}
	
	/***
	 * This function will count
	 * the number of adjacent pieces
	 * to the passed in location
	 * @param loc
	 * @param dir
	 * @return
	 */
	public int CountAdjacent(Location loc, int dir)
	{
		Player owner = loc.getPiece().getOwner();
		int status1, status2;

		ArrayList<Location> nghbrs = SomeNeighbors(loc, dir, owner);
		if (nghbrs.size() == 2)
		{
			// Store the neighbors status's to make sure at least one has moved (req to become a mill)
			status1 = nghbrs.get(0).getPiece().getStatus();
			status2 = nghbrs.get(1).getPiece().getStatus();	

		} else if (nghbrs.size() == 1) {
			status1 = nghbrs.get(0).getPiece().getStatus();

			// See if the neighbor has another adjacent neighbor.
			nghbrs = SomeNeighbors(nghbrs.get(0), dir, owner);
			nghbrs.remove(loc);

			if(nghbrs.size() == 1)
			{
				status2 = nghbrs.get(0).getPiece().getStatus();
			} else
				return 0;
		} else
			return 0;

		// Make sure at least one piece of the mill has moved.
		if (status1 == GamePiece.MOVED || status2 == GamePiece.MOVED || loc.getPiece().getStatus() == GamePiece.MOVED)
			return 3;
		else
			return 0;
		
	}
	
	/***
	 * This function will determine
	 * if two locations are neighbors
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public boolean AreNeighbors(Location loc1, Location loc2)
	{
		ArrayList<Location> all_neighbors = AllNeighbors(loc1);
		if (all_neighbors.contains(loc2))
			return true;
		else
			return false;
	}
	
	/***
	 *  Returns locations with a defined direction and a owner that
	 *  are neighbors to the supplied location.
	 */
	public ArrayList<Location> SomeNeighbors(Location loc, int dir, Player owner)
	{
		ArrayList<Location> NeighborList = new ArrayList<Location>();
		Edge curEdge;
		Location oppLoc;

		// Find all edges that hold this location
		for (int i=0; i < edge_list.size(); i++)
		{
			curEdge = edge_list.get(i);
			oppLoc = curEdge.GetOpposite(loc);
	
			// If the edge has the location, add the adjacent location to the list.
			// Make sure it matches directional and player owned conditions.
			if (curEdge.HasLocation(loc) && curEdge.GetAlignment() == dir)
			{
				// If we want the owner to be null (aka no piece located there),
				// add the location if it holds no piece.
				if(owner == null && oppLoc.getPiece() == null)
					NeighborList.add(curEdge.GetOpposite(loc));
				// We want there to be a piece in the location owned by a particular owner.
				else if(oppLoc.getPiece() != null && oppLoc.getPiece().getOwner() == owner)
					NeighborList.add(curEdge.GetOpposite(loc));
	
			}
		}

		return NeighborList;
	}
	
	/***
	 * This function will return a list
	 * of all the neighbors to a given Location
	 * @param loc
	 * @return
	 */
	public ArrayList<Location> AllNeighbors(Location loc)
	{
		ArrayList<Location> NeighborList = new ArrayList<Location>();
		
		// Find all edges that hold this location
		for (int i=0; i < edge_list.size(); i++)
		{
			// If the edge has the location, add the adjacent location to the list.
			if (edge_list.get(i).HasLocation(loc))
				NeighborList.add(edge_list.get(i).GetOpposite(loc));
		}
		
		return NeighborList;
	}
	
	/***
	 * This function will return the location of the given piece
	 * or return null if not found
	 * @param piece
	 * @return
	 */
	public Location GetPieceLocation(GamePiece piece)
	{
		for (int i = 0; i < location_list.size(); i++)
			if (location_list.get(i).ContainsPiece(piece))
				return location_list.get(i);
		
		return null;
	}
	
	
	/**************************************
	 * Gameboard Creation Related Methods
	 **************************************/
	
	/***
	 * This function will add a location
	 * given the passed in label
	 * @param label
	 * @return
	 */
	private Location AddLocation(String label)
	{
		// First see if there is already a location with this label.
		Location newLocation = GetLocationByLabel(label);
		
		// If null, create a new one and add it to the list.
		if (newLocation == null)
		{
			newLocation = new Location(label);
			this.location_list.add(newLocation);
		}
		
		// Return either old location or the newly created one.
		return newLocation;
	}
	
	/***
	 * This function will add an
	 * edge between the two Locations,
	 * give it a label, and its alignment
	 * @param label
	 * @param loc1
	 * @param loc2
	 * @param align
	 */
	private void AddEdge(String label, Location loc1, Location loc2, int align)
	{
		Edge newEdge = new Edge(label, loc1, loc2, align);
		edge_list.add(newEdge);
	}
	
	/***
	 * This function will load the gameboard
	 * from a .txt file to create the
	 * location lists and edge lists
	 */
	private void LoadBoard()
	{
		BufferedReader br = null;
		String curLine = "";
		String tokens[], locations[];
		Location newLoc1, newLoc2;
		
		try 
		{
			br = new BufferedReader(new FileReader("resources\\board.txt"));
			
			// Load in each line of the file, creating new edges
			// and locations as they are found.
			// Input line format: <EdgeLabel>:<LocLabel1>,<LocLabel2>:<Orientation[0=vert;1=horiz]>
			while ( (curLine = br.readLine()) != null)
			{
				// Split edge label from location labels
				tokens = curLine.split(":");
				
				// Split the location elements apart.
				locations = tokens[1].split(",");
				
				// Get the new locations connected by the edge. 
				// AddLocation will reference current locs if existant already.
				newLoc1 = AddLocation(locations[0]);
				newLoc2 = AddLocation(locations[1]);
				
				AddEdge(tokens[0], newLoc1, newLoc2, Integer.parseInt(tokens[2]));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * This function will count
	 * the number of moves available for
	 * a given player
	 * @param player
	 * @return
	 */
	public int numMovesAvailable(Player player)
	{
		Location curloc;
		int count = 0;
		
		for(int i=0; i < player.getPieces().size(); i++)
		{
			curloc = GetPieceLocation(player.getPiece(i));
			// Is this piece in a gameboard location?
			if (curloc != null)
			{
				// Add to count the amount of neighbors that hold no pieces (null owner)
				count += SomeNeighbors(curloc, 0, null).size();
				count += SomeNeighbors(curloc, 1, null).size();
			}
		}
		
		return count;
	}

	public void setPiece(int r, int c, GamePiece gp) {
		if(gp == null)
			this.boardArray[r][c] = null;
		else
			this.boardArray[r][c] = gp;
	}

	public boolean isFlyMode(Player p) {
		int score = p.getScore();
		
		if (score == 3 || this.cheatMode){
			return true;
		}
		return false;
	}

}
