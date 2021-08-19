package scot.massie.lib.collections.trees;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;
import scot.massie.lib.utils.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
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
            populated, empty at & under path
        getItemsAtAndUnderInOrder
            empty
            populated
            populated, empty at & under path
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
            populated, empty at & under path
        getEntriesAtAndUnderInOrder
            empty
            populated
            populated, empty at & under path
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
     *
     * Should have items at a path length > 2, starting with "first", "second".
     *
     * Should not have any items with a path starting with "zoot"
     * @return A populated tree.
     */
    protected Tree<String, Integer> getPopulatedTree1()
    { return getNewTree(tree1Items); }

    /**
     * Should have no root item.
     *
     * Should have items at a path length > 2, starting with "first", "second".
     *
     * Should not have any items with a path starting with "zoot"
     * @return A populated tree.
     */
    protected Tree<String, Integer> getPopulatedTree2()
    { return getNewTree(tree2Items); }

    private static final Pair<TreePath<String>, Integer>[] tree1Items = (Pair<TreePath<String>, Integer>[]) new Pair[]
    {
            new Pair<>(TreePath.root(), 37),
            new Pair<>(new TreePath<>("first"), 29),
            new Pair<>(new TreePath<>("first", "second"), 65),
            new Pair<>(new TreePath<>("first", "second", "third"), 71),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth"), 11),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "notfive", "notsix"), 29),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "fifth"), 54),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "fiveth"), 98),
            new Pair<>(new TreePath<>("first", "twoth"), 58),
            new Pair<>(new TreePath<>("first", "twoth", "threeth"), 66),
            new Pair<>(new TreePath<>("uno"), 44),
            new Pair<>(new TreePath<>("uno", "dos"), 87),
            new Pair<>(new TreePath<>("uno", "dos", "third"), 92),
    };

    private static final Pair<TreePath<String>, Integer>[] tree2Items = (Pair<TreePath<String>, Integer>[]) new Pair[]
    {
            new Pair<>(TreePath.root(), 37),
            new Pair<>(new TreePath<>("first"), 29),
            new Pair<>(new TreePath<>("first", "second"), 65),
            new Pair<>(new TreePath<>("first", "second", "third"), 71),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth"), 11),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "notfive", "notsix"), 29),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "fifth"), 54),
            new Pair<>(new TreePath<>("first", "second", "third", "fourth", "fiveth"), 98),
            new Pair<>(new TreePath<>("first", "twoth"), 58),
            new Pair<>(new TreePath<>("first", "twoth", "threeth"), 66),
            new Pair<>(new TreePath<>("uno"), 44),
            new Pair<>(new TreePath<>("uno", "dos"), 87),
            new Pair<>(new TreePath<>("uno", "dos", "third"), 92),
    };

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

    @Test
    void getItems_empty()
    { assertThat(getNewTree().getItems()).isEmpty(); }

    @Test
    void getItems_populated()
    {
        assertThat(getPopulatedTree1().getItems())
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(tree1Items)
                                                           .map(Pair::getSecond)
                                                           .collect(Collectors.toList()));
    }

    @Test
    void getItemsInOrder_empty()
    { assertThat(getNewTree().getItems()).isEmpty(); }

    @Test
    void getItemsInOrder_populated()
    {
        assertThat(getPopulatedTree1().getItems())
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsWhere_empty()
    { assertThat(getNewTree().getItemsWhere((path, n) -> path.size() > 2)).isEmpty(); }

    @Test
    void getItemsWhere_populated_matches()
    {
        assertThat(getPopulatedTree1().getItemsWhere((path, n) -> path.size() > 2))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsWhere_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getItemsWhere((path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getItemsWherePath_empty()
    { assertThat(getNewTree().getItemsWherePath(path -> path.size() > 2)).isEmpty(); }

    @Test
    void getItemsWherePath_populated_matches()
    {
        assertThat(getPopulatedTree1().getItemsWherePath(path -> path.size() > 2))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsWherePath_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getItemsWherePath(path -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getItemsInOrderWhere_empty()
    {
        assertThat(getNewTree().getItemsInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .isEmpty();
    }

    @Test
    void getItemsInOrderWhere_populated_matches()
    {
        assertThat(getPopulatedTree1().getItemsInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsInOrderWhere_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getItemsInOrderWhere(Comparator.naturalOrder(),
                                                            (path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getItemsInOrderWherePath_empty()
    {
        assertThat(getNewTree().getItemsInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .isEmpty();
    }

    @Test
    void getItemsInOrderWherePath_populated_matches()
    {
        assertThat(getPopulatedTree1().getItemsInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsInOrderWherePath_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getItemsInOrderWhere(Comparator.naturalOrder(),
                                                            (path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getItemsUnderRoot_empty()
    { assertThat(getNewTree().getItemsUnderRoot()).isEmpty(); }

    @Test
    void getItemsUnderRoot_populated()
    {
        assertThat(getPopulatedTree1().getItemsUnderRoot())
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> !x.getFirst().isRoot())
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsUnderRootInOrder_empty()
    { assertThat(getNewTree().getItemsUnderRootInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getItemsUnderRootInOrder_populated()
    {
        assertThat(getPopulatedTree1().getItemsUnderRootInOrder(Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> !x.getFirst().isRoot())
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndUnder_empty()
    { assertThat(getNewTree().getItemsAtAndUnder(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getItemsAtAndUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsAtAndUnder(path))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrDescendantOf(path))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndUnder_populated_emptyAtAndUnderPath()
    { assertThat(getPopulatedTree1().getItemsAtAndUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getItemsAtAndUnderInOrder_empty()
    {
        assertThat(getNewTree().getItemsAtAndUnderInOrder(new TreePath<>("first", "second"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getItemsAtAndUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsAtAndUnderInOrder(path, Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrDescendantOf(path))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndUnderInOrder_populated_emptyAtAndUnderPath()
    {
        assertThat(getPopulatedTree1().getItemsAtAndUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getItemsUnder_empty()
    { assertThat(getNewTree().getItemsUnder(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getItemsUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsUnder(path))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isDescendantOf(path))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsUnder_populated_emptyAtAndUnderPath()
    { assertThat(getPopulatedTree1().getItemsUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getItemsUnderInOrder_empty()
    {
        assertThat(getNewTree().getItemsUnderInOrder(new TreePath<>("first", "second"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getItemsUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsUnderInOrder(path, Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isDescendantOf(path))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsUnderInOrder_populated_emptyAtAndUnderPath()
    {
        assertThat(getPopulatedTree1().getItemsUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getItemsAlong_empty()
    { assertThat(getNewTree().getItemsAlong(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getItemsAlong_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsAlong(path))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrAncestorOf(path))
                              .sorted(Comparator.comparing(x -> x.getFirst().size()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsAlong_populated_emptyAlongPath()
    { assertThat(getPopulatedTree1().getItemsAlong(new TreePath<>("zoot", "doot"))).isEmpty(); }

    @Test
    void getItemsUnderRootAlong_empty()
    { assertThat(getNewTree().getItemsUnderRootAlong(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getItemsUnderRootAlong_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getItemsUnderRootAlong(path))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (!x.getFirst().isRoot()) && (x.getFirst().isEqualToOrAncestorOf(path)))
                              .sorted(Comparator.comparing(x -> x.getFirst().size()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getItemsUnderRootAlong_populated_emptyAlongPath()
    { assertThat(getPopulatedTree1().getItemsUnderRootAlong(new TreePath<>("zoot", "doot"))).isEmpty(); }

    @Test
    void getImmediateItems_empty()
    { assertThat(getNewTree().getImmediateItems()).isEmpty(); }

    @Test
    void getImmediateItems_populated()
    {
        assertThat(getPopulatedTree1().getImmediateItems())
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() == 1)
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getImmediateItemsInOrder_empty()
    { assertThat(getNewTree().getImmediateItemsInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getImmediateItemsInOrder_populated()
    {
        assertThat(getPopulatedTree1().getImmediateItemsInOrder(Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() == 1)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateItems_empty()
    { assertThat(getNewTree().getRootAndImmediateItems()).isEmpty(); }

    @Test
    void getRootAndImmediateItems_populated()
    {
        assertThat(getPopulatedTree1().getRootAndImmediateItems())
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot()) || (x.getFirst().size() == 1))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateItemsInOrder_empty()
    { assertThat(getNewTree().getRootAndImmediateItemsInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getRootAndImmediateItemsInOrder_populated()
    {
        assertThat(getPopulatedTree1().getRootAndImmediateItemsInOrder(Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot()) || (x.getFirst().size() == 1))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getImmediatelyUnder_empty()
    { assertThat(getNewTree().getImmediatelyUnder(new TreePath<>("first"))).isEmpty(); }

    @Test
    void getImmediatelyUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getImmediatelyUnder(path))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isAncestorOf(path) && x.getFirst().size() == path.size() + 1)
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getImmediatelyUnder_populated_emptyUnderPath()
    { assertThat(getPopulatedTree1().getImmediatelyUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getImmediatelyUnderInOrder_empty()
    {
        assertThat(getNewTree().getImmediatelyUnderInOrder(new TreePath<>("first"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getImmediatelyUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getImmediatelyUnderInOrder(path, Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isAncestorOf(path) && x.getFirst().size() == path.size() + 1)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getImmediatelyUnderInOrder_populated_emptyUnderPath()
    {
        assertThat(getPopulatedTree1().getImmediatelyUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getAtAndImmediatelyUnder_empty()
    { assertThat(getNewTree().getAtAndImmediatelyUnder(new TreePath<>("first"))).isEmpty(); }

    @Test
    void getAtAndImmediatelyUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getAtAndImmediatelyUnder(path))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot())
                                           || (x.getFirst().isAncestorOf(path)
                                               && x.getFirst().size() == path.size() + 1))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getAtAndImmediatelyUnder_populated_emptyUnderPath()
    { assertThat(getPopulatedTree1().getAtAndImmediatelyUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getAtAndImmediatelyUnderInOrder_empty()
    {
        assertThat(getNewTree().getAtAndImmediatelyUnderInOrder(new TreePath<>("first"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getAtAndImmediatelyUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getAtAndImmediatelyUnderInOrder(path, Comparator.naturalOrder()))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot())
                                           || (x.getFirst().isAncestorOf(path)
                                               && x.getFirst().size() == path.size() + 1))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .map(Pair::getSecond)
                              .collect(Collectors.toList()));
    }

    @Test
    void getAtAndImmediatelyUnderInOrder_populated_emptyUnderPath()
    {
        assertThat(getPopulatedTree1().getAtAndImmediatelyUnderInOrder(new TreePath<>("zoot"),
                                                                       Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntries_empty()
    { assertThat(getNewTree().getEntries()).isEmpty(); }

    @Test
    void getEntries_populated()
    {
        assertThat(getPopulatedTree1().getEntries().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(tree1Items)
                                                           .collect(Collectors.toList()));
    }

    @Test
    void getEntriesInOrder_empty()
    { assertThat(getNewTree().getEntries()).isEmpty(); }

    @Test
    void getEntriesInOrder_populated()
    {
        assertThat(getPopulatedTree1().getEntries().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesWhere_empty()
    { assertThat(getNewTree().getEntriesWhere((path, n) -> path.size() > 2)).isEmpty(); }

    @Test
    void getEntriesWhere_populated_matches()
    {
        assertThat(getPopulatedTree1().getEntriesWhere((path, n) -> path.size() > 2)
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesWhere_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getEntriesWhere((path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getEntriesWherePath_empty()
    { assertThat(getNewTree().getEntriesWherePath(path -> path.size() > 2)).isEmpty(); }

    @Test
    void getEntriesWherePath_populated_matches()
    {
        assertThat(getPopulatedTree1().getEntriesWherePath(path -> path.size() > 2)
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesWherePath_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getEntriesWherePath(path -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getEntriesInOrderWhere_empty()
    {
        assertThat(getNewTree().getEntriesInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .isEmpty();
    }

    @Test
    void getEntriesInOrderWhere_populated_matches()
    {
        assertThat(getPopulatedTree1().getEntriesInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2)
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesInOrderWhere_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getEntriesInOrderWhere(Comparator.naturalOrder(),
                                                            (path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getEntriesInOrderWherePath_empty()
    {
        assertThat(getNewTree().getEntriesInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2))
                .isEmpty();
    }

    @Test
    void getEntriesInOrderWherePath_populated_matches()
    {
        assertThat(getPopulatedTree1().getEntriesInOrderWhere(Comparator.naturalOrder(), (path, n) -> path.size() > 2)
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() > 2)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesInOrderWherePath_populated_noMatches()
    {
        assertThat(getPopulatedTree1().getEntriesInOrderWhere(Comparator.naturalOrder(),
                                                            (path, n) -> path.isDescendantOf(new TreePath<>("zoot"))))
                .isEmpty();
    }

    @Test
    void getEntriesUnderRoot_empty()
    { assertThat(getNewTree().getEntriesUnderRoot()).isEmpty(); }

    @Test
    void getEntriesUnderRoot_populated()
    {
        assertThat(getPopulatedTree1().getEntriesUnderRoot().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> !x.getFirst().isRoot())
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnderRootInOrder_empty()
    { assertThat(getNewTree().getEntriesUnderRootInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getEntriesUnderRootInOrder_populated()
    {
        assertThat(getPopulatedTree1().getEntriesUnderRootInOrder(Comparator.naturalOrder())
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> !x.getFirst().isRoot())
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndUnder_empty()
    { assertThat(getNewTree().getEntriesAtAndUnder(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getEntriesAtAndUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesAtAndUnder(path).stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrDescendantOf(path))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndUnder_populated_emptyAtAndUnderPath()
    { assertThat(getPopulatedTree1().getEntriesAtAndUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getEntriesAtAndUnderInOrder_empty()
    {
        assertThat(getNewTree().getEntriesAtAndUnderInOrder(new TreePath<>("first", "second"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesAtAndUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesAtAndUnderInOrder(path, Comparator.naturalOrder())
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrDescendantOf(path))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndUnderInOrder_populated_emptyAtAndUnderPath()
    {
        assertThat(getPopulatedTree1().getEntriesAtAndUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesUnder_empty()
    { assertThat(getNewTree().getEntriesUnder(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getEntriesUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesUnder(path).stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isDescendantOf(path))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnder_populated_emptyAtAndUnderPath()
    { assertThat(getPopulatedTree1().getEntriesUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getEntriesUnderInOrder_empty()
    {
        assertThat(getNewTree().getEntriesUnderInOrder(new TreePath<>("first", "second"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesUnderInOrder(path, Comparator.naturalOrder())
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isDescendantOf(path))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnderInOrder_populated_emptyAtAndUnderPath()
    {
        assertThat(getPopulatedTree1().getEntriesUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesAlong_empty()
    { assertThat(getNewTree().getEntriesAlong(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getEntriesAlong_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesAlong(path).stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrAncestorOf(path))
                              .sorted(Comparator.comparing(x -> x.getFirst().size()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAlong_populated_emptyAlongPath()
    { assertThat(getPopulatedTree1().getEntriesAlong(new TreePath<>("zoot", "doot"))).isEmpty(); }

    @Test
    void getEntriesUnderRootAlong_empty()
    { assertThat(getNewTree().getEntriesUnderRootAlong(new TreePath<>("first", "second"))).isEmpty(); }

    @Test
    void getEntriesUnderRootAlong_populated()
    {
        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getEntriesUnderRootAlong(path).stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (!x.getFirst().isRoot()) && (x.getFirst().isEqualToOrAncestorOf(path)))
                              .sorted(Comparator.comparing(x -> x.getFirst().size()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnderRootAlong_populated_emptyAlongPath()
    { assertThat(getPopulatedTree1().getEntriesUnderRootAlong(new TreePath<>("zoot", "doot"))).isEmpty(); }

    @Test
    void getImmediateEntries_empty()
    { assertThat(getNewTree().getImmediateEntries()).isEmpty(); }

    @Test
    void getImmediateEntries_populated()
    {
        assertThat(getPopulatedTree1().getImmediateEntries().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() == 1)
                              .collect(Collectors.toList()));
    }

    @Test
    void getImmediateEntriesInOrder_empty()
    { assertThat(getNewTree().getImmediateEntriesInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getImmediateEntriesInOrder_populated()
    {
        assertThat(getPopulatedTree1().getImmediateEntriesInOrder(Comparator.naturalOrder()).stream()
                                      .map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().size() == 1)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateEntries_empty()
    { assertThat(getNewTree().getRootAndImmediateEntries()).isEmpty(); }

    @Test
    void getRootAndImmediateEntries_populated()
    {
        assertThat(getPopulatedTree1().getRootAndImmediateEntries().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot()) || (x.getFirst().size() == 1))
                              .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateEntriesInOrder_empty()
    { assertThat(getNewTree().getRootAndImmediateEntriesInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getRootAndImmediateEntriesInOrder_populated()
    {
        assertThat(getPopulatedTree1().getRootAndImmediateEntriesInOrder(Comparator.naturalOrder())
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot()) || (x.getFirst().size() == 1))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesImmediatelyUnder_empty()
    { assertThat(getNewTree().getEntriesImmediatelyUnder(new TreePath<>("first"))).isEmpty(); }

    @Test
    void getEntriesImmediatelyUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getEntriesImmediatelyUnder(path).stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isAncestorOf(path) && x.getFirst().size() == path.size() + 1)
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesImmediatelyUnder_populated_emptyUnderPath()
    { assertThat(getPopulatedTree1().getEntriesImmediatelyUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getEntriesImmediatelyUnderInOrder_empty()
    {
        assertThat(getNewTree().getEntriesImmediatelyUnderInOrder(new TreePath<>("first"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesImmediatelyUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getEntriesImmediatelyUnderInOrder(path, Comparator.naturalOrder()).stream()
                                      .map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isAncestorOf(path) && x.getFirst().size() == path.size() + 1)
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesImmediatelyUnderInOrder_populated_emptyUnderPath()
    {
        assertThat(getPopulatedTree1().getEntriesImmediatelyUnderInOrder(new TreePath<>("zoot"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesAtAndImmediatelyUnder_empty()
    { assertThat(getNewTree().getEntriesAtAndImmediatelyUnder(new TreePath<>("first"))).isEmpty(); }

    @Test
    void getEntriesAtAndImmediatelyUnder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getEntriesAtAndImmediatelyUnder(path)
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot())
                                           || (x.getFirst().isAncestorOf(path)
                                               && x.getFirst().size() == path.size() + 1))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndImmediatelyUnder_populated_emptyUnderPath()
    { assertThat(getPopulatedTree1().getEntriesAtAndImmediatelyUnder(new TreePath<>("zoot"))).isEmpty(); }

    @Test
    void getEntriesAtAndImmediatelyUnderInOrder_empty()
    {
        assertThat(getNewTree().getEntriesAtAndImmediatelyUnderInOrder(new TreePath<>("first"), Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getEntriesAtAndImmediatelyUnderInOrder_populated()
    {
        TreePath<String> path = new TreePath<>("first");

        assertThat(getPopulatedTree1().getEntriesAtAndImmediatelyUnderInOrder(path, Comparator.naturalOrder())
                                      .stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> (x.getFirst().isRoot())
                                           || (x.getFirst().isAncestorOf(path)
                                               && x.getFirst().size() == path.size() + 1))
                              .sorted(Comparator.comparing(Pair::getFirst, TreePath.getComparator()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndImmediatelyUnderInOrder_populated_emptyUnderPath()
    {
        assertThat(getPopulatedTree1().getEntriesAtAndImmediatelyUnderInOrder(new TreePath<>("zoot"),
                                                                       Comparator.naturalOrder()))
                .isEmpty();
    }

    @Test
    void getBranch_withItems()
    {
        // Assumes that .getEntries() is functioning correctly.

        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getBranch(path).getEntries().stream().map(x -> new Pair<>(x.path, x.item)))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrAncestorOf(path))
                              .collect(Collectors.toList()));
    }

    @Test
    void getBranch_withoutItems()
    { assertThat(getPopulatedTree1().getBranch(new TreePath<>("zoot", "doot")).getEntries()).isEmpty(); }

    @Test
    void getBranches_empty()
    { assertThat(getNewTree().getBranches()).isEmpty(); }

    @Test
    void getBranches_populated()
    {
        // Assumes that .getEntries() of the tree objects returned by .getBranches() is functioning correctly.

        Map<String, Tree<String, Integer>> branches = getPopulatedTree1().getBranches();
        Map<String, Collection<Pair<TreePath<String>, Integer>>> expectedEntries = new HashMap<>();

        for(Pair<TreePath<String>, Integer> p : tree1Items)
        {
            if(p.getFirst().isRoot())
                continue;

            expectedEntries.computeIfAbsent(p.getFirst().first(), x -> new ArrayList<>())
                           .add(new Pair<>(p.getFirst().withoutFirstNodes(1), p.getSecond()));
        }

        assertThat(branches).hasSize(expectedEntries.size());

        for(Map.Entry<String, Tree<String, Integer>> e : branches.entrySet())
        {
            assertThat(expectedEntries).containsKey(e.getKey());

            assertThat(e.getValue()
                        .getEntries()
                        .stream()
                        .map(x -> new Pair<>(x.path, x.item))
                        .collect(Collectors.toList()))
                    .containsExactlyInAnyOrderElementsOf(expectedEntries.get(e.getKey()));
        }
    }

    @Test
    void getPaths_empty()
    { assertThat(getNewTree().getPaths()).isEmpty(); }

    @Test
    void getPaths_populated()
    {
        assertThat(getPopulatedTree1().getPaths())
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(tree1Items)
                                                           .map(Pair::getFirst)
                                                           .collect(Collectors.toList()));
    }

    @Test
    void getPathsInOrder_empty()
    { assertThat(getNewTree().getPathsInOrder(Comparator.naturalOrder())).isEmpty(); }

    @Test
    void getPathsInOrder_populated()
    {
        assertThat(getPopulatedTree1().getPathsInOrder(Comparator.naturalOrder()))
                .containsExactlyElementsOf(Arrays.stream(tree1Items)
                                                 .map(Pair::getFirst)
                                                 .sorted(TreePath.getComparator())
                                                 .collect(Collectors.toList()));
    }

    @Test
    void getBranchView_hasCorrectItems()
    {
        // Assumes that the type returned get .getBranchView is working correctly.

        TreePath<String> path = new TreePath<>("first", "second");

        assertThat(getPopulatedTree1().getBranchView(path)
                                      .getEntries()
                                      .stream()
                                      .map(x -> new Pair<>(x.path, x.item))
                                      .collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(tree1Items)
                              .filter(x -> x.getFirst().isEqualToOrDescendantOf(path))
                              .map(x -> new Pair<>(x.getFirst().withoutFirstNodes(2), x.getSecond()))
                              .collect(Collectors.toList()));
    }

    @Test
    void getBranchView_addItem_inBranch()
    {
        // Assumes that the type returned get .getBranchView is working correctly.
        // Assumes that .getAt is working correctly.

        Tree<String, Integer> tree = getNewTree();
        Tree<String, Integer> branch = tree.getBranch(new TreePath<>("first", "second"));

        branch.setRootItem(67);
        assertThat(tree.getAt(new TreePath<>("first", "second"))).isEqualTo(67);
    }

    @Test
    void getBranchView_addItem_inTree()
    {
        // Assumes that the type returned get .getBranchView is working correctly.
        // Assumes that .setAt is working correctly.

        Tree<String, Integer> tree = getNewTree();
        Tree<String, Integer> branch = tree.getBranch(new TreePath<>("first", "second"));

        tree.setAt(new TreePath<>("first", "second"), 67);
        assertThat(branch.getRootItem()).isEqualTo(67);
    }

    @Test
    void getBranchView_removeItem_inBranch()
    {
        // Assumes that the type returned get .getBranchView is working correctly.
        // Assumes that .setAt and .getAtOr are working correctly.

        Tree<String, Integer> tree = getNewTree();
        tree.setAt(new TreePath<>("first", "second", "third"), 67);
        Tree<String, Integer> branch = tree.getBranch(new TreePath<>("first", "second"));

        branch.clearAt(new TreePath<>("third"));
        assertThat(tree.getAtOr(new TreePath<>("first", "second", "third"), 85)).isEqualTo(85);
    }

    @Test
    void getBranchView_removeItem_inTree()
    {
        // Assumes that the type returned get .getBranchView is working correctly.
        // Assumes that .setAt and .clearAt are working correctly.

        Tree<String, Integer> tree = getNewTree();
        tree.setAt(new TreePath<>("first", "second", "third"), 67);
        Tree<String, Integer> branch = tree.getBranch(new TreePath<>("first", "second"));

        tree.clearAt(new TreePath<>("first", "second", "third"));
        assertThat(branch.getAtOr(new TreePath<>("third"), 85)).isEqualTo(85);
    }

    @Test
    void getBranchViews_empty()
    { assertThat(getNewTree().getBranchViews()).isEmpty(); }

    @Test
    void getBranchViews_populated()
    {
        Map<String, Tree<String, Integer>> branchViews = getPopulatedTree1().getBranchViews();
        Map<String, Collection<Pair<TreePath<String>, Integer>>> expectedEntriesPerKey = new HashMap<>();

        for(Pair<TreePath<String>, Integer> pair : tree1Items)
        {
            if(pair.getFirst().isRoot())
                continue;

            expectedEntriesPerKey.computeIfAbsent(pair.getFirst().first(), x -> new ArrayList<>())
                                 .add(new Pair<>(pair.getFirst().withoutFirstNodes(1), pair.getSecond()));
        }

        assertThat(branchViews).hasSize(expectedEntriesPerKey.size());

        for(Map.Entry<String, Tree<String, Integer>> e : branchViews.entrySet())
        {
            assertThat(expectedEntriesPerKey).containsKey(e.getKey());

            assertThat(e.getValue()
                        .getEntries()
                        .stream()
                        .map(x -> new Pair<>(x.path, x.item))
                        .collect(Collectors.toList()))
                    .containsExactlyInAnyOrderElementsOf(expectedEntriesPerKey.get(e.getKey()));
        }
    }
}