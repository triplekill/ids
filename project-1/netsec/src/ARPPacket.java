import java.net.InetAddress;
import java.net.UnknownHostException;


public class ARPPacket extends EthernetPacket {

	//http://www.tcpipguide.com/free/t_ARPMessageFormat.htm
	
	//arp packet locations
	final int ARP_PACKET_START = ETHER_PAYLOAD_START;
	
	final int ARP_HDWR_ADDR_LEN = ARP_PACKET_START + (int)(32/BITS_IN_BYTE); //14+4=18;
	final int ARP_ADDR_LEN_LEN = 1;
	final int ARP_PROT_ADDR_LEN = ARP_HDWR_ADDR_LEN + ARP_ADDR_LEN_LEN;
	
	final int ARP_SNDR_HDWR_ADDR = ARP_PACKET_START + (int)(64/BITS_IN_BYTE);
	//this is the start, the rest must be parsed after we parse the values
	//for the hardware and protocol length fields (so we know how many bytes)
	
	
	//variables
	public int hardware_address_length;
	public int protocol_address_length;
	public int opcode;
	
	public byte[] sender_hardware_address;
	public InetAddress sender_protocol_address;
	public byte[] target_hardware_address;
	public InetAddress target_protocol_address;
	
	public ARPPacket(byte[] packet) {
		super(packet);
		this.packet_type = "arp";
		parse();
	}//end constructor
	
	public String toString() {
		String out = super.toString();
		
		out+=outln("Hardware Address Length: "+hardware_address_length);
		out+=outln("Protocol Address Length: "+protocol_address_length);
		out+=outln("Sender Hardware Address: "+hardwareAddrToPrintString(sender_hardware_address));
		out+=outln("Sender Protocol Address: "+sender_protocol_address);
		out+=outln("Target Hardware Address: "+hardwareAddrToPrintString(target_hardware_address));
		out+=outln("Target Protocol Address: "+target_protocol_address);
		
		return out;
	}//end toString

	public boolean is_type(String type){
		return ( type.equals("all") || type.equals("eth") || type.equals("arp") );
	}//end is_type
	
	private void parse(){
		byte[] hdwrLenBytes = new byte[1];
		byte[] protLenBytes = new byte[1];
		byte[] sender_hdwr_addr;
		byte[] target_hdwr_addr;
		byte[] sender_prot_addr;
		byte[] target_prot_addr;
		
		System.arraycopy(packet, ARP_HDWR_ADDR_LEN, hdwrLenBytes, 0, ARP_ADDR_LEN_LEN);
		hardware_address_length = unsignedByteToInt(hdwrLenBytes[0]);
        
        System.arraycopy(packet, ARP_PROT_ADDR_LEN, protLenBytes, 0, ARP_ADDR_LEN_LEN);
        protocol_address_length = unsignedByteToInt(protLenBytes[0]);
		
		sender_hardware_address = new byte[hardware_address_length];
		target_hardware_address = new byte[hardware_address_length];
		sender_prot_addr = new byte[protocol_address_length];
		target_prot_addr = new byte[protocol_address_length];
		
		int current_spot = ARP_SNDR_HDWR_ADDR;
		System.arraycopy(packet, current_spot, sender_hardware_address, 0, hardware_address_length);
		current_spot = current_spot + hardware_address_length;
		
		System.arraycopy(packet, current_spot, sender_prot_addr, 0, protocol_address_length);
		current_spot = current_spot + protocol_address_length;
		
		System.arraycopy(packet, current_spot, target_hardware_address, 0, hardware_address_length);
		current_spot = current_spot + hardware_address_length;
		
		System.arraycopy(packet, current_spot, target_prot_addr, 0, protocol_address_length);
		current_spot = current_spot + protocol_address_length;
		
        try {
			sender_protocol_address = InetAddress.getByAddress(sender_prot_addr);
			target_protocol_address = InetAddress.getByAddress(target_prot_addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}//end try/catch
	}//end parse
	
	public ARPPacket address_filter(InetAddress source_address,
			InetAddress destination_address, InetAddress OR_source_address,
			InetAddress OR_destination_address, InetAddress AND_source_address,
			InetAddress AND_destination_address) {
		if(sender_protocol_address.equals(source_address)) return this;
		if(target_protocol_address.equals(destination_address)) return this;
		if(sender_protocol_address.equals(OR_source_address) || target_protocol_address.equals(OR_destination_address)) return this;
		if(sender_protocol_address.equals(AND_source_address) && target_protocol_address.equals(AND_destination_address)) return this;
		
		return null;
	}//end address_filter

}//end ARPPacket