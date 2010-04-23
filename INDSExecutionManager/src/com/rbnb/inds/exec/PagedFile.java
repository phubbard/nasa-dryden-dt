/*
	PagedFile.java
	
	Copyright 2008 Creare Inc.
	
	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 
	
	http://www.apache.org/licenses/LICENSE-2.0 
	
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
	
	---  History  ---
	2009/09/24  WHF  Created.
	2009/10/06  WHF  Added page count method; simplified cacheing logic for 
		final empty page.
*/

package com.rbnb.inds.exec;

import java.io.File;

/**
  * A class to serve pages from a large text file.
  *
  * <p> Assumptions:
  * <ol><li>The file never decreases in size.</li>
  * <li>New file content is appended to the end.</li>
  * These assumptions are valid for log files.
*/
public class PagedFile
{
	public PagedFile(File f, int pageSize)
	{
		file = f;
		this.pageSize = pageSize;
	}
	
	public String getPage(int pageSize, int page)
	{
		if (this.pageSize != pageSize) {
			// Cache invalid; repaginate:
			this.pageSize = pageSize;
			nCache = 0;
			cachedFileSize = 0;
		}
		paginate();  // adds new pages to cache
		
		if (page < 0) {
			page = nCache + 1 + page;
			if (page < 0) page = 0;
		}
		if (page > nCache) page = nCache;
		
		int start, end, toRead;
		if (page == 0) start = 0;
		else start = pageCache[page - 1];
		
		if (page == nCache) end = cachedFileSize;
		else end = pageCache[page];
		
		toRead = end - start;
		
		java.io.StringWriter sw = new java.io.StringWriter();
		char buff[] = new char[1024];
		
		try {
			java.io.FileReader fr = new java.io.FileReader(file);
			fr.skip(start);
			int nRead;
			while ((nRead = fr.read(buff, 0, Math.min(toRead, buff.length)))>0){
				sw.write(buff, 0, nRead);
				toRead -= nRead;
			}
			fr.close();
		} catch (java.io.IOException ioe) {
			return "CONFIG FILE READ FAILED!!!";
		}
		String result = sw.toString();

		return result;
	}
	
	public int getPageCount()
	{
		paginate();
		return nCache + 1;		
	}
	
	private void paginate()
	{
		if ((int) file.length() == cachedFileSize) return; // cache up to date
		
		char buff[] = new char[1024];
		
		try {
			java.io.FileReader fr = new java.io.FileReader(file);
			int nRead, lineCount = 0, charCount;
			
			if (nCache > 0) {
				charCount = pageCache[nCache-1];
				fr.skip(charCount);
			} else
				charCount = 0;

			while ((nRead = fr.read(buff)) > 0) {
				for (int ii = 0; ii < nRead; ++ii, ++charCount) {
					if (buff[ii] == '\n' && ++lineCount == pageSize) {
						pageCache[nCache++] = charCount;
						if (nCache == pageCache.length)
							increaseCacheSize();
						lineCount = 0;
					}
				}
			}
			
			if (nCache > 0 && lineCount == 0) { 
				// last page is empty, remove:
				--nCache;
			}
				
			fr.close();
			cachedFileSize = (int) file.length();
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void increaseCacheSize()
	{
		int newLen = pageCache.length*2;
		int[] newCache = new int[newLen];
		System.arraycopy(pageCache, 0, newCache, 0, pageCache.length);
		pageCache = newCache;
	}
	
	private final File file;
	private int 
		pageSize,             // size of each page used in cache   
		nCache,               // number of valid cache entries
		cachedFileSize;       // size of file last time pagination was performed
	private int[] pageCache = new int[128];

	/**
	  * For testing.
	  */
	public static void main(String[] args)
	{
		File f = new File(args[0]);
		int pageSize = Integer.parseInt(args[1]);
		int page = Integer.parseInt(args[2]);

		PagedFile pf = new PagedFile(f, pageSize);
		System.out.println("# of pages: "+pf.getPageCount());
		System.out.println(pf.getPage(pageSize, page));
	}		
}

