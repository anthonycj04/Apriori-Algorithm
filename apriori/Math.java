package apriori;

public class Math {
	// used to get the hash table size, has a maximum value though
	public static int calculateCombination(long n, long r){
		if (n < r)
			return 0;
		long result = n;
		for (int i = 1; i < r; i++)
			result *= (n - i);
		for (int i = 1; i <= r; i++)
			result /= i;
		if (result > Config.maxHashTableSize)
			return Config.maxHashTableSize;
		else
			return (int) result;
	}

	public static int hash(NonDuplicateArrayList<Integer> nonDuplicateArrayList, int hashTableSize){
		int sum = 0;
		for (Integer i: nonDuplicateArrayList)
			sum += i;
		return sum % hashTableSize;
	}
}
