package scot.massie.lib.collections.tree;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

abstract class TreeTest<T extends Tree<String, Integer>>
{
    @SuppressWarnings("PublicField") // Is a POD class
    private static class Triplet<T1, T2, T3>
    {
        // Only here for convenience, and because Java doesn't support tuples or have a standard tuple type.

        public Triplet(T1 first, T2 second, T3 third)
        {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public final T1 first;
        public final T2 second;
        public final T3 third;
    }

    public abstract T getNewEmptyTree();

    private final String[] path_root                    = {  };
    private final String[] path_root_child              = { "qoot" };
    private final String[] path_lvl1                    = { "doot" };
    private final String[] path_lvl1_child              = { "doot", "noot" };
    private final String[] path_lvl1_sibling            = { "foot" };
    private final String[] path_lvl1_nibling            = { "foot", "moot" };
    private final String[] path_lvl1_otherSibling       = { "woot" };
    private final String[] path_lvl1_otherSiblingChild  = { "woot", "boot" };
    private final String[] path_lvl2                    = { "hoot", "yoot" };
    private final String[] path_lvl2_parent             = { "hoot" };
    private final String[] path_lvl2_child              = { "hoot", "yoot", "zoot" };
    private final String[] path_lvl2_grandchild         = { "hoot", "yoot", "zoot", "toot" };
    private final String[] path_lvl2_greatgrandchild    = { "hoot", "yoot", "zoot", "toot", "loot" };
    private final String[] path_lvl2_sibling            = { "hoot", "soot" };
    private final String[] path_lvl2_otherSibling       = { "hoot", "xoot" };
    private final String[] path_lvl2_otherSiblingChild  = { "hoot", "xoot", "poot" };
    private final String[] path_lvl2_uncle              = { "voot" };
    private final String[] path_lvl2_cousin             = { "voot", "joot" };
    private final String[] path_lvl2_otherUncle         = { "poot" };

    private final List<String[]> pathList_empty = new ArrayList<>();
    private final List<String[]> pathList_root = Collections.singletonList(path_root);
    private final List<String[]> pathList_lvl1 = Collections.singletonList(path_lvl1);
    private final List<String[]> pathList_lvl2 = Collections.singletonList(path_lvl2);

    private final Tree.TreePath<String> tpath_root                      = new Tree.TreePath<>(path_root);
    private final Tree.TreePath<String> tpath_root_child                = new Tree.TreePath<>(path_root_child);
    private final Tree.TreePath<String> tpath_lvl1                      = new Tree.TreePath<>(path_lvl1);
    private final Tree.TreePath<String> tpath_lvl1_child                = new Tree.TreePath<>(path_lvl1_child);
    private final Tree.TreePath<String> tpath_lvl1_sibling              = new Tree.TreePath<>(path_lvl1_sibling);
    private final Tree.TreePath<String> tpath_lvl1_nibling              = new Tree.TreePath<>(path_lvl1_nibling);
    private final Tree.TreePath<String> tpath_lvl1_otherSibling         = new Tree.TreePath<>(path_lvl1_otherSibling);
    private final Tree.TreePath<String> tpath_lvl1_otherSiblingChild    = new Tree.TreePath<>(path_lvl1_otherSiblingChild);
    private final Tree.TreePath<String> tpath_lvl2                      = new Tree.TreePath<>(path_lvl2);
    private final Tree.TreePath<String> tpath_lvl2_parent               = new Tree.TreePath<>(path_lvl2_parent);
    private final Tree.TreePath<String> tpath_lvl2_child                = new Tree.TreePath<>(path_lvl2_child);
    private final Tree.TreePath<String> tpath_lvl2_grandchild           = new Tree.TreePath<>(path_lvl2_grandchild);
    private final Tree.TreePath<String> tpath_lvl2_greatgrandchild      = new Tree.TreePath<>(path_lvl2_greatgrandchild);
    private final Tree.TreePath<String> tpath_lvl2_sibling              = new Tree.TreePath<>(path_lvl2_sibling);
    private final Tree.TreePath<String> tpath_lvl2_otherSibling         = new Tree.TreePath<>(path_lvl2_otherSibling);
    private final Tree.TreePath<String> tpath_lvl2_otherSiblingChild    = new Tree.TreePath<>(path_lvl2_otherSiblingChild);
    private final Tree.TreePath<String> tpath_lvl2_uncle                = new Tree.TreePath<>(path_lvl2_uncle);
    private final Tree.TreePath<String> tpath_lvl2_cousin               = new Tree.TreePath<>(path_lvl2_cousin);
    private final Tree.TreePath<String> tpath_lvl2_otherUncle           = new Tree.TreePath<>(path_lvl2_otherUncle);

    private final List<Tree.TreePath<String>> tpathList_empty = new ArrayList<>();
    private final List<Tree.TreePath<String>> tpathList_root = Collections.singletonList(tpath_root);
    private final List<Tree.TreePath<String>> tpathList_lvl1 = Collections.singletonList(tpath_lvl1);
    private final List<Tree.TreePath<String>> tpathList_lvl2 = Collections.singletonList(tpath_lvl2);

    private final List<Integer> listOfJustNull;
    {
        listOfJustNull = new ArrayList<>();
        listOfJustNull.add(null);
    }

    private List<Map.Entry<Tree.TreePath<String>, Integer>> getEntriesInHierarchicalOrder(Collection<Map.Entry<Tree.TreePath<String>, Integer>> source)
    {
        List<Map.Entry<Tree.TreePath<String>, Integer>> result = new ArrayList<>();

        return source.stream().sorted(new Comparator<Map.Entry<Tree.TreePath<String>, Integer>>()
        {
            @Override
            public int compare(Map.Entry<Tree.TreePath<String>, Integer> o1, Map.Entry<Tree.TreePath<String>, Integer> o2)
            {
                // TO DO: Write.
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        }).collect(Collectors.toList());
    }

    private final Comparator<Tree.TreePath<String>> treePathComparator = Tree.TreePath.getComparator();

    private final Comparator<Map.Entry<Tree.TreePath<String>, Integer>> hierarchicalOrderComparator
            = (a, b) ->
    {

        int result = treePathComparator.compare(a.getKey(), b.getKey());
        return result;
    };

    private final Comparator<Tree.Entry<String, Integer>> treeEntryEquator = (a, b) ->
    {
        return (a.getPath().equals(b.getPath()))
            && (Objects.equals(a.getItem(), b.getItem()))
               ? 0 : -1;
    };

    private boolean collectionsAreEqualIgnoringOrder(Collection<?> a, Collection<?> b)
    {
        if(a.size() != b.size())
            return false;

        List<?> bCopy = new ArrayList<>(b);

        for(Object i : a)
            if(!bCopy.remove(i))
                return false;

        return true;
    }

    private void stateCheckTest(Function<T, Boolean> stateChecker, Function<List<String[]>, Boolean> properResultGetter)
    {
        boolean expectOnEmpty  = properResultGetter.apply(pathList_empty);
        boolean expectOnRoot   = properResultGetter.apply(pathList_root);
        boolean expectOnLevel1 = properResultGetter.apply(pathList_lvl1);
        boolean expectOnLevel2 = properResultGetter.apply(pathList_lvl2);

        T tree = getNewEmptyTree();
        assertEquals(expectOnEmpty,
                     stateChecker.apply(tree),
                     "Should return " + expectOnEmpty + " checking an empty tree.");

        tree.setRootItem(5);
        assertEquals(expectOnRoot,
                     stateChecker.apply(tree),
                     "Should return " + expectOnRoot + " checking a tree with a root item. (set via .setRootItem)");

        tree = getNewEmptyTree();
        tree.setAt(6, path_root);
        assertEquals(expectOnRoot,
                     stateChecker.apply(tree),
                     "Should return " + expectOnRoot + " checking a tree with a root item. (set via .setAt)");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl1);
        assertEquals(expectOnLevel1,
                     stateChecker.apply(tree),
                     "Should return " + expectOnLevel1 + " checking a tree with a 1st level item.");

        tree = getNewEmptyTree();
        tree.setAt(8, path_lvl2);
        assertEquals(expectOnLevel2,
                     stateChecker.apply(tree),
                     "Should return " + expectOnLevel2 + " checking a tree with a 2nd level item.");
    }

    private void stateCheckAtPathTest(BiFunction<T, String[], Boolean> stateChecker,
                                      BiFunction<List<String[]>, String[], Boolean> properResultGetter)
    {
        // stateChecker is: boolean Function(T tree, String[] path)
        // properResultGetter is: boolean Function(List<String[]> pathsWithItems, String[] pathCheckingAt)

        boolean expectedOnEmptyAtRoot   = properResultGetter.apply(pathList_empty, path_root);
        boolean expectedOnEmptyAtChild  = properResultGetter.apply(pathList_empty, path_root_child);
        boolean expectedOnRootAtRoot    = properResultGetter.apply(pathList_root,  path_root);
        boolean expectedOnRootAtChild   = properResultGetter.apply(pathList_root,  path_root_child);
        boolean expectedOnLvl1AtSame    = properResultGetter.apply(pathList_lvl1,  path_lvl1);
        boolean expectedOnLvl1AtRoot    = properResultGetter.apply(pathList_lvl1,  path_root);
        boolean expectedOnLvl1AtChild   = properResultGetter.apply(pathList_lvl1,  path_lvl1_child);
        boolean expectedOnLvl1AtSibling = properResultGetter.apply(pathList_lvl1,  path_lvl1_sibling);
        boolean expectedOnLvl2AtSame    = properResultGetter.apply(pathList_lvl2,  path_lvl2);
        boolean expectedOnLvl2AtRoot    = properResultGetter.apply(pathList_lvl2,  path_root);
        boolean expectedOnLvl2AtParent  = properResultGetter.apply(pathList_lvl2,  path_lvl2_parent);
        boolean expectedOnLvl2AtChild   = properResultGetter.apply(pathList_lvl2,  path_lvl2_child);
        boolean expectedOnLvl2AtSibling = properResultGetter.apply(pathList_lvl2,  path_lvl2_sibling);
        boolean expectedOnLvl2AtUncle   = properResultGetter.apply(pathList_lvl2,  path_lvl2_uncle);
        boolean expectedOnLvl2AtCousin  = properResultGetter.apply(pathList_lvl2,  path_lvl2_cousin);

        // Empty
        T tree = getNewEmptyTree();
        assertEquals(expectedOnEmptyAtRoot,
                     stateChecker.apply(tree, path_root),
                     "Should return " + expectedOnEmptyAtRoot + " on an empty tree checking at the root.");

        assertEquals(expectedOnEmptyAtChild,
                     stateChecker.apply(tree, path_root_child),
                     "Should return " + expectedOnEmptyAtChild + " on an empty tree checking a 1 element path.");

        // Root (via .setRootItem)
        tree.setRootItem(5);
        assertEquals(expectedOnRootAtRoot,
                     stateChecker.apply(tree, path_root),
                     "Should return " + expectedOnRootAtRoot + " on a tree with a root item (set via .setRootItem) "
                     + "checking at the root.");

        assertEquals(expectedOnRootAtChild,
                     stateChecker.apply(tree, path_root_child),
                     "Should return " + expectedOnRootAtChild + " on a tree with a root item (set via .setRootItem) "
                     + "checking at a 1 element path.");

        // Root (via .setAt)
        tree = getNewEmptyTree();
        tree.setAt(6, path_root);
        assertEquals(expectedOnRootAtRoot,
                     stateChecker.apply(tree, path_root),
                     "Should return " + expectedOnRootAtRoot + " on a tree with a root item (set via .setAt) checking "
                     + "at the root.");

        assertEquals(expectedOnRootAtChild,
                     stateChecker.apply(tree, path_root_child),
                     "Should return " + expectedOnRootAtChild + " on a tree with a root item (set via .setAt) checking "
                     + "at a 1 element path.");

        // Lvl 1
        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl1);
        assertEquals(expectedOnLvl1AtSame,
                     stateChecker.apply(tree, path_lvl1),
                     "Should return " + expectedOnLvl1AtSame + " on a tree with a 1st level element checking at that "
                     + "element's path.");

        assertEquals(expectedOnLvl1AtRoot,
                     stateChecker.apply(tree, path_root),
                     "Should return " + expectedOnLvl1AtSame + " on a tree with a 1st level element checking at the "
                     + "root.");

        assertEquals(expectedOnLvl1AtChild,
                     stateChecker.apply(tree, path_lvl1_child),
                     "Should return " + expectedOnLvl1AtSame + " on a tree with a 1st level element checking at a "
                     + "child path of that element's.");

        assertEquals(expectedOnLvl1AtSibling,
                     stateChecker.apply(tree, path_lvl1_sibling),
                     "Should return " + expectedOnLvl1AtSame + " on a tree with a 1st level element checking at a "
                     + "sibling path of that element's.");

        // Lvl2
        tree = getNewEmptyTree();
        tree.setAt(8, path_lvl2);
        assertEquals(expectedOnLvl2AtSame,
                     stateChecker.apply(tree, path_lvl2),
                     "Should return " + expectedOnLvl2AtSame + " on a tree with a 2nd level element checking at that "
                     + "element's path.");

        assertEquals(expectedOnLvl2AtRoot,
                     stateChecker.apply(tree, path_root),
                     "Should return " + expectedOnLvl2AtRoot + " on a tree with a 2nd level element checking at the "
                     + "root.");

        assertEquals(expectedOnLvl2AtParent,
                     stateChecker.apply(tree, path_lvl2_parent),
                     "Should return " + expectedOnLvl2AtParent + " on a tree with a 2nd level element checking at a "
                     + "parent path of that element's.");

        assertEquals(expectedOnLvl2AtChild,
                     stateChecker.apply(tree, path_lvl2_child),
                     "Should return " + expectedOnLvl2AtChild + " on a tree with a 2nd level element checking at a "
                     + "child path of that element's.");

        assertEquals(expectedOnLvl2AtSibling,
                     stateChecker.apply(tree, path_lvl2_sibling),
                     "Should return " + expectedOnLvl2AtSibling + " on a tree with a 2nd level element checking at a "
                     + "sibling path of that element's.");

        assertEquals(expectedOnLvl2AtUncle,
                     stateChecker.apply(tree, path_lvl2_uncle),
                     "Should return " + expectedOnLvl2AtUncle + " on a tree with a 2nd level element checking at an "
                     + "uncle path of that element's.");

        assertEquals(expectedOnLvl2AtCousin,
                     stateChecker.apply(tree, path_lvl2_cousin),
                     "Should return " + expectedOnLvl2AtCousin + " on a tree with a 2nd level element checking at a "
                     + "cousin path of that element's.");
    }

    private static <T> boolean arrayStartsWithOtherArray(T[] mightStartWithOther, T[] other)
    {
        if(mightStartWithOther.length < other.length)
            return false;

        for(int i = 0; i < other.length; i++)
        {
            T it = mightStartWithOther[i];

            if((it == null) ? (other[i] != null) : (!it.equals(other[i])))
                return false;
        }

        return true;
    }

