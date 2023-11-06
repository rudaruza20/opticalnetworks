package dac.cba.simulador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.table.DefaultTableModel;

public class HFFD_RWAMaxConn {
	
	private final List<Node> nodes;
	private final List<Link> links;

	public HFFD_RWAMaxConn(Network graph) {
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
	
	
	public ArrayList<Path> execute (final Network graph, ArrayList<Demand> trafficMatrix, int C){
		// C : Number of fixed Wavelengths available. 
		int no_served=0;
		ArrayList<Path> paths= new ArrayList<Path>(); 
		Path auxpath;
		Demand auxDemand;
		ArrayList<HFFD_RWAMaxConn> bins = new ArrayList<HFFD_RWAMaxConn>();
		ArrayList<Wavelength> waves= new ArrayList<Wavelength>();
		ArrayList<Demand> demands = (ArrayList<Demand>)trafficMatrix.clone();
		/*
		 * Original Demands are sorted downward (non increasing) by shortest-path length s-d pair 
		 */
		//Best results are obtained with length in terms of hops
		for (Demand d:demands){
			int idsrc = d.GetSrcNode().GetId();
			int iddst = d.GetDstNode().GetId();
			ArrayList<Path> sp = graph.GetPaths(idsrc, iddst);
			d.SetWeigth(sp.get(sp.size()-1).getHops());
		}
		Collections.sort(demands);
		int k=0,j;
		for (Demand d:demands){
			System.out.println("Demand "+k+" from "+d.GetSrcNode().GetId()+" to "+d.GetDstNode().GetId()+" has "+d.GetWeigth()+" [length]");
			k++;
		}
		/*
		 * fin 
		 */
		/*
		 * Meta-heuristic with Random location for paths with the same length shortest path
		 
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
					System.out.println("Valor de n "+n);
					if (n>=50) break;
				}while (demands.get(q).GetWeigth()==demands.get(newIndex).GetWeigth());
				n=0;
				auxDemand = demands.get(q);
				demands.set(q,demands.get(newIndex));
				demands.set(newIndex, auxDemand);
			}
		}
		
		for (Demand d:demands){
			System.out.println("Demand "+k+" from "+d.GetSrcNode().GetId()+" to "+d.GetDstNode().GetId()+" has "+d.GetWeigth()+" [w]");
			k++;
		}
		/*
		 * fin Meta Heuristic
		 */
		Wavelength w= new Wavelength (1,false);
		waves.add(w);
		graph.CreateLpathsTable();
		bins.add(this);
			for (j=0;j<demands.size();j++){
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
					if (bins.size()>=C){
						System.out.println("Demand "+demands.get(j).GetId()+" no served");
						no_served++;
						j++;
					}
					else {
						HFFD_RWAMaxConn ffrwa_new = new HFFD_RWAMaxConn(graph); //New copy of Graph G
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
				}
				paths.add(auxpath);
			}
		printListOfPaths(paths);
		System.out.println("\n\n"+waves.size()+" Wavelengths has been allocated for "+demands.size()+" requested LightPaths");
		System.out.println("Total demands: "+demands.size());
		System.out.println("Served demands: "+(demands.size()-no_served));
		System.out.println("Blocking Factor: "+(double)no_served/(double)demands.size());
		return paths;
	}
	public void removeLinks (ArrayList<Link> lpath, HFFD_RWAMaxConn net){
		for (Link edge:lpath){
			for (Link flink:net.getLinks()){
				if (edge.equals(flink)){
					Node r_src=edge.GetDstNode();
					Node r_dst=edge.GetSrcNode();
					Link rlink=net.searchLink(r_src,r_dst);
					net.getLinks().remove(flink);
					net.getLinks().remove(rlink);
					break;
				}
			}
		}	
	}
	public void setWavelengthLink (ArrayList<Link> lpath, Network net, Wavelength w){
		for (Link edge:lpath){
			Link flink=net.searchLink(edge.GetSrcNode(), edge.GetDstNode());
			flink.addWavelength(w);
			Link rlink = net.searchLink(edge.GetDstNode(), edge.GetSrcNode());
			rlink.addWavelength(w);
		}	
	}
	public static void printListOfPaths (ArrayList<Path> paths){
		int i=0;
		for (Path path:paths){
			if (paths.get(i)!=null){
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
			else i++;
		}
	}
}
	

