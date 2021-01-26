package scot.massie.lib.collections.tree;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TO DO: Reörder, put methods into specified regions.

public interface Tree<TNode, TLeaf>
{
    class NoItemAtPathException extends RuntimeException
    {
        // TO DO: write
    }

    class ValueWithPresence<T>
    {
        public ValueWithPresence()
        { this(false, null); }

        public ValueWithPresence(boolean wasPresent, T value)
        {
            this.valueWasPresent = wasPresent;
            this.value = wasPresent ? value : null;
        }

        protected final boolean valueWasPresent;
        protected final T value;

        public boolean valueWasPresent()
        { return valueWasPresent; }

        public T getValue()
        { return value; }

        @Override
        public int hashCode()
        {
            int hash = 67, x = 151;
            hash = hash * x + (valueWasPresent ? 1 : 0);
            hash = hash * x + (value == null ? 0 : value.hashCode());
            return hash;
        }

        @Override
        public boolean equals(Object o)
        {
            return (o instanceof  ValueWithPresence)
                && (((ValueWithPresence<?>) o).valueWasPresent == valueWasPresent)
                && (((ValueWithPresence<?>) o).value == value);
        }
    }

    class Entry<TNode, TLeaf>
    {
        public Entry(Tree<TNode, TLeaf> sourceTree, TNode[] path, TLeaf item)
        {
            this.sourceTree = sourceTree;
            this.path = new TreePath<>(path);
            this.item = item;
        }

        public Entry(Tree<TNode, TLeaf> sourceTree, List<TNode> path, TLeaf item)
        {
            this.sourceTree = sourceTree;
            this.path = new TreePath<>(path);
            this.item = item;
        }

        public Entry(Tree<TNode, TLeaf> sourceTree, TreePath<TNode> path, TLeaf item)
        {
            this.sourceTree = sourceTree;
            this.path = path;
            this.item = item;
        }

        protected final Tree<TNode, TLeaf> sourceTree;
        protected final TreePath<TNode> path;
        protected final TLeaf item;

        public final TreePath<TNode> getPath()
        { return path; }

        public final TLeaf getItem()
        { return item; }

        // getKey and getValue provided in imitation of Map.Entry<K, V>
        public final TreePath<TNode> getKey()
        { return path; }

        public final TLeaf getValue()
        { return item; }

        @Override
        public String toString()
        { return "[" + path.toString() + "] = " + (item != null ? item : "(null)"); }
    }

    final class TreePath<TNode>
    {
        public TreePath()
        { this.nodes = Collections.emptyList(); }

        public TreePath(TNode... nodes)
        { this.nodes = Collections.unmodifiableList(Arrays.asList(nodes)); }

        public TreePath(List<TNode> nodes)
        { this.nodes = Collections.unmodifiableList(nodes); }

        public static <TNode> TreePath<TNode> fromStream(Stream<TNode> s)
        { return new TreePath<>(s.collect(Collectors.toList())); }

        public static <TNode> TreePath<TNode> root()
        { return new TreePath<>(); }

        final List<TNode> nodes;

        public List<TNode> getNodes()
        { return nodes; }

        public int getLength()
        { return nodes.size(); }

        public TNode getFirst()
        { return nodes.get(0); }

        public boolean isParentOf(TreePath<TNode> other)
        {
            if(other.nodes.size() <= nodes.size())
                return false;

            return other.nodes.subList(0, nodes.size()).equals(nodes);
        }

        public boolean isEqualToOrParentOf(TreePath<TNode> other)
        {
            if(other.nodes.size() < nodes.size())
                return false;

            return other.nodes.subList(0, nodes.size()).equals(nodes);
        }

        public boolean isChildOf(TreePath<TNode> other)
        { return other.isParentOf(this); }

        public boolean isEqualToOrChildOf(TreePath<TNode> other)
        { return other.isEqualToOrParentOf(this); }

        public boolean isRoot()
        { return nodes.isEmpty(); }

        public TreePath<TNode> withoutFirstNodes(int numberOfElementsToDrop)
        {
            if(numberOfElementsToDrop <= 0)
                return this;

            return new TreePath<>(nodes.subList(numberOfElementsToDrop, nodes.size()));
        }

        @Override
        public int hashCode()
        { return nodes.hashCode(); }

        @Override
        public boolean equals(Object o)
        { return (o instanceof TreePath) && (nodes.equals(((TreePath<?>)o).nodes)); }

        @Override
        public String toString()
        {
            if(nodes.isEmpty())
                return "(root)";

            return nodes.stream().map(x -> x == null ? "(null)" : x.toString()).collect(Collectors.joining("."));
        }

        // Where TNode is not Comparable<TNode>, the behaviour of the returned comparator is undefined.
        public static <TNode extends Comparable<TNode>> Comparator<TreePath<TNode>> getComparator()
        {
            return new Comparator<TreePath<TNode>>()
            {
                @Override
                public int compare(TreePath<TNode> a, TreePath<TNode> b)
                {
                    if(a == null)
                        return b == null ? 0 : -1;

                    if(b == null)
                        return 1;

                    final int asize = a.getNodes().size();
                    final int bsize = b.getNodes().size();

                    if(asize == 0)
                        return bsize == 0 ? 0 : -1;

                    for(int i = 0; i < asize; i++)
                    {
                        TNode anode = a.getNodes().get(i);

                        if(i >= bsize)
                            return 1;

                        TNode bnode = b.getNodes().get(i);

                        if(anode == null && bnode == null)
                            continue;

                        if(anode == null)
                            return -1;

                        if(bnode == null)
                            return 1;

                        int comparison = anode.compareTo(bnode);

                        if(comparison != 0)
                            return comparison;
                    }

                    return bsize > asize ? -1 : 0;
                }
            };
        }
    }

    //region Check state

    /**
     * Checks whether or not this tree has any items.
     *
     * @return True if this tree contains any items, otherwise false.
     */
    default boolean hasItems()
    { return this.hasRootItem() || this.hasNonRootItems(); }

    /**
     * Checks whether or not this tree has any items other than the root item.
     * @return True if this tree contains any items other than the root item, otherwise false.
     */
    boolean hasNonRootItems();

