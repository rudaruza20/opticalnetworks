/**
 * 
 */
package dac.cba.simulador;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;


/**
 * @author rudaruza
 *
 */
public class Network {

	private ArrayList<Node> nodes;
	private ArrayList<Link> links;
	private DefaultTableModel paths;
	private DefaultTableModel lightPaths; /*RWA*/
	
	public Network (){
		//
		this.nodes = new ArrayList<Node>();
		this.links = new ArrayList<Link>();
	}
	
	public Network(Node node, Link link) {
		// TODO Auto-generated constructor stub
		this.nodes.add(node);
		this.links.add(link);
	}
	
	public Network(Node node) {
		// TODO Auto-generated constructor stub
		this.nodes.add(node);	
	}
	
	public Network(Link link) {
		// TODO Auto-generated constructor stub
		this.links.add(link);
	}
	
	public void AddNode (Node node){
		nodes.add(node);	
	}
	
	public void AddLink (Link link){
		links.add(link);
	}
	public void CreatePathsTable (){
		paths = new DefaultTableModel(this.GetNumberOfNodes(),this.GetNumberOfNodes());
	}
	public void CreateLpathsTable (){
		lightPaths = new DefaultTableModel(this.GetNumberOfNodes(),this.GetNumberOfNodes());
		for (int i=0; i<lightPaths.getRowCount();i++){
			for (int j=0; j<lightPaths.getColumnCount();j++){
				lightPaths.setValueAt(new ArrayList<Path>(), i, j);
			}
		}
	}
	public void AddListOfPaths (int idsrc, int iddst, ArrayList<Path> PathList){
		paths.setValueAt(PathList, idsrc, iddst);
		paths.setValueAt(PathList, iddst, idsrc);// same distinct paths in forward and reverse direction - Bidirectional links - 
	}
	public void addListOfLPaths (int idsrc, int iddst, Path lightPath){
		ArrayList<Path> flightpaths = (ArrayList<Path>)lightPaths.getValueAt(idsrc, iddst);
		ArrayList<Path> rlightpaths = (ArrayList<Path>)lightPaths.getValueAt(iddst, idsrc);
		flightpaths.add(lightPath);
		rlightpaths.add(lightPath);
		lightPaths.setValueAt(flightpaths, idsrc, iddst);
		lightPaths.setValueAt(rlightpaths, iddst, idsrc);// same lightPaths in forward and reverse direction - Bidirectional links - OCH full dúplex: WDM one tx-fiber and one rx-fiber
		//En este caso no afecta que el reverse path el nodo origen y destino estén invertidos ya que el wavelength se añade a cada enlace una vez que es asignado. No funcionaria si se usa el atributo free (OJO)
	}
	public Node GetNode (int index){
		Node node = nodes.get(index);
		return node;
	}
	public Link GetLink (int index){
		Link link = links.get(index);
		return link;	
	}
	public int GetNumberOfNodes (){
		return nodes.size();
	}
	
	public int GetNumberOfLinks (){
		return links.size();
	}
	public ArrayList<Node> getNodes (){
		return nodes;
	}
	public ArrayList<Link> getLinks (){
		return links;
	}
	public ArrayList<Path> GetPaths(int idsrc, int iddst){
		return (ArrayList<Path>)paths.getValueAt(idsrc,iddst);
	}
	public ArrayList<Path> getLightPaths(int idsrc, int iddst){
		return (ArrayList<Path>)lightPaths.getValueAt(idsrc,iddst);
	}
	public Link searchLink (Node src, Node dst){
		Link link=null;
		for (Link l:this.links){
			if (l.GetSrcNode().equals(src)&&l.GetDstNode().equals(dst)){
				link=l;
			}
		}
		return link;
	}
}