    // Checks whether they have the same items, maintaining duplicity but ignoring order.
    private static <T> boolean collectionsAreSame(Collection<T> c1, Collection<T> c2)
    {
        Map<T, Integer> c1Counts = new HashMap<>();
        Map<T, Integer> c2Counts = new HashMap<>();

        for(T i : c1)
            c1Counts.put(i, c1Counts.getOrDefault(i, 0) + 1);

        for(T i : c2)
            c2Counts.put(i, c2Counts.getOrDefault(i, 0) + 1);

        return c1Counts.equals(c2Counts);
    }

    private static <T> boolean removeFirstWhere(Collection<T> col, Predicate<T> predicate)
    {
        Iterator<T> iter = col.iterator();

        while(iter.hasNext())
        {
            T i = iter.next();

            if(predicate.test(i))
            {
                iter.remove();
                return true;
            }
        }

        return false;
    }

    // Checks whether they have the item paths, ignoring order.
    private static <T> boolean pathCollectionsAreSame(Collection<Tree.TreePath<T>> c1, Collection<Tree.TreePath<T>> c2)
    {
        if(c1.size() != c2.size())
            return false;

        ArrayList<Tree.TreePath> c2copy = new ArrayList<>(c2);

        for(Tree.TreePath path : c1)
            if(!removeFirstWhere(c2copy, path::equals))
                return false;

        return true;
    }

    // Checks whether they have the same item paths in the same order.
    private static boolean pathListsAreSame(List<Tree.TreePath<String>> l1, List<Tree.TreePath<String>> l2)
    {
        int size = l1.size();

        if(l2.size() != size)
            return false;

        for(int i = 0; i < size; i++)
            if(!l1.get(i).equals(l2.get(i)))
                return false;

        return true;
    }

    @Test
    void hasItems()
    { stateCheckTest(x -> x.hasItems(), y -> !y.isEmpty());}

    @Test
    void hasNonRootItems()
    { stateCheckTest(x -> x.hasNonRootItems(), y -> y.stream().anyMatch(z -> !Arrays.equals(z, new String[] {}))); }

