package dac.cba.simulador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class Heuristic_FF_RWA {
	
	private final List<Node> nodes;
	private final List<Link> links;
	
	public Heuristic_FF_RWA(Network graph) {
		    // create a copy of the array so that we can operate on this array
		    this.nodes = new ArrayList<Node>(graph.getNodes());
		    this.links = new ArrayList<Link>(graph.getLinks());
	}
	public List<Node> getNodes (){
		return nodes;
	}
	public List<Link> getLinks (){
		return links;
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
	public int getNumberOfNodes (){
		return nodes.size();
	}
	public int getNumberOfLinks (){
		return links.size();
	}
	
	
	public ArrayList<Path> execute (final Network graph, ArrayList<Demand> trafficmatrix){
		ArrayList<Path> paths= new ArrayList<Path>(); 
		Path auxpath;
		ArrayList<Heuristic_FF_RWA> bins = new ArrayList<Heuristic_FF_RWA>();
		ArrayList<Wavelength> waves= new ArrayList<Wavelength>();
		Wavelength w= new Wavelength (1,false);
		waves.add(w);
		graph.CreateLpathsTable();
		bins.add(this);
		
			for (int j=0;j<trafficmatrix.size();j++){
				auxpath=null;
				//Allocate lightPath to the demand j
				for (int i=0;i<bins.size();i++){
					DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(bins.get(i));
					dijkstra.execute(trafficmatrix.get(j).GetSrcNode()); 
					DefaultTableModel table = dijkstra.getPath(trafficmatrix.get(j).GetSrcNode(), trafficmatrix.get(j).GetDstNode());
					if (table.getValueAt(0, 1)!=null){
						auxpath = (Path)table.getValueAt(0, 1);
						auxpath.setWavelength(waves.get(i));
						setWavelengthLink ((ArrayList<Link>)(table.getValueAt(0, 0)),graph,waves.get(i));
						graph.addListOfLPaths(trafficmatrix.get(j).GetSrcNode().GetId(), trafficmatrix.get(j).GetDstNode().GetId(), auxpath);
						removeLinks ((ArrayList<Link>)(table.getValueAt(0, 0)),bins.get(i));
						break;
					}
				}
				if (auxpath==null){
					Heuristic_FF_RWA ffrwa_new = new Heuristic_FF_RWA(graph); //New copy of Graph G
					bins.add(ffrwa_new);
					DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(ffrwa_new);
					dijkstra.execute(trafficmatrix.get(j).GetSrcNode()); 
					DefaultTableModel table = dijkstra.getPath(trafficmatrix.get(j).GetSrcNode(), trafficmatrix.get(j).GetDstNode());
					auxpath = (Path)table.getValueAt(0, 1);
					Wavelength w_new = new Wavelength (bins.size(),false); //assigned lambda
					waves.add(w_new);
					auxpath.setWavelength(w_new);
					setWavelengthLink ((ArrayList<Link>)(table.getValueAt(0, 0)),graph,w_new);
					graph.addListOfLPaths(trafficmatrix.get(j).GetSrcNode().GetId(), trafficmatrix.get(j).GetDstNode().GetId(), auxpath);
					removeLinks ((ArrayList<Link>)(table.getValueAt(0, 0)),ffrwa_new);
				}
				paths.add(auxpath);
			}
		printListOfPaths(paths);
		System.out.println("\n\n"+waves.size()+" Wavelengths has been allocated for "+paths.size()+" LightPaths");
		return paths;
	}
	public void removeLinks (ArrayList<Link> lpath, Heuristic_FF_RWA net){
		//For full-duplex OCH
		for (Link edge:lpath){
			for (Link flink:net.getLinks()){
				if (edge.equals(flink)){
					Node r_src=edge.GetDstNode();
					Node r_dst=edge.GetSrcNode();
					Link rlink=net.searchLink(r_src,r_dst);
					net.getLinks().remove(flink); //Forward link
					net.getLinks().remove(rlink); //Reverse Link
					break;
				}
			}
		}	
	}
	public void setWavelengthLink (ArrayList<Link> lpath, Network net, Wavelength w){
		//For full-duplex OCH
		for (Link edge:lpath){
			Link flink=net.searchLink(edge.GetSrcNode(), edge.GetDstNode());
			flink.addWavelength(w); //In forward path
			Link rlink = net.searchLink(edge.GetDstNode(), edge.GetSrcNode());
			rlink.addWavelength(w); //In reverse path
		}	
	}
	public static void printListOfPaths (ArrayList<Path> paths){
		int i=0;
		for (Path path:paths){
			int src = paths.get(i).GetPath().get(0).GetSrcNode().GetId();
			int dst = paths.get(i).GetPath().get(paths.get(i).GetPath().size()-1).GetDstNode().GetId();
			System.out.print("\n\tLightPath "+(i+1)+"("+src+"->"+dst+"): {");
			i++;
			System.out.print(src);
			for (Link link:path.GetPath()){
				System.out.print(","+link.GetDstNode().GetId());
			}
			System.out.print("} with WaveLength ID #"+path.getWavelength().getId());
		}
	}
}