package dac.cba.simulador;

import java.util.ArrayList;

public class Link implements Comparable <Link>{
	private Node source;
	private Node destination;
	private ArrayList<Wavelength> wavelengths; //For RWA Purposes
	private long weight; //  In case of negative weights
	public Link(Node srcnode, Node dstnode, long w) {
		// TODO Auto-generated constructor stub
		this.source= srcnode;
		this.destination= dstnode;
		this.weight = w;
		this.wavelengths = new ArrayList<Wavelength>();
	}
	public Node GetSrcNode (){	
		return source;
	}

	public Node GetDstNode (){
		return destination;
	}
	public long GetWeight (){
		return weight;
	}
	public ArrayList<Wavelength> getWavelenghts (){
		return wavelengths;
	}
	public int getNumberOfWavelengths (){
		return wavelengths.size();
	}
	public void addWavelength (Wavelength w){
		this.wavelengths.add(w);
	}
	@Override
	public int compareTo(Link link){
		if (this.wavelengths.size() > link.wavelengths.size()){
			return 1;
		}else if (this.wavelengths.size() < link.wavelengths.size()){
			return -1;
		}
		else{
			return 0;
		}
	}
	
}
