/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.pvpgames.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigUtils {
	
	public static String toSaveableFormat(String s) {
		if (s != null) {
			return s.replace("\n", "\\n").replace("§", "&");
		}
		
		return s;
	}
	
	public static String replaceLineBreaks(String s) {
		if (s != null) {
			return s.replace("\\n", "\n");
		}
		
		return s;
	}
	
	public static String nullifyIfEmpty(String s) {
		if (s != null && s.isEmpty()) {
			return null;
		}
		
		return s;
	}
	
	public static String emptifyIfNull(String s) {
		if (s == null) {
			return "";
		}
		
		return s;
	}
	
	public static boolean getBoolean(Map<?, ?> config, String path) {
		Object object = config.get(path);
		if (object instanceof Boolean) {
			return ((Boolean) object).booleanValue();
		}
		
		return false;
	}
	
	public static int getInt(Map<?, ?> config, String path) {
		Object object = config.get(path);
		if (object instanceof Number) {
			return ((Number) object).intValue();
		}
		
		return 0;
	}
	
	public static String getString(Map<?, ?> config, String path) {
		Object object = config.get(path);
		if (object instanceof String) {
			return (String) object;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> filter(List<?> list, Class<T> clazz) {
		List<T> newList = new ArrayList<T>();
		
		for (Object o : list) {
			if (o != null && clazz.isInstance(o)) {
				newList.add((T) o);
			}
		}
		
		return newList;
	}

}
