package me.bejosch.battleprogress.server.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Enum.PlayerRanking;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Handler.ProfileHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Main.ServerConnection;

public class PlayerProfile {

	private ClientConnectionThread connection = null;
	
	private long onlineTimestamp = -1;
	
	private int id = 0;
	private String name = null;
	private String password = null;
	private String date = null; //DATUM DER REGISTRIERUNG
	private int level;
	private int XP;
	private int profileImageNumber;
	private int backgroundImageNumber;
	private int nameColorNumber;
	private int statusNumber;
	private PlayerRanking ranking;
	private int rankingPoints;
	private String currentActivity = "Online";
	
	private boolean friendlistLoad;
	
	private boolean markedForDisconnect = false;
	
	private List<PlayerProfile> friendlist = new ArrayList<PlayerProfile>();
	
	//MANUELL
	public PlayerProfile(ClientConnectionThread connection_, int id_, String name_, String password_, String date_, int level_, int XP_, int profileImageNumber, int backgroundImageNumber, int nameColorNumber, int statusNumber, PlayerRanking ranking, int rankingPoints, boolean friendlistLoad_) {
		
		this.connection = connection_;
		
		this.id = id_;
		this.name = name_;
		this.password = password_;
		this.date = date_;
		this.level = level_;
		this.XP = XP_;
		this.profileImageNumber = profileImageNumber;
		this.backgroundImageNumber = backgroundImageNumber;
		this.nameColorNumber = nameColorNumber;
		this.statusNumber = statusNumber;
		this.ranking = ranking;
		this.rankingPoints = rankingPoints;
		
		this.friendlistLoad = friendlistLoad_;
		
		if(friendlistLoad_ == false) {
			//GETTING ONLINE
			this.loadFriendlist();
			this.onlineTimestamp = System.currentTimeMillis();
			ProfileHandler.profileConnects(this);
		}else {
			this.currentActivity = "Offline";
		}
		
	}
	//AUTO LOAD
	public PlayerProfile(ClientConnectionThread connection_, String name_, boolean friendlistLoad_) {
		
		this.connection = connection_;
		
		this.id = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ID", "Name", name_);
		this.name = name_;
		this.date = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Datum", "Name", name_);
		this.level = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "Level", "Name", name_);
		this.XP = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "XP", "Name", name_);
		this.profileImageNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ProfileImageNumber", "Name", name_);
		this.backgroundImageNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "BackgroundImageNumber", "Name", name_);
		this.nameColorNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "NameColorNumber", "Name", name_);
		this.statusNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "StatusNumber", "Name", name_);
		this.ranking = PlayerRanking.valueOf(DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Ranking", "Name", name_));
		this.rankingPoints = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "RankingPoints", "Name", name_);
		
		this.friendlistLoad = friendlistLoad_;
		
		if(friendlistLoad_ == false) {
			//GETTING ONLINE
			this.loadFriendlist();
			this.onlineTimestamp = System.currentTimeMillis();
			this.password = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Password", "Name", name_);
			ProfileHandler.profileConnects(this);
		}else {
			this.currentActivity = "Offline";
		}
		
	}
	public PlayerProfile(ClientConnectionThread connection_, int id_, boolean friendlistLoad_) {
		
		this.connection = connection_;
		
		this.id = id_;
		this.name = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Name", "ID", ""+id);
		this.date = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Datum", "ID", ""+id);
		this.level = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "Level", "ID", ""+id);
		this.XP = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "XP", "ID", ""+id);
		this.profileImageNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ProfileImageNumber", "ID", ""+id);
		this.backgroundImageNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "BackgroundImageNumber", "ID", ""+id);
		this.nameColorNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "NameColorNumber", "ID", ""+id);
		this.statusNumber = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "StatusNumber", "ID", ""+id);
		this.ranking = PlayerRanking.valueOf(DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Ranking", "ID", ""+id));
		this.rankingPoints = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "RankingPoints", "ID", ""+id);
		
		this.friendlistLoad = friendlistLoad_;
		
		if(friendlistLoad_ == false) {
			//GETTING ONLINE
			this.loadFriendlist();
			this.onlineTimestamp = System.currentTimeMillis();
			this.password = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Password", "ID", ""+id);
			ProfileHandler.profileConnects(this);
		}else {
			this.currentActivity = "Offline";
		}
		
	}
	
	//FRIEND LIST LOAD
	public void loadFriendlist() {
		
		friendlist.clear();
		
		try {
			
			String query = "SELECT FriendID FROM "+DatabaseData.tabellName_friendlist+" WHERE ID="+this.getId();
			Statement stmt = DatabaseData.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(query);
			
			rs.first();
			try {
				do {
					int friendID = rs.getInt("FriendID");
					PlayerProfile friendProfile = ProfileHandler.getPlayerProfileByID(friendID);
					if(friendProfile == null) {
						//OFFLINE
						friendProfile = new PlayerProfile(null, friendID, true);
					}
					this.friendlist.add(friendProfile);
				}while(rs.next());
			}catch(SQLException error) {
				//EMPTY RESULT SET
			}
			
		}catch(SQLException error) {
			error.printStackTrace();
		}
		
	}
	
	//CONVERT TO STRING (TODO NEEDS MORE DATA AS DATE ETC.)
	public String convertThisPlayerProfileToString() {
		//ID ; Name ; Level ; XP ; Online Min (-1 wenn offline) ; SN ; PIN ; BIN ; NCN ; SN ; RANKING ; RP ; currentActivity
		String data = this.getId()+";"+this.getName()+";"+this.getLevel()+";"+this.getXP()+";"+this.getOnlineTimeInMin()+";"+this.getProfileImageNumber()+";"+this.getBackgroundImageNumber()+";"+this.getNameColorNumber()+";"+this.getStatusNumber()+";"+this.getRanking()+";"+this.getRankingPoints()+";"+this.currentActivity;
		return data;
	}
	
	
	
	//CHANGE STUFF IN DB AND HERE SYNC
	public void changeName(String newName) {
		
		if(this.friendlistLoad == true) {
			ConsoleOutput.printMessageInConsole("PlayerProfile change name was blocked because its only friendListLoaded!", true);
			return;
		}
		DatabaseHandler.updateString(DatabaseData.tabellName_profile, "Name", newName, "ID", ""+this.id);
		this.name = newName;
		
	}
	public void changePassword(String newPassword) {
		
		if(this.friendlistLoad == true) {
			ConsoleOutput.printMessageInConsole("PlayerProfile change password was blocked because its only friendListLoaded!", true);
			return;
		}
		DatabaseHandler.updateString(DatabaseData.tabellName_profile, "Password", newPassword, "ID", ""+this.id);
		this.password = newPassword;
		
	}
	
	public void setCurrentActivity(String currentActivity) {
		this.currentActivity = currentActivity;
		for(PlayerProfile profile : this.friendlist) {
			if(profile.getOnlineTimeInMin() != -1) {
				//IS ONLINE
				profile.getConnection().sendData(126, ServerConnection.getNewPacketId(), currentActivity);
			}
		}
	}

	public void setOnlineTimestamp(long onlineTimestamp) {
		this.onlineTimestamp = onlineTimestamp;
	}
	public void setConnection(ClientConnectionThread connection) {
		this.connection = connection;
	}
	
	public void setMarkedForDisconnect(boolean markedForDisconnect) {
		this.markedForDisconnect = markedForDisconnect;
	}
	
	//GET
	public ClientConnectionThread getConnection() {
		return connection;
	}
	
	public long getOnlineTimeInMin() {
		if(this.onlineTimestamp == -1) {
			return -1;
		}else {
			return (System.currentTimeMillis()-this.onlineTimestamp)/1000/60;
		}
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getDate() {
		return date;
	}
	public String getPassword() {
		return password;
	}
	public int getLevel() {
		return level;
	}
	public int getXP() {
		return XP;
	}
	public int getProfileImageNumber() {
		return profileImageNumber;
	}
	public int getBackgroundImageNumber() {
		return backgroundImageNumber;
	}
	public int getNameColorNumber() {
		return nameColorNumber;
	}
	public int getStatusNumber() {
		return statusNumber;
	}
	public PlayerRanking getRanking() {
		return ranking;
	}
	public int getRankingPoints() {
		return rankingPoints;
	}
	public String getCurrentActivity() {
		return currentActivity;
	}
	
	public boolean isFriendlistLoaded() {
		return friendlistLoad;
	}
	public boolean isMarkedForDisconnect() {
		return markedForDisconnect;
	}
	
	public List<PlayerProfile> getFriendlist() {
		return friendlist;
	}
	
	
}
