package scot.massie.lib.collections.trees;

import scot.massie.lib.utils.tuples.Pair;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

abstract class TreeTest
{
    /*

    has items
        hasItems
            empty
            item at root
            null at root
            item under root
        hasNonRootItems
            empty
            item at root
            item under root
            null under root
        hasItemsAtOrUnder
            empty
            item at root
            item above path
            item at path
            item under path
            null under path
            item in different unrelated path
        hasItemsUnder
            empty
            item at root
            item above path
            item at path
            item under path
            null under path
            item in different unrelated path
        hasItemsAlong
            empty
            item at root
            item above path
            null above path
            item at path
            item under path
            item in different unrelated path
        hasItemAt
            empty
            item at root
            item above path
            item at path
            null at path
            item under path
            item in different unrelated path
        hasRootItem
            empty
            item at root
            item under root
    is empty
        // "is empty" methods should just be negations of the equivalent "has items" methods.
    get stored items, root
        getRootItem
            empty
            item at root
            null at root
        getRootItemOr
            empty
            item at root
            null at root
        getRootItemOrDefaultOfAnyType
            empty
            item at root
            null at root
        getRootItemOrNull
            empty
            item at root
            null at root
    get stored items, leaves
        getAt
            empty
            item at path
            null at path
        getAtOr
            empty
            item at path
            null at path
        getAtOrDefaultOfAnyType
            empty
            item at path
            null at path
        getAtOrNull
            empty
            item at path
            null at path
    get multiple stored items
        getItems
            empty
            populated
        getItemsInOrder
            empty
            populated
        getItemsWhere
            empty
            populated, matches
            populated, no matches
        getItemsWherePath
            empty
            populated, matches
            populated, no matches
        getItemsInOrderWhere
            empty
            populated, matches
            populated, no matches
        getItemsInOrderWherePath
            empty
            populated, matches
            populated, no matches
        getItemsUnderRoot
            empty
            populated
        getItemsUnderRootInOrder
            empty
            populated
        getItemsAtAndUnder
            empty
            populated
        getItemsAtAndUnderInOrder
            empty
            populated
        getItemsUnder
            empty
            populated
            populated, empty at & under path
        getItemsUnderInOrder
            empty
            populated
            populated, empty at & under path
        getItemsAlong
            empty
            populated
            populated, empty along path
        getItemsUnderRootAlong
            empty
            populated
            populated, empty along path
        getImmediateItems
            empty
            populated
        getImmediateItemsInOrder
            empty
            populated
        getRootAndImmediateItems
            empty
            populated
        getRootAndImmediateItemsInOrder
            empty
            populated
        getImmediatelyUnder
            empty
            populated
            populated, empty under path
        getImmediatelyUnderInOrder
            empty
            populated
            populated, empty under path
        getAtAndImmediatelyUnder
            empty
            populated
            populated, empty under path
        getAtAndImmediatelyUnderInOrder
            empty
            populated
            populated, empty under path
    get entries
        getEntries
            empty
            populated
        getEntriesInOrder
            empty
            populated
        getEntriesWhere
            empty
            populated, matches
            populated, no matches
        getEntriesWherePath
            empty
            populated, matches
            populated, no matches
        getEntriesInOrderWhere
            empty
            populated, matches
            populated, no matches
        getEntriesInOrderWherePath
            empty
            populated, matches
            populated, no matches
        getEntriesUnderRoot
            empty
            populated
        getEntriesUnderRootInOrder
            empty
            populated
        getEntriesAtAndUnder
            empty
            populated
        getEntriesAtAndUnderInOrder
            empty
            populated
        getEntriesUnder
            empty
            populated
            populated, empty at & under path
        getEntriesUnderInOrder
            empty
            populated
            populated, empty at & under path
        getEntriesAlong
            empty
            populated
            populated, empty along path
        getEntriesUnderRootAlong
            empty
            populated
            populated, empty along path
        getImmediateEntries
            empty
            populated
        getImmediateEntriesInOrder
            empty
            populated
        getRootAndImmediateEntries
            empty
            populated
        getRootAndImmediateEntriesInOrder
            empty
            populated
        getEntriesImmediatelyUnder
            empty
            populated
            populated, empty under path
        getEntriesImmediatelyUnderInOrder
            empty
            populated
            populated, empty under path
        getEntriesAtAndImmediatelyUnder
            empty
            populated
            populated, empty under path
        getEntriesAtAndImmediatelyUnderInOrder
            empty
            populated
            populated, empty under path
    get branches
        getBranch
            branch with items
            branch with no items
        getBranches
            empty
            populated
    get paths
        getPaths
            empty
            populated
        getPathsInOrder
            empty
            populated
    get branch views
        getBranchView
            has items from tree
            add item in branch
            add item in source tree
            remove item from branch
            remove item from source tree
        getBranchViews
            empty
            populated
    set items in tree
        setRootItem
            empty
            has root item
        setRootItemIfAbsent
            empty
            has root item
        setRootItemIf
            empty, test succeeds
            empty, test fails
            has root item, test succeeds
            has root item, test fails
        setAt
            empty
            item at path
            setting null
        setAtIfAbsent
            empty
            item at path
            setting null
        setAtIf
            empty
            item at path
            setting null
    remove items in tree
        clear
        clearUnderRoot
        clearAtAndUnder
        clearUnder
        clearWhere
        clearWherePath
        clearRoot
        clearRootIf
        clearAt
        clearAtIf
    conversion
        toCollection
        toList
        toOrderedList
        withReversedKeys
    stats
        size
        countDepth
    string representations
        toTreeString
        toTreeString (ordered with comparator)

     */
    
    protected abstract boolean allowsNull();
    
    protected abstract Tree<String, Integer> getNewTree();
    
    @SuppressWarnings("unchecked")
    protected abstract Tree<String, Integer> getNewTree(Pair<String, Integer>... items);

    /**
     * Should have a root item.
     * @return A populated tree.
     */
    protected abstract Tree<String, Integer> getPopulatedTree1();

    /**
     * Should have no root item.
     * @return A populated tree.
     */
    protected abstract Tree<String, Integer> getPopulatedTree2();
}