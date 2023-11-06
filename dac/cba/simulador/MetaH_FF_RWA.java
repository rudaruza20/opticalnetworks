/*
 * Instead of dijkstra shortest path, use all paths and select in random way the path to route(serve) the demand. This does not conduct to improve results..!! Too bad :(
 */

package dac.cba.simulador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.table.DefaultTableModel;

public class MetaH_FF_RWA {
	
	private final ArrayList<Node> nodes;
	private final ArrayList<Link> links;

	
	public MetaH_FF_RWA(Network graph) {
		    // create a copy of the array so that we can operate on this array
		    //
			this.nodes = new ArrayList<Node>(graph.getNodes());
		    this.links = new ArrayList<Link>(graph.getLinks());
		//this.nodes = (ArrayList<Node>)graph.getNodes().clone();
		//this.links = (ArrayList<Link>)graph.getLinks().clone();
	}
	public List<Node> getNodes (){
		return nodes;
	}
	public List<Link> getLinks (){
		return links;
	}
	public Link findOutLink (Node src, Node dst){
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
	public Node GetNode (int index){
		Node node = nodes.get(index);
		return node;
	}
	
	
	public ArrayList<Path> execute (final Network graph, ArrayList<Demand> trafficmatrix){
		int selectedIndex, idsrc, iddst;
		ArrayList<Path> paths= new ArrayList<Path>();
		//ArrayList<Path> kPaths;
		Path auxpath;
		ArrayList<MetaH_FF_RWA> bins = new ArrayList<MetaH_FF_RWA>();
		ArrayList<Wavelength> waves= new ArrayList<Wavelength>();
		Random randomIndex = new Random();
		
		
		Wavelength w= new Wavelength (1,false);
		waves.add(w);
		graph.CreateLpathsTable();
		bins.add(this);
			for (int j=0;j<trafficmatrix.size();j++){
				auxpath=null;
				//Allocate lightPath to the demand j
				for (int i=0;i<bins.size();i++){
					idsrc = trafficmatrix.get(j).GetSrcNode().GetId();
					iddst = trafficmatrix.get(j).GetDstNode().GetId();
					Node src = bins.get(i).GetNode(idsrc);
					Node dst = bins.get(i).GetNode(iddst);
					AllDistinctPaths r= new AllDistinctPaths();
					ArrayList<Path> allPaths = r.ComputeAllDistinctPaths(src, dst);
					Collections.sort(allPaths);
					if (allPaths.size()>=3)
					{
						selectedIndex = randomIndex.nextInt(3);
						
					}else if (allPaths.size()>0){
						selectedIndex = randomIndex.nextInt(allPaths.size());
					} else {
						selectedIndex = 1; //por poner algo
					}
					
					if (allPaths.size()>0){
						auxpath = allPaths.get(selectedIndex);
						auxpath.setWavelength(waves.get(i));
						setWavelengthLink (allPaths.get(selectedIndex).GetPath(),graph,waves.get(i));
						graph.addListOfLPaths(trafficmatrix.get(j).GetSrcNode().GetId(), trafficmatrix.get(j).GetDstNode().GetId(), auxpath);
						removeLinks (allPaths.get(selectedIndex).GetPath(),bins.get(i));	
						break;
					}
				}
				if (auxpath==null){
					MetaH_FF_RWA ffrwa_new = new MetaH_FF_RWA(graph); //New copy of Graph G
					bins.add(ffrwa_new);
					Node src = trafficmatrix.get(j).GetSrcNode();
					Node dst= trafficmatrix.get(j).GetDstNode();					
					ArrayList<Path> allPaths = graph.GetPaths(src.GetId(), dst.GetId());
					Collections.sort(allPaths);
					if (allPaths.size()>=5)
					{
						selectedIndex = randomIndex.nextInt(5);
						
					}else if (allPaths.size()>0){
						selectedIndex = randomIndex.nextInt(allPaths.size());
						
					} else {
						selectedIndex = 1; //por poner algo
					}
					Wavelength w_new = new Wavelength (bins.size(),false); //assigned lambda
					waves.add(w_new);
					auxpath = allPaths.get(selectedIndex);//new
					auxpath.setWavelength(w_new);
					setWavelengthLink (allPaths.get(selectedIndex).GetPath(),graph,w_new);
					graph.addListOfLPaths(trafficmatrix.get(j).GetSrcNode().GetId(), trafficmatrix.get(j).GetDstNode().GetId(), auxpath);
					removeLinks (allPaths.get(selectedIndex).GetPath(),ffrwa_new);
				}
				paths.add(auxpath);
			}
		printListOfPaths(paths);
		System.out.println("\n\n"+waves.size()+" Wavelengths has been allocated for "+paths.size()+" LightPaths");
		return paths;
	}
	public void removeLinks (ArrayList<Link> lpath, MetaH_FF_RWA net){
		for (Link edge:lpath){
			for (Link flink:net.getLinks()){
				if (edge.equals(flink)){
					Node r_src=flink.GetDstNode();
					Node r_dst=flink.GetSrcNode();
					Link rlink=net.findOutLink(r_src,r_dst);
					if (flink.GetSrcNode().GetOutLinks().remove(flink)) // se eliminan los links out e in de cada nodo
					if (flink.GetDstNode().GetInLinks().remove(flink))  
					if (rlink.GetDstNode().GetOutLinks().remove(rlink)) 
					if (rlink.GetSrcNode().GetInLinks().remove(rlink))  
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
