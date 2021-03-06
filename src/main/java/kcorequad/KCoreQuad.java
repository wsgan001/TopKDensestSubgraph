package kcorequad;
import interfaces.DensestSubgraph;
import interfaces.IncrementalKCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import output.Output;
import struct.DegreeMap;
import struct.NodeMap;


public class KCoreQuad implements DensestSubgraph,IncrementalKCore{
	HashMap<String,HashSet<String>> graph;
	public HashMap<String,Integer> kCore;
	int maxCore = 0;
	
	public KCoreQuad(HashMap<String,HashSet<String>> graph) {
		kCore = new HashMap<String,Integer>();
		this.graph = graph;
	}
	
	public void removeNode(String src) {
		kCore.remove(src);
	}
	
	public int getKCore(String src) {
		if(kCore.containsKey(src))
			return this.kCore.get(src);
		else 
			return 0;
	}
	
	public void addEdge(String src, String dst) {
		updateKCoreafterAddition(src,dst);
	}
	
	public void removeEdge(String src, String dst) {
		updateKCoreafterDeletion(src,dst);
	}
	
	void updateKCoreafterAddition(String src, String dst) {
		HashSet<String> visited = new HashSet<String>();
		HashSet<String> color = new HashSet<String>();
		
		int C_u = getKCore(src);
		int C_v = getKCore(dst);
		if (C_u > C_v) {
			int c = C_v;
			xcolor(dst,c, visited,color);
			reColorInsert(c,color);
			updateInsert(c,color);
		} else {
			int c = C_u;
			xcolor(src,c, visited,color);
			reColorInsert(c, color);
			updateInsert(c,color);
		}
		
	}
	
	public void color(String dst, int c , HashSet<String> visited, HashSet<String> color) {
		visited.add(dst);
		if(!color.contains(dst))
			color.add(dst);
		
		HashSet<String> temp = graph.get(dst);
		HashSet<String> neighbors = null;
		if(temp == null)
			neighbors = new HashSet<String>();
		else
			neighbors = new HashSet<String>(temp);
		for(String neighbor:neighbors) {
			if(!visited.contains(neighbor) && this.getKCore(neighbor) == c )
				color(neighbor,c, visited,color);
		}
	}
	
	public void xcolor(String u, int c, HashSet<String> visited, HashSet<String> color) {
		visited.add(u);
		
		int Xu = 0;
		HashSet<String> temp = graph.get(u);
		HashSet<String> neighbors = null;
		if(temp == null)
			neighbors = new HashSet<String>();
		else
			neighbors = new HashSet<String>(temp);
		for(String neighbor:neighbors) {
			if(this.getKCore(neighbor) >= c) {
				Xu++;
			}
		}
		
		if( Xu > c) {
			if(!color.contains(u))
				color.add(u);
	
			for(String neighbor:neighbors) {
				if(!visited.contains(neighbor) && this.getKCore(neighbor) == c )
					xcolor(neighbor,c,visited,color);
			}
		}
	}
	
	void reColorInsert(int c, HashSet<String> color) {
		boolean flag = false;
		HashSet<String> nodestoRemove = new HashSet<String>();
		for(String str: color) {
			int X_u = 0;;
			HashSet<String> neighbors = graph.get(str);
			for(String neighbor: neighbors)  {
				if(color.contains(neighbor) || this.getKCore(neighbor) > c) 
					X_u++;
			}
			if (X_u <= c) {
				nodestoRemove.add(str);
				flag = true;
			}	
		}
		
		if(flag) {
			color.removeAll(nodestoRemove);
			reColorInsert(c,color);
		}
		
	}
	
