package me.bejosch.battleprogress.server.Data;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Objects.ServerGroup;

public class ServerGroupData {

	public static final int groupIdLength = 8;
	
	public static List<ServerGroup> activeGroups = new ArrayList<ServerGroup>();
	
}