    /**
     * Checks whether or not this tree has any items at or under the given path. That is, with paths starting with the
     * given path.
     *
     * @param path An ordered array of items used to traverse the tree.
     * @return True if this tree contains any items at or under the given path.
     */
    default boolean hasItemsAtOrUnder(TNode... path)
    { return this.hasItemsUnder(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree has any items at or under the given path. That is, with paths starting with the
     * given path.
     *
     * @param path A list of items used to traverse the tree.
     * @return True if this tree contains any items at or under the given path.
     */
    default boolean hasItemsAtOrUnder(List<TNode> path)
    { return this.hasItemAt(path) || this.hasItemsUnder(path); }

    /**
     * Checks whether or not this tree has any items at or under the given path. That is, with paths starting with the
     * given path.
     *
     * @param path A tree path for traversing the tree.
     * @return True if this tree contains any items at or under the given path.
     */
    default boolean hasItemsAtOrUnder(TreePath<TNode> path)
    { return this.hasItemsUnder(path.getNodes()); }

    /**
     * Checks whether or not this tree has any items under, but not necessarily at, the given path. That is, with paths starting
     * with the given path, but not equal to the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if this tree contains any items under, but not at, the given path. Otherwise, false.
     */
    default boolean hasItemsUnder(TNode... path)
    { return this.hasItemsUnder(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree has any items under, but not necessarily at, the given path. That is, with paths starting
     * with the given path, but not equal to the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if this tree contains any items under, but not at, the given path. Otherwise, false.
     */
    boolean hasItemsUnder(List<TNode> path);

    /**
     * Checks whether or not this tree has any items under, but not necessarily at, the given path. That is, with paths starting
     * with the given path, but not equal to the given path.
     * @param path A tree path for traversing the tree.
     * @return True if this tree contains any items under, but not at, the given path. Otherwise, false.
     */
    default boolean hasItemsUnder(TreePath<TNode> path)
    { return this.hasItemsUnder(path.getNodes()); }

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, including the root.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if this tree contains any items along the given path. Otherwise, false.
     */
    default boolean hasItemsAlong(TNode... path)
    { return this.hasItemsAlong(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, including the root.
     * @param path A list of items used to traverse the tree.
     * @return True if this tree contains any items along the given path. Otherwise, false.
     */
    boolean hasItemsAlong(List<TNode> path);

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, including the root.
     * @param path A tree path for traversing the tree.
     * @return True if this tree contains any items along the given path. Otherwise, false.
     */
    default boolean hasItemsAlong(TreePath<TNode> path)
    { return this.hasItemsAlong(path.getNodes()); }

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, not including the root.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if this tree contains any items along the given path, ignoring the root. Otherwise, false.
     */
    default boolean hasNonRootItemsAlong(TNode... path)
    { return this.hasNonRootItemsAlong(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, not including the root.
     * @param path A list of items used to traverse the tree.
     * @return True if this tree contains any items along the given path, ignoring the root. Otherwise, false.
     */
    boolean hasNonRootItemsAlong(List<TNode> path);

    /**
     * Checks whether or not this tree has any items at any nodes along the provided path, not including the root.
     * @param path A tree path for traversing the tree.
     * @return True if this tree contains any items along the given path, ignoring the root. Otherwise, false.
     */
    default boolean hasNonRootItemsAlong(TreePath<TNode> path)
    { return this.hasNonRootItemsAlong(path.getNodes()); }

    /**
     * Checks whether or not an item exists at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if an item exists at the given path, otherwise false.
     */
    default boolean hasItemAt(TNode... path)
    { return this.hasItemAt(Arrays.asList(path)); }

    /**
     * Checks whether or not an item exists at the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if an item exists at the given path, otherwise false.
     */
    boolean hasItemAt(List<TNode> path);

    /**
     * Checks whether or not an item exists at the given path.
     * @param path A tree path for traversing the tree.
     * @return True if an item exists at the given path, otherwise false.
     */
    default boolean hasItemAt(TreePath<TNode> path)
    { return this.hasItemAt(path.getNodes()); }

    /**
     * Checks whether or not this tree has an item at the base level.
     * @return True if a value exists at the base level, otherwise false.
     */
    default boolean hasRootItem()
    { return this.hasItemAt(); }

    /**
     * Checks whether or not this tree is empty.
     * @return True if there are no items in this tree at any paths. Otherwise, false.
     */
    default boolean isEmpty()
    { return !this.hasItems(); }

    /**
     * Checks whether or not this tree is empty, disregarding any possible root item.
     * @return True if there are no items in this tree at any paths other than root. Otherwise, false.
     */
    default boolean isEmptyUnderRoot()
    { return !this.hasNonRootItems(); }

    /**
     * Checks whether or not this tree is empty at and under the given path. That is, at paths matching and starting
     * with the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if there are no items in this tree at or under the given path. Otherwise, false.
     */
    default boolean isEmptyAtAndUnder(TNode... path)
    { return isEmptyAtAndUnder(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree is empty at and under the given path. That is, at paths matching and starting
     * with the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if there are no items in this tree at or under the given path. Otherwise, false.
     */
    default boolean isEmptyAtAndUnder(List<TNode> path)
    { return !this.hasItemsAtOrUnder(path); }

    /**
     * Checks whether or not this tree is empty at and under the given path. That is, at paths matching and starting
     * with the given path.
     * @param path A tree path for traversing the tree.
     * @return True if there are no items in this tree at or under the given path. Otherwise, false.
     */
    default boolean isEmptyAtAndUnder(TreePath<TNode> path)
    { return this.isEmptyAtAndUnder(path.getNodes()); }

    /**
     * Checks whether or not this tree is empty under the given path, ignoring the item if present at the given path.
     * That is, at paths starting with the given paths, but not matching the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if there are no items in this tree under the given path, ignoring any possible items at the given
     * path itself. Otherwise, false.
     */
    default boolean isEmptyUnder(TNode... path)
    { return this.isEmptyUnder(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree is empty under the given path, ignoring the item if present at the given path.
     * That is, at paths starting with the given paths, but not matching the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if there are no items in this tree under the given path, ignoring any possible items at the given
     * path itself. Otherwise, false.
     */
    default boolean isEmptyUnder(List<TNode> path)
    { return !this.hasItemsUnder(path); }

    /**
     * Checks whether or not this tree is empty under the given path, ignoring the item if present at the given path.
     * That is, at paths starting with the given paths, but not matching the given path.
     * @param path A tree path for traversing the tree.
     * @return True if there are no items in this tree under the given path, ignoring any possible items at the given
     * path itself. Otherwise, false.
     */
    default boolean isEmptyUnder(TreePath<TNode> path)
    { return this.isEmptyUnder(path.getNodes()); }

    /**
     * Checks whether or not this tree is empty at all points along the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if there are no items at any paths this path starts with. Otherwise, false.
     */
    default boolean isEmptyAlong(TNode... path)
    { return this.isEmptyAlong(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree is empty at all points along the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if there are no items at any paths this path starts with. Otherwise, false.
     */
    default boolean isEmptyAlong(List<TNode> path)
    { return !hasItemsAlong(path); }

    /**
     * Checks whether or not this tree is empty at all points along the given path.
     * @param path A tree path for traversing the tree.
     * @return True if there are no items at any paths this path starts with. Otherwise, false.
     */
    default boolean isEmptyAlong(TreePath<TNode> path)
    { return this.isEmptyAlong(path.getNodes()); }

    /**
     * Checks whether or not this tree is empty at all points along the given path, ignoring the item if present at
     * root.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if there are no items at any paths this path starts with, other than root. Otherwise, false.
     */
    default boolean isEmptyAlongAfterRoot(TNode... path)
    { return this.isEmptyAlongAfterRoot(Arrays.asList(path)); }

    /**
     * Checks whether or not this tree is empty at all points along the given path, ignoring the item if present at
     * root.
     * @param path A list of items used to traverse the tree.
     * @return True if there are no items at any paths this path starts with, other than root. Otherwise, false.
     */
    default boolean isEmptyAlongAfterRoot(List<TNode> path)
    { return !hasNonRootItemsAlong(path); }

    /**
     * Checks whether or not this tree is empty at all points along the given path, ignoring the item if present at
     * root.
     * @param path A tree path for traversing the tree.
     * @return True if there are no items at any paths this path starts with, other than root. Otherwise, false.
     */
    default boolean isEmptyAlongAfterRoot(TreePath<TNode> path)
    { return this.isEmptyAlongAfterRoot(path.getNodes()); }

    /**
     * Checks whether or not there is no item at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return True if there is no item at the given path. Otherwise, false.
     */
    default boolean isEmptyAt(TNode... path)
    { return this.isEmptyAt(Arrays.asList(path)); }

    /**
     * Checks whether or not there is no item at the given path.
     * @param path A list of items used to traverse the tree.
     * @return True if there is no item at the given path. Otherwise, false.
     */
    default boolean isEmptyAt(List<TNode> path)
    { return !hasItemAt(path); }

    /**
     * Checks whether or not there is no item at the given path.
     * @param path A tree path for traversing the tree.
     * @return True if there is no item at the given path. Otherwise, false.
     */
    default boolean isEmptyAt(TreePath<TNode> path)
    { return this.isEmptyAt(path.getNodes()); }

    /**
     * Checks whether or not there is no item at the base level.
     * @return True if there is no item at the base level. Otherwise, false.
     */
    default boolean isEmptyAtRoot()
    { return !hasRootItem(); }
    //endregion

    //region get stored items
    //region root level

    /**
     * Gets the item at the root level.
     *
     * @return The item at the root level of the tree.
     * @throws NoItemAtPathException if there is no item at the root level of this tree.
     */
    default TLeaf getRootItem() throws NoItemAtPathException
    {
        ValueWithPresence<TLeaf> getterResult = this.getRootItemSafely();

        if(!getterResult.valueWasPresent())
            throw new NoItemAtPathException();

        return getterResult.getValue();
    }

    /**
     * Gets the item at the root level.
     *
     * @return The item at the root level of the tree, paired with whether or not this tree had an item at the root
     * level.
     */
    default ValueWithPresence<TLeaf> getRootItemSafely()
    { return this.getAtSafely(); }

    /**
     * Gets the item at the root level, or the provided default item if there is no item at the root level.
     *
     * @param defaultItem The item to return if this tree has no root item.
     * @return This tree's root level item, or the provided default item if this tree has no root level item.
     */
    default TLeaf getRootItemOrDefault(TLeaf defaultItem)
    {
        ValueWithPresence<TLeaf> getterResult = this.getRootItemSafely();
        return getterResult.valueWasPresent() ? getterResult.getValue() : defaultItem;
    }

    /**
     * Gets the item at the root level, or the provided default item if there is no item at the root level.
     *
     * @param defaultItem The item to return if this tree has no root item.
     * @return This tree's root level item, or the provided default item if this tree has no root level item.
     */
    default Object getRootItemOrDefaultAnyType(Object defaultItem)
    {
        ValueWithPresence<TLeaf> getterResult = this.getRootItemSafely();
        return getterResult.valueWasPresent() ? getterResult.getValue() : defaultItem;
    }

    /**
     * Gets the item at the root level, or null if there is no item at the root level.
     *
     * @return The tree's root level item, or null if this tree has no root level item.
     */
    default TLeaf getRootItemOrNull()
    {
        ValueWithPresence<TLeaf> getterResult = this.getRootItemSafely();
        return getterResult.valueWasPresent() ? getterResult.getValue() : null;
    }
    //endregion

    //region leaves
    /**
     * Gets the item at the given path.
     *
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path.
     * @throws NoItemAtPathException If no item exists at the given path.
     */
    default TLeaf getAt(TNode... path) throws NoItemAtPathException
    { return this.getAt(Arrays.asList(path)); }

    /**
     * Gets the item at the given path.
     *
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path.
     * @throws NoItemAtPathException If no item exists at the given path.
     */
    default TLeaf getAt(List<TNode> path) throws NoItemAtPathException
    {
        ValueWithPresence<TLeaf> getterResult = this.getAtSafely(path);

        if(!getterResult.valueWasPresent())
            throw new NoItemAtPathException();

        return getterResult.getValue();
    }

    /**
     * Gets the item at the given path.
     *
     * @param path A tree path for traversing the tree.
     * @return The item at the given path.
     * @throws NoItemAtPathException If no item exists at the given path.
     */
    default TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException
    { return this.getAt(path.getNodes()); }

    /**
     * Gets the item at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path, paired with whether or not an item existed at the given path.
     */
    default ValueWithPresence<TLeaf> getAtSafely(TNode... path)
    { return getAtSafely(Arrays.asList(path)); }

    /**
     * Gets the item at the given path.
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path, paired with whether or not an item existed at the given path.
     */
    ValueWithPresence<TLeaf> getAtSafely(List<TNode> path);

    /**
     * Gets the item at the given path.
     * @param path A tree path for traversing the tree.
     * @return The item at the given path, paired with whether or not an item existed at the given path.
     */
    default ValueWithPresence<TLeaf> getAtSafely(TreePath<TNode> path)
    { return getAtSafely(path.getNodes()); }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default TLeaf getAtOrDefault(TLeaf defaultItem, TNode... path)
    { return this.getAtOrDefault(defaultItem, Arrays.asList(path)); }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default TLeaf getAtOrDefault(TLeaf defaultItem, List<TNode> path)
    {
        ValueWithPresence<TLeaf> getterResult = this.getAtSafely(path);
        return getterResult.valueWasPresent() ? getterResult.getValue() : defaultItem;
    }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path A tree path for traversing the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default TLeaf getAtOrDefault(TLeaf defaultItem, TreePath<TNode> path)
    { return this.getAtOrDefault(defaultItem, path.getNodes()); }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default Object getAtOrDefaultAnyType(Object defaultItem, TNode... path)
    { return this.getAtOrDefaultAnyType(defaultItem, Arrays.asList(path)); }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default Object getAtOrDefaultAnyType(Object defaultItem, List<TNode> path)
    {
        ValueWithPresence<TLeaf> getterResult = this.getAtSafely(path);
        return getterResult.valueWasPresent() ? getterResult.getValue() : defaultItem;
    }

    /**
     * Gets the item at the given path, or the provided default value if no item exists at the given path.
     *
     * @param defaultItem The item to return if no item exists at the given path.
     * @param path A tree path for traversing the tree.
     * @return The item at the given path, or defaultValue if no such item exists.
     */
    default Object getAtOrDefaultAnyType(Object defaultItem, TreePath<TNode> path)
    { return this.getAtOrDefaultAnyType(defaultItem, path.getNodes()); }

    /**
     * Gets the item at the given path, or null if no item exists at the given path.
     *
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path, or null if no such item exists. Note that this may return null if the value
     * at the given path *is* null.
     */
    default TLeaf getAtOrNull(TNode... path)
    { return this.getAtOrNull(Arrays.asList(path)); }

    /**
     * Gets the item at the given path, or null if no item exists at the given path.
     *
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path, or null if no such item exists. Note that this may return null if the value
     * at the given path *is* null.
     */
    default TLeaf getAtOrNull(List<TNode> path)
    {
        ValueWithPresence<TLeaf> getterResult = this.getAtSafely(path);
        return getterResult.valueWasPresent() ? getterResult.getValue() : null;
    }

    /**
     * Gets the item at the given path, or null if no item exists at the given path.
     *
     * @param path A tree path for traversing the tree.
     * @return The item at the given path, or null if no such item exists. Note that this may return null if the value
     * at the given path *is* null.
     */
    default TLeaf getAtOrNull(TreePath<TNode> path)
    { return this.getAtOrNull(path.getNodes()); }
    //endregion
    //endregion

    //region get paths
    /**
     * Gets the paths of all items in this tree.
     * @return A collection of arrays of TNode, where each TNode array represents the path of an item in this tree.
     */
    Collection<TreePath<TNode>> getPaths();

    /**
     * Gets the paths of all items in this tree, in order.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of arrays of TNode, in order as determined by the provided comparator, where each TNode array
     * represents the path of an item in this tree.
     */
    List<TreePath<TNode>> getPathsInOrder(Comparator<TNode> comparator);
    //endregion

    //region get multiple items
    /**
     * Gets all items in this tree.
     * @return A collection of all the items in this tree. Where the tree is empty, returns an empty collection.
     */
    default Collection<TLeaf> getItems()
    { return this.getItemsAtAndUnder(); }

    /**
     * Gets all items in this tree in order of keys as determined by the provided comparator.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of all the items in this tree in order of keys as determined by the provided comparator.
     */
    default List<TLeaf> getItemsInOrder(Comparator<TNode> comparator)
    { return getItemsAtAndUnderInOrder(comparator); }

    /**
     * Gets all items in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of items in this tree at or under the given path. That is, items at paths starting with the
     * given path. Where there are no items at or under the given path, returns an empty collection.
     */
    default Collection<TLeaf> getItemsAtAndUnder(TNode... path)
    { return this.getItemsAtAndUnder(Arrays.asList(path)); }

    /**
     * Gets all items in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of items in this tree at or under the given path. That is, items at paths starting with the
     * given path. Where there are no items at or under the given path, returns an empty collection.
     */
    Collection<TLeaf> getItemsAtAndUnder(List<TNode> path);

    /**
     * Gets all items in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of items in this tree at or under the given path. That is, items at paths starting with the
     * given path. Where there are no items at or under the given path, returns an empty collection.
     */
    default Collection<TLeaf> getItemsAtAndUnder(TreePath<TNode> path)
    { return this.getItemsAtAndUnder(path.getNodes()); }

    /**
     * Gets all items in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of items in this tree at or under the given path, in order of keysas determined by the provided
     * comparator
     */
    default List<TLeaf> getItemsAtAndUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getItemsAtAndUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets all items in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of items in this tree at or under the given path, in order of keysas determined by the provided
     * comparator
     */
    List<TLeaf> getItemsAtAndUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets all items in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of items in this tree at or under the given path, in order of keysas determined by the provided
     * comparator
     */
    default List<TLeaf> getItemsAtAndUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getItemsAtAndUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets all items in this tree other than the root item.
     * @return A collection of all items in this tree at any path other than the root.
     */
    default Collection<TLeaf> getNonRootItems()
    { return this.getItemsUnder(); }

    /**
     * Gets all items in this tree other than the root item in order.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of all items in this tree at any path other than the root, in order of keys as determined by the
     * provided comparator.
     */
    default List<TLeaf> getNonRootItemsInOrder(Comparator<TNode> comparator)
    { return this.getItemsUnderInOrder(comparator); }

    /**
     * Gets all items in this tree under a given path, with the exception of any possible items at the given path. That
     * is, items at paths starting with the given path, but not matching the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of items in this tree under the given path, but not at the given path. That is, items at
     * paths starting with the given path, but not matching the given path.
     */
    default Collection<TLeaf> getItemsUnder(TNode... path)
    { return this.getItemsUnder(Arrays.asList(path)); }

    /**
     * Gets all items in this tree under a given path, with the exception of any possible items at the given path. That
     * is, items at paths starting with the given path, but not matching the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of items in this tree under the given path, but not at the given path. That is, items at
     * paths starting with the given path, but not matching the given path.
     */
    Collection<TLeaf> getItemsUnder(List<TNode> path);

    /**
     * Gets all items in this tree under a given path, with the exception of any possible items at the given path. That
     * is, items at paths starting with the given path, but not matching the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of items in this tree under the given path, but not at the given path. That is, items at
     * paths starting with the given path, but not matching the given path.
     */
    default Collection<TLeaf> getItemsUnder(TreePath<TNode> path)
    { return this.getItemsUnder(path.getNodes()); }

    /**
     * Gets all items in this tree under a given path in order as determined by a comparator, with the exception of any
     * possible items at the given path. That it, items at paths starting with the given path, but not matching the
     * given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of items in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, items at the paths starting with the given path, but not matching
     * the given path.
     */
    default List<TLeaf> getItemsUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getItemsUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets all items in this tree under a given path in order as determined by a comparator, with the exception of any
     * possible items at the given path. That it, items at paths starting with the given path, but not matching the
     * given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of items in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, items at the paths starting with the given path, but not matching
     * the given path.
     */
    List<TLeaf> getItemsUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets all items in this tree under a given path in order as determined by a comparator, with the exception of any
     * possible items at the given path. That it, items at paths starting with the given path, but not matching the
     * given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of items in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, items at the paths starting with the given path, but not matching
     * the given path.
     */
    default List<TLeaf> getItemsUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getItemsUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets items in this tree along the provided path. That is, where the provided path starts with the item's path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of items in this tree along the given path, including any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    default List<TLeaf> getItemsAlong(TNode... path)
    { return this.getItemsAlong(Arrays.asList(path)); }

    /**
     * Gets items in this tree along the provided path. That is, where the provided path starts with the item's path.
     * @param path A list of items used to traverse the tree.
     * @return A list of items in this tree along the given path, including any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    List<TLeaf> getItemsAlong(List<TNode> path);

    /**
     * Gets items in this tree along the provided path. That is, where the provided path starts with the item's path.
     * @param path A tree path for traversing the tree.
     * @return A list of items in this tree along the given path, including any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    default List<TLeaf> getItemsAlong(TreePath<TNode> path)
    { return this.getItemsAlong(path.getNodes()); }

    /**
     * Gets items in this tree along the provided path, not including root. That is, where the provided path starts with
     * the item's path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of items in this tree along the given path, excluding any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    default List<TLeaf> getNonRootItemsAlong(TNode... path)
    { return getNonRootItemsAlong(Arrays.asList(path)); }

    /**
     * Gets items in this tree along the provided path, not including root. That is, where the provided path starts with
     * the item's path.
     * @param path A list of items used to traverse the tree.
     * @return A list of items in this tree along the given path, excluding any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    List<TLeaf> getNonRootItemsAlong(List<TNode> path);

    /**
     * Gets items in this tree along the provided path, not including root. That is, where the provided path starts with
     * the item's path.
     * @param path A tree path for traversing the tree.
     * @return A list of items in this tree along the given path, excluding any possible root item. That is, where the
     * provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy to
     * deepest.
     */
    default List<TLeaf> getNonRootItemsAlong(TreePath<TNode> path)
    { return this.getNonRootItemsAlong(path.getNodes()); }

    /**
     * Gets the items at levels immediately under the root level.
     * @return A collection of items in this tree with single element paths, not at the root level.
     */
    default Collection<TLeaf> getImmediateItems()
    { return this.getItemsImmediatelyUnder(); }

    /**
     * Gets the items at levels immediately under the root level, in order by keys.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of items in this tree with single element paths, not at the root level, in order of keys as
     * determined by the provided comparator.
     */
    default List<TLeaf> getImmediateItemsInOrder(Comparator<TNode> comparator)
    { return this.getItemsImmediatelyUnderInOrder(comparator); }

    /**
     * Gets the items immediately under the given path, but not at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of all the items at the given path plus one element. That is, all the items immediately
     * under the given path, but not at.
     */
    default Collection<TLeaf> getItemsImmediatelyUnder(TNode... path)
    { return getItemsImmediatelyUnder(Arrays.asList(path)); }

    /**
     * Gets the items immediately under the given path, but not at the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of all the items at the given path plus one element. That is, all the items immediately
     * under the given path, but not at.
     */
    Collection<TLeaf> getItemsImmediatelyUnder(List<TNode> path);

    /**
     * Gets the items immediately under the given path, but not at the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of all the items at the given path plus one element. That is, all the items immediately
     * under the given path, but not at.
     */
    default Collection<TLeaf> getItemsImmediatelyUnder(TreePath<TNode> path)
    { return this.getItemsImmediatelyUnder(path.getNodes()); }

    /**
     * Gets the items immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of all the items at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, all the items under the given path, but not at.
     */
    default List<TLeaf> getItemsImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getItemsImmediatelyUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets the items immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of all the items at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, all the items under the given path, but not at.
     */
    List<TLeaf> getItemsImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets the items immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of all the items at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, all the items under the given path, but not at.
     */
    default List<TLeaf> getItemsImmediatelyUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getItemsImmediatelyUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets the items at the root level or immediately under it.
     * @return A collection of items in this tree with single element or empty paths.
     */
    default Collection<TLeaf> getRootAndImmediateItems()
    { return getItemsAtAndImmediatelyUnder(); }

    /**
     * Gets the items at the root level or immediately under it in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of items in this tree with single element or empty paths, in order of keys as determined by the
     * provided comparator.
     */
    default List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<TNode> comparator)
    { return getItemsAtAndImmediatelyUnderInOrder(comparator); }

    /**
     * Gets the items at and immediately under the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of items in this tree with the given path or the given path with one additional element.
     */
    default Collection<TLeaf> getItemsAtAndImmediatelyUnder(TNode... path)
    { return this.getItemsAtAndImmediatelyUnder(Arrays.asList(path)); }

    /**
     * Gets the items at and immediately under the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of items in this tree with the given path or the given path with one additional element.
     */
    Collection<TLeaf> getItemsAtAndImmediatelyUnder(List<TNode> path);

    /**
     * Gets the items at and immediately under the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of items in this tree with the given path or the given path with one additional element.
     */
    default Collection<TLeaf> getItemsAtAndImmediatelyUnder(TreePath<TNode> path)
    { return this.getItemsAtAndImmediatelyUnder(path.getNodes()); }

    /**
     * Gets the items at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of items in this tree with the given path or the given path with one additional element, in order
     * of keys as determined by the provided comparator.
     */
    default List<TLeaf> getItemsAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getItemsAtAndImmediatelyUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets the items at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of items in this tree with the given path or the given path with one additional element, in order
     * of keys as determined by the provided comparator.
     */
    List<TLeaf> getItemsAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets the items at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of items in this tree with the given path or the given path with one additional element, in order
     * of keys as determined by the provided comparator.
     */
    default List<TLeaf> getItemsAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getItemsAtAndImmediatelyUnderInOrder(comparator, path.getNodes()); }
    //endregion

    //region get multiple entries
    /**
     * Gets all entries in this tree.
     * @return A collection of all the entries in this tree. Where the tree is empty, returns an empty collection.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntries()
    { return this.getEntriesAtAndUnder(); }

    /**
     * Gets all entries in this tree in order of keys as determined by the provided comparator.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of all the entries in this tree in order of keys as determined by the provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesInOrder(Comparator<TNode> comparator)
    { return getEntriesAtAndUnderInOrder(comparator); }

    /**
     * Gets all entries in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of entries in this tree at or under the given path. That is, entries of items at paths
     * starting with the given path. Where there are no items at or under the given path, returns an empty collection.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder(TNode... path)
    { return this.getEntriesAtAndUnder(Arrays.asList(path)); }

    /**
     * Gets all entries in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of entries in this tree at or under the given path. That is, entries of items at paths
     * starting with the given path. Where there are no items at or under the given path, returns an empty collection.
     */
    Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder(List<TNode> path);

    /**
     * Gets all entries in this tree at and under a given path. That is, items at paths starting with the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of entries in this tree at or under the given path. That is, entries of items at paths
     * starting with the given path. Where there are no items at or under the given path, returns an empty collection.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder(TreePath<TNode> path)
    { return this.getEntriesAtAndUnder(path.getNodes()); }

    /**
     * Gets all entries in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of entries in this tree at or under the given path, in order of keys as determined by the provided
     * comparator
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getEntriesAtAndUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets all entries in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of entries in this tree at or under the given path, in order of keys as determined by the provided
     * comparator
     */
    List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets all entries in this tree under a given path ordered hierarchically by each branch's key.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of entries in this tree at or under the given path, in order of keys as determined by the provided
     * comparator
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getEntriesAtAndUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets all entries in this tree other than the root entry.
     * @return A collection of all entries in this tree at any path other than the root.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getNonRootEntries()
    { return this.getEntriesUnder(); }

    /**
     * Gets all entries in this tree other than the root entry in order.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of all entries in this tree at any path other than the root, in order of keys as determined by the
     * provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesInOrder(Comparator<TNode> comparator)
    { return this.getEntriesUnderInOrder(comparator); }

    /**
     * Gets all entries in this tree under a given path, with the exception of the entries of any possible items at the
     * given path. That is, entries of items at paths starting with the given path, but not matching the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of entries in this tree under the given path, but not at the given path. That is, entries of
     * items at paths starting with the given path, but not matching the given path.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesUnder(TNode... path)
    { return this.getEntriesUnder(Arrays.asList(path)); }

    /**
     * Gets all entries in this tree under a given path, with the exception of the entries of any possible items at the
     * given path. That is, entries of items at paths starting with the given path, but not matching the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of entries in this tree under the given path, but not at the given path. That is, entries of
     * items at paths starting with the given path, but not matching the given path.
     */
    Collection<Tree.Entry<TNode, TLeaf>> getEntriesUnder(List<TNode> path);

    /**
     * Gets all entries in this tree under a given path, with the exception of the entries of any possible items at the
     * given path. That is, entries of items at paths starting with the given path, but not matching the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of entries in this tree under the given path, but not at the given path. That is, entries of
     * items at paths starting with the given path, but not matching the given path.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesUnder(TreePath<TNode> path)
    { return this.getEntriesUnder(path.getNodes()); }

    /**
     * Gets all entries in this tree under a given path in order as determined by a comparator, with the exception of
     * the entries of any possible items at the given path. That it, entries at paths starting with the given path, but
     * not matching the given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of entries in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, entries of items at the paths starting with the given path, but
     * not matching the given path.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getEntriesUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets all entries in this tree under a given path in order as determined by a comparator, with the exception of
     * the entries of any possible items at the given path. That it, entries at paths starting with the given path, but
     * not matching the given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of entries in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, entries of items at the paths starting with the given path, but
     * not matching the given path.
     */
    List<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets all entries in this tree under a given path in order as determined by a comparator, with the exception of
     * the entries of any possible items at the given path. That it, entries at paths starting with the given path, but
     * not matching the given path.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of entries in this tree under the given path, but not at the given path, in order of keys as
     * determined by the provided comparator. That is, entries of items at the paths starting with the given path, but
     * not matching the given path.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getEntriesUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets entries of items in this tree along the provided path. That is, where the provided path starts with the
     * item's path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of entries in this tree along the given path, including any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAlong(TNode... path)
    { return this.getEntriesAlong(Arrays.asList(path)); }

    /**
     * Gets entries of items in this tree along the provided path. That is, where the provided path starts with the
     * item's path.
     * @param path A list of items used to traverse the tree.
     * @return A list of entries in this tree along the given path, including any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    List<Tree.Entry<TNode, TLeaf>> getEntriesAlong(List<TNode> path);

    /**
     * Gets entries of items in this tree along the provided path. That is, where the provided path starts with the
     * item's path.
     * @param path A tree path for traversing the tree.
     * @return A list of entries in this tree along the given path, including any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    { return this.getEntriesAlong(path.getNodes()); }

    /**
     * Gets entries of items in this tree along the provided path, not including root. That is, where the provided path
     * starts with the item's path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of entries in this tree along the given path, excluding any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    default List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAlong(TNode... path)
    { return this.getNonRootEntriesAlong(Arrays.asList(path)); }

    /**
     * Gets entries of items in this tree along the provided path, not including root. That is, where the provided path
     * starts with the item's path.
     * @param path A list of items used to traverse the tree.
     * @return A list of entries in this tree along the given path, excluding any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAlong(List<TNode> path);

    /**
     * Gets entries of items in this tree along the provided path, not including root. That is, where the provided path
     * starts with the item's path.
     * @param path A tree path for traversing the tree.
     * @return A list of entries in this tree along the given path, excluding any possible root entry. That is, where
     * the provided path starts with the item's path. The list is ordered, from items shallowest in the tree's hierarchy
     * to deepest.
     */
    default List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAlong(TreePath<TNode> path)
    { return this.getNonRootEntriesAlong(path.getNodes()); }

    /**
     * Gets the entries at levels immediately under the root level.
     * @return A collection of entries in this tree with single element paths, not at the root level.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getImmediateEntries()
    { return this.getEntriesImmediatelyUnder(); }

    /**
     * Gets the entries at levels immediately under the root level, in order by keys.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of entries in this tree with single element paths, not at the root level, in order of keys as
     * determined by the provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<TNode> comparator)
    { return this.getEntriesImmediatelyUnderInOrder(comparator); }

    /**
     * Gets the entries immediately under the given path, but not at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of all the entries at the given path plus one element. That is, entries of all the items
     * immediately under the given path, but not at.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder(TNode... path)
    { return this.getEntriesImmediatelyUnder(Arrays.asList(path)); }

    /**
     * Gets the entries immediately under the given path, but not at the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of all the entries at the given path plus one element. That is, entries of all the items
     * immediately under the given path, but not at.
     */
    Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder(List<TNode> path);

    /**
     * Gets the entries immediately under the given path, but not at the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of all the entries at the given path plus one element. That is, entries of all the items
     * immediately under the given path, but not at.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder(TreePath<TNode> path)
    { return this.getEntriesImmediatelyUnder(path.getNodes()); }

    /**
     * Gets the entries immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of all the entries at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, entries of all the items under the given path, but not at.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return this.getEntriesImmediatelyUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets the entries immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of all the entries at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, entries of all the items under the given path, but not at.
     */
    List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets the entries immediately under the given path, but not at the given path, ordered by their keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of all the entries at the given path plus one element, in order of keys as determined by the
     * provided comparator. That is, entries of all the items under the given path, but not at.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return this.getEntriesImmediatelyUnderInOrder(comparator, path.getNodes()); }

    /**
     * Gets the entries at the root level or immediately under it.
     * @return A collection of entries in this tree with single element or empty paths.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getRootAndImmediateEntries()
    { return getEntriesAtAndImmediatelyUnder(); }

    /**
     * Gets the entries at the root level or immediately under it in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @return A list of entries in this tree with single element or empty paths, in order of keys as determined by the
     * provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<TNode> comparator)
    { return getEntriesAtAndImmediatelyUnderInOrder(comparator); }

    /**
     * Gets the entries at and immediately under the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of entries in this tree with the given path or the given path with one additional element.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(TNode... path)
    { return this.getEntriesAtAndImmediatelyUnder(Arrays.asList(path)); }

    /**
     * Gets the entries at and immediately under the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of entries in this tree with the given path or the given path with one additional element.
     */
    Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(List<TNode> path);

    /**
     * Gets the entries at and immediately under the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of entries in this tree with the given path or the given path with one additional element.
     */
    default Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(TreePath<TNode> path)
    { return this.getEntriesAtAndImmediatelyUnder(path.getNodes()); }

    /**
     * Gets the entries at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path An ordered array of items used to traverse the tree.
     * @return A list of entries in this tree with the given path or the given path with one additional element, in
     * order of keys as determined by the provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getEntriesAtAndImmediatelyUnderInOrder(comparator, Arrays.asList(path)); }

    /**
     * Gets the entries at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A list of items used to traverse the tree.
     * @return A list of entries in this tree with the given path or the given path with one additional element, in
     * order of keys as determined by the provided comparator.
     */
    List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path);

    /**
     * Gets the entries at and immediately under the given path in order of keys.
     * @param comparator The comparator to use in ordering keys.
     * @param path A tree path for traversing the tree.
     * @return A list of entries in this tree with the given path or the given path with one additional element, in
     * order of keys as determined by the provided comparator.
     */
    default List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TreePath<TNode> path)
    { return getEntriesAtAndImmediatelyUnderInOrder(comparator, path.getNodes()); }
    //endregion

    //region get branches
    /**
     * Gets a branch of this tree at the given path.
     * @param branchId the single-element path to the branch to get.
     * @return A copy of the branch at the given path as a tree. An empty tree if this tree has no such branch.
     * @apiNote Changes made to the returned branch will not be reflected in this tree.
     */
    Tree<TNode, TLeaf> getBranch(TNode branchId);

    /**
     * Gets a branch of this tree at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A copy of the branch at the given path as a tree. An empty tree if this tree has no such branch.
     * @apiNote Changes made to the returned branch will not be reflected in this tree.
     */
    default Tree<TNode, TLeaf> getBranch(TNode... path)
    { return this.getBranch(Arrays.asList(path)); }

    /**
     * Gets a branch of this tree at the given path.
     * @param path A list of items used to traverse the tree.
     * @return A copy of the branch at the given path as a tree. An empty tree if this tree has no such branch.
     * @apiNote Changes made to the returned branch will not be reflected in this tree.
     */
    Tree<TNode, TLeaf> getBranch(List<TNode> path);

    /**
     * Gets a branch of this tree at the given path.
     * @param path A tree path for traversing the tree.
     * @return A copy of the branch at the given path as a tree. An empty tree if this tree has no such branch.
     * @apiNote Changes made to the returned branch will not be reflected in this tree.
     */
    default Tree<TNode, TLeaf> getBranch(TreePath<TNode> path)
    { return this.getBranch(path.getNodes()); }

    /**
     * Gets the immediate branches of this tree.
     * @return A collection of copies of the immediate branches of this tree.
     * @apiNote Changes made to the returned branches will not be reflected in this tree.
     */
    Collection<Tree<TNode, TLeaf>> getBranches();

    /**
     * Gets the branches of this tree immediately under the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return A collection of copies of the immediate branches of the given path of this tree.
     * @apiNote Changes made to the returned branches will not be reflected in this tree.
     */
    default Collection<Tree<TNode, TLeaf>> getBranches(TNode... path)
    { return this.getBranches(Arrays.asList(path)); }

    /**
     * Gets the branches of this tree immediately under the given path.
     * @param path A list of items used to traverse the tree.
     * @return A collection of copies of the immediate branches of the given path of this tree.
     * @apiNote Changes made to the returned branches will not be reflected in this tree.
     */
    Collection<Tree<TNode, TLeaf>> getBranches(List<TNode> path);

    /**
     * Gets the branches of this tree immediately under the given path.
     * @param path A tree path for traversing the tree.
     * @return A collection of copies of the immediate branches of the given path of this tree.
     * @apiNote Changes made to the returned branches will not be reflected in this tree.
     */
    default Collection<Tree<TNode, TLeaf>> getBranches(TreePath<TNode> path)
    { return this.getBranches(path.getNodes()); }

    /**
     * Gets the branches of this tree immediately under the given path, in a map with each branch's initial path element
     * serving as the key.
     * @return A map containing each of the branches of this tree as trees, using each branch's initial path element as
     * a key.
     */
    Map<TNode, ? extends Tree<TNode, TLeaf>> getBranchesWithFirstPathFragments();
    //endregion

    //region get branch views

    // TO DO: Write functions for this bit.

    //endregion

    //region set items in tree
    /**
     * Sets the item in this tree at the root level, overwriting it if present.
     *
     * @param newItem The item to be set to be at the root level.
     * @return The previous item at the root level, paired with whether or not there was an overwritten item at the root
     * level of the tree.
     */
    default ValueWithPresence<TLeaf> setRootItem(TLeaf newItem)
    { return this.setAt(newItem); }

    /**
     * Sets the item in this tree at the root level, unless an item already exists.
     *
     * @param newItem The item to be set to be at the root level.
     * @return The item at the root level, paired with whether or not there was an item at the root level of the tree.
     */
    default ValueWithPresence<TLeaf> setRootItemIfAbsent(TLeaf newItem)
    { return this.setAtIfAbsent(newItem); }

    /**
     * Sets the item in this tree at the given path, overwriting it if present.
     * @param newItem The item to be set to be at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return The previous item at the given path in the tree, paired with whether or not there was an overwritten item
     * at the specified path in the tree.
     */
    default ValueWithPresence<TLeaf> setAt(TLeaf newItem, TNode... path)
    { return this.setAt(newItem, Arrays.asList(path)); }

    /**
     * Sets the item in this tree at the given path, overwriting it if present.
     * @param newItem The item to be set to be at the given path.
     * @param path A list of items used to traverse the tree.
     * @return The previous item at the given path in the tree, paired with whether or not there was an overwritten item
     * at the specified path in the tree.
     */
    ValueWithPresence<TLeaf> setAt(TLeaf newItem, List<TNode> path);

    /**
     * Sets the item in this tree at the given path, overwriting it if present.
     * @param newItem The item to be set to be at the given path.
     * @param path A tree path for traversing the tree.
     * @return The previous item at the given path in the tree, paired with whether or not there was an overwritten item
     * at the specified path in the tree.
     */
    default ValueWithPresence<TLeaf> setAt(TLeaf newItem, TreePath<TNode> path)
    { return this.setAt(newItem, path.getNodes()); }

    /**
     * Sets the item in this tree at the given path, overwriting it if present.
     * @param newItem The item to be set to be at the given path.
     * @param id The single-element path to set a value at.
     * @return The previous item at the given path in the tree, paired with whether or not there was an overwritten item
     * at the specified path in the tree.
     */
    ValueWithPresence<TLeaf> setAt(TLeaf newItem, TNode id);

    /**
     * Sets the item in this tree at the given path, unless an item already exists.
     * @param newItem The item to be set to be at the given path.
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was already present at the
     * given path.
     */
    default ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, TNode... path)
    { return this.setAtIfAbsent(newItem, Arrays.asList(path)); }

    /**
     * Sets the item in this tree at the given path, unless an item already exists.
     * @param newItem The item to be set to be at the given path.
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was already present at the
     * given path.
     */
    ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, List<TNode> path);

    /**
     * Sets the item in this tree at the given path, unless an item already exists.
     * @param newItem The item to be set to be at the given path.
     * @param path A tree path for traversing the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was already present at the
     * given path.
     */
    default ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, TreePath<TNode> path)
    { return this.setAtIfAbsent(newItem, path.getNodes()); }

    /**
     * Sets the item in this tree at the given path, unless an item already exists.
     * @param newItem The item to be set to be at the given path.
     * @param id The single-element path to set a value at.
     * @return The item at the given path in the tree, paired with whether or not an item was already present at the
     * given path.
     */
    ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, TNode id);
    //endregion

    //region remove items from tree
    /**
     * Removes all items from the tree.
     */
    void clear();

    /**
     * Removes all items from the tree except for the root item, if present.
     */
    void clearNonRootItems();

    /**
     * Removes the root item from the tree.
     *
     * @return The root item, paired with whether or not there was a root item.
     */
    default ValueWithPresence<TLeaf> clearRootItem()
    { return this.clearAt(); }

    /**
     * Removes the item at the given path in the tree.
     * @param path An ordered array of items used to traverse the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was present at the given path.
     */
    default ValueWithPresence<TLeaf> clearAt(TNode... path)
    { return this.clearAt(Arrays.asList(path)); }

    /**
     * Removes the item at the given path in the tree.
     * @param path A list of items used to traverse the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was present at the given path.
     */
    ValueWithPresence<TLeaf> clearAt(List<TNode> path);

    /**
     * Removes the item at the given path in the tree.
     * @param path A tree path for traversing the tree.
     * @return The item at the given path in the tree, paired with whether or not an item was present at the given path.
     */
    default ValueWithPresence<TLeaf> clearAt(TreePath<TNode> path)
    { return this.clearAt(path.getNodes()); }

    /**
     * Removes all items at or under the given path.
     * @param path An ordered array of items used to traverse the tree.
     */
    default void clearAtAndUnder(TNode... path)
    { this.clearAtAndUnder(Arrays.asList(path)); }

    /**
     * Removes all items at or under the given path.
     * @param path A list of items used to traverse the tree.
     */
    void clearAtAndUnder(List<TNode> path);

    /**
     * Removes all items at or under the given path.
     * @param path A tree path for traversing the tree.
     */
    default void clearAtAndUnder(TreePath<TNode> path)
    { this.clearAtAndUnder(path.getNodes()); }

    /**
     * Removes all items at or under the given path.
     * @param id The single-element path to clear.
     */
    void clearAtAndUnder(TNode id);

    /**
     * Removes all items under, but not at, the given path.
     * @param path An ordered array of items used to traverse the tree.
     */
    default void clearUnder(TNode... path)
    { this.clearUnder(Arrays.asList(path)); }

    /**
     * Removes all items under, but not at, the given path.
     * @param path A list of items used to traverse the tree.
     */
    void clearUnder(List<TNode> path);

    /**
     * Removes all items under, but not at, the given path.
     * @param path A tree path for traversing the tree.
     */
    default void clearUnder(TreePath<TNode> path)
    { this.clearUnder(path.getNodes()); }

    /**
     * Removes all items under, but not at, the given path.
     * @param id The single-element path to clear under.
     */
    void clearUnder(TNode id);
    //endregion

    //region conversion

    /**
     * Converts the members of this tree into a collection of the items in the tree.
     *
     * @return A collection of all of the items in the tree at any given path.
     */
    default Collection<TLeaf> toCollection()
    { return this.getItems(); }

    /**
     * Converts the members of this tree into an ordered list of the items in the tree.
     * @return An ordered list of all of the items in the tree at any given path. Items are ordered by their paths if
     * the path nodes are comparable, starting with the first element of its path and continuing from there.
     */
    List<TLeaf> toList();

    /**
     * Converts the members of this tree into an ordered list of the items in the tree.
     * @param comparator A comparator to compare nodes at the same level of matching paths before the node.
     * @return An ordered list of all of the items in the tree at any given path. Items are ordered by their paths,
     * starting with the first element of its path and continuing from there.
     */
    List<TLeaf> toList(Comparator<TNode> comparator);
    //endregion

    //region statistics

    /**
     * Gets the number of items in the tree.
     *
     * @return The number of items in the tree.
     */
    default int count()
    {
        int sum = 0;

        for(Tree<TNode, TLeaf> branch : this.getBranches())
            sum += branch.count();

        if(this.hasRootItem())
            sum++;

        return sum;
    }

    /**
     * Gets the maximum depth of the tree.
     *
     * @return The maximum length of any leaf path in the tree, where an empty tree (with or without a root level value)
     * would return 0, and a tree with only direct child leaves would return 1.
     */
    default int countDepth()
    {
        int maxDepth = 0;

        for(Tree<TNode, TLeaf> branch : this.getBranches())
        {
            int depth = branch.countDepth() + 1;

            if(depth > maxDepth)
                maxDepth = depth;
        }

        return maxDepth;
    }
    //endregion

    //region string representations

    /**
     * Gets a multiline indented representation of the tree.
     *
     * @return A multiline indented string representation of the tree and its leaves, using the .toString() method on
     * the leaves.
     */
    default String toTreeString()
    {
        StringBuilder sb = new StringBuilder();
        ValueWithPresence<TLeaf> rootItemGetterResult = this.getRootItemSafely();
        boolean hasRootItem = rootItemGetterResult.valueWasPresent();
        TLeaf rootItem = rootItemGetterResult.getValue();

        if(hasRootItem)
            // sb.append("[root] = ").append(rootItem == null ? null : rootItem.toString()).append("\n");
            sb.append("[root] = ").append(Objects.toString(rootItem, "(null)"));
        else
            sb.append("[root]");

        Map<TNode, ? extends Tree<TNode, TLeaf>> branchesWithIds = this.getBranchesWithFirstPathFragments();

        for(TNode node : TreeDefaultHelperMethods.getKeysOrdered(branchesWithIds))
        {
            Tree<TNode, TLeaf> branch = branchesWithIds.get(node);
            ValueWithPresence<TLeaf> branchRootItemWithPresence = branch.getRootItemSafely();
            boolean branchHasRootItem = branchRootItemWithPresence.valueWasPresent();
            TLeaf branchRootItem = branchRootItemWithPresence.getValue();

            String branchString = branch.toTreeString();
            int newLinePosition = branchString.indexOf("\n");
            branchString = newLinePosition == -1 ? "" : branchString.substring(newLinePosition + 1);

            if(branchHasRootItem)
                branchString = "[" + Objects.toString(node, " (null) ") + "] = " + Objects.toString(branchRootItem, "(null)") + branchString;
            else
                branchString = "[" + Objects.toString(node, " (null) ") + "]" + branchString;

            branchString = branchString.replaceAll("(?m)^", "    ");
            sb.append('\n').append(branchString);
        }

        return sb.toString();
    }
    //endregion

    //region copying
    Tree<TNode, TLeaf> copy();
    //endregion
}

class TreeDefaultHelperMethods
{
    /*

    These functions are to allow items in a generic collection to be provided/printed/whatever in a natural order, if
    a natural order exists, and not in any particular order if it doesn't.

    Type erasure is the major stumbling block here - I can't check if T is comparable because that information isn't
    retained - I have to check if T implements comparable and the type that's passed into Comparable as a type argument,
    working on the assumption that all types passing in the same type argument to Comparable are intercomparable.

    In C#, this can be replicated quicker, more easily, and more efficiently, making use of reified generics, with:

        if(typeof(K).IsAssignableFrom(typeof(IComparable<K>)))
            return dict.Keys.OrderBy(x => (IComparable<K>)x).ToList();
        else
            return new List<K>(dict.Keys);
     */

    public static <K> List<K> getKeysOrdered(Map<K, ?> map)
    {
        Map<Type, List<K>> klists = new TreeMap<>(Comparator.comparing(Type::getTypeName, Comparator.nullsLast(Comparator.naturalOrder())));

        for(K k : map.keySet())
        {
            Type comparableType = getComparableType(k);
            List<K> klist = klists.getOrDefault(comparableType, null);

            if(klist == null)
            {
                klist = new ArrayList<>();
                klists.put(comparableType, klist);
            }

            klist.add(k);
        }

        List<K> merged = new ArrayList<>();

        for(Map.Entry<Type, List<K>> klistsEntry : klists.entrySet())
        {
            List<K> klist = klistsEntry.getValue();
            Type compType = klistsEntry.getKey();

            if(compType != null) // If true, Members of klist are comparable, and comparable to eachother
                klist.sort(Comparator.comparing(x -> (Comparable) x));

            merged.addAll(klist);
        }

        return merged;
    }

    public static Type getComparableType(Object o)
    {
        Class t = o.getClass();
        return t.isInterface() ? getComparableTypeFromInterface(t) : getComparableTypeFromClass(t);
    }

    public static Type getComparableTypeFromClass(Class t)
    {
        Type result = getComparableTypeFromInterface(t);

        if(result != null)
            return result;

        Class parentClass = t.getSuperclass();

        if(parentClass != null)
            result = getComparableTypeFromClass(parentClass);

        return result;
    }

    public static Type getComparableTypeFromInterface(Class t)
    {
        for(Type ti : t.getGenericInterfaces())
            if(ti.getTypeName().startsWith("java.lang.Comparable<"))
                return ((ParameterizedType)ti).getActualTypeArguments()[0];

        for(Class tiface : t.getInterfaces())
        {
            Type result = getComparableTypeFromInterface(tiface);

            if(result != null)
                return result;
        }

        return null;
    }
}