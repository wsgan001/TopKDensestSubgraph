package greedy;
import input.StreamEdge;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;

import kcorelinear.KCoreTraversal;
import kcorequad.KCoreQuad;
import struct.NodeMap;
import utility.SetFunctions;

public class SnowBall implements Serializable, Comparable<SnowBall>{
	private UUID serialVersionUID = UUID.randomUUID();;
	double maximalDensity;
	double density;
	int numEdges;
	int numNodes;
	static long counter = 0 ;
	long id;
	HashMap<String,HashSet<String>> graph;
	KCoreTraversal kCore;

	public SnowBall() {
		this.id = counter++;
		this.density = 0;
		this.graph = new HashMap<String,HashSet<String>>();
		this.kCore = new KCoreTraversal(graph);
	}
	public int getNumNodes() {
		return this.numNodes;
	}
	public int getNumEdges() {
		return this.numEdges;
	}
	boolean containsNode (String src ) {
		return this.graph.containsKey(src);
	}
	void removeNode(String src) {
		HashSet<String> temp = graph.get(src);
		if(temp != null ) {
			HashSet<String> neighbors = new HashSet<String>(temp);
			for(String neighbor:neighbors) {
				graph.get(neighbor).remove(src);
				graph.get(src).remove(neighbor);
				numEdges--;
				kCore.removeEdge(src, neighbor);
			}
		}
		graph.remove(src);
		kCore.removeNode(src);
		numNodes--;
	}
	void setMaximalDensity(double externalDensity, NodeMap nodeMap) {
		this.maximalDensity = Math.max(density,externalDensity);
	}

	Set<String> getNodes() {
		return graph.keySet();
	}
	public double getDensity() {
		if(numNodes == 0)
			return 0;

		this.density = numEdges/(double)numNodes;
		//density = approximator.getApproximation((HashMap<String,HashSet<String>>)graph.clone(), numEdges, numNodes);
		return this.density;
	}
	public void addNode(String src, NodeMap nodeMap) {
		//kCore.addNode(src);
		HashSet<String> tempNeighbors = nodeMap.getNeighbors(src);
		
		SetFunctions helper = new SetFunctions();
		HashSet<String> neighbors = new HashSet<String>(helper.intersectionSet(tempNeighbors,graph.keySet()));
		if(neighbors!= null) {
			graph.put(src,neighbors);
	
			for(String neighbor:neighbors) {
				graph.get(neighbor).add(src);
				kCore.addEdge(src, neighbor);
			}
			numEdges+=neighbors.size();
		}
		numNodes++;
	}
	public void ensureFirstInVariant(NodeMap nodeMap, HashSet<String> temp, BagOfSnowballs bag) {

		while(!verifyFirstInVariant(nodeMap, temp,bag)) {
			/* 1. remove all the node with degrees lower than the density
			 * 2. remove all the nodes with degree less than the maximal density
			 * 3. remove all the nodes with core number lower than the maximum core
			 */
			getDensity();
		}
	}

	boolean verifyFirstInVariant(NodeMap nodeMap, HashSet<String> temp, BagOfSnowballs bag) {

		boolean flag = true;
		double newDensity = getDensity();
		HashSet<String> removeNodes = new HashSet<String>();

		for(String str:graph.keySet()) {
			int globalDegree = nodeMap.getDegree(str);
			HashSet<String> neighbors = graph.get(str);
			if(globalDegree < maximalDensity) {
				removeNodes.add(str);
				flag =  false;
			}else if ( kCore.getKCore(str) < this.getMainCore()) {
				removeNodes.add(str);
				temp.add(str);
				flag = false;
			}
		}
		if(!flag)
			for(String str:removeNodes) {
				removeNode(str);
				if(!temp.contains(str))
					bag.removeNodekCore(str);
			}
		return flag;
	}

	boolean contains(String src) {
		return graph.containsKey(src);
	}

	int getMainCore() {
		return kCore.mainCore();
	}

