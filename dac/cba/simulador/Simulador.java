package dac.cba.simulador;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import javax.swing.table.DefaultTableModel;


public class Simulador {
	private static ArrayList<Demand> demands = new ArrayList<Demand>();

	public Simulador() {
		// TODO Auto-generated constructor stub
	}
	public static void AddDemand (Demand demand){
		demands.add(demand);	
	}
	public static Demand GetDemand (int index){
		Demand demand = demands.get(index);
		return demand;	
	}
	public static ArrayList<Demand> GetDemands (){
		return demands;
	}
	public static int GetNumberOfDemands (){
		return demands.size();
	}
	public static DefaultTableModel ReadFile (String Path, boolean b){
		//boolean b -->"1" Square matrix, "0" Not square Matrix
		File f = new File(Path);
		Integer row=0, column; /*Matrix Dimension row*column */
		Integer i=0,j=0;
		DefaultTableModel table;
		try {
			Scanner s = new Scanner(f);
			while (s.hasNextLine()) {
				String linea = s.nextLine();
				row++;
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (b){
			column = row;
			table = new DefaultTableModel(row,column);
		}
		else {
			column=4;
			table = new DefaultTableModel(row,column);
		}
		try {
			Scanner s = new Scanner(f);
			while (i<row || s.hasNextLine()){
				String linea = s.nextLine();
				Scanner sl = new Scanner(linea);
				sl.useDelimiter("\\s*,\\s*");
				while (j<column && sl.hasNext()){
					table.setValueAt(sl.next(), i, j);
					//System.out.print("Valor "+i+" : "+j+"= "+sl.next()+"\n");
					j++;
				}
				i++;
				j=0;
				sl.close();
			}
			s.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	public static Network BuildNode(int d){
		Integer id;
		Network net = new Network();
		for (id=0;id<d;id++){
			ArrayList<Link> outl = new ArrayList<Link>();
			ArrayList<Link> inl = new ArrayList<Link>();
			Node node=new Node(id,outl,inl);
			net.AddNode(node);
			System.out.println("Created NodeId: "+node.GetId());
		}
		return net;
	}
	public static void BuildLink(DefaultTableModel adjacencies, Network net){
		int i,j;
		Node srcnode;
		Node dstnode;
		for (i=0;i<adjacencies.getColumnCount();i++)
		{
			srcnode= net.GetNode(i);
			for (j=0;j<adjacencies.getColumnCount();j++)
			{
				long weigth = Long.parseLong((String)adjacencies.getValueAt(i,j)); //weight= value(i,j)<>0 (distance)
				if (weigth!=0){
					dstnode= net.GetNode(j);
					Link link= new Link(srcnode, dstnode, weigth);
					srcnode.SetOutlink(link);
					dstnode.SetInlink(link);
					net.AddLink(link);
					System.out.println("Creado Link node "+srcnode.GetId()+" --> "+dstnode.GetId());
				}
			}	
		}
	}
	public static void BuildDemand(DefaultTableModel demands, Network net){
		int idsrc, iddst;
		Node srcnode;
		Node dstnode;
		
		for (int i=0; i<demands.getRowCount();i++){
			idsrc = Integer.parseInt((String)demands.getValueAt(i,1));
			iddst = Integer.parseInt((String)demands.getValueAt(i,2));
			double BitRate = Double.parseDouble((String)demands.getValueAt(i,3));
			srcnode = net.GetNode(idsrc);
			dstnode = net.GetNode(iddst);
			Demand demand= new Demand(i,srcnode, dstnode,BitRate);
			AddDemand(demand);
		}
		//Collections.sort(GetDemands()); /*ordenar Demandas por weigth de mayor a menor*/ 
	}
	
	public static void PrintListOfNodes (Network net){
	
		int size = net.GetNumberOfNodes();
		for (int i=0;i<size;i++){
			Node node = net.GetNode(i);
			int id = node.GetId();
			System.out.println("NodeId "+id+" :");
			System.out.println("Number de Outlinks:"+node.GetSizeOutLinks());
			ArrayList<Link> outlinks = node.GetOutLinks();
			for (Link outlink : outlinks){
				Node dst = outlink.GetDstNode();
				System.out.println("\t SrcNode: "+id+" ----> DstNode: "+dst.GetId());
			}
			System.out.println("Number de Inlinks:"+node.GetSizeInLinks());
			ArrayList<Link> inlinks = node.GetInLinks();
			for (Link inlink:inlinks){
				Node src = inlink.GetSrcNode();
				System.out.println("\t SrcNode: "+src.GetId()+" ----> DstNode: "+id);
			}
		}
	}
	
	public static void PrintListOfLinks (Network net){
		//ArrayList<Link> links = net.getLinks();
		Collections.sort(net.getLinks());
		int size = net.GetNumberOfLinks();
		for (int i=0;i<size;i++){
			Link link = net.GetLink(i);
			System.out.println("Link "+i+":");
			Node src = link.GetSrcNode();
			Node dst = link.GetDstNode();
			long w = link.GetWeight(); 
			System.out.println(src.GetId()+"-->" +dst.GetId()+", Weigth: "+w+", "+link.getNumberOfWavelengths()+" OCH in use");
			if (link.getNumberOfWavelengths()!=0){
				System.out.print("{");
				Collections.sort(link.getWavelenghts());
				for (Wavelength wavelength:link.getWavelenghts()){
					System.out.print(","+wavelength.getId());
				}
				System.out.println("}");
			}
		}
	}
	
	public static void PrintListOfDemands (Network net){
		int size = GetNumberOfDemands();
		for (int i=0;i<size;i++){
			Demand demand = GetDemand(i);
			System.out.println("Demand "+i+":");
			Node src = demand.GetSrcNode();
			Node dst = demand.GetDstNode();
			System.out.print(demand.GetId()+"\t"+src.GetId()+"\t"+dst.GetId()+"\t"+demand.GetBitRate()+"\t"+demand.GetWeigth()+"\n");
		}
	}
	
	public static void computeUserPaths (Network net){
		String option;
		int idsrc, iddst;
		Node srcnode, dstnode;
		ArrayList <Path> PathList;
		do{
			System.out.println("\n Do you want to compute all routes from one origin to one destination (Y/N): ");
			Scanner op = new Scanner (System.in);
			option = op.nextLine();
			if ("Y".equals(option)){
				System.out.println("Enter src node id: ");
				Scanner src = new Scanner (System.in);
				idsrc = src.nextInt();
				srcnode = net.GetNode(idsrc);
				System.out.println("Enter dst node id: ");
				Scanner dst = new Scanner (System.in);
				iddst = dst.nextInt();
				dstnode = net.GetNode(iddst);
				AllDistinctPaths r= new AllDistinctPaths();
				PathList = r.ComputeAllDistinctPaths(srcnode, dstnode);
				Collections.sort(PathList); // Ordenar por Weigth de mayor a menor y luego insertar ordenados en Table de Network
				net.AddListOfPaths(idsrc, iddst, PathList);
			}else break;
		}while ("Y".equals(option));
		return;
		
	}
	public static void computePaths (Network net){
		//String option;
		//int idsrc, iddst;
		Node srcnode, dstnode;
		ArrayList <Path> PathList;
		for (Demand d:demands)
		{
			srcnode = d.GetSrcNode();
			dstnode = d.GetDstNode();
			if ((net.GetPaths(srcnode.GetId(), dstnode.GetId()))==null){
				AllDistinctPaths r= new AllDistinctPaths();
				PathList = r.ComputeAllDistinctPaths(srcnode, dstnode);
				Collections.sort(PathList); // Ordenar por Weigth de mayor a menor y luego insertar ordenados en Table de Network
				System.out.println("There are "+PathList.size()+" candidate paths between node "+srcnode.GetId()+" and "+dstnode.GetId());
				net.AddListOfPaths(srcnode.GetId(), dstnode.GetId(), PathList);	
			}
			
		}
		return;
	}
	public static void PrintListOfPaths (ArrayList<Path> paths){
		int i=0;
		if (paths==null){
			System.out.println("\nERROR: The path between src and dst node should be computed first or not exist");
			return;
		}
		if (paths.size()==0){	//OJO for search of LightPaths from i to j node
			System.out.println("\nERROR: The (L)Path between src and dst node should be computed first or not exist");
			return;
		}
		int src = paths.get(0).GetPath().get(0).GetSrcNode().GetId();
		int dst = paths.get(0).GetPath().get(paths.get(0).GetPath().size()-1).GetDstNode().GetId();
		//System.out.println("\n\n(L)Paths from node "+src+" to node "+dst+" ("+paths.size()+")");
		for (Path path:paths){
			System.out.print("\n\tPath "+(i+1)+"("+src+"->"+dst+"): {");
			i++;
			System.out.print(src);
			for (Link link:path.GetPath()){
				System.out.print(","+link.GetDstNode().GetId());
			}
			System.out.print("} --> Length: "+path.getLength()+" [u]");
			if (path.getWavelength()!=null)
				System.out.print(", WaveLength ID: "+path.getWavelength().getId());
		}
	}
	public static void FindOutPaths (Network net){
		String option;
		int idsrc, iddst;
		do{
			System.out.println("\nDo you want to find out all distinct paths from one origin to one destination (Y/N): ");
			Scanner op = new Scanner (System.in);
			option = op.nextLine();
			if ("Y".equals(option)){
				System.out.println("Enter src node id: ");
				Scanner src = new Scanner (System.in);
				idsrc = src.nextInt();
				System.out.println("Enter dst node id: ");
				Scanner dst = new Scanner (System.in);
				iddst = dst.nextInt();
				ArrayList<Path> paths = net.GetPaths(idsrc, iddst);
				if (paths!=null)
					System.out.println("\n\nAll Distict Paths from node "+idsrc+" to node "+iddst+" ("+paths.size()+"):");
				PrintListOfPaths(paths);
				
			}else break;
		}while ("Y".equals(option));
		return;
	}
	public static void FindOutLpaths (Network net){
		String option;
		int idsrc, iddst;
		do{
			System.out.println("\nDo you want to find out all lighpaths established from one origin to one destination (Y/N): ");
			Scanner op = new Scanner (System.in);
			option = op.nextLine();
			if ("Y".equals(option)){
				System.out.println("Enter src node id: ");
				Scanner src = new Scanner (System.in);
				idsrc = src.nextInt();
				System.out.println("Enter dst node id: ");
				Scanner dst = new Scanner (System.in);
				iddst = dst.nextInt();
				ArrayList<Path> paths = net.getLightPaths(idsrc, iddst);
				System.out.println("\n\nFrom node "+idsrc+" to node "+iddst+" there is(are) "+paths.size()+" LightPaths:");
				PrintListOfPaths(paths);
				
			}else break;
		}while ("Y".equals(option));
		return;
	}
	public static void findOutWLightPaths (ArrayList<Path> lightPaths){
		String option;
		int idwave;
		do{
			ArrayList<Path> paths = new ArrayList<Path>();
			System.out.println("\nDo you want to find out all lighpaths established using one Wavelenght (Y/N): ");
			Scanner op = new Scanner (System.in);
			option = op.nextLine();
			if ("Y".equals(option)){
				System.out.println("Enter wavelength id: ");
				Scanner src = new Scanner (System.in);
				idwave = src.nextInt();
				for (Path lpath:lightPaths){
					if (lpath.getWavelength().getId()==idwave)
							paths.add(lpath);
				}
				PrintListOfPaths(paths);
				
			}else break;
		}while ("Y".equals(option));
		return;
	}
	public static void findOutWLinks (Network net){
		String option;
		int idsrc, iddst;
		do{
			System.out.println("\nDo you want to find-out how many Wavelengths(optical Channels) traverse or are assigned to one specific link (Y/N): ");
			Scanner op = new Scanner (System.in);
			option = op.nextLine();
			if ("Y".equals(option)){
				System.out.println("Enter src node id of the Link: ");
				Scanner src = new Scanner (System.in);
				idsrc = src.nextInt();
				System.out.println("Enter dst node id of the Link: ");
				Scanner dst = new Scanner (System.in);
				iddst = dst.nextInt();
				Link link = net.searchLink(net.GetNode(idsrc), net.GetNode(iddst));
				ArrayList<Wavelength> wavelengths = link.getWavelenghts();
				Collections.sort(wavelengths);//Sort upward optical channels by id
				System.out.println("\n\nLink from node "+idsrc+" to node "+iddst+" has assigned "+link.getNumberOfWavelengths()+" optical channels:");
				for (Wavelength w:wavelengths)
					System.out.println("\n\t Wavelength assigned: "+w.getId());
			}else break;
		}while ("Y".equals(option));
		return;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DefaultTableModel Adjacencies;
		DefaultTableModel Demands;
		ArrayList<Path> paths;
		Adjacencies= ReadFile("/home/yt/adyacencias_new.txt",true);
		int dim= Adjacencies.getColumnCount();
		Network net = BuildNode (dim);
		BuildLink(Adjacencies, net);
		PrintListOfNodes(net);
		PrintListOfLinks(net);
		net.CreatePathsTable();
		Demands = ReadFile("/home/yt/Demands_new(10).txt",false);
		BuildDemand(Demands, net);
		PrintListOfDemands(net);
		//computeUserPaths(net);
		computePaths(net); //according to the demands. Pre-compute all distinct routes (candidate Paths) to serve all demands in order to run ILP optimization
		//FindOutPaths(net);
		
	    /*
	     * Execute k-LinkDisjointPaths Dijkstra
	    
	    KLinkDisjointPaths kspd = new KLinkDisjointPaths(net);
	    paths=kspd.execute(net.GetNode(0), net.GetNode(5),5);
	    System.out.println("\n\nFrom node "+net.GetNode(0).GetId()+" to node "+net.GetNode(5).GetId()+" there is(are) "+paths.size()+"-Link-Disjoint Paths:");
	    PrintListOfPaths(paths);
	    /*
	     * Execute LinkProtection
	    
	    LinkProtection kdisjoint = new LinkProtection(net);
	    paths=kdisjoint.execute(net.GetNode(0), net.GetNode(5));
	    System.out.println("\n\nLink Protection from node "+net.GetNode(0).GetId()+" to node "+net.GetNode(5).GetId()+". Primary and Secondary Paths:");
	    PrintListOfPaths(paths);
	    /******************************************************************************************************************/
		// 1st Objective:
	    // Minimize the number of lambdas to serve all demands given a Traffic Matrix.
	    /*******************************************************************************************************************/
	    /*
	     * execute Meta Heuristic FF_RWA Bin Packing
	    
	    System.out.println("\n\nLightPaths for traffic matrix:");
	    MetaH_FF_RWA mffrwa = new MetaH_FF_RWA(net);
	    ArrayList<Path> lightPaths = mffrwa.execute(net, demands);
	   */
	    /*
	     * execute Heuristic FF_RWA Bin Packing
	    */
	    System.out.println("\nLightPaths established for traffic matrix using FF_RWA Bin Packing :");
	    Heuristic_FF_RWA hffRWA = new Heuristic_FF_RWA(net);
	    ArrayList<Path> hlightPaths = hffRWA.execute(net, demands);
	    
	    /*
	     * execute Heuristic FFD_RWA Bin Packing
	    
	    System.out.println("\n\nLightPaths established for traffic matrix using FFD_RWA Bin Packing :");
	    Heuristic_FFD_RWA hffdRWA = new Heuristic_FFD_RWA(net);
	    ArrayList<Path> hlightPaths2 = hffdRWA.execute(net, demands);
	    
	    //FindOutLpaths(net);
	    /*
	     * Find-out all LightPaths for one Wavelength
	    
	    findOutWLightPaths(lightPaths);
	    /*
	     * Find-out how many Wavelengths traverse or are allocated to one specific link
	    
	    findOutWLinks(net); 
	    /*
	     * Print List of Links sorted ascending by number of optical channels in use
	     
	    System.out.println("List of Links sorted ascending by number of optical channels in use");
	    PrintListOfLinks(net);
	    /*
	     * Find out and Printout ascending the Number of LightPaths by OCH (pending implementation) - given one wavelength list the Lpaths that use it
	     */
	    /*
	     * RWA_Optimizer_through ILP
	    */
	    RWA_MinLambdasOptimizer rwa_op= new RWA_MinLambdasOptimizer();
		ArrayList<Demand> pdemands = rwa_op.initialize(net, demands, 80);//pdemands --> proccessed demands: original demands are grouped by s-d pair full duplex connections
		rwa_op.solve(net, pdemands, 80);
		
	    
	    /******************************************************************************************************************/
		// 2nd Objective:
	    // Maximize number of established LightPaths for limited or fixed number of lambdas and given Traffic Matrix 
	    /*******************************************************************************************************************/
	    
		/*
	     * execute Heuristic FFD_RWA Bin Packing
	    */
		/*
	    System.out.println("\n\nLightPaths established for traffic matrix using FFD_RWA Bin Packing :");
	    HFFD_RWAMaxConn hffdRWAc = new HFFD_RWAMaxConn(net);
	    ArrayList<Path> hlightPaths3 = hffdRWAc.execute(net, demands, 80);
		*/
	    /*
	     * RWA_Optimizer_through ILP
	    */
		/*
	    RWA_MaxConnOptimizer crwa_op= new RWA_MaxConnOptimizer();
		ArrayList<Demand> prdemands = crwa_op.initialize(net, demands, 79);//pdemands --> proccessed demands: original demands are grouped by s-d pair full duplex connections
		crwa_op.solve(net, prdemands, 79);
		*/
		
	}
}