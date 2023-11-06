package dac.cba.simulador;

import java.util.ArrayList;

public class Path implements Comparable <Path> {
	private Node src, dst;
	private ArrayList<Link> route = new ArrayList<Link>();
	private int hops;
	private Double length;
	private Wavelength wavelength; //RWA - Established LightPath(i) uses wavelength w 

	public Path() {
		// TODO Auto-generated constructor stub
	}
	public Path(Node src, Node dst, ArrayList<Link> LinkList, int h) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.route = LinkList;
		this.hops = h;
		Integer hops = new Integer (h); 
		this.length= hops.doubleValue(); //Initialized length value to number of hops
	}
	public Path(Node src, Node dst, ArrayList<Link> LinkList, double length) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.route = LinkList;
		this.length= length;
	}
	public Path(Node src, Node dst, ArrayList<Link> LinkList, int h, double length) {
		// TODO Auto-generated constructor stub
		this.src=src;
		this.dst=dst;
		this.route = LinkList;
		this.hops = h;
		this.length= length;
	}
	
	public ArrayList<Link> GetPath (){
		return route;
	}
	public Node getSrcNode (){
		return src;
	}
	public Node getDstNode (){
		return dst;
	}
	public int getHops (){
		return hops;
	}
	public double getLength (){
		return length;
	}
	/****RWA***/
	public Wavelength getWavelength (){
		return wavelength;
	}
	public void setLength (double l){
		length = l;
	}
	public void setWavelength (Wavelength w){
		wavelength = w;
	}
	@Override
	public int compareTo(Path path){
		if (this.hops < path.hops){ //< decreasing
			return 1;
		}else if (this.hops > path.hops){
			return -1;
		}
		else{
			return 0;
		}
	}
	

}
