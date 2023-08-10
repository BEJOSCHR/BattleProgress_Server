package me.bejosch.battleprogress.server.Data;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Objects.ServerQueue;

public class ServerQueueData {

	public static final int queueIdLength = 8;
	
	public static final int waitLevel_1 = 60*1, waitLevel_2 = 60*2, waitLevel_3 = 60*3; //IN SECONDS
	
	public static List<ServerQueue> waitingQueues_LV0 = new ArrayList<ServerQueue>();
	public static List<ServerQueue> waitingQueues_LV1 = new ArrayList<ServerQueue>();
	public static List<ServerQueue> waitingQueues_LV2 = new ArrayList<ServerQueue>();
	public static List<ServerQueue> waitingQueues_LV3 = new ArrayList<ServerQueue>();
	
	public static boolean matchingQueues = false;
	public static List<ServerQueue> initQueuesQueue = new ArrayList<ServerQueue>(); //WARTESCHLANGE DER QUEUES UM SICH GEGENSEITIG ZU MATCHEN
	
}