	void updateInsert(int c, HashSet<String> color) {
		for(String str: color) {
			kCore.put(str, c+1);
		}
	}
	void updateKCoreafterDeletion(String u, String v) {
		HashSet<String> visited = new HashSet<String>();
		HashSet<String> color = new HashSet<String>();
		
		int C_u = kCore.get(u);
		int C_v = kCore.get(v);
		
		int Xu = 0;
		HashSet<String> temp_u = graph.get(u);
		HashSet<String> neighbors_u = null;
		if(temp_u == null)
			neighbors_u = new HashSet<String>();
		else
			neighbors_u = new HashSet<String>(temp_u);
		for(String neighbor:neighbors_u) {
			if(this.getKCore(neighbor) >= C_u) {
				Xu++;
			}
		}
		
		int Xv = 0;
		HashSet<String> temp_v = graph.get(v);
		HashSet<String> neighbors_v = null;
		if(temp_v == null)
			neighbors_v = new HashSet<String>();
		else
			neighbors_v = new HashSet<String>(temp_v);
		for(String neighbor:neighbors_v) {
			if(this.getKCore(neighbor) >= C_v) {
				Xv++;
			}
		}
		

		if (C_u > C_v) {
			int c = C_v;
			if(Xv < c) {
				color(v, c, visited,color);
				HashSet<String> V_c = new HashSet<String>();
				reColorDelete(c,V_c,color);
				updateDelete(c,V_c);
			}
			
		} else if (C_u < C_v) {
			int c = C_u;
			if(Xu < c) {
				color(u,c,  visited,color);
				HashSet<String> V_c = new HashSet<String>();
				reColorDelete(c,V_c,color);
				updateDelete(c,V_c);
			}
		} else {
			int c = C_u;
			if( Xu < c && Xv < c ) {
				color(u,c, visited,color);
				if(!color.contains(v)) {
					visited = new HashSet<String>();
					color(v,c, visited,color);
					HashSet<String> V_c = new HashSet<String>();
					reColorDelete(c,V_c,color);
					updateDelete(c,V_c);
					
				}else {
					HashSet<String> V_c = new HashSet<String>();
					reColorDelete(c,V_c,color);
					updateDelete(c,V_c);
				}
			}
			if(Xu < c && Xv >= c) {
				color(u,c, visited,color);
				HashSet<String> V_c = new HashSet<String>();
				reColorDelete(c,V_c,color);
				updateDelete(c,V_c);
			}
			if(Xu>= c  && Xv < c ) {
				color(v,c, visited,color);
				HashSet<String> V_c = new HashSet<String>();
				reColorDelete(c,V_c,color);
				updateDelete(c,V_c);
			}	
		}
	}
	void reColorDelete(int c, HashSet<String> color, HashSet<String> V_c) {
			boolean flag = false;
			HashSet<String> nodestoRemove = new HashSet<String>();
			for(String str: V_c) {
				int X_u = 0;;
				HashSet<String> temp = graph.get(str);
				HashSet<String> neighbors;
				if(temp == null) {
					neighbors = new HashSet<String>();
				}else {
					neighbors = new HashSet<String>(temp);
				}
				
				for(String neighbor: neighbors)  {
					if(V_c.contains(neighbor) || this.getKCore(neighbor) > c) 
						X_u++;
				}
				if (X_u < c) {
					color.add(str);
					nodestoRemove.add(str);
					flag = true;
				}	
			}
			
			if(flag) {
				V_c.removeAll(nodestoRemove);
				reColorDelete(c,color, V_c);
			}
			
		}
	
	void updateDelete(int c, HashSet<String> color) {
		for(String str: color) {
			kCore.put(str, c-1);
			if(getKCore(str) == 0)
				kCore.remove(str);
		}
	}

	@Override
	public ArrayList<Output> getDensest(DegreeMap degreeMap, NodeMap nodeMap) {
		// TODO Auto-generated method stub
		ArrayList<Output> outputArray = new ArrayList<Output>();
		Output returnOutput = new Output();
		ArrayList<String> maxCore = new ArrayList<String>();
		int maxCoreNum = 0;
		for(String str: kCore.keySet()) {
			int core = kCore.get(str);
			if(core > maxCoreNum)  {
				maxCoreNum = core;
				maxCore = new ArrayList<String>();
			}
			if(core == maxCoreNum) {
				maxCore.add(str);
			}
		}
		
		returnOutput.setCoreNum(maxCoreNum);
		returnOutput.setDensity(maxCoreNum/(double)2);
		returnOutput.setNodes(maxCore);
		returnOutput.setSize(maxCore.size());
		outputArray.add(returnOutput);
		return outputArray;
	}
	public int mainCore() {
		// TODO Auto-generated method stub
		ArrayList<String> maxCore = new ArrayList<String>();
		int maxCoreNum = 0;
		for(String str: kCore.keySet()) {
			int core = kCore.get(str);
			if(core > maxCoreNum)  {
				maxCoreNum = core;
				maxCore = new ArrayList<String>();
			}
			if(core == maxCoreNum) {
				maxCore.add(str);
			}
		}
		
		return maxCoreNum;
	}
	
}
