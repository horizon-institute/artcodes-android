package uk.ac.horizon.artcodes.storage;

public interface StoreListener<T>
{
	void onItemChanged(T item);
}
