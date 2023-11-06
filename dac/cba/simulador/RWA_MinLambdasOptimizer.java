package dac.cba.simulador;
import java.util.ArrayList;

import ilog.concert.*;
import ilog.cplex.*;

public class RWA_MinLambdasOptimizer {
	private long t0, t1,t2;
	IloCplex cplex;
	IloNumVar[][][] x;
	IloNumVar[] y;
	//IloNumVar[] z;
	private long construction_time,solution_time;

	public RWA_MinLambdasOptimizer() {
		// TODO Auto-generated constructor stub
		try 
		{
			cplex = new IloCplex();
		} catch (IloException e)
		{
		}
	}
	public ArrayList<Demand> initialize(Network net,ArrayList<Demand> original_demands, int C)
	{
		ArrayList<Demand> demands = (ArrayList<Demand>)original_demands.clone();
		int n_lambdas; 
		//C : number of optical Channels (Lambdas)
		t0 = System.currentTimeMillis();
		
		// Initialize Cplex environment
		
		try
		{			
			IloLinearNumExpr expr = cplex.linearNumExpr();
			//IloLinearNumExpr expr2 = cplex.linearNumExpr();
			int idsrc, iddst;
			// Cplex parameters
			cplex.setParam(IloCplex.IntParam.NodeFileInd, 3); //IMPORTANTE:  si se excede el tamaño de la memoria lo guarda en disco
			cplex.setParam(IloCplex.DoubleParam.EpGap, 0.05); //IMPORTANTE: Devuelve la solución cuando haya alcanzado el 95% de ejecución de la optimización
			//cplex.setParam(IloCplex.IntParam.VarSel, 3);
			//cplex.setParam(IloCplex.DoubleParam.TiLim, 120); //IMPORTANTE: Tiempo limite en SEG de ejecución y devuelve la solución
			/*
			 * Original Demands are grouped by s-d pairs (considering full-dúplex connections)
			 */
			int n=demands.size();
			for (int i=0; i<n;i++){
				n_lambdas=1;
				for (int j=i+1; j<n;j++){
					if ((demands.get(i).GetSrcNode().GetId()==demands.get(j).GetSrcNode().GetId() && demands.get(i).GetDstNode().GetId()==demands.get(j).GetDstNode().GetId()) ||(demands.get(i).GetSrcNode().GetId()==demands.get(j).GetDstNode().GetId() && demands.get(i).GetDstNode().GetId()==demands.get(j).GetSrcNode().GetId())){
						n_lambdas++;
						demands.remove(j);
						j--;
						n--;	
					}
				}
				demands.get(i).SetWeigth(n_lambdas);
			}
			for (Demand d:demands){
				System.out.println(d.GetSrcNode().GetId()+"<->"+d.GetDstNode().GetId()+" : "+d.GetWeigth());
			}
			
			// Variables
			// Boolean Variables x(d,p,w)
			x = new IloNumVar[demands.size()][][];
			for (int d=0; d<demands.size();d++){
				idsrc = demands.get(d).GetSrcNode().GetId();
				iddst = demands.get(d).GetDstNode().GetId();
				x[d]= new IloNumVar[net.GetPaths(idsrc, iddst).size()][];
				for (int p=0; p<net.GetPaths(idsrc, iddst).size();p++){
					x[d][p]= cplex.boolVarArray(C);
				}
			}
			// Boolean Variables Y(w)
			y = cplex.boolVarArray(C);
			
			// Constraints
			// All demands have to be served
			for (int d=0;d<demands.size();d++){
				for (int p=0;p<net.GetPaths(demands.get(d).GetSrcNode().GetId(), demands.get(d).GetDstNode().GetId()).size();p++){
					for (int w=0; w<C;w++){
						expr.addTerm(1, x[d][p][w]);
					}
				}
				cplex.addEq(expr, demands.get(d).GetWeigth()); // 1 OCH per connection request, or # of requested OCH in case of grouping the demands by s-d pairs
				expr.clear();
			}
			//Clashing constraint
			//OCH are full duplex, so this constraint should ensure that the wavelength w in link e (forward) and e' (reverse) is assigned only once.
			
			for(int e = 0; e<net.getLinks().size(); e++)
			{
				Link linkf = net.GetLink(e); //forward link
				Link linkr =  net.searchLink(net.getLinks().get(e).GetDstNode(), net.getLinks().get(e).GetSrcNode()); //reverse link
				
				for(int w = 0; w<C; w++)
				{
					for(int d = 0; d<demands.size(); d++)
					{
						for(int p = 0; p<net.GetPaths(demands.get(d).GetSrcNode().GetId(), demands.get(d).GetDstNode().GetId()).size();p++)
						{
							Path path = net.GetPaths(demands.get(d).GetSrcNode().GetId(), demands.get(d).GetDstNode().GetId()).get(p);
							if(containsLink(path,linkf)||containsLink(path,linkr))
							{												
								expr.addTerm(x[d][p][w], 1);
							}
						}
					}
					cplex.addLe(expr,1);
					expr.clear();
				}
			}
			//Bound Y: force Yw to be boolean {0,1}
			
			for (int d = 0; d<demands.size();d++){
				for (int p=0;p<net.GetPaths(demands.get(d).GetSrcNode().GetId(), demands.get(d).GetDstNode().GetId()).size();p++){
					for (int w=0;w<C;w++){
						expr.addTerm(x[d][p][w], 1);
						cplex.addLe(expr,y[w]);
						expr.clear();
						
					}
				}
			}
			
			// Objective function
			
			for(int w = 0; w<C; w++)
			{
				expr.addTerm(y[w],1+(1+w)*10E-4);
			}
									
			IloObjective objective_function = cplex.minimize(expr);
			cplex.add(objective_function);

			
		}catch (IloException ev) 
		{
	        //Log.write("Concert exception caught: " + ev);
		}
		return demands;
	}
	
