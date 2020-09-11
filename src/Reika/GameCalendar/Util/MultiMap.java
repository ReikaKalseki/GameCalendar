/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GameCalendar.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiMap<K, V> {

	private final Map<K, Collection<V>> data;

	private boolean nullEmpty = false;
	private Comparator<V> ordering = null;

	private final CollectionType factory;

	public MultiMap() {
		this(CollectionType.LIST);
	}

	public MultiMap(CollectionType cf) {
		this(cf, null);
	}

	public MultiMap(CollectionType cf, MapDeterminator md) {
		factory = cf;
		data = md != null ? md.getMapType() : new HashMap();
	}

	public Collection<V> put(K key, Collection<V> value) {
		if (ordering != null && value instanceof List)
			Collections.sort((List)value, ordering);
		Collection<V> old = data.put(key, factory.createCollection(value));
		return old;
	}

	public Collection<V> putValue(K key, V value) {
		Collection<V> ret = this.remove(key);
		this.addValue(key, value, false, false);
		return ret;
	}

	public boolean addValue(K key, V value) {
		return this.addValue(key, value, false);
	}

	public boolean addValues(K key, Collection<V> values) {
		boolean flag = false;
		for (V v : values)
			flag |= this.addValue(key, v, false);
		return flag;
	}

	public boolean addValue(K key, V value, boolean allowCopies) {
		return this.addValue(key, value, true, allowCopies);
	}

	private boolean addValue(K key, V value, boolean load, boolean copy) {
		Collection<V> li = null;
		if (load)
			li = data.get(key);
		if (!load || li == null) {
			li = this.createCollection();
			data.put(key, li);
		}
		if (copy || !factory.allowsDuplicates || /*!li.contains(value)*/!this.containsValueForKey(key, value)) {
			boolean ret = li.add(value);
			if (ordering != null && li instanceof List)
				Collections.sort((List)li, ordering);
			return ret;
		}
		return false;
	}

	public void sort(Comparator<V> sorter) {
		for (Collection<V> c : this.data.values()) {
			if (c instanceof List)
				Collections.sort((List)c, sorter);
		}
	}

	private Collection<V> createCollection() {
		return this.factory.createCollection();
	}

	public Collection<V> remove(K key) {
		Collection<V> ret = data.remove(key);
		return ret != null ? ret : new ArrayList();
	}

	public Collection<V> get(K key) {
		Collection<V> c = data.get(key);
		if (c == null && this.nullEmpty)
			return null;
		return c != null ? c : this.factory.createCollection(); //Internal NPE protection
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	public int getSize() {
		return data.size();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public void clear() {
		data.clear();
	}

	public Collection<K> keySet() {
		return Collections.unmodifiableCollection(data.keySet());
	}

	public Collection<Collection<V>> values() {
		return Collections.unmodifiableCollection(data.values());
	}

	public Collection<V> allValues(boolean duplicates) {
		Collection<V> li = duplicates ? new ArrayList() : new HashSet();
		for (Collection<V> c : data.values()) {
			li.addAll(c);
		}
		return li;
	}

	public void shuffleValues() {
		for (Collection c : data.values()) {
			if (c instanceof List)
				Collections.shuffle((List)c);
		}
	}

	public int totalSize() {
		int s = 0;
		for (Collection<V> c : data.values())
			s += c.size();
		return s;
	}

	public boolean containsValueForKey(K key, V value) {
		Collection<V> c = data.get(key);
		return c != null && c.contains(value);
	}

	public boolean remove(K key, V value) {
		Collection<V> c = data.get(key);
		boolean flag = c != null && c.remove(value);
		if (flag && c.isEmpty()) {
			this.data.remove(key);
		}
		return flag;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MultiMap && this.data.equals(((MultiMap)o).data);
	}

	public MultiMap<K, V> setNullEmpty() {
		nullEmpty = true;
		return this;
	}

	public MultiMap<K, V> setOrdered(Comparator<V> order) {
		this.ordering = order;
		return this;
	}

	public void putAll(Map<K, V> map) {
		for (K k : map.keySet()) {
			this.addValue(k, map.get(k));
		}
	}

	public void putAll(MultiMap<K, V> map) {
		for (K k : map.keySet()) {
			for (V v : map.get(k)) {
				this.addValue(k, v);
			}
		}
	}

	public void removeValue(V val) {
		for (K key : this.keySet()) {
			this.remove(key, val);
		}
	}

	public static interface MapDeterminator<K, V> {

		public Map<? extends K, ? extends V> getMapType();

	}

	public static enum CollectionType {
		LIST(true),
		HASHSET(false);

		private final boolean allowsDuplicates;

		private CollectionType(boolean b) {
			allowsDuplicates = b;
		}

		public Collection createCollection() {
			switch(this) {
				case HASHSET:
					return new HashSet();
				case LIST:
					return new ArrayList();
			}
			return null;
		}

		public Collection createCollection(Collection c) {
			switch(this) {
				case HASHSET:
					return new HashSet(c);
				case LIST:
					return new ArrayList(c);

			}
			return null;
		}
	}

	public static final class ConcurrencyDeterminator implements MapDeterminator {

		@Override
		public Map getMapType() {
			return new ConcurrentHashMap();
		}

	}

	public static final class SortedDeterminator implements MapDeterminator {

		private final Comparator sorter;

		public SortedDeterminator() {
			this(null);
		}

		public SortedDeterminator(Comparator c) {
			sorter = c;
		}

		@Override
		public Map getMapType() {
			return new TreeMap(sorter);
		}

	}

}
