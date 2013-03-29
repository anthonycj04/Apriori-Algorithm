package apriori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class NonDuplicateArrayList<E> extends ArrayList<E>{
	private static final long serialVersionUID = 1L;
	private HashSet<E> hashSet;

	public NonDuplicateArrayList(){
		super();
		hashSet = new HashSet<E>();
	}

	public NonDuplicateArrayList(Collection<? extends E> c){
		super(c);
		hashSet = new HashSet<E>(c);
	}

	public NonDuplicateArrayList(int initialCapacity){
		super(initialCapacity);
		hashSet = new HashSet<E>();
	}

	@Override
	public boolean add(E e){
		boolean added = hashSet.add(e);
		if (added)
			super.add(e);
		return added;
	}

	@Override
	public E remove(int index){
		E e;
		e = super.remove(index);
		hashSet.remove(e);
		return e;
	}

	@Override
	public boolean remove(Object o){
		boolean removed = super.remove(o);
		if (removed)
			hashSet.remove(o);
		return removed;
	}

	public void printHashSet(){
		System.out.println(hashSet.toString());
	}

	@Override
	public void clear(){
		super.clear();
		hashSet.clear();
	}

	public HashSet<E> getHashSet(){
		return hashSet;
	}

	public boolean equals(NonDuplicateArrayList<E> nonDuplicateArrayList){
		return hashSet.equals(nonDuplicateArrayList.getHashSet());
	}

	public boolean contains(NonDuplicateArrayList<E> nonDuplicateArrayList){
		return hashSet.contains(nonDuplicateArrayList.getHashSet());
	}

	@Override
	public boolean contains(Object o){
		return hashSet.contains(o);
	}

	public void retainAll(NonDuplicateArrayList<E> nonDuplicateArrayList){
		// NonDuplicateArrayList<E> result = new Non
	}

}
