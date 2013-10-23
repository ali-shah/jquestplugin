/**
 * Copyright 2009 Jens Dietrich Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package nz.ac.massey.cs.jquest.graphbuilder.depfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.gql4jung.io.JarReader;
import nz.ac.massey.cs.guery.adapters.jungalt.io.graphml.ProgressListener;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.GraphBuilder;
import nz.ac.massey.cs.jdg.Progress;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Utility methods.
 * @author jens dietrich, Ali
 */
public class Utils {
	
	public final static String SEP = ",";
	
	public static DirectedGraph<nz.ac.massey.cs.jdg.TypeNode, Dependency> loadGraph(String name, final IProgressMonitor monitor) throws Exception {

		DirectedGraph<nz.ac.massey.cs.jdg.TypeNode, Dependency> g = null;
		File in = new File(name);
//		JarReader reader = new JarReader(in);
//		monitor.beginTask("loading graph", 100);
//		ProgressListener l = new ProgressListener(){
//
//			@Override
//			public void progressMade(int progres, int total) {
//				monitor.worked(progres);
//			}
//			
//		};
//		reader.addProgressListener(l);
//		g = reader.readGraph();
		
		Progress p = new Progress() {

			@Override public void progressMade(int progress, int total,String name) {
				monitor.worked(progress);
				System.out.println("building graph " + progress + "/" + total + " - " + name);
			}

			@Override public void done() {
				System.out.println("done");
			}
			
		};
		g = new GraphBuilder().extractDependencyGraph(p,in);
		monitor.done();
		return g; 
		
	}
	
	public static DirectedGraph<TypeNode, Dependency> loadGraph(List<File> files, final IProgressMonitor monitor) throws Exception {

		DirectedGraph<TypeNode, Dependency> g = null;
//		JarReader reader = new JarReader(files);
//		monitor.beginTask("loading graph", 100);
//		ProgressListener l = new ProgressListener(){
//
//			@Override
//			public void progressMade(int progres, int total) {
//				monitor.worked(progres);
//			}
//			
//		};
//		reader.addProgressListener(l);
//		g = reader.readGraph();
		Progress p = new Progress() {

			@Override public void progressMade(int progress, int total,String name) {
				monitor.worked(progress);
				System.out.println("building graph " + progress + "/" + total + " - " + name);
			}

			@Override public void done() {
				System.out.println("done");
			}
			
		};
		File[] fileArray = new File[files.size()];
		int i = 0;
		for(File f : files) {
			fileArray[i++] = f;
		}
		g = new GraphBuilder().extractDependencyGraph(p,fileArray);
		monitor.done();
		return g; 
		
	}
	
	public static void log(Object... s) {
		for (Object t:s) {
			System.out.print(t);
		}
		System.out.println();
	}
	// filter to allow only files wit a certain extension
	public static FileFilter getExtensionFileFilter(final String extension) {
		return new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(extension);
			}
		};
	}
	// filter to exclude hidden files
	public static FileFilter getExcludeHiddenFilesFilter() {
		return new FileFilter(){
			@Override
			public boolean accept(File f) {
				return !f.getName().startsWith(".");
			}
		};
	}
	
	// get or add a folder
	public static File getOrAddFolder(String name) { 
		File f = new File(name);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	
	public static String replaceExtension(String name,String oldExtension,String newExtension) {
		String n = name.endsWith(oldExtension)?name.substring(0,name.length()-oldExtension.length()):name;
		return n+newExtension;
	}
	
	public static String removeExtension(String name,String extension) {
		return name.endsWith(extension)?name.substring(0,name.length()-extension.length()):name;
	}
	
	public static String readValueFromCSV(File file,int col, int row) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int c = -1;
		while ((line=reader.readLine())!=null) {
			c=c+1;
			if (c==row) break;
		}
		reader.close();
			
		if (line==null) {
			return null;
		}
		else {
			// scan line
			c = -1;
			String token = null;
			for (StringTokenizer tok=new StringTokenizer(line,SEP);tok.hasMoreTokens();) {
				c=c+1;
				token = tok.nextToken();
				if (c==col) return token;			
			}
		}
		return null;
	}
	
	public static int readIntValueFromCSV(File file,int col, int row) throws Exception {
		
		return Integer.valueOf(readValueFromCSV(file,col,row));
	}
	
	public static String getPackageName (String fullClassName) {
		return fullClassName.substring(0,fullClassName.lastIndexOf('.'));
	}
	public static String getClassName (String fullClassName) {
		return fullClassName.substring(fullClassName.lastIndexOf('.'));
	}
	
}
