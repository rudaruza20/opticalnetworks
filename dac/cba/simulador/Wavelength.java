package dac.cba.simulador;
/*
 * For RWA Purposes
 */
public class Wavelength implements Comparable <Wavelength> {
	//private int count=0;
	private int identifier;
	private boolean free;

	public Wavelength(int id, boolean b) {
		// TODO Auto-generated constructor stub
		this.identifier= id;
		this.free=b;
	}
	
	public int getId (){
		return identifier;
	}

	@Override
	public int compareTo(Wavelength w) {
		if (this.identifier > w.identifier){
			return 1;
		}else if (this.identifier < w.identifier){
			return -1;
		}
		else{
			return 0;
		}
	}

}
