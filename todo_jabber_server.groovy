#!/usr/bin/env groovy -cp lib/smack.jar:lib/smackx.jar

import java.io.*
import org.jivesoftware.smack.*
import org.jivesoftware.smack.packet.*
import org.jivesoftware.smack.filter.*

def connection = new XMPPConnection("jabber.org")
connection.connect()
connection.login("todobot123", "passwd")

presence = new Presence(Presence.Type.available)
presence.setStatus("Getting Things Done!")
connection.sendPacket(presence)

filter = new AndFilter();
filter.addFilter(new FromMatchesFilter("i.gouss@gmail.com"));
filter.addFilter(new PacketTypeFilter(Message.class));


class TodoPacketListener implements PacketListener {
	private XMPPConnection connection
	
	TodoPacketListener(XMPPConnection connection) {
		this.connection = connection
	}

	void processPacket(Packet packet) {
		try {
			def message = (Message) packet
			def program = ["/Users/i_gouss/bin/todo.py", "-p"]
			def args = message.getBody().split().toList();
			program.addAll(args)

			def proc = program.execute() 
			proc.waitFor()

			def reply = new Message(message.getFrom(), Message.Type.chat)
			reply.setFrom(message.getTo())
			reply.setThread(message.getThread())
			reply.setBody(proc.in.text)

			connection.sendPacket(reply)
		} catch(Exception ex) {
			println ex
		}
	}
}

connection.addPacketListener(new TodoPacketListener(connection), filter)

InputStreamReader converter = new InputStreamReader(System.in)
BufferedReader input = new BufferedReader(converter)
input.readLine()
