# Part A
## Overview
This implementation of OrderBook aims to provide high throughput for insertion and deletion of orders along with fast retrieval of orders based on side and level. 

## Data Structure Design
Class OrderEntry is created to be the entry stored in the OrderBook. It servers the following purposes:
- The provided Order class is immutable, OrderEntry allows editing of size with replaced Order object.
- It contains price as int to prevent floating point comparison issues. This implementation assumes that the price precision is up to 2 decimal places, and this can be adjusted based on the requirement.
- It contains Side as enum to prevent invalid side values and improves readability.
- It contains a timestamp on insertion. Although the timestamp is not used in Order comparison, it helps debugging and logging.

The OrderBook class has a ConcurrentHashMap from order id to OrderEntry, this allows fast look up based on order id.
OrderEntries of the same side and integer price are grouped in OrderLevelBucket. OrderBook contains two sorted maps of OrderLevelBucket, one for BID (in reversed order of integer price) and one for OFFER(in natural order of integer price). 
The implementation of the sorted map is ConcurrentHashMap to allow concurrent access and modification.
Inside OrderLevelBucket, OrderEntries are stored in LinkedHashMap to maintain insertion order. 
The types of collections used here are the thread safe version, except for LinkedHashMap. We will address the thread safety of LinkedHashMap in the next section.

## Thread Safety
To prevent multiple threads from modifying the OrderBook, I added a BlookingQueue to store the actions, and a separate single thread to execute the actions.
This allows the add/modify/delete functions to be thread safe without synchronization. 
Allowing single thread access also addresses the thread safety issue of LinkedHashMap.

## Complexity Analysis
I will assign the number of entry to be N, the number of levels on one side to be L, input argument level to be l, and the average number of orders in a level to be M.
N = L * M
Number of levels L should be a relatively small number, as most of the orders are near the market price.

addOrder/deleteOrder/modifyOrder functions themselves are O(1) operations, as they only insert the action into the BlockingQueue.

During processing, the complexity is as follows:
- Add: O(1) to insert into ConcurrentHashMap, O(L) to lookup or insert into ConcurrentSkipLevelMap and O(1) to insert into LinkedHashMap. Overall O(L)
- Delete: O(1) to delete from ConcurrentHashMap, O(L) to lookup and maybe delete from ConcurrentSkipLevelMap and O(1) to delete from LinkedHashMap. Overall O(L)
- Modify: O(1) to lookup OrderEntry in ConcurrentHashMap, and replace the Order object. No need to modify the sorted map. Overall O(1)

Retrieval functions:
- getLevelPrice: O(l) to iterate ConcurrentSkipLevelMap, then O(1) to get the price from OrderLevelBucket. Overall O(l), l tends to be small number, 
as people mostly care about the best prices in the market.
- getLevelTotalSize: O(l) to iterate ConcurrentSkipLevelMap, then O(M) to sum up the size in OrderLevelBucket. Overall O(l * M).
- getAllOrderOnSide: O(L * M) to iterate all levels and all orders in the level. Overall O(N).
The retrieval functions are synchronised, the main concern is the iteration and lookup of ConcurrentSkipListMap, the output might be undetermined if the map is updated during the iteration.
There are other options to resolve this issue if synchronisation is not acceptable, such as using a snapshot of the map, or retrying the operation.

## Test
OrderBookTest tests the functionality of the class.
I also included a StressTest to show how the class behaves in concurrent environment.

# Part B
In real world scenario, the BlockingQueue can be replaced by a message queue, such as Kafka.
This allows actions coming from different source for the same OrderBook. 
The sorted map can be replaced by a database, such as Redis to allow persistence, and processing of actions can be parallelized across multiple processes.