    @Test
    void hasItemsAtOrUnder()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.hasItemsAtOrUnder(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().anyMatch(x -> arrayStartsWithOtherArray(x, pathCheckingAt)));
    }

    @Test
    void hasItemsUnder()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.hasItemsUnder(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream() .anyMatch(x -> (!Arrays.equals(x, pathCheckingAt))
                                                                  && (arrayStartsWithOtherArray(x, pathCheckingAt))));
    }

    @Test
    void hasItemsAlong()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.hasItemsAlong(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().anyMatch(x -> arrayStartsWithOtherArray(pathCheckingAt, x)));
    }

    @Test
    void hasNonRootItemsAlong()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.hasNonRootItemsAlong(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().anyMatch(x -> (!Arrays.equals(x, new String[0]))
                                                                 && (arrayStartsWithOtherArray(pathCheckingAt, x))));
    }

    @Test
    void hasItemAt()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.hasItemAt(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().anyMatch(x -> Arrays.equals(x, pathCheckingAt)));
    }

    @Test
    void hasRootItem()
    { stateCheckTest(x -> x.hasRootItem(), y -> y.stream().anyMatch(z -> Arrays.equals(z, new String[] {}))); }

    @Test
    void isEmpty()
    { stateCheckTest(x -> x.isEmpty(), y -> y.isEmpty()); }

    @Test
    void isEmptyUnderRoot()
    { stateCheckTest(x -> x.isEmptyUnderRoot(), y -> y.stream().noneMatch(z -> !Arrays.equals(z, new String[] {}))); }

    @Test
    void isEmptyAtAndUnder()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.isEmptyAtAndUnder(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().noneMatch(x -> arrayStartsWithOtherArray(x, pathCheckingAt)));
    }

    @Test
    void isEmptyUnder()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.isEmptyUnder(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().noneMatch(x -> (!Arrays.equals(x, pathCheckingAt))
                                                                  && (arrayStartsWithOtherArray(x, pathCheckingAt))));
    }

    @Test
    void isEmptyAlong()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.isEmptyAlong(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().noneMatch(x -> arrayStartsWithOtherArray(pathCheckingAt, x)));
    }

    @Test
    void isEmptyAlongAfterRoot()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.isEmptyAlongAfterRoot(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().noneMatch(x -> (!Arrays.equals(x, path_root))
                                                                  && (arrayStartsWithOtherArray(pathCheckingAt, x))));
    }

    @Test
    void isEmptyAt()
    {
        stateCheckAtPathTest((T tree, String[] path) -> tree.isEmptyAt(path),
                (List<String[]> pathsWithItems, String[] pathCheckingAt)
                        -> pathsWithItems.stream().noneMatch(x -> Arrays.equals(pathCheckingAt, x)));
    }

    @Test
    void isEmptyAtRoot()
    { stateCheckTest(x -> x.isEmptyAtRoot(), y -> y.stream().noneMatch(z -> Arrays.equals(z, new String[] {}))); }

    @Test
    void getRootItem()
    {
        T tree = getNewEmptyTree();
        assertThrows(Tree.NoItemAtPathException.class,
                     tree::getRootItem,
                     "Should throw an exception when getting the root item of a new empty tree.");

        tree.setAt(5, path_lvl1);
        assertThrows(Tree.NoItemAtPathException.class,
                     tree::getRootItem,
                     "Should throw an exception when getting the root item of a tree with only a level 1 item.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        assertThrows(Tree.NoItemAtPathException.class,
                     tree::getRootItem,
                     "Should throw an exception when getting the root item of a tree with only a level 2 item.");

        tree = getNewEmptyTree();
        tree.setRootItem(7);
        assertEquals(7, tree.getRootItem(),
                     "When an item is set at the root with .setRootItem, that item should be returned.");

        tree = getNewEmptyTree();
        tree.setAt(8);
        assertEquals(8, tree.getRootItem(),
                     "When an item is set at the root with .setAt, that item should be returned.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getRootItem(),
                   "When null is set at the root with .setRootItem, null should be returned.");
    }

    @Test
    void getRootItemSafely()
    {
        T tree = getNewEmptyTree();
        Tree.ValueWithPresence<Integer> r = tree.getRootItemSafely();
        assertFalse(r.valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting the root item of a new empty tree.");

        tree.setAt(5, path_lvl1);
        r = tree.getRootItemSafely();
        assertFalse(r.valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting the root item of a tree with only a level 1 "
                    + "item.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        r = tree.getRootItemSafely();
        assertFalse(r.valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting the root item of a tree with only a level 2 "
                    + "item.");

        tree = getNewEmptyTree();
        tree.setRootItem(7);
        r = tree.getRootItemSafely();
        assertTrue(r.valueWasPresent(),
                   "When an item is set at the root with .setRootItem, should return a ValueWithPresence containing an "
                   + "item.");

        assertEquals(7, r.getValue(),
                     "When an item is set at the root with .setRootItem, should return a ValueWithPresence containing "
                     + "*that same* item.");

        tree = getNewEmptyTree();
        tree.setAt(8);
        r = tree.getRootItemSafely();
        assertTrue(r.valueWasPresent(),
                   "When an item is set at the root with .setAt, should return a ValueWithPresence containing an "
                   + "item.");
        assertEquals(8, r.getValue(),
                     "When an item is set at the root with .setAt, should return a ValueWithPresence containing *that "
                     + "same* item.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        r = tree.getRootItemSafely();
        assertTrue(r.valueWasPresent(),
                   "When null is set at the root with .setRootItem, should return a ValueWithPresence containing an "
                   + "item. (even if it's null)");
        assertNull(r.getValue(),
                   "When null is set at the root with .setRootItem, should return a ValueWithPresence specifically "
                   + "containing null.");
    }

    @Test
    void getRootItemOrDefault()
    {
        int d = 4;
        T tree = getNewEmptyTree();
        assertEquals(d, tree.getRootItemOrDefault(d),
                     "Should return default when getting the root item of a new empty tree.");

        tree.setAt(5, path_lvl1);
        assertEquals(d, tree.getRootItemOrDefault(d),
                     "Should return default when getting the root item of a tree with only a level 1 item.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        assertEquals(d, tree.getRootItemOrDefault(d),
                     "Should return default when getting the root item of a tree with only a level 2 item.");

        tree = getNewEmptyTree();
        tree.setRootItem(7);
        assertEquals(7, tree.getRootItemOrDefault(d),
                     "When an item is set at the root with .setRootItem, should return that item.");

        tree = getNewEmptyTree();
        tree.setAt(8);
        assertEquals(8, tree.getRootItemOrDefault(d),
                     "When an item is set at the root with .setAt, should return that item.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getRootItemOrDefault(d),
                   "When an null is set at the root with .setRootItem, should return null.");
    }

    @Test
    void getRootItemOrDefaultAnyType()
    {
        Object d = new Object();
        T tree = getNewEmptyTree();
        assertEquals(d, tree.getRootItemOrDefaultAnyType(d),
                     "Should return default when getting the root item of a new empty tree.");

        tree.setAt(5, path_lvl1);
        assertEquals(d, tree.getRootItemOrDefaultAnyType(d),
                     "Should return default when getting the root item of a tree with only a level 1 item.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        assertEquals(d, tree.getRootItemOrDefaultAnyType(d),
                     "Should return default when getting the root item of a tree with only a level 2 item.");

        tree = getNewEmptyTree();
        tree.setRootItem(7);
        assertEquals(7, tree.getRootItemOrDefaultAnyType(d),
                     "When an item is set at the root with .setRootItem, should return that item.");

        tree = getNewEmptyTree();
        tree.setAt(8);
        assertEquals(8, tree.getRootItemOrDefaultAnyType(d),
                     "When an item is set at the root with .setAt, should return that item.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getRootItemOrDefaultAnyType(d),
                   "When an null is set at the root with .setRootItem, should return null.");
    }

    @Test
    void getRootItemOrNull()
    {
        T tree = getNewEmptyTree();
        assertNull(tree.getRootItemOrNull(),
                   "Should return null when getting the root item of a new empty tree.");

        tree.setAt(5, path_lvl1);
        assertNull(tree.getRootItemOrNull(),
                   "Should return null when getting the root item of a tree with only a level 1 item.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        assertNull(tree.getRootItemOrNull(),
                   "Should return null when getting the root item of a tree with only a level 2 item.");

        tree = getNewEmptyTree();
        tree.setRootItem(7);
        assertEquals(7, tree.getRootItemOrNull(),
                     "When an item is set at the root with .setRootItem, should return that item.");

        tree = getNewEmptyTree();
        tree.setAt(8);
        assertEquals(8, tree.getRootItemOrNull(),
                     "When an item is set at the root with .setAt, should return that item.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getRootItemOrNull(),
                   "When an null is set at the root with .setRootItem, should return null.");
    }

    @Test
    void getAt()
    {
        T tree1 = getNewEmptyTree();
        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree1.getAt(),
                     "Should throw an exception when getting the root item of an empty tree");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree1.getAt(path_lvl1),
                     "Should throw an exception when getting from a level 1 path of an empty tree");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree1.getAt(path_lvl2),
                     "Should throw an exception when getting from a level 2 path of an empty tree");

        T tree2 = getNewEmptyTree();
        tree2.setRootItem(5);
        assertEquals(5, tree2.getAt(), "When an item is set at root, getting from root should return that item.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree2.getAt(path_lvl1),
                     "When an item is set at root and nowhere else, getting at a level 1 path should throw an "
                     + "exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree2.getAt(path_lvl2),
                     "When an item is set at root and nowhere else, getting at a level 1 path should throw an "
                     + "exception.");

        T tree3 = getNewEmptyTree();
        tree3.setRootItem(null);
        assertEquals(null, tree3.getAt(), "When null is set at root, getting from root should return null.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree3.getAt(path_lvl1),
                     "When null is set at root and nowhere else, getting at a level 1 path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree3.getAt(path_lvl2),
                     "When null is set at root and nowhere else, getting at a level 1 path should throw an exception.");

        T tree4 = getNewEmptyTree();
        tree4.setAt(6, path_lvl1);
        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree4.getAt(),
                     "When an item is set at a level 1 path, getting at root should throw an exception.");

        assertEquals(6, tree4.getAt(path_lvl1),
                     "When an item is set at a level 1 path, getting at that path should return that item.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree4.getAt(path_lvl1_child),
                     "When an item is set at a level 1 path, getting at a child path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree4.getAt(path_lvl1_sibling),
                     "When an item is set at a level 1 path, getting at a sibling path should throw an exception.");

        T tree5 = getNewEmptyTree();
        tree5.setAt(null, path_lvl1);
        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree5.getAt(),
                     "When null is set at a level 1 path, getting at root should throw an exception.");

        assertEquals(null, tree5.getAt(path_lvl1),
                     "When null is set at a level 1 path, getting at that path should return null.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree5.getAt(path_lvl1_child),
                     "When null is set at a level 1 path, getting at a child path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree5.getAt(path_lvl1_sibling),
                     "When null is set at a level 1 path, getting at a sibling path should throw an exception.");

        T tree6 = getNewEmptyTree();
        tree6.setAt(7, path_lvl2);
        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(),
                     "When an item is set at a level 2 path, getting at root should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(path_lvl2_parent),
                     "When an item is set at a level 2 path, getting at a parent path should throw an exception.");

        assertEquals(7, tree6.getAt(path_lvl2),
                     "When an item is set at a level 2 path, getting at that path should return that item.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(path_lvl2_child),
                     "When an item is set at a level 2 path, getting at a child path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(path_lvl2_sibling),
                     "When an item is set at a level 2 path, getting at a sibling path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(path_lvl2_uncle),
                     "When an item is set at a level 2 path, getting at an uncle path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree6.getAt(path_lvl2_cousin),
                     "When an item is set at a level 2 path, getting at a cousin path should throw an exception.");

        T tree7 = getNewEmptyTree();
        tree7.setAt(null, path_lvl2);
        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(),
                     "When null is set at a level 2 path, getting at root should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(path_lvl2_parent),
                     "When null is set at a level 2 path, getting at a parent path should throw an exception.");

        assertEquals(null, tree7.getAt(path_lvl2),
                     "When null is set at a level 2 path, getting at that path should return null.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(path_lvl2_child),
                     "When null is set at a level 2 path, getting at a child path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(path_lvl2_sibling),
                     "When null is set at a level 2 path, getting at a sibling path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(path_lvl2_uncle),
                     "When null is set at a level 2 path, getting at an uncle path should throw an exception.");

        assertThrows(Tree.NoItemAtPathException.class,
                     () -> tree7.getAt(path_lvl2_cousin),
                     "When null is set at a level 2 path, getting at a cousin path should throw an exception.");
    }

    @Test
    void getAtSafely()
    {
        T tree = getNewEmptyTree();
        assertFalse(tree.getAtSafely().valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting at the root of a new empty tree.");

        assertFalse(tree.getAtSafely(path_lvl1).valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting at a level 1 path of a new empty tree.");

        assertFalse(tree.getAtSafely(path_lvl2).valueWasPresent(),
                    "Should return an empty ValueWithPresence when getting at a level 2 path of a new empty tree.");

        tree.setRootItem(5);
        assertTrue(tree.getAtSafely().valueWasPresent(),
                   "When an item is set at root, getting at root should return a ValueWithPresence with an item.");

        assertEquals(5, tree.getAtSafely().getValue(),
                     "When an item is set at root, getting at root should return a ValueWithPresence with *the same "
                     + "item as the one that was set.*");

        assertFalse(tree.getAtSafely(path_lvl1).valueWasPresent(),
                    "When an item is set at root, getting at a level 1 path should return an empty ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2).valueWasPresent(),
                    "When an item is set at root, getting at a level 2 path should return an empty ValueWithPresence.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertTrue(tree.getAtSafely().valueWasPresent(),
                   "When null is set at root, getting at root should return a ValueWithPresence with an item.");

        assertEquals(null, tree.getAtSafely().getValue(),
                     "When null is set at root, getting at root should return a ValueWithPresence with *null*.");

        assertFalse(tree.getAtSafely(path_lvl1).valueWasPresent(),
                    "When null is set at root, getting at a level 1 path should return an empty ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2).valueWasPresent(),
                    "When null is set at root, getting at a level 2 path should return an empty ValueWithPresence.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertFalse(tree.getAtSafely().valueWasPresent(),
                    "When an item is set at a level 1 path, getting at root should return an empty ValueWithPresence.");

        assertTrue(tree.getAtSafely(path_lvl1).valueWasPresent(),
                   "When an item is set at a level 1 path, getting at that path should return a ValueWithPresence with "
                   + "an item.");

        assertEquals(6, tree.getAtSafely(path_lvl1).getValue(),
                     "When an item is set at a level 1 path, getting at that path should return a ValueWithPresence "
                     + "with *the same item as the one that was set.*");

        assertFalse(tree.getAtSafely(path_lvl1_child).valueWasPresent(),
                    "When an item is set at a level 1 path, getting at a child path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl1_sibling).valueWasPresent(),
                    "When an item is set at a level 1 path, getting at a sibling path should return an empty "
                    + "ValueWithPresence.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertFalse(tree.getAtSafely().valueWasPresent(),
                    "When null is set at a level 1 path, getting at root should return an empty ValueWithPresence.");

        assertTrue(tree.getAtSafely(path_lvl1).valueWasPresent(),
                   "When null is set at a level 1 path, getting at that path should return a ValueWithPresence with an "
                   + "item.");

        assertEquals(null, tree.getAtSafely(path_lvl1).getValue(),
                     "When null is set at a level 1 path, getting at that path should return a ValueWithPresence with "
                     + "*null*.");

        assertFalse(tree.getAtSafely(path_lvl1_child).valueWasPresent(),
                    "When null is set at a level 1 path, getting at a child path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl1_sibling).valueWasPresent(),
                    "When null is set at a level 1 path, getting at a sibling path should return an empty "
                    + "ValueWithPresence.");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertFalse(tree.getAtSafely().valueWasPresent(),
                    "When an item is set at a level 2 path, getting at root should return an empty ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_parent).valueWasPresent(),
                    "When an item is set at a level 2 path, getting at a parent path should return an empty "
                    + "ValueWithPresence.");

        assertTrue(tree.getAtSafely(path_lvl2).valueWasPresent(),
                   "When an item is set at a level 2 path, getting at that path should return a ValueWithPresence "
                   + "with an item.");

        assertEquals(7, tree.getAtSafely(path_lvl2).getValue(),
                     "When an item is set at a level 2 path, getting at that path should return a ValueWithPresence "
                     + "with *the same item as the one that was set.*");

        assertFalse(tree.getAtSafely(path_lvl2_child).valueWasPresent(),
                    "When an item is set at a level 2 path, getting at a child path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_sibling).valueWasPresent(),
                    "When an item is set at a level 2 path, getting at a sibling path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_uncle).valueWasPresent(),
                    "When an item is set at a level 2 path, getting at an uncle path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_cousin).valueWasPresent(),
                    "When an item is set at a level 2 path, getting at a cousin path should return an empty "
                    + "ValueWithPresence.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertFalse(tree.getAtSafely().valueWasPresent(),
                    "When null is set at a level 2 path, getting at root should return an empty ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_parent).valueWasPresent(),
                    "When null is set at a level 2 path, getting at a parent path should return an empty "
                    + "ValueWithPresence.");

        assertTrue(tree.getAtSafely(path_lvl2).valueWasPresent(),
                   "When null is set at a level 2 path, getting at that path should return a ValueWithPresence with an "
                   + "item.");

        assertEquals(null, tree.getAtSafely(path_lvl2).getValue(),
                     "When null is set at a level 2 path, getting at that path should return a ValueWithPresence with "
                     + "*null*.");

        assertFalse(tree.getAtSafely(path_lvl2_child).valueWasPresent(),
                    "When null is set at a level 2 path, getting at a child path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_sibling).valueWasPresent(),
                    "When null is set at a level 2 path, getting at a sibling path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_uncle).valueWasPresent(),
                    "When null is set at a level 2 path, getting at an uncle path should return an empty "
                    + "ValueWithPresence.");

        assertFalse(tree.getAtSafely(path_lvl2_cousin).valueWasPresent(),
                    "When null is set at a level 2 path, getting at a cousin path should return an empty "
                    + "ValueWithPresence.");
    }

    @Test
    void getAtOrDefault()
    {
        int d = 4;
        T tree = getNewEmptyTree();
        assertEquals(d, tree.getAtOrDefault(d), "On a new empty tree, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1),
                     "On a new empty tree, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2),
                     "On a new empty tree, getting at a level 2 path should return default.");
        
        tree.setRootItem(5);
        assertEquals(5, tree.getAtOrDefault(d),
                     "When an item is set at root, getting at root should return that item.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1),
                     "When an item is set at root, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2),
                     "When an item is set at root, getting at a level 2 path should return default.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertEquals(null, tree.getAtOrDefault(d), "When null is set at root, getting at root should return null.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1),
                     "When null is set at root, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2),
                     "When null is set at root, getting at a level 2 path should return default.");
        
        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertEquals(d, tree.getAtOrDefault(d),
                     "When an item is set at a level 1 path, getting at root should return default.");

        assertEquals(6, tree.getAtOrDefault(d, path_lvl1),
                     "When an item is set at a level 1 path, getting at that path should return that item.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1_child),
                     "When an item is set at a level 1 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1_sibling),
                     "When an item is set at a level 1 path, getting at a sibling path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertEquals(d, tree.getAtOrDefault(d),
                     "When null is set at a level 1 path, getting at root should return default.");

        assertEquals(null, tree.getAtOrDefault(d, path_lvl1),
                     "When null is set at a level 1 path, getting at that path should return null.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1_child),
                     "When null is set at a level 1 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl1_sibling),
                     "When null is set at a level 1 path, getting at a sibling path should return default.");
        
        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertEquals(d, tree.getAtOrDefault(d),
                     "When an item is set at a level 2 path, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_parent),
                     "When an item is set at a level 2 path, getting at a parent path should return default.");

        assertEquals(7, tree.getAtOrDefault(d, path_lvl2),
                     "When an item is set at a level 2 path, getting at that path should return that item.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_child),
                     "When an item is set at a level 2 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_sibling),
                     "When an item is set at a level 2 path, getting at a sibling path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_uncle),
                     "When an item is set at a level 2 path, getting at an uncle path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_cousin),
                     "When an item is set at a level 2 path, getting at a cousin path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertEquals(d, tree.getAtOrDefault(d),
                     "When an item is set at a level 2 path, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_parent),
                     "When an item is set at a level 2 path, getting at a parent path should return default.");

        assertEquals(null, tree.getAtOrDefault(d, path_lvl2),
                     "When an item is set at a level 2 path, getting at that path should return null.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_child),
                     "When an item is set at a level 2 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_sibling),
                     "When an item is set at a level 2 path, getting at a sibling path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_uncle),
                     "When an item is set at a level 2 path, getting at an uncle path should return default.");

        assertEquals(d, tree.getAtOrDefault(d, path_lvl2_cousin),
                     "When an item is set at a level 2 path, getting at a cousin path should return default.");
    }

    @Test
    void getAtOrDefaultAnyType()
    {
        Object d = new Object();
        T tree = getNewEmptyTree();
        assertEquals(d, tree.getAtOrDefaultAnyType(d), "On a new empty tree, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1),
                     "On a new empty tree, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2),
                     "On a new empty tree, getting at a level 2 path should return default.");

        tree.setRootItem(5);
        assertEquals(5, tree.getAtOrDefaultAnyType(d),
                     "When an item is set at root, getting at root should return that item.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1),
                     "When an item is set at root, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2),
                     "When an item is set at root, getting at a level 2 path should return default.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getAtOrDefaultAnyType(d), "When null is set at root, getting at root should return null.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1),
                     "When null is set at root, getting at a level 1 path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2),
                     "When null is set at root, getting at a level 2 path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertEquals(d, tree.getAtOrDefaultAnyType(d),
                     "When an item is set at a level 1 path, getting at root should return default.");

        assertEquals(6, tree.getAtOrDefaultAnyType(d, path_lvl1),
                     "When an item is set at a level 1 path, getting at that path should return that item.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1_child),
                     "When an item is set at a level 1 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1_sibling),
                     "When an item is set at a level 1 path, getting at a sibling path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertEquals(d, tree.getAtOrDefaultAnyType(d),
                     "When null is set at a level 1 path, getting at root should return default.");

        assertNull(tree.getAtOrDefaultAnyType(d, path_lvl1),
                   "When null is set at a level 1 path, getting at that path should return null.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1_child),
                     "When null is set at a level 1 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl1_sibling),
                     "When null is set at a level 1 path, getting at a sibling path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertEquals(d, tree.getAtOrDefaultAnyType(d),
                     "When an item is set at a level 2 path, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_parent),
                     "When an item is set at a level 2 path, getting at a parent path should return default.");

        assertEquals(7, tree.getAtOrDefaultAnyType(d, path_lvl2),
                     "When an item is set at a level 2 path, getting at that path should return that item.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_child),
                     "When an item is set at a level 2 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_sibling),
                     "When an item is set at a level 2 path, getting at a sibling path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_uncle),
                     "When an item is set at a level 2 path, getting at an uncle path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_cousin),
                     "When an item is set at a level 2 path, getting at a cousin path should return default.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertEquals(d, tree.getAtOrDefaultAnyType(d),
                     "When an item is set at a level 2 path, getting at root should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_parent),
                     "When an item is set at a level 2 path, getting at a parent path should return default.");

        assertNull(tree.getAtOrDefaultAnyType(d, path_lvl2),
                   "When an item is set at a level 2 path, getting at that path should return null.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_child),
                     "When an item is set at a level 2 path, getting at a child path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_sibling),
                     "When an item is set at a level 2 path, getting at a sibling path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_uncle),
                     "When an item is set at a level 2 path, getting at an uncle path should return default.");

        assertEquals(d, tree.getAtOrDefaultAnyType(d, path_lvl2_cousin),
                     "When an item is set at a level 2 path, getting at a cousin path should return default.");
    }

    @Test
    void getAtOrNull()
    {
        T tree = getNewEmptyTree();
        assertNull(tree.getAtOrNull(), "On a new empty tree, getting at root should return null.");
        assertNull(tree.getAtOrNull(path_lvl1), "On a new empty tree, getting at a level 1 path should return null.");
        assertNull(tree.getAtOrNull(path_lvl2), "On a new empty tree, getting at a level 2 path should return null.");

        tree.setRootItem(5);
        assertEquals(5, tree.getAtOrNull(), "When an item is set at root, getting at root should return that item.");

        assertNull(tree.getAtOrNull(path_lvl1),
                   "When an item is set at root, getting at a level 1 path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2),
                   "When an item is set at root, getting at a level 2 path should return null.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertNull(tree.getAtOrNull(), "When null is set at root, getting at root should return null.");

        assertNull(tree.getAtOrNull(path_lvl1),
                   "When null is set at root, getting at a level 1 path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2),
                   "When null is set at root, getting at a level 2 path should return null.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertNull(tree.getAtOrNull(), "When an item is set at a level 1 path, getting at root should return null.");

        assertEquals(6, tree.getAtOrNull(path_lvl1),
                     "When an item is set at a level 1 path, getting at that path should return that item.");

        assertNull(tree.getAtOrNull(path_lvl1_child),
                   "When an item is set at a level 1 path, getting at a child path should return null.");

        assertNull(tree.getAtOrNull(path_lvl1_sibling),
                   "When an item is set at a level 1 path, getting at a sibling path should return null.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertNull(tree.getAtOrNull(), "When null is set at a level 1 path, getting at root should return null.");

        assertNull(tree.getAtOrNull(path_lvl1),
                   "When null is set at a level 1 path, getting at that path should return null.");

        assertNull(tree.getAtOrNull(path_lvl1_child),
                   "When null is set at a level 1 path, getting at a child path should return null.");

        assertNull(tree.getAtOrNull(path_lvl1_sibling),
                   "When null is set at a level 1 path, getting at a sibling path should return null.");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertNull(tree.getAtOrNull(), "When an item is set at a level 2 path, getting at root should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_parent),
                   "When an item is set at a level 2 path, getting at a parent path should return null.");

        assertEquals(7, tree.getAtOrNull(path_lvl2),
                     "When an item is set at a level 2 path, getting at that path should return that item.");

        assertNull(tree.getAtOrNull(path_lvl2_child),
                   "When an item is set at a level 2 path, getting at a child path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_sibling),
                   "When an item is set at a level 2 path, getting at a sibling path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_uncle),
                   "When an item is set at a level 2 path, getting at an uncle path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_cousin),
                   "When an item is set at a level 2 path, getting at a cousin path should return null.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertNull(tree.getAtOrNull(), "When an item is set at a level 2 path, getting at root should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_parent),
                   "When an item is set at a level 2 path, getting at a parent path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2),
                   "When an item is set at a level 2 path, getting at that path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_child),
                   "When an item is set at a level 2 path, getting at a child path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_sibling),
                   "When an item is set at a level 2 path, getting at a sibling path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_uncle),
                   "When an item is set at a level 2 path, getting at an uncle path should return null.");

        assertNull(tree.getAtOrNull(path_lvl2_cousin),
                   "When an item is set at a level 2 path, getting at a cousin path should return null.");
    }

    @Test
    void getPaths()
    {
        T tree = getNewEmptyTree();
        assertTrue(tree.getPaths().isEmpty(), "On a new empty tree, getPaths should return an empty collection.");

        tree.setRootItem(5);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_root),
                   "With an item at root, should return a collection containing just the root path. (an empty array)");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_root),
                   "With null at root, should return a collection containing just the root path. (an empty array)");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_lvl1),
                   "With an item at a level 1 path, should return a collection containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_lvl1),
                   "With null at a level 1 path, should return a collection containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_lvl2),
                   "With an item at a level 2 path, should return a collection containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), tpathList_lvl2),
                   "With null at a level 2 path, should return a collection containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(8, path_lvl1);
        tree.setAt(8, path_lvl1_sibling);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), Arrays.asList(tpath_lvl1, tpath_lvl1_sibling)),
                   "With the same item at two paths, should return both paths.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        tree.setAt(null, path_lvl1_sibling);
        assertTrue(pathCollectionsAreSame(tree.getPaths(), Arrays.asList(tpath_lvl1, tpath_lvl1_sibling)),
                   "With null at two paths, should return both paths.");
    }

    @Test
    void getPathsInOrder()
    {
        T tree = getNewEmptyTree();
        assertTrue(tree.getPaths().isEmpty(), "On a new empty tree, getPaths should return an empty collection.");

        tree.setRootItem(5);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_root),
                   "With an item at root, should return a collection list just the root path. (an empty array)");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_root),
                   "With null at root, should return a collection list just the root path. (an empty array)");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl1);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_lvl1),
                   "With an item at a level 1 path, should return a list containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl1);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_lvl1),
                   "With null at a level 1 path, should return a list containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(7, path_lvl2);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_lvl2),
                   "With an item at a level 2 path, should return a list containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(null, path_lvl2);
        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()), tpathList_lvl2),
                   "With null at a level 2 path, should return a list containing just that path.");

        tree = getNewEmptyTree();
        tree.setAt(5, "foot");
        tree.setRootItem(7);
        tree.setAt(6, "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("doot"),
                        new Tree.TreePath<>("foot"))),
                "With items set at root and 2 level 1 paths, should return a list containing root and those two paths "
                + "in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "foot");
        tree.setRootItem(null);
        tree.setAt(6, "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("doot"),
                        new Tree.TreePath<>("foot"))),
                "With null set at root and items set at 2 level 1 paths, should return a list containing root and "
                + "those two paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "foot");
        tree.setAt(6, "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>("doot"),
                        new Tree.TreePath<>("foot"))),
                "With items set 2 level 1 paths, should return a list containing root and those two paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "goot", "foot");
        tree.setAt(7, "goot");
        tree.setAt(6, "goot", "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>("goot"),
                        new Tree.TreePath<>("goot", "doot"),
                        new Tree.TreePath<>("goot", "foot"))),
                "With items set at a level 1 path and 2 level 1 paths, should return a list containing root and those "
                + "two paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "goot", "foot");
        tree.setAt(null, "goot");
        tree.setAt(6, "goot", "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>("goot"),
                        new Tree.TreePath<>("goot", "doot"),
                        new Tree.TreePath<>("goot", "foot"))),
                "With null set at a level 1 path and items set at 2 level 1 paths, should return a list containing "
                + "root and those two paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "goot", "foot");
        tree.setAt(6, "goot", "doot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>("goot", "doot"),
                        new Tree.TreePath<>("goot", "foot"))),
                "With items set 2 level 2 paths, should return a list containing root and those two paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "joot", "soot");
        tree.setAt(6, "joot", "soot", "foot", "hoot");
        tree.setAt(7, "joot");
        tree.setRootItem(8);
        tree.setAt(9, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With values set at paths in a 5-item-deep chain, should return a list containing those paths in "
                + "order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "joot", "soot");
        tree.setAt(6, "joot", "soot", "foot", "hoot");
        tree.setAt(7, "joot");
        tree.setRootItem(null);
        tree.setAt(9, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With values set at paths in a 5-item-deep chain, with null at the root, should return a list "
                + "containing those paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(null, "joot", "soot");
        tree.setAt(6, "joot", "soot", "foot", "hoot");
        tree.setAt(7, "joot");
        tree.setRootItem(8);
        tree.setAt(9, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With values set at paths in a 5-item-deep chain, with null at at the level 2 path, should return a "
                + "list containing those paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "joot", "soot");
        tree.setAt(6, "joot", "soot", "foot", "hoot");
        tree.setAt(7, "joot");
        tree.setAt(9, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With values set at paths in a 5-item-deep chain, excluding root, should return a list containing "
                + "those paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(6, "joot", "soot", "foot", "hoot");
        tree.setAt(7, "joot");
        tree.setRootItem(8);
        tree.setAt(9, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With values set at paths in a 5-item-deep chain, excluding an item at level 2, should return a list "
                + "containing those paths in order.");

        tree = getNewEmptyTree();
        tree.setAt(5, "joot", "soot");
        tree.setAt(5, "joot", "soot", "foot", "hoot");
        tree.setAt(5, "joot");
        tree.setRootItem(5);
        tree.setAt(5, "joot", "soot", "foot");

        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With the same value set at paths in a 5-item-deep chain, should return a list containing those paths "
                + "in order.");

        tree = getNewEmptyTree();
        tree.setAt(null, "joot", "soot");
        tree.setAt(null, "joot", "soot", "foot", "hoot");
        tree.setAt(null, "joot");
        tree.setRootItem(null);
        tree.setAt(null, "joot", "soot", "foot");



        assertTrue(pathListsAreSame(tree.getPathsInOrder(Comparator.naturalOrder()),
                Arrays.asList(
                        new Tree.TreePath<>(),
                        new Tree.TreePath<>("joot"),
                        new Tree.TreePath<>("joot", "soot"),
                        new Tree.TreePath<>("joot", "soot", "foot"),
                        new Tree.TreePath<>("joot", "soot", "foot", "hoot"))),
                "With null set at paths in a 5-item-deep chain, should return a list containing those paths in order.");
    }

    List<Triplet<Map<Tree.TreePath<String>, Integer>, Tree.TreePath<String>, String>> treePresetsForMultiGettersAtPaths = new ArrayList<>();
    {
        Map<Tree.TreePath<String>, Integer>
                paths_empty                         = new HashMap<>(),
                paths_root                          = new HashMap<>(),
                paths_root_null                     = new HashMap<>(),
                paths_lvl1                          = new HashMap<>(),
                paths_lvl1_null                     = new HashMap<>(),
                paths_lvl1s                         = new HashMap<>(),
                paths_lvl1sAndRoot                  = new HashMap<>(),
                paths_lvl2                          = new HashMap<>(),
                paths_lvl2_null                     = new HashMap<>(),
                paths_lvl2s                         = new HashMap<>(),
                paths_lvl2sAndLvl1_siblings         = new HashMap<>(),
                paths_lvl2sAndLvl1_childAndCousin   = new HashMap<>(),
                paths_lvl2sAndLvl1_cousinsDiffUncle = new HashMap<>(),
                paths_lvl2sAndRoot                  = new HashMap<>(),
                paths_lvl1_same                     = new HashMap<>(),
                paths_lvl1_samenull                 = new HashMap<>(),
                paths_chain                         = new HashMap<>(),
                paths_chain_same                    = new HashMap<>(),
                paths_chain_samenull                = new HashMap<>(),
                paths_chain_noroot                  = new HashMap<>(),
                paths_chain_broken                  = new HashMap<>();

        int x = 5;

        paths_root                          .put(tpath_root,            ++x);
        paths_root_null                     .put(tpath_root,            null);
        paths_lvl1                          .put(tpath_lvl1,            ++x);
        paths_lvl1_null                     .put(tpath_lvl1,            null);
        paths_lvl2                          .put(tpath_lvl2,            ++x);
        paths_lvl2_null                     .put(tpath_lvl2,            null);
        paths_lvl1s                         .put(tpath_lvl1,            ++x);
        paths_lvl1s                         .put(tpath_lvl1_sibling,    ++x);
        paths_lvl1sAndRoot                  .put(tpath_lvl1,            ++x);
        paths_lvl1sAndRoot                  .put(tpath_lvl1_sibling,    ++x);
        paths_lvl1sAndRoot                  .put(tpath_root,            ++x);
        paths_lvl2s                         .put(tpath_lvl2,            ++x);
        paths_lvl2s                         .put(tpath_lvl2_sibling,    ++x);
        paths_lvl2sAndRoot                  .put(tpath_lvl2,            ++x);
        paths_lvl2sAndRoot                  .put(tpath_lvl2_sibling,    ++x);
        paths_lvl2sAndRoot                  .put(tpath_root,            ++x);
        paths_lvl2sAndLvl1_siblings         .put(tpath_lvl2,            ++x);
        paths_lvl2sAndLvl1_siblings         .put(tpath_lvl2_sibling,    ++x);
        paths_lvl2sAndLvl1_siblings         .put(tpath_lvl2_parent,     ++x);
        paths_lvl2sAndLvl1_childAndCousin   .put(tpath_lvl2,            ++x);
        paths_lvl2sAndLvl1_childAndCousin   .put(tpath_lvl2_cousin,     ++x);
        paths_lvl2sAndLvl1_childAndCousin   .put(tpath_lvl2_parent,     ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle .put(tpath_lvl2,            ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle .put(tpath_lvl2_cousin,     ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle .put(tpath_lvl2_otherUncle, ++x);
        paths_lvl1_same                     .put(tpath_lvl1,            x);
        paths_lvl1_same                     .put(tpath_lvl1_sibling,    x);
        paths_lvl1_samenull                 .put(tpath_lvl1,            null);
        paths_lvl1_samenull                 .put(tpath_lvl1_sibling,    null);
        paths_chain                         .put(tpath_root,            ++x);
        paths_chain                         .put(tpath_lvl2_parent,     ++x);
        paths_chain                         .put(tpath_lvl2,            ++x);
        paths_chain                         .put(tpath_lvl2_child,      ++x);
        paths_chain                         .put(tpath_lvl2_grandchild, ++x);
        paths_chain_same                    .put(tpath_root,            x);
        paths_chain_same                    .put(tpath_lvl2_parent,     x);
        paths_chain_same                    .put(tpath_lvl2,            x);
        paths_chain_same                    .put(tpath_lvl2_child,      x);
        paths_chain_same                    .put(tpath_lvl2_grandchild, x);
        paths_chain_samenull                .put(tpath_root,            null);
        paths_chain_samenull                .put(tpath_lvl2_parent,     null);
        paths_chain_samenull                .put(tpath_lvl2,            null);
        paths_chain_samenull                .put(tpath_lvl2_child,      null);
        paths_chain_samenull                .put(tpath_lvl2_grandchild, null);
        paths_chain_noroot                  .put(tpath_lvl2_parent,     ++x);
        paths_chain_noroot                  .put(tpath_lvl2,            ++x);
        paths_chain_noroot                  .put(tpath_lvl2_child,      ++x);
        paths_chain_noroot                  .put(tpath_lvl2_grandchild, ++x);
        paths_chain_broken                  .put(tpath_root,            ++x);
        paths_chain_broken                  .put(tpath_lvl2_parent,     ++x);
        paths_chain_broken                  .put(tpath_lvl2_child,      ++x);
        paths_chain_broken                  .put(tpath_lvl2_grandchild, ++x);

        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_empty, tpath_root, "new empty tree, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_empty, tpath_lvl1, "new empty tree, checking at a level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_root, tpath_root, "item at root, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_root, tpath_lvl1, "item at root, checking at a level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_root_null, tpath_root, "null at root, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_root_null, tpath_lvl1, "null at root, checking at a level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1, tpath_root, "item at level 1, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1, tpath_lvl1, "item at level 1, checking the same path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1, tpath_lvl1_child, "item at level 1, checking at a child path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_null, tpath_root, "null at level 1, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_null, tpath_lvl1, "null at level 1, checking the same path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_null, tpath_lvl1_child, "null at level 1, checking at a child path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1s, tpath_root, "items at multiple level 1 paths, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1s, tpath_lvl1, "items at multiple level 1 paths, checking at one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1s, tpath_lvl1_child, "items at multiple level 1 paths, checking at a child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1sAndRoot, tpath_root, "items at multiple level 1 paths and root, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1sAndRoot, tpath_lvl1, "items at multiple level 1 paths and root, checking at one of those path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1sAndRoot, tpath_lvl1_child, "items at multiple level 1 paths and root, checking at a child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2, tpath_root, "item at level 2, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2, tpath_lvl2_parent, "item at level 2, checking at a parent path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2, tpath_lvl2, "item at level 2, checking the same path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2, tpath_lvl2_child, "item at level 2, checking at a child path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2_null, tpath_root, "null at level 2, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2_null, tpath_lvl2_parent, "null at level 2, checking at a parent path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2_null, tpath_lvl2, "null at level 2, checking the same path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2_null, tpath_lvl2_child, "null at level 2, checking at a child path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_root, "items at multiple level 2 paths, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_lvl2_parent, "items at multiple level 2 paths, checking at their parent path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_lvl2, "items at multiple level 2 paths, checking at one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_lvl2_child, "items at multiple level 2 paths, checking at a child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_lvl2_uncle, "items at multiple level 2 paths, checking at a non-ancestral level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2s, tpath_lvl2_cousin, "items at multiple level 2 paths, checking at a child path of a non-ancestral level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_root, "items at multiple level 2 paths and their shared parent path, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_lvl2_parent, "items at multiple level 2 paths and their shared parent path, checking at that parent path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_lvl2, "items at multiple level 2 paths and their shared parent path, checking one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_lvl2_child, "items at multiple level 2 paths and their shared parent path, checking at the child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_lvl2_uncle, "items at multiple level 2 paths and their shared parent path, checking at a different non-ancestral path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_siblings, tpath_lvl2_cousin, "items at multiple level 2 paths and their shared parent path, checking at the child of a different non-ancestral path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_childAndCousin, tpath_root, "items at multiple level 2 paths and the parent path of one of them, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_childAndCousin, tpath_lvl2_parent, "items at multiple level 2 paths and the parent path of one of them, checking at the parent path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_childAndCousin, tpath_lvl2, "items at multiple level 2 paths and the parent path of one of them, checking at the path with the parent"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_childAndCousin, tpath_lvl2_child, "items at multiple level 2 paths and the parent path of one of them, checking at the child path of the path with the parent"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_cousinsDiffUncle, tpath_root, "items at multiple level 2 paths and level 1 path they're not descended from, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_cousinsDiffUncle, tpath_lvl2_parent, "items at multiple level 2 paths and level 1 path they're not descended from, checking at the parent of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_cousinsDiffUncle, tpath_lvl2, "items at multiple level 2 paths and level 1 path they're not descended from, checking at one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_cousinsDiffUncle, tpath_lvl2_child, "items at multiple level 2 paths and level 1 path they're not descended from, checking at the child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndLvl1_cousinsDiffUncle, tpath_lvl2_otherUncle, "items at multiple level 2 paths and level 1 path they're not descended from, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndRoot, tpath_root, "items at multiple level 2 paths and root, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndRoot, tpath_lvl2_parent, "items at multiple level 2 paths and root, checking at the parent of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndRoot, tpath_lvl2, "items at multiple level 2 paths and root, checking at one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl2sAndRoot, tpath_lvl2_child, "items at multiple level 2 paths and root, checking at the child of one of those paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_same, tpath_root, "the same item at different level 1 paths, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_same, tpath_lvl1, "the same item at different level 1 paths, checking at one of the paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_same, tpath_lvl1_child, "the same item at different level 1 paths, checking at a child of one of the paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_samenull, tpath_root, "null at different level 1 paths, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_samenull, tpath_lvl1, "null at different level 1 paths, checking at one of the paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_lvl1_samenull, tpath_lvl1_child, "null at different level 1 paths, checking at a child of one of the paths"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_root, "items at chained paths directly descended from one another, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2_parent, "items at chained paths directly descended from one another, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2, "items at chained paths directly descended from one another, checking at the level 2 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2_child, "items at chained paths directly descended from one another, checking at the level 3 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2_grandchild, "items at chained paths directly descended from one another, checking at the level 4 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2_uncle, "items at chained paths directly descended from one another, checking at a path not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain, tpath_lvl2_greatgrandchild, "items at chained paths directly descended from one another, checking at a path starting with, but not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_root, "the same item at chained paths directly descended from one another, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2_parent, "the same item at chained paths directly descended from one another, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2, "the same item at chained paths directly descended from one another, checking at the level 2 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2_child, "the same item at chained paths directly descended from one another, checking at the level 3 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2_grandchild, "the same item at chained paths directly descended from one another, checking at the level 4 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2_uncle, "the same item at chained paths directly descended from one another, checking at a path not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_same, tpath_lvl2_greatgrandchild, "the same item at chained paths directly descended from one another, checking at a path starting with, but not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_root, "null at chained paths directly descended from one another, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2_parent, "null at chained paths directly descended from one another, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2, "null at chained paths directly descended from one another, checking at the level 2 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2_child, "null at chained paths directly descended from one another, checking at the level 3 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2_grandchild, "null at chained paths directly descended from one another, checking at the level 4 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2_uncle, "null at chained paths directly descended from one another, checking at a path not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_samenull, tpath_lvl2_greatgrandchild, "null at chained paths directly descended from one another, checking at a path starting with, but not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_root, "items at paths directly descended from one another, starting after root, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2_parent, "items at paths directly descended from one another, starting after root, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2, "items at paths directly descended from one another, starting after root, checking at the level 2 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2_child, "items at paths directly descended from one another, starting after root, checking at the level 3 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2_grandchild, "items at paths directly descended from one another, starting after root, checking at the level 4 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2_uncle, "items at paths directly descended from one another, starting after root, checking at a path not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_noroot, tpath_lvl2_greatgrandchild, "items at paths directly descended from one another, starting after root, checking at a path starting with, but not along the chain"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_root, "items at paths directly descended from one another, missing one in the middle, checking at root"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2_parent, "items at paths directly descended from one another, missing one in the middle, checking at the level 1 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2, "items at paths directly descended from one another, missing one in the middle, checking at the level 2 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2_child, "items at paths directly descended from one another, missing one in the middle, checking at the level 3 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2_grandchild, "items at paths directly descended from one another, missing one in the middle, checking at the level 4 path"));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2_uncle, "items at paths directly descended from one another, missing one in the middle, checking at a path not along the chain."));
        treePresetsForMultiGettersAtPaths.add(new Triplet<>(paths_chain_broken, tpath_lvl2_greatgrandchild, "items at paths directly descended from one another, missing one in the middle, checking at a path starting with, but not along the chain."));
    }

    Map<Map<Tree.TreePath<String>, Integer>, String> treePresetsForMultiGetters = new LinkedHashMap<>();
    {
        Map<Tree.TreePath<String>, Integer>
                paths_empty                         = new HashMap<>(),
                paths_root                          = new HashMap<>(),
                paths_root_null                     = new HashMap<>(),
                paths_lvl1                          = new HashMap<>(),
                paths_lvl1_null                     = new HashMap<>(),
                paths_lvl1s                         = new HashMap<>(),
                paths_lvl1sAndRoot                  = new HashMap<>(),
                paths_lvl2                          = new HashMap<>(),
                paths_lvl2_null                     = new HashMap<>(),
                paths_lvl2s                         = new HashMap<>(),
                paths_lvl2sAndLvl1_siblings         = new HashMap<>(),
                paths_lvl2sAndLvl1_childAndCousin   = new HashMap<>(),
                paths_lvl2sAndLvl1_cousinsDiffUncle = new HashMap<>(),
                paths_lvl2sAndRoot                  = new HashMap<>(),
                paths_lvl1_same                     = new HashMap<>(),
                paths_lvl1_samenull                 = new HashMap<>(),
                paths_chain                         = new HashMap<>(),
                paths_chain_same                    = new HashMap<>(),
                paths_chain_samenull                = new HashMap<>(),
                paths_chain_noroot                  = new HashMap<>(),
                paths_chain_broken                  = new HashMap<>();

        int x = 5;

        paths_root.put(tpath_root, ++x);
        paths_root_null.put(tpath_root, null);
        paths_lvl1.put(tpath_lvl1, ++x);
        paths_lvl1_null.put(tpath_lvl1, null);
        paths_lvl2.put(tpath_lvl2, ++x);
        paths_lvl2_null.put(tpath_lvl2, null);
        paths_lvl1s.put(tpath_lvl1, ++x);
        paths_lvl1s.put(tpath_lvl1_sibling, ++x);
        paths_lvl1sAndRoot.put(tpath_lvl1, ++x);
        paths_lvl1sAndRoot.put(tpath_lvl1_sibling, ++x);
        paths_lvl1sAndRoot.put(tpath_root, ++x);
        paths_lvl2s.put(tpath_lvl2, ++x);
        paths_lvl2s.put(tpath_lvl2_sibling, ++x);
        paths_lvl2sAndRoot.put(tpath_lvl2, ++x);
        paths_lvl2sAndRoot.put(tpath_lvl2_sibling, ++x);
        paths_lvl2sAndRoot.put(tpath_root, ++x);
        paths_lvl2sAndLvl1_siblings.put(tpath_lvl2, ++x);
        paths_lvl2sAndLvl1_siblings.put(tpath_lvl2_sibling, ++x);
        paths_lvl2sAndLvl1_siblings.put(tpath_lvl2_parent, ++x);
        paths_lvl2sAndLvl1_childAndCousin.put(tpath_lvl2, ++x);
        paths_lvl2sAndLvl1_childAndCousin.put(tpath_lvl2_cousin, ++x);
        paths_lvl2sAndLvl1_childAndCousin.put(tpath_lvl2_parent, ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle.put(tpath_lvl2, ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle.put(tpath_lvl2_cousin, ++x);
        paths_lvl2sAndLvl1_cousinsDiffUncle.put(tpath_lvl2_otherUncle, ++x);
        paths_lvl1_same.put(tpath_lvl1, x);
        paths_lvl1_same.put(tpath_lvl1_sibling, x);
        paths_lvl1_samenull.put(tpath_lvl1, null);
        paths_lvl1_samenull.put(tpath_lvl1_sibling, null);
        paths_chain.put(tpath_root, ++x);
        paths_chain.put(tpath_lvl2_parent, ++x);
        paths_chain.put(tpath_lvl2, ++x);
        paths_chain.put(tpath_lvl2_child, ++x);
        paths_chain.put(tpath_lvl2_grandchild, ++x);
        paths_chain_same.put(tpath_root, x);
        paths_chain_same.put(tpath_lvl2_parent, x);
        paths_chain_same.put(tpath_lvl2, x);
        paths_chain_same.put(tpath_lvl2_child, x);
        paths_chain_same.put(tpath_lvl2_grandchild, x);
        paths_chain_samenull.put(tpath_root, null);
        paths_chain_samenull.put(tpath_lvl2_parent, null);
        paths_chain_samenull.put(tpath_lvl2, null);
        paths_chain_samenull.put(tpath_lvl2_child, null);
        paths_chain_samenull.put(tpath_lvl2_grandchild, null);
        paths_chain_noroot.put(tpath_lvl2_parent, ++x);
        paths_chain_noroot.put(tpath_lvl2, ++x);
        paths_chain_noroot.put(tpath_lvl2_child, ++x);
        paths_chain_noroot.put(tpath_lvl2_grandchild, ++x);
        paths_chain_broken.put(tpath_root, ++x);
        paths_chain_broken.put(tpath_lvl2_parent, ++x);
        paths_chain_broken.put(tpath_lvl2_child, ++x);
        paths_chain_broken.put(tpath_lvl2_grandchild, ++x);

        treePresetsForMultiGetters.put(paths_empty, "a new empty tree");
        treePresetsForMultiGetters.put(paths_root, "an item at root");
        treePresetsForMultiGetters.put(paths_root_null, "null at root");
        treePresetsForMultiGetters.put(paths_lvl1, "an item at a level 1 path");
        treePresetsForMultiGetters.put(paths_lvl1_null, "null at a level 1 path");
        treePresetsForMultiGetters.put(paths_lvl1s, "items at multiple level 1 paths");
        treePresetsForMultiGetters.put(paths_lvl1sAndRoot, "items at multiple level 1 paths and root");
        treePresetsForMultiGetters.put(paths_lvl2, "an item at a level 2 path");
        treePresetsForMultiGetters.put(paths_lvl2_null, "null at a level 2 path");
        treePresetsForMultiGetters.put(paths_lvl2s, "items at multiple level 2 paths");
        treePresetsForMultiGetters.put(paths_lvl2sAndRoot, "items at multiple level 2 paths and root");
        treePresetsForMultiGetters.put(paths_lvl2sAndLvl1_siblings, "items at multiple level 2 paths and a shared parent path");
        treePresetsForMultiGetters.put(paths_lvl2sAndLvl1_childAndCousin, "items at multiple level 2 paths and the parent path of one of them");
        treePresetsForMultiGetters.put(paths_lvl2sAndLvl1_cousinsDiffUncle, "items at multiple level 2 paths and a level 1 path neither of them descend from");
        treePresetsForMultiGetters.put(paths_lvl1_same, "the same item at different level 1 paths");
        treePresetsForMultiGetters.put(paths_lvl1_samenull, "null at different level 1 paths");
        treePresetsForMultiGetters.put(paths_chain, "items at chained paths directly descended from one another");
        treePresetsForMultiGetters.put(paths_chain_same, "the same item at multiple chained paths directly descended from one another");
        treePresetsForMultiGetters.put(paths_chain_samenull, "null at multiple chained paths directly descended from one another");
        treePresetsForMultiGetters.put(paths_chain_noroot, "items at chained paths directly descended from one another, not including root");
        treePresetsForMultiGetters.put(paths_chain_broken, "items at chained paths directly descended from one another, missing one in the middle.");
    }

    private void multipleItemGettersTestAtPath(
            boolean isForOrdered,
            BiFunction<T, Tree.TreePath<String>, Collection<Integer>> getter,
            BiFunction<Map<Tree.TreePath<String>, Integer>, Tree.TreePath<String>, Collection<Integer>> properRGetter)
    {
        for(Triplet<Map<Tree.TreePath<String>, Integer>, Tree.TreePath<String>, String> test : treePresetsForMultiGettersAtPaths)
        {
            Map<Tree.TreePath<String>, Integer> itemsInTreeAsMap = test.first;
            Tree.TreePath<String> pathCheckingAt = test.second;

            T tree = getNewEmptyTree();

            for(Map.Entry<Tree.TreePath<String>, Integer> e : itemsInTreeAsMap.entrySet())
                tree.setAt(e.getValue(), e.getKey().getNodes().toArray(new String[0]));


            // Bughunting code

//            System.out.println("\n____________________________________");
//            System.out.println("Populated paths:");
//
//            for(Map.Entry<Tree.TreePath<String>, Integer> e : itemsInTreeAsMap.entrySet())
//                System.out.println("  " + e.getKey().toString());
//
//            System.out.println("\nChecking at: " + pathCheckingAt.toString());
//            System.out.println("Expected: " + properRGetter .apply(itemsInTreeAsMap, pathCheckingAt).stream().map(x -> x == null ? "(null)" : x.toString()).collect(Collectors.joining(", ")));
//            System.out.println("Actual:   " + getter        .apply(tree,             pathCheckingAt).stream().map(x -> x == null ? "(null)" : x.toString()).collect(Collectors.joining(", ")));


            Collection<Integer> actual = getter.apply(tree, pathCheckingAt);
            Collection<Integer> expected = properRGetter.apply(itemsInTreeAsMap, pathCheckingAt);

            String failMessage
                    = "With " + test.third + " returned a wrong collection."
                      + "\n\nProvided items:\n" + itemsInTreeAsMap
                                                          .entrySet()
                                                          .stream()
                                                          .map(x -> "  - " + x.getKey() + " = " + x.getValue())
                                                          .collect(Collectors.joining("\n"))
                      + "\n\nChecking at path: " + test.second
                      + "\n\nExpected result: " + expected
                      + "\nActual result:   " + actual;

            if(isForOrdered)
            {
                assertThat(getter.apply(tree, pathCheckingAt))
                        .withFailMessage(failMessage)
                        .containsExactlyElementsOf(properRGetter.apply(itemsInTreeAsMap, pathCheckingAt));
            }
            else
            {
                assertThat(getter.apply(tree, pathCheckingAt))
                        .withFailMessage(failMessage)
                        .hasSameElementsAs(properRGetter.apply(itemsInTreeAsMap, pathCheckingAt));
            }
        }
    }

    private void multipleItemGettersTest(
            boolean isForOrdered,
            Function<T, Collection<Integer>> getter,
            Function<Map<Tree.TreePath<String>, Integer>, Collection<Integer>> properResultGetter)
    {
        for(Map.Entry<Map<Tree.TreePath<String>, Integer>, String> test : treePresetsForMultiGetters.entrySet())
        {
            Map<Tree.TreePath<String>, Integer> itemsInTreeAsMap = test.getKey();
            T tree = getNewEmptyTree();

            for(Map.Entry<Tree.TreePath<String>, Integer> entry : itemsInTreeAsMap.entrySet())
                tree.setAt(entry.getValue(), entry.getKey().getNodes().toArray(new String[0]));

            Collection<Integer> actual = getter.apply(tree);
            Collection<Integer> expected = properResultGetter.apply(itemsInTreeAsMap);

            String failMessage
                    = "With " + test.getValue() + ", returned a wrong collection."
                    + "\n\nProvided items:\n" + itemsInTreeAsMap
                                                        .entrySet()
                                                        .stream()
                                                        .map(x -> "  - " + x.getKey() + " = " + x.getValue())
                                                        .collect(Collectors.joining("\n"))
                    + "\n\nExpected result: " + expected
                    + "\nActual result:   " + actual;

            if(isForOrdered)
            {
                assertThat(actual)
                        .withFailMessage(failMessage)
                        .containsExactlyElementsOf(expected);
            }
            else
            {
                assertThat(getter.apply(tree))
                        .withFailMessage(failMessage)
                        .hasSameElementsAs(properResultGetter.apply(itemsInTreeAsMap));
            }
        }
    }

    private void multipleEntryGettersTestAtPath(
            boolean isForOrdered,
            BiFunction<T, Tree.TreePath<String>, Collection<Tree.Entry<String, Integer>>> getter,
            BiFunction<Map<Tree.TreePath<String>, Integer>, Tree.TreePath<String>, Collection<Tree.Entry<String, Integer>>> properResultGetter)
    {
        //for(Map.Entry<Map<Tree.TreePath<String>, Integer>, String> test : treePresetsForMultiGetters.entrySet())
        for(Triplet<Map<Tree.TreePath<String>, Integer>, Tree.TreePath<String>, String> test : treePresetsForMultiGettersAtPaths)
        {
            Map<Tree.TreePath<String>, Integer> itemsInTreeAsMap = test.first;
            Tree.TreePath<String> path = test.second;
            T tree = getNewEmptyTree();

            for(Map.Entry<Tree.TreePath<String>, Integer> entry : itemsInTreeAsMap.entrySet())
                tree.setAt(entry.getValue(), entry.getKey().getNodes().toArray(new String[0]));

            Collection<Tree.Entry<String, Integer>> actual = getter.apply(tree, path);
            Collection<Tree.Entry<String, Integer>> expected = properResultGetter.apply(itemsInTreeAsMap, path);

            String failMessage
                    = "With " + test.third + ", returned a wrong collection."
                      + "\n\nProvided items:\n" + itemsInTreeAsMap
                                                          .entrySet()
                                                          .stream()
                                                          .map(x -> "  - " + x.getKey() + " = " + x.getValue())
                                                          .collect(Collectors.joining("\n"))
                      + "\n\nExpected result:\n" + expected.stream()
                                                          .map(x -> "  - [" + x.getPath() + " = " + x.getItem() + "]")
                                                          .collect(Collectors.joining("\n"))
                      + "\n\nActual result:\n" + actual.stream()
                                                      .map(x -> "  - [" + x.getPath() + " = " + x.getItem() + "]")
                                                      .collect(Collectors.joining("\n"));

            if(isForOrdered)
            {
                assertThat(actual)
                        .withFailMessage(failMessage)
                        .usingElementComparator((a, b) -> (a.getPath().equals(b.getPath())
                                                           && (Objects.equals(a.getValue(), b.getValue()))) ? 0 : -1)
                        .containsExactlyElementsOf(expected);
            }
            else
            {
                assertThat(actual)
                        .withFailMessage(failMessage)
                        .usingElementComparator((a, b) -> (a.getPath().equals(b.getPath())
                                                           && (Objects.equals(a.getValue(), b.getValue()))) ? 0 : -1)
                        .containsExactlyInAnyOrderElementsOf(expected);
            }
        }
    }

    private void multipleEntryGettersTest(
            boolean isForOrdered,
            Function<T, Collection<Tree.Entry<String, Integer>>> getter,
            Function<Map<Tree.TreePath<String>, Integer>, Collection<Tree.Entry<String, Integer>>> properResultGetter)
    {
        for(Map.Entry<Map<Tree.TreePath<String>, Integer>, String> test : treePresetsForMultiGetters.entrySet())
        {
            Map<Tree.TreePath<String>, Integer> itemsInTreeAsMap = test.getKey();
            T tree = getNewEmptyTree();

            for(Map.Entry<Tree.TreePath<String>, Integer> entry : itemsInTreeAsMap.entrySet())
                tree.setAt(entry.getValue(), entry.getKey().getNodes().toArray(new String[0]));

            Collection<Tree.Entry<String, Integer>> actual = getter.apply(tree);
            Collection<Tree.Entry<String, Integer>> expected = properResultGetter.apply(itemsInTreeAsMap);

            String failMessage
                    = "With " + test.getValue() + ", returned a wrong collection."
                     + "\n\nProvided items:\n" + itemsInTreeAsMap
                                                         .entrySet()
                                                         .stream()
                                                         .map(x -> "  - " + x.getKey() + " = " + x.getValue())
                                                         .collect(Collectors.joining("\n"))
                     + "\n\nExpected result:\n" + expected.stream()
                                                         .map(x -> "  - [" + x.getPath() + " = " + x.getItem() + "]")
                                                         .collect(Collectors.joining("\n"))
                     + "\n\nActual result:\n" + actual.stream()
                                                     .map(x -> "  - [" + x.getPath() + " = " + x.getItem() + "]")
                                                     .collect(Collectors.joining("\n"));

            if(isForOrdered)
            {
                assertThat(actual)
                        .withFailMessage(failMessage)
                        .usingElementComparator((a, b) -> (a.getPath().equals(b.getPath())
                                                           && (Objects.equals(a.getValue(), b.getValue()))) ? 0 : -1)
                        .containsExactlyElementsOf(expected);
            }
            else
            {
                assertThat(actual)
                        .withFailMessage(failMessage)
                        .usingElementComparator((a, b) -> (a.getPath().equals(b.getPath())
                                                           && (Objects.equals(a.getValue(), b.getValue()))) ? 0 : -1)
                        .containsExactlyInAnyOrderElementsOf(expected);
            }
        }
    }

    @Test
    void getItems()
    { multipleItemGettersTest(false, t -> t.getItems(), itemsInTreeAsMap -> itemsInTreeAsMap.values()); }

    @Test
    void getItemsInOrder()
    {
        multipleItemGettersTest(true,
                                t -> t.getItemsInOrder(Comparator.naturalOrder()),
                                treeAsMap -> treeAsMap.entrySet()
                                                      .stream()
                                                      .sorted(hierarchicalOrderComparator)
                                                      .map(x -> x.getValue())
                                                      .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndUnder()
    {
        multipleItemGettersTestAtPath(
                false,
                (t, path) -> t.getItemsAtAndUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isEqualToOrChildOf(path))
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndUnderInOrder()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) -> t.getItemsAtAndUnderInOrder(Comparator.naturalOrder(),
                                                         path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isEqualToOrChildOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getNonRootItems()
    {
        multipleItemGettersTest(
                false,
                (t) -> t.getNonRootItems(),
                (itemsInTreeAsMap) -> itemsInTreeAsMap
                                              .entrySet()
                                              .stream()
                                              .filter(x -> !x.getKey().isRoot())
                                              .map(x -> x.getValue())
                                              .collect(Collectors.toList()));
    }

    @Test
    void getNonRootItemsInOrder()
    {
        multipleItemGettersTest(
                true,
                (t) -> t.getNonRootItemsInOrder(Comparator.naturalOrder()),
                (itemsInTreeAsMap) ->
                {
                    return itemsInTreeAsMap
                                   .entrySet()
                                   .stream()
                                   .filter(x -> !x.getKey().isRoot())
                                   .sorted(hierarchicalOrderComparator)
                                   .map(x -> x.getValue())
                                   .collect(Collectors.toList());
                });
    }

    @Test
    void getItemsUnder()
    {
        multipleItemGettersTestAtPath(
                false,
                (t, path) -> t.getItemsUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isChildOf(path))
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getItemsUnderInOrder()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) -> t.getItemsUnderInOrder(Comparator.naturalOrder(), path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isChildOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getItemsAlong()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) ->  t.getItemsAlong(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isEqualToOrParentOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getNonRootItemsAlong()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) ->  t.getNonRootItemsAlong(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (!x.getKey().isRoot()) && x.getKey().isEqualToOrParentOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getImmediateItems()
    {
        multipleItemGettersTest(
                false,
                (t) -> t.getImmediateItems(),
                (itemsInTreeAsPath) -> itemsInTreeAsPath
                                               .entrySet()
                                               .stream()
                                               .filter(x -> x.getKey().getLength() == 1)
                                               .map(x -> x.getValue())
                                               .collect(Collectors.toList()));
    }

    @Test
    void getImmediateItemsInOrder()
    {
        multipleItemGettersTest(
                true,
                (t) -> t.getImmediateItemsInOrder(Comparator.naturalOrder()),
                (itemsInTreeAsPath) -> itemsInTreeAsPath
                                               .entrySet()
                                               .stream()
                                               .filter(x -> x.getKey().getLength() == 1)
                                               .sorted(hierarchicalOrderComparator)
                                               .map(x -> x.getValue())
                                               .collect(Collectors.toList()));
    }

    @Test
    void getItemsImmediatelyUnder()
    {
        multipleItemGettersTestAtPath(
                false,
                (t, path) -> t.getItemsImmediatelyUnderInOrder(Comparator.naturalOrder(), path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() == (path.getLength() + 1))
                                                                 && (x.getKey().isChildOf(path)))
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getItemsImmediatelyUnderInOrder()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) -> t.getItemsImmediatelyUnderInOrder(Comparator.naturalOrder(), path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() == (path.getLength() + 1))
                                                                 && (x.getKey().isChildOf(path)))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateItems()
    {
        multipleItemGettersTest(
                false,
                t -> t.getRootAndImmediateItems(),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() <= 1)
                                            .map(x -> x.getValue())
                                            .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateItemsInOrder()
    {
        multipleItemGettersTest(
                true,
                t -> t.getRootAndImmediateItemsInOrder(Comparator.naturalOrder()),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() <= 1)
                                            .sorted(hierarchicalOrderComparator)
                                            .map(x -> x.getValue())
                                            .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndImmediatelyUnder()
    {
        multipleItemGettersTestAtPath(
                false,
                (t, path) -> t.getItemsAtAndImmediatelyUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() <= path.getLength() + 1)
                                                                 && (x.getKey().isEqualToOrChildOf(path)))
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getItemsAtAndImmediatelyUnderInOrder()
    {
        multipleItemGettersTestAtPath(
                true,
                (t, path) -> t.getItemsAtAndImmediatelyUnderInOrder(Comparator.naturalOrder(),
                                                                    path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() <= path.getLength() + 1)
                                                                 && (x.getKey().isEqualToOrChildOf(path)))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> x.getValue())
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getEntries()
    {
        multipleEntryGettersTest(
                false,
                t -> t.getEntries(),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesInOrder()
    {
        multipleEntryGettersTest(
                true,
                t -> t.getEntriesInOrder(Comparator.naturalOrder()),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .sorted(hierarchicalOrderComparator)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndUnder()
    {
        multipleEntryGettersTestAtPath(
                false,
                (t, path) -> t.getEntriesAtAndUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().isEqualToOrChildOf(path))
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndUnderInOrder()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getEntriesAtAndUnderInOrder(Comparator.naturalOrder(),
                                                           path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isEqualToOrChildOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getNonRootEntries()
    {
        multipleEntryGettersTest(
                false,
                t -> t.getNonRootEntries(),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> !x.getKey().isRoot())
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getNonRootEntriesInOrder()
    {
        multipleEntryGettersTest(
                true,
                t -> t.getNonRootEntriesInOrder(Comparator.naturalOrder()),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> !x.getKey().isRoot())
                                            .sorted(hierarchicalOrderComparator)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnder()
    {
        multipleEntryGettersTestAtPath(
                false,
                (t, path) -> t.getEntriesUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isChildOf(path))
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getEntriesUnderInOrder()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getEntriesUnderInOrder(Comparator.naturalOrder(),
                                                      path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isChildOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAlong()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getEntriesAlong(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> x.getKey().isEqualToOrParentOf(path))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getNonRootEntriesAlong()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getNonRootEntriesAlong(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (!x.getKey().isRoot())
                                                                 && (x.getKey().isEqualToOrParentOf(path)))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getImmediateEntries()
    {
        multipleEntryGettersTest(
                false,
                t -> t.getImmediateEntries(),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() == 1)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getImmediateEntriesInOrder()
    {
        multipleEntryGettersTest(
                true,
                t -> t.getImmediateEntriesInOrder(Comparator.naturalOrder()),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() == 1)
                                            .sorted(hierarchicalOrderComparator)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesImmediatelyUnder()
    {
        multipleEntryGettersTestAtPath(
                false,
                (t, path) -> t.getEntriesImmediatelyUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() == path.getLength() + 1)
                                                                 && (x.getKey().isChildOf(path)))
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getEntriesImmediatelyUnderInOrder()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getEntriesImmediatelyUnderInOrder(Comparator.naturalOrder(),
                                                                 path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (x.getKey().getLength() == path.getLength() + 1)
                                                                 && (x.getKey().isChildOf(path)))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateEntries()
    {
        multipleEntryGettersTest(
                false,
                t -> t.getRootAndImmediateEntries(),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() <= 1)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getRootAndImmediateEntriesInOrder()
    {
        multipleEntryGettersTest(
                true,
                t -> t.getRootAndImmediateEntriesInOrder(Comparator.naturalOrder()),
                itemsInTreeAsMap -> itemsInTreeAsMap
                                            .entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().getLength() <= 1)
                                            .sorted(hierarchicalOrderComparator)
                                            .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                            .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndImmediatelyUnder()
    {
        multipleEntryGettersTestAtPath(
                false,
                (t, path) -> t.getEntriesAtAndImmediatelyUnder(path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (    (x.getKey().getLength() == path.getLength())
                                                                   || (x.getKey().getLength() == path.getLength() + 1))
                                                                 && (x.getKey().isEqualToOrChildOf(path)))
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    @Test
    void getEntriesAtAndImmediatelyUnderInOrder()
    {
        multipleEntryGettersTestAtPath(
                true,
                (t, path) -> t.getEntriesAtAndImmediatelyUnderInOrder(Comparator.naturalOrder(),
                                                                      path.getNodes().toArray(new String[0])),
                (itemsInTreeAsMap, path) -> itemsInTreeAsMap
                                                    .entrySet()
                                                    .stream()
                                                    .filter(x -> (    (x.getKey().getLength() == path.getLength())
                                                                   || (x.getKey().getLength() == path.getLength() + 1))
                                                                 && (x.getKey().isEqualToOrChildOf(path)))
                                                    .sorted(hierarchicalOrderComparator)
                                                    .map(x -> new Tree.Entry<>(null, x.getKey(), x.getValue()))
                                                    .collect(Collectors.toList()));
    }

    void getBranch_singleTest(String[] branchPathToGet, Collection<Tree.Entry<String, Integer>> itemsToPutInTree, String failMsg)
    {
        final Collection<Tree.Entry<String, Integer>> expectedItemsInTree = itemsToPutInTree
                          .stream()
                          .filter(x -> x.getPath().isEqualToOrChildOf(new Tree.TreePath<>(branchPathToGet)))
                          .map(x -> new Tree.Entry<>(null, x.getPath().withoutFirstNodes(branchPathToGet.length), x.getItem()))
                          .collect(Collectors.toList());

        T tree = getNewEmptyTree();

        for(Tree.Entry<String, Integer> i : itemsToPutInTree)
            tree.setAt(i.getItem(), i.getPath().getNodes().toArray(new String[0]));

        Tree<String, Integer> branchGotten = tree.getBranch(branchPathToGet);

        assertNotNull(branchGotten, "On" + failMsg + " returned null instead of a branch."
                                    + "\n\nGot branch at: " + String.join(",", branchPathToGet)
                                    + "\n\nProvided items: \n" + itemsToPutInTree.stream()
                                                                                 .map(x -> "  - "+ x.toString())
                                                                                 .collect(Collectors.joining("\n"))
                                    + "\n\nExpected: \n" + expectedItemsInTree.stream()
                                                                              .map(x -> "  - "+ x.toString())
                                                                              .collect(Collectors.joining("\n")));

        Collection<Tree.Entry<String, Integer>> entriesInTree = branchGotten.getEntries();

        assertThat(entriesInTree)
                .withFailMessage("On " + failMsg + " caused .getEntries() on the returned branch to return the wrong collection."
                                 + "\n\nGot branch at: " + String.join(",", branchPathToGet)
                                 + "\n\nProvided items: \n" + itemsToPutInTree.stream()
                                                                              .map(x -> "  - "+ x.toString())
                                                                              .collect(Collectors.joining("\n"))
                                 + "\n\nExpected: \n" + expectedItemsInTree.stream()
                                                                           .map(x -> "  - "+ x.toString())
                                                                           .collect(Collectors.joining("\n"))
                                 + "\n\nActual: \n" + entriesInTree.stream()
                                                                   .map(x -> "  - "+ x.toString())
                                                                   .collect(Collectors.joining("\n")))
                .usingElementComparator(treeEntryEquator)
                .containsExactlyInAnyOrderElementsOf(expectedItemsInTree);
    }

    @Test // Relies on getEntries returning correctly
    void getBranch()
    {
        int x = 5;

        ArrayList<Tree.Entry<String, Integer>>
                items_empty = new ArrayList<>(),
                items_itemAtRoot = new ArrayList<>(),
                items_itemsAtLvl1 = new ArrayList<>(),
                items_itemsAtLvl2 = new ArrayList<>(),
                items_nullAtRoot = new ArrayList<>(),
                items_nullsAtLvl1 = new ArrayList<>(),
                items_nullsAtLvl2 = new ArrayList<>(),
                items_alongChain = new ArrayList<>(),
                items_everywhere = new ArrayList<>();

        items_itemAtRoot.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_itemsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1, ++x));
        items_itemsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1_sibling, ++x));
        items_itemsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_itemsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2_sibling, ++x));
        items_nullAtRoot.add(new Tree.Entry<>(null, tpath_root, null));
        items_nullsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1, null));
        items_nullsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1_sibling, null));
        items_nullsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2, null));
        items_nullsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2_sibling, null));
        items_alongChain.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_parent, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_child, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_grandchild, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_parent, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_uncle, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_otherUncle, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_sibling, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_cousin, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_child, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_grandchild, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_greatgrandchild, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_child, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_sibling, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_otherSibling, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_otherSiblingChild, null));

        getBranch_singleTest(path_root,                   items_empty,       "new empty tree, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_empty,       "new empty tree, getting the branch at a level 1 path");
        getBranch_singleTest(path_lvl2,                   items_empty,       "new empty tree, getting the branch at a level 2 path");
        getBranch_singleTest(path_root,                   items_itemAtRoot,  "a tree with an item at root, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_itemAtRoot,  "a tree with an item at root, getting the branch at a child path");
        getBranch_singleTest(path_root,                   items_nullAtRoot,  "a tree with null at root, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_nullAtRoot,  "a tree with null at root, getting the branch at a child path");
        getBranch_singleTest(path_root,                   items_itemsAtLvl1, "a tree with items at level 1 paths, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_itemsAtLvl1, "a tree with items at level 1 paths, getting the branch at one of those level 1 paths");
        getBranch_singleTest(path_lvl1_otherSibling,      items_itemsAtLvl1, "a tree with items at level 1 paths, getting the branch at a different level 1 path");
        getBranch_singleTest(path_lvl1_child,             items_itemsAtLvl1, "a tree with items at level 1 paths, getting the branch at a child of one of those level 1 paths");
        getBranch_singleTest(path_lvl1_otherSiblingChild, items_itemsAtLvl1, "a tree with items at level 1 paths, getting the branch at an unrelated level 2 path");
        getBranch_singleTest(path_root,                   items_nullsAtLvl1, "a tree with null at level 1 paths, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_nullsAtLvl1, "a tree with null at level 1 paths, getting the branch at one of those level 1 paths");
        getBranch_singleTest(path_lvl1_otherSibling,      items_nullsAtLvl1, "a tree with null at level 1 paths, getting the branch at a different level 1 path");
        getBranch_singleTest(path_lvl1_child,             items_nullsAtLvl1, "a tree with null at level 1 paths, getting the branch at a child of one of those level 1 paths");
        getBranch_singleTest(path_lvl1_otherSiblingChild, items_nullsAtLvl1, "a tree with null at level 1 paths, getting the branch at an unrelated level 2 path");
        getBranch_singleTest(path_root,                   items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at root");
        getBranch_singleTest(path_lvl2_parent,            items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at the parent of one of those paths");
        getBranch_singleTest(path_lvl2_uncle,             items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at an unrelated level 1 path");
        getBranch_singleTest(path_lvl2,                   items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at one of those paths");
        getBranch_singleTest(path_lvl2_otherSibling,      items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at a different sibling path");
        getBranch_singleTest(path_lvl2_child,             items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at the child of one of those paths");
        getBranch_singleTest(path_lvl2_otherSiblingChild, items_itemsAtLvl2, "a tree with items at level 2 paths, getting the branch at the nephew of those paths");
        getBranch_singleTest(path_root,                   items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at root");
        getBranch_singleTest(path_lvl2_parent,            items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at the parent of one of those paths");
        getBranch_singleTest(path_lvl2_uncle,             items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at an unrelated level 1 path");
        getBranch_singleTest(path_lvl2,                   items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at one of those paths");
        getBranch_singleTest(path_lvl2_otherSibling,      items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at a different sibling path");
        getBranch_singleTest(path_lvl2_child,             items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at the child of one of those paths");
        getBranch_singleTest(path_lvl2_otherSiblingChild, items_nullsAtLvl2, "a tree with null at level 2 paths, getting the branch at the nephew of those paths");
        getBranch_singleTest(path_root,                   items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at root");
        getBranch_singleTest(path_lvl2_parent,            items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at the first non-root path in that chain");
        getBranch_singleTest(path_lvl2,                   items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at a path in the middle of that chain");
        getBranch_singleTest(path_lvl2_grandchild,        items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at the path at the end of that chain");
        getBranch_singleTest(path_lvl2_greatgrandchild,   items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at a path starting with that chain, but going past it.");
        getBranch_singleTest(path_lvl2_sibling,           items_alongChain,  "a tree with items in a hierarchical chain, getting the branch at a path not part of that chain");
        getBranch_singleTest(path_root,                   items_everywhere,  "a populated tree, getting the branch at root");
        getBranch_singleTest(path_lvl1,                   items_everywhere,  "a populated tree, getting a branch at a level 1 path");
        getBranch_singleTest(path_lvl2_parent,            items_everywhere,  "a populated tree, getting a branch at a level 1 path");
        getBranch_singleTest(path_lvl2,                   items_everywhere,  "a populated tree, getting a branch at a level 2 path");
        getBranch_singleTest(path_lvl1_child,             items_everywhere,  "a populated tree, getting a branch at a level 2 path");
        getBranch_singleTest(path_lvl2_child,             items_everywhere,  "a populated tree, getting a branch at a level 3 path");
    }

    void getBranches_root_singleTest(Collection<Tree.Entry<String, Integer>> itemsToPutInTree, String failMsg)
    {
        Map<String, Collection<Tree.Entry<String, Integer>>> expectedEntriesPerBranch = new HashMap<>();

        for(Tree.Entry<String, Integer> i : itemsToPutInTree)
        {
            if(i.getPath().isRoot())
                continue;

            Collection<Tree.Entry<String, Integer>> expectedEntries
                    = expectedEntriesPerBranch.getOrDefault(i.getPath().getFirst(), null);

            if(expectedEntries == null)
            {
                expectedEntries = new ArrayList<>();
                expectedEntriesPerBranch.put(i.getPath().getFirst(), expectedEntries);
            }

            expectedEntries.add(new Tree.Entry<>(null, i.getPath().withoutFirstNodes(1), i.getItem()));
        }

        T tree = getNewEmptyTree();

        for(Tree.Entry<String, Integer> entry : itemsToPutInTree)
            tree.setAt(entry.getItem(), entry.getPath().getNodes().toArray(new String[0]));

        Collection<Tree<String, Integer>> branchesGotten = tree.getBranches();

        assertNotNull(branchesGotten, () ->
        {
            return "On " + failMsg + ", calling getBranches() returned null."
                   + "\n\nEntries provided:\n" + itemsToPutInTree.stream()
                                                                 .map(x -> "  - " + x.toString())
                                                                 .collect(Collectors.joining("\n"))
                   + "\n\nExpected: \n" + expectedEntriesPerBranch.entrySet()
                                                                  .stream()
                                                                  .map(y -> "  - " + y.getKey() + ":\n" + y.getValue()
                                                                                                           .stream()
                                                                                                           .map(z -> "  -   - " + z.getPath() + ": " + z.getItem())
                                                                                                           .collect(Collectors.joining("\n")))
                                                                  .collect(Collectors.joining("\n"));
        });

        List<Collection<Tree.Entry<String, Integer>>> branchesAsEntries = branchesGotten.stream()
                                                                                        .map(x -> x.getEntries())
                                                                                        .collect(Collectors.toList());

        assertThat(branchesAsEntries)
                .usingElementComparator((a, b) ->
                {
                    List<Tree.TreePath<String>> aPaths = a.stream().map(x -> x.getPath()).collect(Collectors.toList());
                    List<Tree.TreePath<String>> bPaths = b.stream().map(x -> x.getPath()).collect(Collectors.toList());

                    if(!collectionsAreEqualIgnoringOrder(aPaths, bPaths))
                        return -1;

                    List<Integer> aItems = a.stream().map(x -> x.getItem()).collect(Collectors.toList());
                    List<Integer> bItems = b.stream().map(x -> x.getItem()).collect(Collectors.toList());

                    return collectionsAreEqualIgnoringOrder(aItems, bItems) ? 0 : -1;
                })
                .withFailMessage(() ->
                {
                    return "On " + failMsg + ", calling getBranches() returned an invalid result."
                           + "\n\nEntries provided:\n" + itemsToPutInTree.stream()
                                                                         .map(x -> "  - " + x.toString())
                                                                         .collect(Collectors.joining("\n"))
                           + "\n\nExpected:\n" + expectedEntriesPerBranch.entrySet()
                                                                         .stream()
                                                                         .map(y -> "  - Branch:\n" + y.getValue()
                                                                                                      .stream()
                                                                                                      .map(z -> "  -   - " + z.toString())
                                                                                                      .collect(Collectors.joining("\n")))
                                                                         .collect(Collectors.joining("\n"))
                           + "\n\nActual:\n" + branchesAsEntries.stream().map(x -> "  - Branch:\n" + x.stream()
                                                                                                      .map(y -> "  -   - " + y.toString())
                                                                                                      .collect(Collectors.joining("\n")));
                })
                .containsExactlyInAnyOrderElementsOf(expectedEntriesPerBranch.values());

    }

    @Test
    void getBranches()
    {
        ArrayList<Tree.Entry<String, Integer>>
                items_empty = new ArrayList<>(),
                items_itemAtRoot = new ArrayList<>(),
                items_nullAtRoot = new ArrayList<>(),
                items_itemsAtLvl1 = new ArrayList<>(),
                items_nullsAtLvl1 = new ArrayList<>(),
                items_itemsAtLvl2 = new ArrayList<>(),
                items_nullsAtLvl2 = new ArrayList<>(),
                items_alongChain = new ArrayList<>(),
                items_everywhere = new ArrayList<>();

        int x = 5;

        items_itemAtRoot.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_itemsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1, ++x));
        items_itemsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1_sibling, ++x));
        items_itemsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_itemsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2_sibling, ++x));
        items_nullAtRoot.add(new Tree.Entry<>(null, tpath_root, null));
        items_nullsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1, null));
        items_nullsAtLvl1.add(new Tree.Entry<>(null, tpath_lvl1_sibling, null));
        items_nullsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2, null));
        items_nullsAtLvl2.add(new Tree.Entry<>(null, tpath_lvl2_sibling, null));
        items_alongChain.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_parent, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_child, ++x));
        items_alongChain.add(new Tree.Entry<>(null, tpath_lvl2_grandchild, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_root, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_parent, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_uncle, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_otherUncle, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_sibling, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_cousin, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_child, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_grandchild, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl2_greatgrandchild, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_child, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_sibling, null));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_otherSibling, ++x));
        items_everywhere.add(new Tree.Entry<>(null, tpath_lvl1_otherSiblingChild, null));

        getBranches_root_singleTest(items_empty, "a new empty tree");
        getBranches_root_singleTest(items_itemAtRoot, "tree with an item at root");
        getBranches_root_singleTest(items_nullAtRoot, "tree with null at root");
        getBranches_root_singleTest(items_itemsAtLvl1, "tree with items at multiple level 1 paths");
        getBranches_root_singleTest(items_nullsAtLvl1, "tree with null at multiple level 1 paths");
        getBranches_root_singleTest(items_itemsAtLvl2, "tree with items at multiple level 2 paths");
        getBranches_root_singleTest(items_nullsAtLvl2, "tree with null at multiple level 2 paths");
        getBranches_root_singleTest(items_alongChain, "tree with items along a hierarchical chain");
        getBranches_root_singleTest(items_everywhere, "populated tree");

        // At this point, I'm assuming that if getBranch runs without problems and so does the above, then getBranches
        // should work for any path passed in.
    }

    @Test
    void getBranchesWithFirstPathFragments()
    {
        // Assuming that if getBranches passes its tests, this will as well.
    }

    @Test
    abstract void setRootItem();

    @Test
    void setRootItemIfAbsent()
    {
        // Assumes .getRootItem passed tests
        T tree = getNewEmptyTree();

        tree.setRootItemIfAbsent(5);
        assertEquals(5, tree.getRootItem(), "Didn't set the root item to the value provided on a new empty tree.");

        tree.setRootItemIfAbsent(6);
        assertEquals(5, tree.getRootItem(), () -> tree.getRootItem() == 6 ? "Set the root item when there was already an item at root." : "Modified the item at root when there was already an item at root.");

        tree.setRootItemIfAbsent(null);
        assertEquals(5, tree.getRootItem(), () -> tree.getRootItem() == null ? "Set the root item when there was already an item at root." : "Modified the item at root when there was already an item at root.");

        T tree2 = getNewEmptyTree();
        tree2.setRootItemIfAbsent(null);
        tree2.setRootItemIfAbsent(7);
        assertEquals(null, tree2.getRootItem(), () -> tree2.getRootItem() == 7 ? "Set the room item when there was already null at root." : "Modified the item at root when there was already null at root.");

        tree2.setRootItemIfAbsent(null);
        assertEquals(null, tree2.getRootItem(), "Modified the item at root when there was already null at root.");
    }

    @Test
    abstract void setAt();

    void setAtIfAbsent_singleTest(String[] path)
    {
        // Place item
        // Place null
        // Fail to overwrite item with item
        // Fail to overwrite item with null
        // Fail to overwrite null with item
        // Fail to overwrite null with null

        T tree = getNewEmptyTree();
        int x = 5;

        Integer expected1 = ++x;
        tree.setAtIfAbsent(expected1, path);
        Tree.ValueWithPresence<Integer> fromTree1 = tree.getAtSafely(path);

        assertTrue(fromTree1.valueWasPresent(), () -> "Didn't set an item at the path when there was no item.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path)
                                                      + "\nExpected: " + expected1);

        assertEquals(expected1, fromTree1.getValue(), () -> "Didn't set the correct item at the path when there was no item.\n"
                                                            + "\nPath: " + new Tree.TreePath<>(path)
                                                            + "\nExpected: " + expected1
                                                            + "\nActual: " + fromTree1.getValue());

        tree = getNewEmptyTree();
        tree.setAtIfAbsent(null, path);
        Tree.ValueWithPresence<Integer> fromTree2 = tree.getAtSafely(path);

        assertTrue(fromTree2.valueWasPresent(), () -> "Didn't set null at the path when there was no item.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path)
                                                      + "\nExpected: (null)");

        assertEquals(null, fromTree2.getValue(), () -> "Didn't set the correct item at the path when there was no item.\n"
                                                       + "\nPath: " + new Tree.TreePath<>(path)
                                                       + "\nExpected: (null)"
                                                       + "\nActual: " + fromTree2.getValue());

        Integer expected3 = ++x;
        Integer notExpected3 = ++x;
        tree = getNewEmptyTree();
        tree.setAtIfAbsent(expected3, path);
        tree.setAtIfAbsent(notExpected3, path);
        Tree.ValueWithPresence<Integer> fromTree3 = tree.getAtSafely(path);

        assertTrue(fromTree3.valueWasPresent(), () -> "Just cleared the path in the tree when there was already an item there, without setting it to the new item.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path)
                                                      + "\nExpected: " + expected3);

        assertEquals(expected3, fromTree3.getValue(), () -> (notExpected3.equals(fromTree3.getValue())
                                                                     ? "Overwrote the item at the path with the item provided despite an item already being present."
                                                                     : "Overwrote the item at the path with an item despite an item already being present.\n")
                                                            + "\nPath: " + new Tree.TreePath<>(path)
                                                            + "\nExpected: " + expected3
                                                            + "\nActual: " + fromTree3.getValue());

        Integer expected4 = ++x;
        tree = getNewEmptyTree();
        tree.setAtIfAbsent(expected4, path);
        tree.setAtIfAbsent(null, path);
        Tree.ValueWithPresence<Integer> fromTree4 = tree.getAtSafely(path);

        assertTrue(fromTree4.valueWasPresent(), () -> "Just cleared the path in the tree when there was already an item there, without setting it to the new item.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path)
                                                      + "\nExpected: " + expected4);

        assertEquals(expected4, fromTree4.getValue(), () -> (fromTree4.getValue() == null
                                                                     ? "Overwrote the item at the path with the item provided despite an item already being present."
                                                                     : "Overwrote the item at the path with an item despite an item already being present.\n")
                                                            + "\nPath: " + new Tree.TreePath<>(path)
                                                            + "\nExpected: " + expected4
                                                            + "\nActual: " + fromTree4.getValue());

        Integer notExpected5 = ++x;
        tree = getNewEmptyTree();
        tree.setAtIfAbsent(null, path);
        tree.setAtIfAbsent(notExpected5, path);
        Tree.ValueWithPresence<Integer> fromTree5 = tree.getAtSafely(path);

        assertTrue(fromTree5.valueWasPresent(), () -> "Just cleared the path in the tree when there was null there, without setting it to the new item.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path)
                                                      + "\nExpected: (null)");

        assertEquals(null, fromTree5.getValue(), () -> (notExpected5.equals(fromTree5.getValue())
                                                                     ? "Overwrote the item at the path with the item provided despite an item already being present."
                                                                     : "Overwrote the item at the path with an item despite an item already being present.\n")
                                                       + "\nPath: " + new Tree.TreePath<>(path)
                                                       + "\nExpected: (null)"
                                                       + "\nActual: " + fromTree5.getValue());

        tree = getNewEmptyTree();
        tree.setAtIfAbsent(null, path);
        tree.setAtIfAbsent(null, path);
        Tree.ValueWithPresence<Integer> fromTree6 = tree.getAtSafely(path);

        assertTrue(fromTree6.valueWasPresent(), () -> "Overwriting null at null cleared the item from the path.\n"
                                                      + "\nPath: " + new Tree.TreePath<>(path));

        assertNull(fromTree6.getValue(), () -> "Trying to overwrite null with another null at the path set it to something else.\n"
                                               + "\nPath: " + new Tree.TreePath<>(path)
                                               + "\nExpected: (null)"
                                               + "\nActual: " + fromTree6.getValue());
    }

    @Test
    void setAtIfAbsent()
    {
        // Assumes .getAtSafely passed tests
        setAtIfAbsent_singleTest(path_root);
        setAtIfAbsent_singleTest(path_lvl1);
        setAtIfAbsent_singleTest(path_lvl2);
    }

    void clearerTest(Consumer<T> clearFunc,
                     Function<List<Tree.Entry<String, Integer>>, List<Tree.Entry<String, Integer>>> expectedEntriesGetter)
    {
        List<Tree.Entry<String, Integer>> itemsToPopulateWith = new ArrayList<>();
        int x = 5;
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_root, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_sibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_child, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_nibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_otherSibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_otherSiblingChild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_sibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_cousin, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_uncle, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_child, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_parent, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_grandchild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherSibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherSiblingChild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherUncle, ++x));

        T tree = getNewEmptyTree();

        for(Tree.Entry<String, Integer> entryToAdd : itemsToPopulateWith)
            tree.setAt(entryToAdd.getItem(), entryToAdd.getPath().getNodes().toArray(new String[0]));

        List<Tree.Entry<String, Integer>> expectedItems = expectedEntriesGetter.apply(itemsToPopulateWith);
        clearFunc.accept(tree);
        Collection<Tree.Entry<String, Integer>> actualItems = tree.getEntries();

        assertThat(actualItems)
                .usingElementComparator(treeEntryEquator)
                .withFailMessage(() -> "On a populated tree, calling produced the wrong result:"
                                       + "\n\nProvided items:\n" + itemsToPopulateWith.stream()
                                                                                      .map(y -> "  - " + y.toString())
                                                                                      .collect(Collectors.joining("\n"))
                                       + "\n\nExpected entries in resulting tree:\n" + expectedItems.stream()
                                                                                                    .map(y -> "  - " + y.toString())
                                                                                                    .collect(Collectors.joining("\n"))
                                       + "\n\nActual entries in resulting tree:\n" + actualItems.stream()
                                                                                                .map(y -> "  - " + y.toString())
                                                                                                .collect(Collectors.joining("\n")))
                .containsExactlyInAnyOrderElementsOf(expectedItems);
    }

    void clearerTestAtPath(BiConsumer<T, Tree.TreePath<String>> clearFunc,
                           BiFunction<List<Tree.Entry<String, Integer>>, Tree.TreePath<String>, List<Tree.Entry<String, Integer>>> expectedEntriesGetter)
    {
        // assumed getEntries have passed their tests

        List<Tree.Entry<String, Integer>> itemsToPopulateWith = new ArrayList<>();
        int x = 5;
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_root, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_sibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_child, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_nibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_otherSibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl1_otherSiblingChild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_sibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_cousin, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_uncle, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_child, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_parent, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_grandchild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherSibling, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherSiblingChild, ++x));
        itemsToPopulateWith.add(new Tree.Entry<>(null, path_lvl2_otherUncle, ++x));

        List<Tree.TreePath<String>> pathsToTest = new ArrayList<>();
        pathsToTest.add(tpath_root);
        pathsToTest.add(tpath_lvl1);
        pathsToTest.add(tpath_lvl2);
        pathsToTest.add(tpath_lvl2_greatgrandchild);

        for(Tree.TreePath<String> path : pathsToTest)
        {
            T tree = getNewEmptyTree();

            for(Tree.Entry<String, Integer> entryToAdd : itemsToPopulateWith)
                tree.setAt(entryToAdd.getItem(), entryToAdd.getPath().getNodes().toArray(new String[0]));

            List<Tree.Entry<String, Integer>> expectedItems = expectedEntriesGetter.apply(itemsToPopulateWith, path);
            clearFunc.accept(tree, path);
            Collection<Tree.Entry<String, Integer>> actualItems = tree.getEntries();

            assertThat(actualItems)
                    .usingElementComparator(treeEntryEquator)
                    .withFailMessage(() -> "On a populated tree, calling on the given path produced the wrong result:"
                                           + "\n\nPath: " + path.toString()
                                           + "\n\nProvided items:\n" + itemsToPopulateWith.stream()
                                                                                          .map(y -> "  - " + y.toString())
                                                                                          .collect(Collectors.joining("\n"))
                                           + "\n\nExpected entries in resulting tree:\n" + expectedItems.stream()
                                                                                                        .map(y -> "  - " + y.toString())
                                                                                                        .collect(Collectors.joining("\n"))
                                           + "\n\nActual entries in resulting tree:\n" + actualItems.stream()
                                                                                                    .map(y -> "  - " + y.toString())
                                                                                                    .collect(Collectors.joining("\n")))
                    .containsExactlyInAnyOrderElementsOf(expectedItems);
        }
    }

    @Test
    void clear()
    {
        clearerTest(x -> x.clear(),
                    x -> new ArrayList<>());
    }

    @Test
    void clearNonRootItems()
    {
        clearerTest(x -> x.clearNonRootItems(),
                    x -> x.stream()
                          .filter(y -> y.getPath().isRoot())
                          .collect(Collectors.toList()));
    }

    @Test
    void clearRootItem()
    {
        clearerTest(x -> x.clearRootItem(),
                    x -> x.stream()
                          .filter(y -> !y.getPath().isRoot())
                          .collect(Collectors.toList()));
    }

    @Test
    void clearAt()
    {
        clearerTestAtPath((tree, path) -> tree.clearAt(path.getNodes().toArray(new String[0])),
                          (items, path) -> items.stream()
                                                .filter(x -> !x.getPath().equals(path))
                                                .collect(Collectors.toList()));
    }

    @Test
    void clearAtAndUnder()
    {
        clearerTestAtPath((tree, path) -> tree.clearAtAndUnder(path.getNodes().toArray(new String[0])),
                          (items, path) -> items.stream()
                                                .filter(x -> !x.getPath().isEqualToOrChildOf(path))
                                                .collect(Collectors.toList()));
    }

    @Test
    void clearUnder()
    {
        clearerTestAtPath((tree, path) -> tree.clearUnder(path.getNodes().toArray(new String[0])),
                          (items, path) -> items.stream()
                                                .filter(x -> !x.getPath().isChildOf(path))
                                                .collect(Collectors.toList()));
    }

    @Test
    void toCollection()
    { /* .toCollection is assumed to be tested if .getItems is tested */ }

    @Test
    void toList()
    { /* .toList is assumed to be tested if .getItems and .getItemsInOrder are tested */ }

    @Test
    void count()
    {
        int x = 5;

        T tree = getNewEmptyTree();
        assertEquals(0, tree.count());

        tree.setRootItem(5);
        assertEquals(1, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        assertEquals(1, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "soot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "soot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "doot");
        tree.setAt(++x, "soot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot", "noot");
        assertEquals(1, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot", "noot");
        tree.setAt(++x, "doot", "foot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "toot", "noot");
        tree.setAt(++x, "doot", "foot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "doot", "foot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "joot");
        tree.setAt(++x, "doot", "foot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "doot", "foot");
        assertEquals(2, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "doot", "foot");
        tree.setAt(++x, "doot", "soot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "doot", "foot");
        tree.setAt(++x, "joot", "soot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "toot", "foot");
        tree.setAt(++x, "joot", "soot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "toot", "foot");
        tree.setAt(++x, "toot", "soot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "doot", "foot");
        tree.setAt(++x, "doot", "toot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "doot", "foot");
        tree.setAt(++x, "joot", "toot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setAt(++x, "doot");
        tree.setAt(++x, "doot", "toot");
        assertEquals(3, tree.count());

        tree = getNewEmptyTree();
        tree.setRootItem(++x);
        tree.setRootItem(++x);
        assertEquals(1, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot");
        tree.setAt(++x, "doot");
        assertEquals(1, tree.count());

        tree = getNewEmptyTree();
        tree.setAt(++x, "doot", "toot");
        tree.setAt(++x, "doot", "toot");
        assertEquals(1, tree.count());
    }

    @Test
    void countDepth()
    {
        T tree = getNewEmptyTree();

        assertEquals(0, tree.countDepth(), "Counting depth on a new empty tree doesn't return 0.");

        tree.setRootItem(5);
        assertEquals(0, tree.countDepth(), "Counting depth on a tree with just a root item doesn't return 0.");

        tree = getNewEmptyTree();
        tree.setAt(5, path_lvl1);
        assertEquals(1, tree.countDepth(), "Counting depth on a tree with an item at a level 1 path doesn't return 1.");

        tree = getNewEmptyTree();
        tree.setAt(6, path_lvl2);
        assertEquals(2, tree.countDepth(), "Counting depth on a tree with an item at a level 2 path doesn't return 2.");

        tree.setAt(7, path_lvl1);
        assertEquals(2, tree.countDepth(), "Counting depth on a tree with items at level 1 and 2 paths doesn't return 2.");
    }
}