	private boolean containsLink(Path path, Link link)
	{
		boolean contains = false;
		
		for(int i = 0; i<path.GetPath().size(); i++)
		{
			if(link.GetSrcNode().GetId() == path.GetPath().get(i).GetSrcNode().GetId() && link.GetDstNode().GetId() == path.GetPath().get(i).GetDstNode().GetId())
			{
				return true;
			}
		}
		return contains;
	}
	
	public void solve(Network net, ArrayList<Demand> demands, int C)
	{				
		try
		{
			int idsrc, iddst;
			t1 = System.currentTimeMillis();
			
			cplex.solve();
			
			t2 = System.currentTimeMillis();
			
			if(cplex.getStatus()!=IloCplex.Status.Infeasible)
			{	
				construction_time = t1-t0;
				
				solution_time = t2-t1;
				
				System.out.println("\nTime to construct the model is "+(construction_time)/1000.0+" sec.");
				
				System.out.println("Found feasible solution in "+(solution_time)/1000.0+" sec.");
				
				int OCH = 0; //Number of Wavelengths in Use
				
				for(int w = 0; w<C; w++)
				{
					if(cplex.getValue(y[w])>=0.99 && cplex.getValue(y[w])<=1.01)
					{
						OCH++;
					}
				}
				//System.out.println("Utilized Channels = "+OCH);
				
				for(int d = 0; d<demands.size(); d++)
				{
					idsrc = demands.get(d).GetSrcNode().GetId();
					iddst = demands.get(d).GetDstNode().GetId();
					
					for(int p=0; p<net.GetPaths(idsrc, iddst).size();p++)
					{
						for (int w=0;w<C;w++){
							if(cplex.getValue(x[d][p][w])>=0.99 && cplex.getValue(x[d][p][w])<=1.01)
							{
								System.out.print("Demand "+d+" utilizes candidate lightpath "+p+" : {");
								System.out.print(net.GetPaths(idsrc, iddst).get(p).getSrcNode().GetId());
								for (Link link:net.GetPaths(idsrc, iddst).get(p).GetPath()){
									System.out.print(","+link.GetDstNode().GetId());
								}
								System.out.println("}, with wavelength id "+w);
							}
						}
					
					}
				}
				System.out.println("Utilized Channels = "+OCH);
			}
			else
			{
					System.out.println("No solution found");
			}
		}catch (IloException ev) 
		{
			//Log.write("Concert exception caught: " + ev);
		}
	}
}