	void merge(SnowBall newSnowBall, NodeMap nodeMap) {
		HashSet<String> nodes = new HashSet<String>(newSnowBall.getNodes());
		HashSet<String> localNodes = new HashSet<String>(this.getNodes());
		
		
		SetFunctions helper = new SetFunctions();
		int intersection = helper.intersection(nodes, localNodes);
		if(intersection > 0) {
			System.out.println(this.getNodes() + " "  + newSnowBall.getNodes());
			System.out.println(this.graph);
			System.out.println(newSnowBall.graph);
			System.exit(1);
		}
			
		//System.out.println(("step 1 "   + (System.currentTimeMillis()-startTime)/(double)1000));
		
		this.numEdges+=newSnowBall.numEdges;
		this.numNodes+=newSnowBall.numNodes;
		
		for(String node:nodes) {
			this.graph.put(node, new HashSet<String>(newSnowBall.graph.get(node)));
			this.kCore.kCore.put(node, newSnowBall.getCoreNumber(node));
			this.kCore.mcd.put(node, newSnowBall.kCore.getmcd(node));
			this.kCore.pcd.put(node, newSnowBall.kCore.getpcd(node));
			
		}
		
		//System.out.println("step 2 "  + ((System.currentTimeMillis()-startTime)/(double)1000));
		for(String node:nodes) {
			HashSet<String> neighbors = nodeMap.getNeighbors(node);
			if(neighbors!=null) {
				for(String neighbor:neighbors) {
					if(localNodes.contains(neighbor))
						addEdge(new StreamEdge(node,neighbor));
				}

			}
		}
		//System.out.println("step 3 " + ((System.currentTimeMillis()-startTime)/(double)1000 ) );
		newSnowBall.graph = new HashMap<String,HashSet<String>>();
		newSnowBall.kCore = new KCoreTraversal(newSnowBall.graph);
		newSnowBall.numEdges = 0 ;
		newSnowBall.numNodes = 0 ;
		//System.out.println("step 4 " + ((System.currentTimeMillis()-startTime)/(double)1000));
		//System.out.println(this.getNodes());
		//System.out.println(this.numEdges + " " + this.numNodes);
	}

	public void addEdge(StreamEdge edge) {
		String src = edge.getSource();
		String dst = edge.getDestination();
		if(graph.containsKey(src) )
			if(!graph.get(src).contains(dst)) {
				if(graph.containsKey(dst))
					if(!graph.get(dst).contains(src)) {
						graph.get(src).add(dst);
						graph.get(dst).add(src);
						numEdges++;
						kCore.addEdge(src, dst);
					}
			}
	}

	void print() {

		for (String name: graph.keySet()){

			String key =name;
			HashSet<String> value = graph.get(key);  
			System.out.print(key + " ["); 
			for(String str:value) {
				System.out.print("<"+str+","+kCore.getKCore(str)+">");
			}
			System.out.println("]");

		} 
	}

	boolean containsEdge(StreamEdge edge) {
		String src = edge.getSource();
		String dst = edge.getDestination();
		if(graph.containsKey(src) && graph.containsKey(dst))
			if(graph.get(src).contains(dst) && graph.get(dst).contains(src))
				return true;

		return false;
	}

	public void removeEdge(StreamEdge edge,NodeMap nodeMap) {
		String src = edge.getSource();
		String dst = edge.getDestination();
		if(graph.containsKey(src)) {
			if(graph.containsKey(dst)) {
				if(graph.get(src).contains(dst)) {
					if(graph.get(dst).contains(src)) {
						graph.get(src).remove(dst);
						graph.get(dst).remove(src);
						kCore.removeEdge(src, dst);

						numEdges--;
					}
				}
			}
		}
		
		if(nodeMap.getDegree(src) == 0)
			graph.remove(src);
		if(nodeMap.getDegree(dst) == 0)
			graph.remove(dst);
		

		
	}
	boolean isEmpty() {
		return (this.getNumEdges()== 0 && this.getNumNodes() == 0);
	}

	@Override
	public int compareTo(SnowBall o) {
		if (this.id == o.id) {
			return 0;
		}else if (this.getDensity() > o.getDensity())
			return 1;
		else 
			return -1;
	}

	public int  getCoreNumber(String src) {
		return this.kCore.getKCore(src);
	}
	
	public int hashcode() {
		int hashCode =  new HashCodeBuilder(17, 31).
				append(this.serialVersionUID).
				toHashCode();
		return hashCode;
	}
	public boolean equals(Object o) {
		SnowBall other  = (SnowBall)o;
		return (this.id == other.id && this.density == other.density && this.numNodes == other.numNodes && this.numEdges == other.numEdges);
	}
}