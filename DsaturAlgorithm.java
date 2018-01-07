package datastruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

class vertex {
	int vIndex;
	int oindex;
	int color = -1;
	int possiblecolor;
	int maxcolor;
	int sdegreerenewtime = 0;

	int adjacencydegree = 0;
	int saturationdegree = 0;
	HashMap<Integer, ArrayList<Integer>> colormap;
	// ArrayList<ArrayList<Integer>> colormetrix;

	int[] edgeList;

	public vertex() {
		colormap = new HashMap<Integer, ArrayList<Integer>>();
	}

	public void renewSdegree() {
		saturationdegree = colormap.size();
		sdegreerenewtime += 1;

		// possiblecolor = 0;
		// maxcolor = 0;
	}

	public void getpossiblecolor() {
		int t = 0;
		for (int i = 0; i <= maxcolor; i++) {
			if (!colormap.containsKey(i)) {
				t = 1;
				possiblecolor = i;
				break;
			}
		}
		if (t == 0)
			possiblecolor = maxcolor + 1;

		return;

	}

	public static int[][] generateGraph(int numv, double dens) {
		int[][] graph = new int[numv][];
		int[][] possibleedge = new int[numv][numv];
		int[] sumedge = new int[numv];
		float p;
		Random random = new Random();
		for (int i = 0; i < numv; i++)
			sumedge[i] = 0;

		for (int i = 0; i < numv; i++) {

			for (int j = 0; j < numv; j++) {
				if (random.nextFloat() <= dens && i != j && possibleedge[i][j] != 1) {
					possibleedge[i][j] = 1;
					if (possibleedge[j][i] == 0) {
						possibleedge[j][i] = 1;
						sumedge[j] += 1;
					}
					sumedge[i] += 1;
				}
			}
		}
		for (int i = 0; i < numv; i++) {
			graph[i] = new int[sumedge[i]];
			int t = 0;
			for (int j = 0; j < numv; j++) {
				if (possibleedge[i][j] == 1) {
					graph[i][t] = j;
					t++;
				}
			}

		}

		return graph;
	}

}

class adegreeCompare implements Comparator<vertex> {

	public int compare(vertex va, vertex vb) {

		if (va.adjacencydegree > vb.adjacencydegree)
			return 1;
		else if (va.adjacencydegree < vb.adjacencydegree)
			return -1;
		else
			return 0;

	}
}

class maxdegreeholder {

	degreemap map;

	public maxdegreeholder() {
		map = new degreemap();
	}
}

class degreemap {
	HashMap<Integer, HashMap<Integer, vertex>> sdegreemap;
	int maxsdegree;
	int bestnodeindex;
	// HashMap<Integer, ArrayList<degreepair>> colormaxadegreemap;

	public degreemap() {
		sdegreemap = new HashMap<Integer, HashMap<Integer, vertex>>();
		// colormaxadegreemap = new HashMap<Integer, ArrayList<degreepair>>();
	}

	public void getmaxsdegree() {
		Set<Integer> set = sdegreemap.keySet();
		Iterator<Integer> iter = set.iterator();
		int max = 0;
		while (iter.hasNext()) {
			int temp = iter.next();

			globaltimer.timer += 1;

			if (temp > max)
				max = temp;
		}
		maxsdegree = max;
	}

	public void removecolorednode(vertex node) {
		if (sdegreemap.containsKey(node.saturationdegree)) {
			if (sdegreemap.get(node.saturationdegree).containsKey(node.vIndex)) {
				sdegreemap.get(node.saturationdegree).remove(node.vIndex);
				if (sdegreemap.get(node.saturationdegree).isEmpty()) {
					sdegreemap.remove(node.saturationdegree);
					//getmaxsdegree();
				}
			}
		}
		return;
	}

	public void getbestnode() {
		int tempadegree = 0;
		HashMap<Integer, vertex> adegreeList = sdegreemap.get(maxsdegree);
		Iterator<Integer> iter = adegreeList.keySet().iterator();
		while (iter.hasNext()) {
			vertex tempnode = adegreeList.get(iter.next());

			globaltimer.timer += 1;

			int adjdegree = tempnode.adjacencydegree;
			int tempindex = tempnode.vIndex;
			if (adjdegree > tempadegree) {
				bestnodeindex = tempindex;
				tempadegree = adjdegree;
			}
		}
		return;
	}

}

class globaltimer {
	static int timer = 0;
}

public class TCSS543 {

	public static int sumcolor = 0;
	public static int targetnodeindex = 0;
	public static maxdegreeholder mdh = new maxdegreeholder();

	static void colornode(vertex node) {
		node.getpossiblecolor();
		node.color = node.possiblecolor;
	}

