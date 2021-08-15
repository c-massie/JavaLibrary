package scot.massie.lib.collections.trees;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;
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
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item above path
            item at path
            item under path
            null under path
            item in different unrelated path
        hasItemsUnder
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item above path
            item at path
            item under path
            null under path
            item in different unrelated path
        hasItemsAlong
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item above path
            null above path
            item at path
            item under path
            item in different unrelated path
        hasItemAt
            root, empty
            root, item at root
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
            null at root
            item under root
    is empty
        // "is empty" methods should just be negations of the equivalent "has items" methods.
    get stored items, root
        getRootItem
            empty
            item at root
            null at root
            item under root
        getRootItemOr
            empty
            item at root
            null at root
            item under root
        getRootItemOrDefaultOfAnyType
            empty
            item at root
            null at root
            item under root
        getRootItemOrNull
            empty
            item at root
            null at root
            item under root
    get stored items, leaves
        getAt
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item at path
            null at path
        getAtOr
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item at path
            null at path
        getAtOrDefaultOfAnyType
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
            item at path
            null at path
        getAtOrNull
            root, empty
            root, item at root
            root, item under root
            empty
            item at root
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
            empty
            populated
        clearUnderRoot
            empty
            populated
        clearAtAndUnder
            empty
            populated
            populated, empty under path
        clearUnder
            empty
            populated
            populated, empty under path
        clearWhere
            empty
            populated
            populated, no matches
        clearWherePath
            empty
            populated
            populated, no matches
        clearRoot
            no root item
            root item
        clearRootIf
            no root item
            root item, test succeeds
            root item, test fails
        clearAt
            no item at path
            item at path
        clearAtIf
            no item at path
            item at path, test succeeds
            item at path, test fails
    conversion
        toCollection
            empty
            populated
        toList
            empty
            populated
        toOrderedList
            empty
            populated
        withReversedKeys
            empty
            populated
    stats
        size
            empty
            populated
        countDepth
            empty
            populated
    string representations
        toTreeString
            empty
            populated
        toTreeString (ordered with comparator)
            empty
            populated

     */
    
    protected abstract boolean allowsNull();
    
    protected abstract Tree<String, Integer> getNewTree();
    
    @SuppressWarnings("unchecked")
    protected abstract Tree<String, Integer> getNewTree(Pair<TreePath<String>, Integer>... items);

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

    @Test
    void hasItems_empty()
    { assertThat(getNewTree().hasItems()).isFalse(); }

    @Test
    void hasItems_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>(), 1)).hasItems()).isTrue(); }

    @Test
    void hasItems_nullAtRoot()
    {
        AbstractBooleanAssert<?> assertion = assertThat(getNewTree(new Pair<>(new TreePath<>(), null)).hasItems());

        if(allowsNull())    assertion.isTrue();
        else                assertion.isFalse();
    }

    @Test
    void hasItems_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasItems()).isTrue(); }

    @Test
    void hasNonRootItems_empty()
    { assertThat(getNewTree().hasNonRootItems()).isFalse(); }

    @Test
    void hasNonRootItems_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>(), 1)).hasNonRootItems()).isFalse(); }

    @Test
    void hasNonRootItems_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasNonRootItems()).isTrue(); }

    @Test
    void hasNonRootItems_nullUnderRoot()
    {
        AbstractBooleanAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), null)).hasNonRootItems());

        if(allowsNull())    assertion.isTrue();
        else                assertion.isFalse();
    }


    @Test
    void hasItemsAtOrUnder_root_empty()
    { assertThat(getNewTree().hasItemsAtOrUnder(TreePath.root())).isFalse(); }

    @Test
    void hasItemsAtOrUnder_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsAtOrUnder(TreePath.root())).isTrue(); }

    @Test
    void hasItemsAtOrUnder_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasItemsAtOrUnder(TreePath.root())).isTrue(); }

    @Test
    void hasItemsAtOrUnder_path_empty()
    { assertThat(getNewTree().hasItemsAtOrUnder(new TreePath<>("first", "second"))).isFalse(); }

    @Test
    void hasItemsAtOrUnder_path_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsAtOrUnder(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsAtOrUnder_path_itemAbovePath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1))
                           .hasItemsAtOrUnder(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsAtOrUnder_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second"), 1))
                           .hasItemsAtOrUnder(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsAtOrUnder_path_itemUnderPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), 1))
                           .hasItemsAtOrUnder(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsAtOrUnder_path_nullUnderPath()
    {
        AbstractBooleanAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), null))
                                     .hasItemsAtOrUnder(new TreePath<>("first", "second")));
        
        if(allowsNull())
            assertion.isTrue();
        else
            assertion.isFalse();
    }

    @Test
    void hasItemsAtOrUnder_path_itemInDifferentPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("un", "dos"), 1))
                           .hasItemsAtOrUnder(new TreePath<>("first", "second")))
                .isFalse();
    }


    @Test
    void hasItemsUnder_root_empty()
    { assertThat(getNewTree().hasItemsUnder(TreePath.root())).isFalse(); }

    @Test
    void hasItemsUnder_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsUnder(TreePath.root())).isFalse(); }

    @Test
    void hasItemsUnder_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasItemsUnder(TreePath.root())).isTrue(); }

    @Test
    void hasItemsUnder_path_empty()
    { assertThat(getNewTree().hasItemsUnder(new TreePath<>("first", "second"))).isFalse(); }

    @Test
    void hasItemsUnder_path_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsUnder(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsUnder_path_itemAbovePath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1))
                           .hasItemsUnder(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsUnder_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second"), 1))
                           .hasItemsUnder(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsUnder_path_itemUnderPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), 1))
                           .hasItemsUnder(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsUnder_path_nullUnderPath()
    {
        AbstractBooleanAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), null))
                                     .hasItemsUnder(new TreePath<>("first", "second")));

        if(allowsNull())
            assertion.isTrue();
        else
            assertion.isFalse();
    }

    @Test
    void hasItemsUnder_path_itemInDifferentPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("un", "dos"), 1))
                           .hasItemsUnder(new TreePath<>("first", "second")))
                .isFalse();
    }


    @Test
    void hasItemsAlong_root_empty()
    { assertThat(getNewTree().hasItemsAlong(TreePath.root())).isFalse(); }

    @Test
    void hasItemsAlong_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsAlong(TreePath.root())).isTrue(); }

    @Test
    void hasItemsAlong_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasItemsAlong(TreePath.root())).isFalse(); }

    @Test
    void hasItemsAlong_path_empty()
    { assertThat(getNewTree().hasItemsAlong(new TreePath<>("first", "second"))).isFalse(); }

    @Test
    void hasItemsAlong_path_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemsAlong(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsAlong_path_itemAbovePath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1))
                           .hasItemsAlong(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsAlong_path_nullAbovePath()
    {
        AbstractBooleanAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first"), null))
                                     .hasItemsAlong(new TreePath<>("first", "second")));

        if(allowsNull())
            assertion.isTrue();
        else
            assertion.isFalse();
    }

    @Test
    void hasItemsAlong_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second"), 1))
                           .hasItemsAlong(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemsAlong_path_itemUnderPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), 1))
                           .hasItemsAlong(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemsAlong_path_itemInDifferentPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("un", "dos"), 1))
                           .hasItemsAlong(new TreePath<>("first", "second")))
                .isFalse();
    }


    @Test
    void hasItemAt_root_empty()
    { assertThat(getNewTree().hasItemAt(TreePath.root())).isFalse(); }

    @Test
    void hasItemAt_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemAt(TreePath.root())).isTrue(); }

    @Test
    void hasItemAt_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("doot"), 1)).hasItemAt(TreePath.root())).isFalse(); }

    @Test
    void hasItemAt_path_empty()
    { assertThat(getNewTree().hasItemAt(new TreePath<>("first", "second"))).isFalse(); }

    @Test
    void hasItemAt_path_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasItemAt(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemAt_path_itemAbovePath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1))
                           .hasItemAt(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemAt_path_nullAtPath()
    {
        AbstractBooleanAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second"), null))
                                     .hasItemAt(new TreePath<>("first", "second")));

        if(allowsNull())
            assertion.isTrue();
        else
            assertion.isFalse();
    }

    @Test
    void hasItemAt_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second"), 1))
                           .hasItemAt(new TreePath<>("first", "second")))
                .isTrue();
    }

    @Test
    void hasItemAt_path_itemUnderPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first", "second", "third"), 1))
                           .hasItemAt(new TreePath<>("first", "second")))
                .isFalse();
    }

    @Test
    void hasItemAt_path_itemInDifferentPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("un", "dos"), 1))
                           .hasItemAt(new TreePath<>("first", "second")))
                .isFalse();
    }


    @Test
    void hasRootItem_empty()
    { assertThat(getNewTree().hasRootItem()).isFalse(); }

    @Test
    void hasRootItem_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).hasRootItem()).isTrue(); }

    @Test
    void hasRootItem_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).hasRootItem()).isFalse(); }


    @Test
    void getRootItem_empty()
    { assertThrows(NoItemAtPathException.class, () -> getNewTree().getRootItem()); }

    @Test
    void getRootItem_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getRootItem()).isEqualTo(1); }

    @Test
    void getRootItem_nullAtRoot()
    {
        if(allowsNull())
            assertThat(getNewTree(new Pair<>(TreePath.root(), null)).getRootItem()).isEqualTo(null);
        else
            assertThrows(NoItemAtPathException.class,
                         () -> getNewTree(new Pair<>(TreePath.root(), null)).getRootItem());
    }

    @Test
    void getRootItem_itemUnderRoot()
    {
        assertThrows(NoItemAtPathException.class,
                     () -> getNewTree(new Pair<>(new TreePath<>("first"), 1)).getRootItem());
    }


    @Test
    void getRootItemOr_empty()
    { assertThat(getNewTree().getRootItemOr(1)).isEqualTo(1); }

    @Test
    void getRootItemOr_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getRootItemOr(2)).isEqualTo(1); }

    @Test
    void getRootItemOr_nullAtRoot()
    {
        AbstractIntegerAssert<?> assertion = assertThat(getNewTree(new Pair<>(TreePath.root(), null)).getRootItemOr(1));
        
        if(allowsNull())
            assertion.isEqualTo(null);
        else
            assertion.isEqualTo(1);
    }

    @Test
    void getRootItemOr_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getRootItemOr(2)).isEqualTo(2); }


    @Test
    void getRootItemOrDefaultOfAnyType_empty()
    { assertThat(getNewTree().getRootItemOrDefaultOfAnyType("doot")).isEqualTo("doot"); }

    @Test
    void getRootItemOrDefaultOfAnyType_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getRootItemOrDefaultOfAnyType("doot")).isEqualTo(1); }

    @Test
    void getRootItemOrDefaultOfAnyType_nullAtRoot()
    {
        ObjectAssert<Object> assertion
                = assertThat(getNewTree(new Pair<>(TreePath.root(), null)).getRootItemOrDefaultOfAnyType("doot"));

        if(allowsNull())
            assertion.isEqualTo(null);
        else
            assertion.isEqualTo("doot");
    }

    @Test
    void getRootItemOrDefaultOfAnyType_itemUnderRoot()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getRootItemOrDefaultOfAnyType("doot"))
                .isEqualTo("doot");
    }


    @Test
    void getRootItemOrNull_empty()
    { assertThat(getNewTree().getRootItemOrNull()).isEqualTo(null); }

    @Test
    void getRootItemOrNull_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getRootItemOrNull()).isEqualTo(1); }

    @Test
    void getRootItemOrNull_nullAtRoot()
    {
        AbstractIntegerAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(TreePath.root(), null)).getRootItemOrNull());

        if(allowsNull()) // Decisions decisions ...
            assertion.isEqualTo(null); // From being set
        else
            assertion.isEqualTo(null); // From falling back to null
    }

    @Test
    void getRootItemOrNull_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getRootItemOrNull()).isEqualTo(null); }


    @Test
    void getAt_root_empty()
    { assertThrows(NoItemAtPathException.class, () -> getNewTree().getAt(TreePath.root())); }

    @Test
    void getAt_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAt(TreePath.root())).isEqualTo(1); }

    @Test
    void getAt_root_itemUnderRoot()
    {
        assertThrows(NoItemAtPathException.class,
                     () -> getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAt(TreePath.root()));
    }

    @Test
    void getAt_path_empty()
    { assertThrows(NoItemAtPathException.class, () -> getNewTree().getAt(new TreePath<>("first"))); }

    @Test
    void getAt_path_itemAtRoot()
    {
        assertThrows(NoItemAtPathException.class,
                     () -> getNewTree(new Pair<>(TreePath.root(), 1)).getAt(new TreePath<>("first")));
    }

    @Test
    void getAt_path_itemAtPath()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAt(new TreePath<>("first"))).isEqualTo(1); }

    @Test
    void getAt_path_nullAtPath()
    {
        if(allowsNull())
            assertThat(getNewTree(new Pair<>(new TreePath<>("first"), null)).getAt(new TreePath<>("first")))
                    .isEqualTo(null);
        else
            assertThrows(NoItemAtPathException.class,
                         () -> getNewTree(new Pair<>(new TreePath<>("first"), null)).getAt(new TreePath<>("first")));
    }


    @Test
    void getAtOr_root_empty()
    { assertThat(getNewTree().getAtOr(TreePath.root(), 1)).isEqualTo(1); }

    @Test
    void getAtOr_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOr(TreePath.root(), 2)).isEqualTo(1); }

    @Test
    void getAtOr_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAtOr(TreePath.root(), 2)).isEqualTo(2); }

    @Test
    void getAtOr_path_empty()
    { assertThat(getNewTree().getAtOr(new TreePath<>("first"), 1)).isEqualTo(1); }

    @Test
    void getAtOr_path_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOr(new TreePath<>("first"), 2)).isEqualTo(2); }

    @Test
    void getAtOr_path_itemAtPath()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAtOr(new TreePath<>("first"), 2)).isEqualTo(1); }

    @Test
    void getAtOr_path_nullAtPath()
    {
        AbstractIntegerAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first"), null)).getAtOr(new TreePath<>("first"), 1));
        
        if(allowsNull())
            assertion.isEqualTo(null);
        else
            assertion.isEqualTo(1);
    }


    @Test
    void getAtOrDefaultOfAnyType_root_empty()
    { assertThat(getNewTree().getAtOrDefaultOfAnyType(TreePath.root(), "doot")).isEqualTo("doot"); }

    @Test
    void getAtOrDefaultOfAnyType_root_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOrDefaultOfAnyType(TreePath.root(), "doot"))
                .isEqualTo(1);
    }

    @Test
    void getAtOrDefaultOfAnyType_root_itemUnderRoot()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAtOrDefaultOfAnyType(TreePath.root(), "doot"))
                .isEqualTo("doot");
    }

    @Test
    void getAtOrDefaultOfAnyType_path_empty()
    { assertThat(getNewTree().getAtOrDefaultOfAnyType(new TreePath<>("first"), "doot")).isEqualTo("doot"); }

    @Test
    void getAtOrDefaultOfAnyType_path_itemAtRoot()
    {
        assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOrDefaultOfAnyType(new TreePath<>("first"), "doot"))
                .isEqualTo("doot");
    }

    @Test
    void getAtOrDefaultOfAnyType_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1))
                           .getAtOrDefaultOfAnyType(new TreePath<>("first"), "doot"))
                .isEqualTo(1);
    }

    @Test
    void getAtOrDefaultOfAnyType_path_nullAtPath()
    {
        ObjectAssert<Object> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first"), null))
                                     .getAtOrDefaultOfAnyType(new TreePath<>("first"), "doot"));

        if(allowsNull())
            assertion.isEqualTo(null);
        else
            assertion.isEqualTo("doot");
    }


    @Test
    void getAtOrNull_root_empty()
    { assertThat(getNewTree().getAtOrNull(TreePath.root())).isEqualTo(null); }

    @Test
    void getAtOrNull_root_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOrNull(TreePath.root())).isEqualTo(1); }

    @Test
    void getAtOrNull_root_itemUnderRoot()
    { assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAtOrNull(TreePath.root())).isEqualTo(null); }

    @Test
    void getAtOrNull_path_empty()
    { assertThat(getNewTree().getAtOrNull(new TreePath<>("first"))).isEqualTo(null); }

    @Test
    void getAtOrNull_path_itemAtRoot()
    { assertThat(getNewTree(new Pair<>(TreePath.root(), 1)).getAtOrNull(new TreePath<>("first"))).isEqualTo(null); }

    @Test
    void getAtOrNull_path_itemAtPath()
    {
        assertThat(getNewTree(new Pair<>(new TreePath<>("first"), 1)).getAtOrNull(new TreePath<>("first")))
                .isEqualTo(1);
    }

    @Test
    void getAtOrNull_path_nullAtPath()
    {
        AbstractIntegerAssert<?> assertion
                = assertThat(getNewTree(new Pair<>(new TreePath<>("first"), null))
                                     .getAtOrNull(new TreePath<>("first")));

        if(allowsNull())
            assertion.isEqualTo(null);
        else
            assertion.isEqualTo(null);
    }
}