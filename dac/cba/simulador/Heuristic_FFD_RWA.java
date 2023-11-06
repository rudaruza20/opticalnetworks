package dac.cba.simulador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.table.DefaultTableModel;

public class Heuristic_FFD_RWA {
	
	private final List<Node> nodes;
	private final List<Link> links;

	public Heuristic_FFD_RWA(Network graph) {
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
	
	
	public ArrayList<Path> execute (final Network graph, ArrayList<Demand> trafficMatrix){
		ArrayList<Path> paths= new ArrayList<Path>(); 
		Path auxpath;
		Demand auxDemand;
		ArrayList<Heuristic_FFD_RWA> bins = new ArrayList<Heuristic_FFD_RWA>();
		ArrayList<Wavelength> waves= new ArrayList<Wavelength>();
		ArrayList<Demand> demands = (ArrayList<Demand>)trafficMatrix.clone();
		/*
		 * Original Demands are sorted in descending order (non increasing) by shortest-path length (in terms of both hops and weight) of s-d pair 
		 */
		//Best results are obtained with length in terms of hops
		for (Demand d:demands){
			int idsrc = d.GetSrcNode().GetId();
			int iddst = d.GetDstNode().GetId();
			ArrayList<Path> sp = graph.GetPaths(idsrc, iddst);
			d.SetWeigth(sp.get(sp.size()-1).getHops());
		}
		Collections.sort(demands);
		int k=0;
		for (Demand d:demands){
			System.out.println("Demand "+k+" from "+d.GetSrcNode().GetId()+" to "+d.GetDstNode().GetId()+" has "+d.GetWeigth()+" [length]");
			k++;
		}
		/*
		 * fin 
		 */
		/*
		 * Meta-heuristic with Random location for paths with the same length shortest path (Optional)
		 */
		k=0;
		Random randomIndex = new Random();
		int newIndex, n=0;
		for (int q=0; q<demands.size()-1;q++){
			if (demands.get(q).GetWeigth()==demands.get(q+1).GetWeigth()){
				do{
					int n1=q; 
					int n2=(demands.size()-1)-q;
					if (n2>n1)newIndex = randomIndex.nextInt(n2-n1)+n1;
					else if (n2<n1)newIndex = randomIndex.nextInt(n1-n2)+n2;
					else newIndex = randomIndex.nextInt(n2);
					n++;
					if (n>=50) break;
				}while (demands.get(q).GetWeigth()==demands.get(newIndex).GetWeigth());
				n=0;
				auxDemand = demands.get(q);
				demands.set(q,demands.get(newIndex));
				demands.set(newIndex, auxDemand);
			}
		}
		
		for (Demand d:demands){
			System.out.println("Demand "+k+" from "+d.GetSrcNode().GetId()+" to "+d.GetDstNode().GetId()+" has "+d.GetWeigth()+" [length]");
			k++;
		}
		/*
		 * fin Meta Heuristic
		 */
		Wavelength w= new Wavelength (1,false);
		waves.add(w);
		graph.CreateLpathsTable();
		bins.add(this);
			for (int j=0;j<demands.size();j++){
				auxpath=null;
				//Allocate lightPath to the demand j
				for (int i=0;i<bins.size();i++){
					DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(bins.get(i));
					dijkstra.execute(demands.get(j).GetSrcNode()); 
					DefaultTableModel table = dijkstra.getPath(demands.get(j).GetSrcNode(), demands.get(j).GetDstNode());
					if (table.getValueAt(0, 1)!=null){
						auxpath = (Path)table.getValueAt(0, 1);
						auxpath.setWavelength(waves.get(i));
						setWavelengthLink ((ArrayList<Link>)(table.getValueAt(0, 0)),graph,waves.get(i));
						graph.addListOfLPaths(demands.get(j).GetSrcNode().GetId(), demands.get(j).GetDstNode().GetId(), auxpath);
						removeLinks ((ArrayList<Link>)(table.getValueAt(0, 0)),bins.get(i));
						break;
					}
				}
				if (auxpath==null){
					Heuristic_FFD_RWA ffrwa_new = new Heuristic_FFD_RWA(graph); //New copy of Graph G
					bins.add(ffrwa_new);
					DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(ffrwa_new);
					dijkstra.execute(demands.get(j).GetSrcNode()); 
					DefaultTableModel table = dijkstra.getPath(demands.get(j).GetSrcNode(), demands.get(j).GetDstNode());
					auxpath = (Path)table.getValueAt(0, 1);
					Wavelength w_new = new Wavelength (bins.size(),false); //assigned lambda
					waves.add(w_new);
					auxpath.setWavelength(w_new);
					setWavelengthLink ((ArrayList<Link>)(table.getValueAt(0, 0)),graph,w_new);
					graph.addListOfLPaths(demands.get(j).GetSrcNode().GetId(), demands.get(j).GetDstNode().GetId(), auxpath);
					removeLinks ((ArrayList<Link>)(table.getValueAt(0, 0)),ffrwa_new);
				}
				paths.add(auxpath);
			}
		printListOfPaths(paths);
		System.out.println("\n\n"+waves.size()+" Wavelengths has been allocated for "+paths.size()+" LightPaths");
		return paths;
	}
	public void removeLinks (ArrayList<Link> lpath, Heuristic_FFD_RWA net){
		//For full duplex OC
		for (Link edge:lpath){
			for (Link flink:net.getLinks()){
				if (edge.equals(flink)){
					Node r_src=edge.GetDstNode();
					Node r_dst=edge.GetSrcNode();
					Link rlink=net.searchLink(r_src,r_dst);
					net.getLinks().remove(flink); //forward link
					net.getLinks().remove(rlink); //reverse link
					break;
				}
			}
		}	
	}
	public void setWavelengthLink (ArrayList<Link> lpath, Network net, Wavelength w){
		//For full-duplex OCH - symmetrical transmission with same wavelength for simplicity. 
		for (Link edge:lpath){
			Link flink=net.searchLink(edge.GetSrcNode(), edge.GetDstNode());
			flink.addWavelength(w); //In forward link - TX Fiber
			Link rlink = net.searchLink(edge.GetDstNode(), edge.GetSrcNode());
			rlink.addWavelength(w); //In reverse link - RX Fiber
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
	