	static void updateColor(vertex node, ArrayList<vertex> vertexlist) {

		for (int i = 0; i < node.edgeList.length; i++) {

			vertex edgenode = vertexlist.get(node.edgeList[i]);
			if (edgenode.color == -1) {

				if (node.color > edgenode.maxcolor)
					edgenode.maxcolor = node.color;

				if (edgenode.colormap.containsKey(node.color))
					edgenode.colormap.get(node.color).add(node.vIndex);
				else {
					ArrayList<Integer> kcoloredAdjVertex = new ArrayList<Integer>();
					kcoloredAdjVertex.add(node.vIndex);
					edgenode.colormap.put(node.color, kcoloredAdjVertex);
				}
				// edgenode.getpossiblecolor();

				int originsdegree = edgenode.saturationdegree;
				edgenode.renewSdegree();

				degreemap map = mdh.map;
				int tempsdegree = edgenode.saturationdegree;

				if (map.sdegreemap.containsKey(tempsdegree)) {
					map.sdegreemap.get(tempsdegree).put(edgenode.vIndex, edgenode);
				} else {
					HashMap<Integer, vertex> newsdegreeList = new HashMap<Integer, vertex>();
					newsdegreeList.put(edgenode.vIndex, edgenode);
					map.sdegreemap.put(tempsdegree, newsdegreeList);

				}

				if (originsdegree != tempsdegree) {
					HashMap<Integer, vertex> odegree = mdh.map.sdegreemap.get(originsdegree);
					odegree.remove(vertexlist.get(node.edgeList[i]).vIndex);
					if (odegree.size() == 0) {
						mdh.map.sdegreemap.remove(originsdegree);
					}
				}//vertexlist.get(node.edgeList[i]).sdegreerenewtime != 1 && 
			}
		}

		return;

	}

	public static void main(String[] args) throws IOException {

		int numofv = 0;
		double dens = 1;
		int numcolor = 0;
		File file = new File("outputdenseC.txt");
		file.createNewFile();
		FileWriter out = new FileWriter(file);

		for (int sample = 1; sample <= 10; sample++) {

			numofv = sample * 10;
			numcolor = 0;
			globaltimer.timer = 0;

			for (int time = 0; time < 100; time++) {

				ArrayList<vertex> vertexlist = new ArrayList<vertex>();
				// int[][] adjm = { { 1, 6 }, { 0, 2, 4, 7, 6 }, { 1, 4, 9, 5, 3
				// },
				// { 2, 5, 13 }, { 1, 2, 9, 8 }, { 2, 3, 11 },
				// { 0, 1, 8 }, { 1, 8 }, { 4, 6, 7, 9, 10 }, { 2, 4, 11, 8, 10
				// }, {
				// 8, 9, 11, 12 }, { 5, 9, 13, 12, 10 },
				// { 10, 11, 13 }, { 3, 11, 12 } };

				int[][] adjm = vertex.generateGraph(numofv, dens);
				for (int i = 0; i < adjm.length; i++) {
					System.out.print(i + ": ");
					for (int j = 0; j < adjm[i].length; j++)
						System.out.print(adjm[i][j] + " ");
					System.out.print("\n");
				}
				System.out.print("\n");

				for (int i = 0; i < adjm.length; i++) {

					vertex tvertex = new vertex();
					tvertex.vIndex = i;
					tvertex.color = -1;
					vertexlist.add(tvertex);
				}

				int firstindex = 0;
				int firstmaxadegree = 0;
				HashMap<Integer, vertex> oList = new HashMap<Integer, vertex>();

				for (int i = 0; i < adjm.length; i++) {
					vertexlist.get(i).edgeList = adjm[i];
					vertexlist.get(i).adjacencydegree = adjm[i].length;
					vertexlist.get(i).saturationdegree = 0;
					vertexlist.get(i).oindex = i;
					if (vertexlist.get(i).adjacencydegree > firstmaxadegree) {
						firstmaxadegree = vertexlist.get(i).adjacencydegree;
						firstindex = i;
					}
					oList.put(i, vertexlist.get(i));
				}
				mdh.map.sdegreemap.put(0, oList);

				vertex fnode = vertexlist.get(firstindex);
				fnode.color = 0;
				updateColor(fnode, vertexlist);
				mdh.map.removecolorednode(fnode);

				// maxdegreeholder mdh = new maxdegreeholder();

				for (int i = 0; i < vertexlist.size() - 1; i++) {

					mdh.map.getmaxsdegree();
					mdh.map.getbestnode();
					fnode = vertexlist.get(mdh.map.bestnodeindex);
					colornode(fnode);
					updateColor(fnode, vertexlist);
					mdh.map.removecolorednode(fnode);

				}

				// for (int i=0;i<pList.size();i++)
				// System.out.print(pList.get(i).adjacencydegree);
				System.out.print("Color: ");
				int tempnumcolor = 0;
				for (int i = 0; i < vertexlist.size(); i++) {
					System.out.print(vertexlist.get(i).color + " ");
					if (tempnumcolor <= vertexlist.get(i).color) {
						tempnumcolor = vertexlist.get(i).color;
					}
				}
				tempnumcolor++;
				numcolor += tempnumcolor;
				System.out.print("\n");
				System.out.print("Index: ");
				for (int i = 0; i < vertexlist.size(); i++) {
					System.out.print(vertexlist.get(i).vIndex + " ");
				}
				System.out.print("\n");
				System.out.print("Adegree: ");
				for (int i = 0; i < vertexlist.size(); i++) {
					System.out.print(vertexlist.get(i).adjacencydegree + " ");
				}
				System.out.print("\n");
				for (int i = 0; i < vertexlist.size(); i++)
					for (int j = 0; j < vertexlist.get(i).edgeList.length; j++)
						if (vertexlist.get(i).color == vertexlist.get(vertexlist.get(i).edgeList[j]).color) {
							System.out.print("\n" + "It's bug! " + i + " "
									+ vertexlist.get(vertexlist.get(i).edgeList[j]).vIndex + "\n");
						}

			}
			System.out.print("\n" + globaltimer.timer / 100);
			out.write("num of v: " + sample * 10 + "\r\n" + "num of color: " + numcolor / 100 + "\r\n" + "complexity: "
					+ globaltimer.timer / 100 + "\r\n" + "\r\n");
		}
		out.close();

	}

}