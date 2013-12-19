/**
  * Digital Ligter
  * Customer Driven Project - NTNU
  * 20th November  2013
  *
  * @author Jan Bednarik
  * @author Tomas Dohnalek
  * @author Milos Jovac
  * @author Agnethe Soraa
  */

package com.silentducks.digitallighterserver.network;

public class Protocol {

	public static final String MESSAGE_TYPE = "message_type";
	public static final String NEW_SERVICE_NAME = "new";
	public static final String COMMAND = "command";
	public static final int MESSAGE_TYPE_USER_ADDED = 1;
	public static final int MESSAGE_TYPE_SERVER_STARTED = 2;
	public static final int MESSAGE_TYPE_COMMAND = 3;
	public static final int MESSAGE_TYPE_NEW_SERVICE_FOUND = 4;

}
